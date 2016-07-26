package da.artality.ivtool;

import java.util.Comparator;
import java.util.List;

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
		options.addOption("f", "force", false, "forces the rename for pokemon that already have a nickname");
		options.addOption("h", "help", false, "print this message");
		options.addOption(null, "ptc", false, "use ptc for login instead of google");
		options.addOption(null, "user", true, "your ptc username");
		options.addOption(null, "pass", true, "your ptc password");
		options.addOption(null, "token", true, "your google token if you have one (not needed)");
		options.addOption(null, "sleep", true, "the time the tool waits after each renaming (default = 1000ms)");
	}

	public void parse(String[] args) {
		CommandLineParser parser = new DefaultParser();
		try {

			CommandLine cmd = parser.parse(options, args);

			if (cmd.hasOption("h")) {
				help();
			}

			boolean rename = cmd.hasOption("r");
			boolean force = cmd.hasOption("f");

			boolean usePTC;
			String user = "", pass = "";
			if ((usePTC = cmd.hasOption("ptc"))) {
				if (!cmd.hasOption("user") || !cmd.hasOption("pass")) {
					help();
				}
			}

			int sleepTime = 1000;
			if (cmd.hasOption("sleep")) {
				try {
					sleepTime = Integer.parseInt(cmd.getOptionValue("sleep"));
				} catch (Exception e) {
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
			List<Pokemon> pokemons = go.getInventories().getPokebank().getPokemons();

			pokemons.sort(new PokeComparator());
			for (Pokemon poke : pokemons) {
				printPokemon(poke);
				if (rename && ("".equals(poke.getNickname()) || force)) {
					rename(poke);
					Thread.sleep(sleepTime);
				}
			}
			System.out.println("Finished.");

		} catch (ParseException e) {
			System.out.println("Parsing failed. Reason: " + e.getMessage());
			help();
		} catch (LoginFailedException | RemoteServerException e) {
			System.out.println("Connection failed. Reason: " + e.getMessage());
			System.out.println("Check http://ispokemongodownornot.com/ to see if servers are down.");
		} catch (InterruptedException e) {
			System.out.println("Renaming failed. Reason: " + e.getMessage());
		}
	}

	private void help() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("ivtool [-token <token>] [-ptc -user <user> -pass <pass>] [-r] [-f] [-sleep <x>]", options);
		System.exit(0);
	}

	private class PokeComparator implements Comparator<Pokemon> {

		@Override
		public int compare(Pokemon p1, Pokemon p2) {
			int i = p1.getPokemonId().getNumber() - p2.getPokemonId().getNumber();
			if (i != 0) {
				return i;
			}
			i = getPerc(p2) - getPerc(p1);
			if (i != 0) {
				return i;
			}
			return p2.getCp() - p1.getCp();
		}
	}

	private void printPokemon(Pokemon poke) {
		System.out.println(poke.getPokemonId().getNumber() + " " + poke.getPokemonId() + " " + getPerc(poke) + "% " + poke.getIndividualAttack()
				+ " " + poke.getIndividualDefense() + " " + poke.getIndividualStamina() + " " + poke.getCp() + "CP ");
	}

	private void rename(Pokemon poke) {
		int atk = poke.getIndividualAttack();
		int def = poke.getIndividualDefense();
		int sta = poke.getIndividualStamina();
		int perc = getPerc(poke);
		try {
			poke.renamePokemon(perc + "% " + ((perc == 100) ? "" : (atk + " " + def + " " + sta)));
			System.out.println("Successfully renamed.");
		} catch (LoginFailedException | RemoteServerException e) {
			System.out.println("Renaming failed. Reason: " + e.getMessage());
		}
	}

	private int getPerc(Pokemon poke) {
		int atk = poke.getIndividualAttack();
		int def = poke.getIndividualDefense();
		int sta = poke.getIndividualStamina();

		return (int) Math.round((atk + def + sta) / 45.0 * 100);
	}

	public static final void main(String[] args) {
		new IVTool().parse(args);
	}
}
