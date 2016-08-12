package da.artality.ivtool;

import java.util.List;

import com.google.protobuf.InvalidProtocolBufferException;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import com.pokegoapi.main.ServerRequest;

import POGOProtos.Networking.Requests.RequestTypeOuterClass.RequestType;
import POGOProtos.Networking.Requests.Messages.NicknamePokemonMessageOuterClass.NicknamePokemonMessage;
import POGOProtos.Networking.Responses.NicknamePokemonResponseOuterClass.NicknamePokemonResponse;

public class PokeUtils {

	private PokemonGo go;

	public PokeUtils(PokemonGo go) {
		this.go = go;
	}
	
	public void batchRename(PokeInfo[] pokeList, String pattern) {
		for(PokeInfo poke : pokeList) {
			String nickname = "harry";
			//TODO: auflösen
			
			
			try {
				NicknamePokemonResponse.Result result = rename(poke.getPokemon(), nickname);
				System.out.println(result);
			} catch (Exception e) {
				System.out.println("Renaming failed. Reason: " + e.getMessage());
			}
			
			try {
				Thread.sleep(getSleepTime());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private NicknamePokemonResponse.Result rename(Pokemon poke, String nickname) throws RemoteServerException, LoginFailedException {

		NicknamePokemonMessage reqMsg = NicknamePokemonMessage.newBuilder()
				.setPokemonId(poke.getId())
				.setNickname(nickname)
				.build();

		ServerRequest serverRequest = new ServerRequest(RequestType.NICKNAME_POKEMON, reqMsg);
		go.getRequestHandler().sendServerRequests(serverRequest);

		NicknamePokemonResponse response;
		try {
			response = NicknamePokemonResponse.parseFrom(serverRequest.getData());
		} catch (InvalidProtocolBufferException e) {
			throw new RemoteServerException(e);
		}
		return response.getResult();
	}
	
	private int getSleepTime() {
		int min = 5000, max = 7000;
		return min + (int)(Math.random() * ((max - min) + 1));
	}

}
