package da.artality.ivtool;

import java.text.DecimalFormat;

import POGOProtos.Enums.PokemonIdOuterClass.PokemonId;

import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.api.pokemon.PokemonMeta;
import com.pokegoapi.api.pokemon.PokemonMoveMeta;
import com.pokegoapi.api.pokemon.PokemonMoveMetaRegistry;
import com.pokegoapi.api.pokemon.PokemonType;
import com.pokegoapi.exceptions.NoSuchItemException;

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

	public int getNr() {
		return pokemon.getPokemonId()
				.getNumber();
	}

	public PokemonId getPokemonId() {
		return pokemon.getPokemonId();
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
		return (pokemon.getMeta()
				.getBaseAttack() + getIvAtk()) * pokemon.getCpMultiplier();
	}

	public double getDef() {
		return (pokemon.getMeta()
				.getBaseDefense() + getIvDef()) * pokemon.getCpMultiplier();
	}

	public double getSta() {
		return (pokemon.getMeta()
				.getBaseStamina() + getIvSta()) * pokemon.getCpMultiplier();
	}

	public int getCp() {
		return pokemon.getCp();
	}

	public int getMaxCp() {
		try {
			return pokemon.getMaxCp();
		} catch (NoSuchItemException e) {
			return 0;
		}
	}

	public double getDpsNormal() {
		PokemonMoveMeta moveMeta = PokemonMoveMetaRegistry.getMeta(pokemon.getMove1());
		return moveMeta.getPower() / (double) moveMeta.getTime() * 1000 * getAttackModifier(moveMeta.getType());
	}

	public double getDpsSpecial() {
		PokemonMoveMeta moveMeta = PokemonMoveMetaRegistry.getMeta(pokemon.getMove2());
		return moveMeta.getPower() / (double) moveMeta.getTime() * 1000 * getAttackModifier(moveMeta.getType());
	}

	/**
	 * Calculates the theoretically combined dps of the normal and special attack of the pokemon if used perfectly
	 * 
	 * @return combined dps
	 */
	public double getDpsCombined() {
		PokemonMoveMeta move1Meta = PokemonMoveMetaRegistry.getMeta(pokemon.getMove1());
		PokemonMoveMeta move2Meta = PokemonMoveMetaRegistry.getMeta(pokemon.getMove2());
		double atkCount = Math.abs(move2Meta.getEnergy() / (double) move1Meta.getEnergy());

		return ((atkCount * move1Meta.getPower() * getAttackModifier(move1Meta.getType()) + move2Meta.getPower()
				* getAttackModifier(move2Meta.getType()))
				/ (atkCount * move1Meta.getTime() + move2Meta.getTime()) * 1000);
	}

	/**
	 * Calculates the theoretically maximum dps of the pokemon
	 * 
	 * @return maximum dps
	 */
	public double getDpsMax() {
		return Math.max(getDpsNormal(), getDpsCombined());
	}

	/**
	 * Multiplies the maximum dps of the pokemon with it's current atk value to see how much damage potential the pokemon currently has
	 * 
	 * @return maximum dps * atk
	 */
	public double getDmg() {
		return getDpsMax() * getAtk();
	}

	/**
	 * Calculates the potential damage per cp. Useful for finding pokemon for efficiently farming exp at gyms
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
	public double getAttackModifier(PokemonType type) {
		PokemonMeta meta = pokemon.getMeta();
		if (meta.getType1() == type || meta.getType2() == type) {
			return 1.25;
		}
		return 1;
	}

	@Override
	public String toString() {
		DecimalFormat f = new DecimalFormat("0.00");

		return getNr() + ";" + getPokemonId() + ";" + getIvPerc() + ";" + getIvAtk() + ";" + getIvDef() + ";" + getIvSta() + ";" + f.format(getAtk())
				+ ";" + f.format(getDef()) + ";" + f.format(getSta()) + ";" + getCp() + ";" + f.format(getMaxCp()) + ";" + f.format(getDpsNormal())
				+ ";" + f.format(getDpsSpecial()) + ";" + f.format(getDpsCombined()) + ";" + f.format(getDpsMax()) + ";" + f.format(getDmg()) + ";"
				+ f.format(getDmgPerCp());
	}

}
