package da.artality.ivtool;

import okhttp3.OkHttpClient;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.auth.GoogleLogin;
import com.pokegoapi.auth.PtcLogin;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

public class IVTool {

	private Options options = new Options();

	public IVTool() {
		options.addOption("r", "rename", false, "rename every pokemon without a nickname");
		options.addOption("h", "help", false, "print this message");
		options.addOption(null, "ptc", false, "use ptc for login instead of google");
		options.addOption(null, "user", true, "your ptc username");
		options.addOption(null, "pass", true, "your ptc password");
		options.addOption(null, "token", true, "your google token if you already have one");
	}

	public void parse(String[] args) {
		CommandLineParser parser = new DefaultParser();
		try {

			CommandLine cmd = parser.parse(options, args);

			boolean rename = cmd.hasOption("r");

			if (cmd.hasOption("h")) {
				help();
			}

			boolean usePTC;
			String user = "", pass = "";
			if ((usePTC = cmd.hasOption("ptc"))) {
				if (!cmd.hasOption("user") || !cmd.hasOption("pass")) {
					help();
				}
			}

			OkHttpClient client = new OkHttpClient();
			AuthInfo auth;
			if (usePTC) {
				auth = new PtcLogin(client).login(user, pass);
			} else {
				if (cmd.hasOption("token")) {
					auth = new GoogleLogin(client).login(cmd.getOptionValue("token"));
				} else {
					auth = new GoogleLogin(client).login();
				}
			}

			PokemonGo go = new PokemonGo(auth, client);

			for (Pokemon poke : go.getInventories().getPokebank().getPokemons()) {
				printPokemon(poke);
				if (rename && "".equals(poke.getNickname())) {
					poke.renamePokemon(getPokemonInfo(poke));
					System.out.println("successfully renamed");
				}
			}
			System.out.println("finished");

		} catch (ParseException e) {
			System.out.println("Parsing failed. Reason: " + e.getMessage());
			help();
		} catch (LoginFailedException | RemoteServerException e) {
			System.out.println("Login failed. Reason: " + e.getMessage());
			System.out.println("Check http://ispokemongodownornot.com/ to see if servers are down.");
		}
	}

	private void help() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("ivtool [-token <token>] [-ptc -user <user> -pass <pass>] [-r|-rename]", options);
		System.exit(0);
	}

	private void printPokemon(Pokemon poke) {
		System.out.println(poke.getPokemonId() + " " + poke.getCp() + "CP " + getPokemonInfo(poke));
	}

	private String getPokemonInfo(Pokemon poke) {
		int atk = poke.getIndividualAttack();
		int def = poke.getIndividualDefense();
		int sta = poke.getIndividualStamina();

		int perc = (int) Math.round((atk + def + sta) / 45.0 * 100);

		return perc + "% " + ((perc == 100) ? "" : (atk + " " + def + " " + sta));
	}

	public static final void main(String[] args) {
		new IVTool().parse(args);
	}
}
