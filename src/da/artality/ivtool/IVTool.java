package da.artality.ivtool;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.auth.CredentialProvider;
import com.pokegoapi.auth.GoogleUserCredentialProvider;
import com.pokegoapi.auth.PtcCredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import com.pokegoapi.util.Log;

import okhttp3.OkHttpClient;

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
		options.addOption("o", "out", true, "create an output file");
		options.addOption(null, "reset", false, "resets all(!) nicknames");
		options.addOption(null, "token", true, "your google token if you already have one");
	}

	public void parse(String[] args) {
		try {
			System.out.println("Start CLI parsing.");

			CommandLineParser parser = new DefaultParser();
			CommandLine cmd = parser.parse(options, args);

			Log.setLevel(cmd.hasOption("d") ? Log.Level.VERBOSE : Log.Level.ASSERT);

			// CLI info dump
			for (Option o : cmd.getOptions()) {
				Log.d("Option [" + o.getOpt() + "|" + o.getLongOpt() + "]", o.getValue());
			}
			for (String s : cmd.getArgs()) {
				Log.d("Arg", s);
			}

			if (cmd.hasOption("h")) {
				help();
			}

			boolean rename = cmd.hasOption("r");
			boolean forceRename = cmd.hasOption("f");
			boolean resetNickname = cmd.hasOption("reset");

			boolean createFile = cmd.hasOption("out");
			String filePath = cmd.getOptionValue("out");

			boolean star = cmd.hasOption("s");
			int starThreshold = Utils.parseInt(cmd.getOptionValue("s"), 85);

			System.out.println("Logging in.");

			OkHttpClient client = new OkHttpClient();
			PokemonGo go;

			if (cmd.hasOption("ptc")) {
				if (!cmd.hasOption("user") || !cmd.hasOption("pass")) {
					help();
				}

				String user = cmd.getOptionValue("user");
				String pass = cmd.getOptionValue("pass");
				CredentialProvider cred = new PtcCredentialProvider(client, user, pass);
				go = new PokemonGo(cred, client);

			} else {

				GoogleUserCredentialProvider cred;
				if (cmd.hasOption("token")) {
					cred = new GoogleUserCredentialProvider(client, cmd.getOptionValue("token"));
				} else {
					cred = new GoogleUserCredentialProvider(client);
					System.out.println("Visit this url: " + GoogleUserCredentialProvider.LOGIN_URL);
					System.out.println("Enter authorisation code:");
					Scanner sc = new Scanner(System.in);
					String access = sc.nextLine();
					sc.close();
					cred.login(access);
					System.out.println("Refresh token:" + cred.getRefreshToken());
				}
				go = new PokemonGo(cred, client);

			}

			System.out.println("Getting pokemon information.");

			PokeInfo[] pokeInfos = go.getInventories()
					.getPokebank()
					.getPokemons()
					.stream()
					.map(p -> new PokeInfo(p))
					.sorted(new PokeInfoComparator())
					.toArray(PokeInfo[]::new);
			
			PokeUtils utils = new PokeUtils(go);
			utils.batchRename(pokeInfos, "");

			/*
			for (PokeInfo p : pokeInfos) {
				System.out.println(p.toString());

				if (rename && ("".equals(p.getPokemon().getNickname()) || forceRename)) {
					rename(p);
					System.out.println("Successfully renamed.");
					Thread.sleep(getSleepTime());
				} else if (resetNickname) {
					p.getPokemon().renamePokemon("");
					System.out.println("Nickname resetted.");
					Thread.sleep(getSleepTime());
				}

				if (star && (p.getIvPerc() > starThreshold)) {
					p.getPokemon().setFavoritePokemon(true);
					System.out.println("Successfully starred.");
					Thread.sleep(getSleepTime());
				}
			}

			if (createFile) {
				System.out.println("Creating output file.");
				createFile(filePath, pokeInfos);
			}
			*/
			System.out.println("Finished.");

		} catch (ParseException e) {
			System.out.println("Parsing failed. Reason: " + e.getMessage());
			help();
		} catch (LoginFailedException | RemoteServerException e) {
			System.out.println("Connection failed. Reason: " + e.getMessage());
			System.out.println("Check http://ispokemongodownornot.com/ to see if servers are down.");
		}/* catch (InterruptedException e) {
			System.out.println("Sleep failed. Reason: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("CSV creation failed. Reason: " + e.getMessage());
		}*/
	}

	private void help() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("ivtool -token <token> [-ptc -user <user> -pass <1234>] [-r] [-f] [-out <file>]", options);
		System.exit(0);
	}
	
	public static final void main(String[] args) {
		new IVTool().parse(args);
		System.exit(0);
	}
}
