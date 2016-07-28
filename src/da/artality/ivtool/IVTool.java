package da.artality.ivtool;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import okhttp3.OkHttpClient;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.auth.CredentialProvider;
import com.pokegoapi.auth.GoogleAutoCredentialProvider;
import com.pokegoapi.auth.PtcCredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import com.pokegoapi.util.Log;
import com.pokegoapi.util.SystemTimeImpl;
import com.pokegoapi.util.Time;

public class IVTool {

	private Options options = new Options();

	public IVTool() {
		options.addOption("r", "rename", false, "rename every pokemon without a nickname");
		options.addOption("f", "force", false, "forces the rename for pokemon that already have a nickname");
		options.addOption("h", "help", false, "print this message");
		options.addOption("d", "debug", false, "show the debug messages");
		options.addOption("s", "star", true, "star every pokemon with a iv % above the given argument (doesn't work yet)");
		options.addOption(null, "ptc", false, "use your ptc account for login instead of google");
		options.addOption(null, "user", true, "your ptc username / google email");
		options.addOption(null, "pass", true, "your ptc/google password");
		options.addOption(null, "sleep", true, "sleep time after each action in ms (default = 2000ms)");
		options.addOption("o", "out", true, "create an output file");
	}

	public void parse(String[] args) {
		try {
			CommandLineParser parser = new DefaultParser();
			CommandLine cmd = parser.parse(options, args);

			Log.setLevel(cmd.hasOption("d") ? Log.Level.VERBOSE : Log.Level.ASSERT);

			if (cmd.hasOption("h")) {
				help();
			}

			boolean rename = cmd.hasOption("r");
			boolean forceRename = cmd.hasOption("f");

			boolean createFile = cmd.hasOption("out");
			String filePath = cmd.getOptionValue("out");

			int sleepTime = Utils.parseInt(cmd.getOptionValue("sleep"), 2000);

			boolean star = cmd.hasOption("s");
			int starThreshold = star ? Integer.parseInt(cmd.getOptionValue("s")) : 85;

			OkHttpClient client = new OkHttpClient();
			Time time = new SystemTimeImpl();
			CredentialProvider cred;

			if (!cmd.hasOption("user") || !cmd.hasOption("pass")) {
				help();
			}
			String user = cmd.getOptionValue("user");
			String pass = cmd.getOptionValue("pass");

			if (cmd.hasOption("ptc")) {
				cred = new PtcCredentialProvider(client, user, pass, time);
			} else {
				cred = new GoogleAutoCredentialProvider(client, user, pass, time);
			}

			PokemonGo go = new PokemonGo(cred, client, time);

			PokeInfo[] pokeInfos = go.getInventories()
					.getPokebank()
					.getPokemons()
					.stream()
					.map(p -> new PokeInfo(p))
					.sorted(new PokeInfoComparator())
					.toArray(PokeInfo[]::new);

			for (PokeInfo p : pokeInfos) {
				System.out.println(p.toString());

				if (rename && ("".equals(p.getPokemon()
						.getNickname()) || forceRename)) {
					rename(p);
					System.out.println("Successfully renamed.");
					Thread.sleep(sleepTime);
				}

				if (star && (p.getIvPerc() > starThreshold)) {
					p.getPokemon()
							.setFavoritePokemon(true);
					System.out.println("Successfully starred.");
					Thread.sleep(sleepTime);
				}
			}

			if (createFile) {
				createFile(filePath, pokeInfos);
			}

			System.out.println("Finished.");

		} catch (ParseException e) {
			System.out.println("Parsing failed. Reason: " + e.getMessage());
			help();
		} catch (LoginFailedException | RemoteServerException e) {
			System.out.println("Connection failed. Reason: " + e.getMessage());
			System.out.println("Check http://ispokemongodownornot.com/ to see if servers are down.");
		} catch (InterruptedException e) {
			System.out.println("Sleep failed. Reason: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("CSV creation failed. Reason: " + e.getMessage());
		}
	}

	private void help() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("ivtool [-ptc] -user <user> -pass <1234> [-r] [-f] [-out <file>]", options);
		System.exit(0);
	}

	private void rename(PokeInfo poke) throws LoginFailedException, RemoteServerException {
		int atk = poke.getIvAtk();
		int def = poke.getIvDef();
		int sta = poke.getIvSta();
		int perc = poke.getIvPerc();

		poke.getPokemon()
				.renamePokemon(perc + "% " + ((perc == 100) ? "" : (atk + " " + def + " " + sta)));
	}

	private void createFile(String filePath, PokeInfo[] pokes) throws IOException {
		String header = "Nr;Pokemon;IV %;IV Atk;IV Def;IV Sta;Atk;Def;Sta;CP;Max CP;DPS Normal;DPS Special;DPS combined;DPS max;Damage;Damage per CP";

		BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath));
		writer.write(header);
		for (PokeInfo poke : pokes) {
			writer.newLine();
			writer.write(poke.toString());
		}
		writer.close();
	}

	public static final void main(String[] args) {
		new IVTool().parse(args);
		System.exit(0);
	}
}
