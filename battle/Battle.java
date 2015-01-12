//: battle/Battle.java

package pokepon.battle;

import pokepon.pony.*;
import pokepon.player.*;
import static pokepon.util.MessageManager.*;
import pokepon.util.*;
import pokepon.enums.*;
import pokepon.net.jack.*;
import java.util.*;
import java.util.concurrent.*;

/** The main class and entry point for battle, used to retrieve teams
 * before the battle control is handed to BattleTask; also used to defined
 * the battle constant values.
 *
 * @author silverweed
 */
public abstract class Battle {
	
	////////////// CONSTANT VALUES ////////////////
	
	/** Probability of skipping turn due to paralysis */
	public static final float CHANCE_FULL_PARALYSIS = 0.25f;
	/** Probability to thaw out of petrification per turn */
	public static final float CHANCE_DEPETRIFICATE = 0.10f;
	/** Maximum number of turns the sleep lasts. */
	public static final int MAX_SLEEP_DURATION = 5;
	/** Maximum number of attacking turns the confusion lasts */
	public static final int MAX_CONFUSION_DURATION = 4;
	/** Probability to attack self during confusion */
	public static final float CHANCE_SELF_DAMAGE_FOR_CONFUSION = 0.5f;
	/** Show HP as percentual */
	public static final boolean SHOW_HP_PERC = true;
	/** Amount of burn damage (in %/100) */
	public static final float BURN_DAMAGE = 0.125f;
	/** Amount of poison damage (in %/100) */
	public static final float POISON_DAMAGE = 0.125f;
	/** Amount of first bad poison damage (in %/100) */
	public static final float BAD_POISON_DAMAGE = 0.0625f;

	/////////////// PUBLIC METHODS / FIELDS ////////////////

	public boolean initialize() throws InterruptedException {
		return initialize(false);
	}

	public abstract boolean initialize(boolean randomBattle) throws InterruptedException;

	public abstract Player getPlayer(int num);
	public abstract Team getTeam(int num);

	public final Random getRNG() {
		return rng;
	}

	/////////////// PRIVATE METHODS / FIELDS ////////////////

	/** This is supposed to be overridden by children to generate
	 * more balanced teams for the specific kind of battle.
	 */
	protected Team randomTeam() {
		return Team.randomTeam();
	}

	protected WeatherHolder weather = new WeatherHolder(Weather.CLEAR, 0);
	protected ExecutorService executor = Executors.newFixedThreadPool(2);
	protected Random rng = new Random();
}
