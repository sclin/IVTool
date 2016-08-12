package da.artality.ivtool;

import org.apache.commons.lang3.text.WordUtils;

import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.api.pokemon.PokemonMeta;
import com.pokegoapi.api.pokemon.PokemonMoveMeta;
import com.pokegoapi.api.pokemon.PokemonMoveMetaRegistry;
import com.pokegoapi.api.pokemon.PokemonType;
import com.sun.xml.internal.ws.util.StringUtils;

/**
 * Wrapper Class for easier access to all the needed infos of the pokemon
 * 
 * @author Tsunamii
 *
 */
public class PokeInfo {

	private Pokemon pokemon;

	public PokeInfo(Pokemon pokemon) {
		this.pokemon = pokemon;
	}
	
	public Pokemon getPokemon() {
		return pokemon;
	}

	public long getId() {
		return pokemon.getId();
	}

	public int getNr() {
		return pokemon.getPokemonId().getNumber();
	}

	public String getName() {
		return beautifyName(pokemon.getPokemonId().toString());
	}

	public String getType1() {
		return beautifyType(pokemon.getMeta().getType1().toString());
	}

	public String getType2() {
		return beautifyType(pokemon.getMeta().getType2().toString());
	}

	public int getIvPerc() {
		return (int) (pokemon.getIvRatio() * 100);
	}

	public int getIvAtk() {
		return pokemon.getIndividualAttack();
	}

	public int getIvDef() {
		return pokemon.getIndividualDefense();
	}

	public int getIvSta() {
		return pokemon.getIndividualStamina();
	}

	public double getAtk() {
		return (pokemon.getMeta().getBaseAttack() + getIvAtk()) * pokemon.getCpMultiplier();
	}

	public double getDef() {
		return (pokemon.getMeta().getBaseDefense() + getIvDef()) * pokemon.getCpMultiplier();
	}

	public double getSta() {
		return (pokemon.getMeta().getBaseStamina() + getIvSta()) * pokemon.getCpMultiplier();
	}

	public int getCp() {
		return pokemon.getCp();
	}

	public String getMoveFast() {
		PokemonMoveMeta moveMeta = PokemonMoveMetaRegistry.getMeta(pokemon.getMove1());
		return beautifyMove(moveMeta.getMove().toString());
	}

	public String getMoveFastType() {
		PokemonMoveMeta moveMeta = PokemonMoveMetaRegistry.getMeta(pokemon.getMove1());
		return beautifyType(moveMeta.getType().toString());
	}

	public double getMoveFastDps() {
		PokemonMoveMeta moveMeta = PokemonMoveMetaRegistry.getMeta(pokemon.getMove1());
		return moveMeta.getPower() / (double) moveMeta.getTime() * 1000 * getAttackModifier(moveMeta.getType());
	}

	public String getMoveCharge() {
		PokemonMoveMeta moveMeta = PokemonMoveMetaRegistry.getMeta(pokemon.getMove2());
		return beautifyMove(moveMeta.getMove().toString());
	}

	public String getMoveChargeType() {
		PokemonMoveMeta moveMeta = PokemonMoveMetaRegistry.getMeta(pokemon.getMove2());
		return beautifyType(moveMeta.getType().toString());
	}

	public double getMoveChargeDps() {
		PokemonMoveMeta moveMeta = PokemonMoveMetaRegistry.getMeta(pokemon.getMove2());
		return moveMeta.getPower() / (double) moveMeta.getTime() * 1000 * getAttackModifier(moveMeta.getType());
	}

	/**
	 * Calculates the combined dps of the normal and special attack of the
	 * pokemon if used perfectly
	 * 
	 * @return combined dps
	 */
	public double getDpsCombined() {
		PokemonMoveMeta move1Meta = PokemonMoveMetaRegistry.getMeta(pokemon.getMove1());
		PokemonMoveMeta move2Meta = PokemonMoveMetaRegistry.getMeta(pokemon.getMove2());
		double atkCount = Math.ceil(move2Meta.getEnergy() / (double) move1Meta.getEnergy());

		return ((atkCount * move1Meta.getPower() * getAttackModifier(move1Meta.getType())
				+ move2Meta.getPower() * getAttackModifier(move2Meta.getType()))
				/ (atkCount * move1Meta.getTime() + move2Meta.getTime()) * 1000);
	}

	/**
	 * Calculates the theoretically maximum dps of the pokemon
	 * 
	 * @return maximum dps
	 */
	public double getDpsMax() {
		return Math.max(getMoveFastDps(), getDpsCombined());
	}

	/**
	 * Multiplies the maximum dps of the pokemon with it's current atk value to
	 * see how much damage potential the pokemon currently has
	 * 
	 * @return maximum dps * atk
	 */
	public double getDmg() {
		return getDpsMax() * getAtk();
	}

	/**
	 * Calculates the potential damage per cp. Useful for finding pokemon for
	 * efficiently farming exp at gyms
	 * 
	 * @return (maximum dps * atk) / cp
	 */
	public double getDmgPerCp() {
		return getDmg() / getCp();
	}

	/**
	 * Returns the attack modifier
	 * 
	 * @return <b>1.25</b> if move and pokemon have the same type<br/>
	 *         <b>1.00</b> otherwise
	 */
	private double getAttackModifier(PokemonType type) {
		PokemonMeta meta = pokemon.getMeta();
		if (meta.getType1() == type || meta.getType2() == type) {
			return 1.25;
		}
		return 1;
	}

	private String beautifyName(String name) {
		return StringUtils.capitalize(name.toLowerCase().replace("_male", "♂").replace("_female", "♀"));
	}

	private String beautifyType(String type) {
		return StringUtils.capitalize(type.toLowerCase());
	}

	private String beautifyMove(String move) {
		return WordUtils
				.capitalize(move.toLowerCase().replace("_fast", "").replace("_blastoise", "").replace('_', ' '));
	}

}
