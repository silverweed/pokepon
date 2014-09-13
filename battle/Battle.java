//: battle/Battle.java

package pokepon.battle;

import pokepon.pony.*;
import pokepon.player.*;
import static pokepon.util.MessageManager.*;
import pokepon.util.*;
import pokepon.enums.*;
import pokepon.net.jack.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.*;

/** The main class and entry point for battle.
 *
 * @author silverweed
 */
public class Battle {
	
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

	/** Constructor used for tests */
	public Battle(Player p1, Player p2) {
		this.p1 = p1;
		this.p2 = p2;
	}

	/** Constructor used by the Pokepon Server: initialize() must be called before starting battle. */
	public Battle(Connection c1,Connection c2) {
		this.c1 = c1;
		this.c2 = c2;
		p1 = new Player(c1.getName());
		p2 = new Player(c2.getName());
	}

	public boolean initialize() throws InterruptedException {
		return initialize(false);
	}

	/** Makes pre-battle preparations: retrieves and validates teams and so on 
	 * @return true - if initialization succeeded; false - otherwise.
	 */
	public boolean initialize(boolean randomBattle) throws InterruptedException {
		if(randomBattle) {
			// generate random teams
			p1.setTeam(Team.randomTeam());
			p2.setTeam(Team.randomTeam());
			// set random levels within certain limits
			for(int i = 0; i < p1.getTeam().members(); ++i) {
				int lv = 85 + rng.nextInt(16);
				p1.getTeam().getPony(i).setLevel(lv);
				p2.getTeam().getPony(i).setLevel(lv);
			}

		} else {
			// concurrently retreive teams
			printDebug("[BATTLE "+p1+","+p2+"]: Retrieving teams...");
			List<Callable<Boolean>> tr = new ArrayList<Callable<Boolean>>();
			tr.add(new TeamRetreiver(c1,p1));
			tr.add(new TeamRetreiver(c2,p2));
			List<Future<Boolean>> results = executor.invokeAll(tr);
			
			try {
				if(Debug.pedantic) printDebug("Future.get(1)...");
				if(!results.get(0).get(15,TimeUnit.SECONDS)) {
					printDebug(p1.getName()+"'s team not retreived correctly.");
					printMsg("Error starting battle between "+p1+" and "+p2+": aborting battle.");
					c1.sendMsg(BTL_PREFIX+"ko");
					c2.sendMsg(BTL_PREFIX+"ko");
					return false;
				}
				if(Debug.pedantic) printDebug("Future.get(2)...");
				if(!results.get(1).get(15,TimeUnit.SECONDS)) {
					printDebug(p2.getName()+"'s team not retreived correctly");
					printMsg("Error starting battle between "+p1+" and "+p2+": aborting battle.");
					c1.sendMsg(BTL_PREFIX+"ko");
					c2.sendMsg(BTL_PREFIX+"ko");
					return false;
				}
			} catch(TimeoutException e) {
				printDebug("Timeout: "+e);
				c1.sendMsg(BTL_PREFIX+"ko");
				c2.sendMsg(BTL_PREFIX+"ko");
				return false;
			} catch(ExecutionException e) {
				printDebug("Caught exception in result.get(): "+e);
				e.printStackTrace();
				c1.sendMsg(BTL_PREFIX+"ko");
				c2.sendMsg(BTL_PREFIX+"ko");
				return false;
			}
			printDebug("[BATTLE "+c1.getName()+" ~ "+c2.getName()+"] Teams retreived correctly.");
		}

		return true;
	}

	public Player getPlayer(int num) {
		if(num == 1) return p1;
		else if(num == 2) return p2;
		else throw new IllegalArgumentException("[Battle.getPlayer()]: num is "+num);
	}

	public Team getTeam(int num) {
		if(num == 1) return p1.getTeam();
		else if(num == 2) return p2.getTeam();
		else throw new IllegalArgumentException("[Battle.getTeam()]: num is "+num);
	}

	public final Random getRNG() {
		return rng;
	}

/*	public BattleEngine getBattleEngine() {
		return engine;
	}
*/
	public boolean setActivePony(int num,int i) {
		if(num == 1) return p1.getTeam().setActivePony(i);
		else if(num == 2) return p2.getTeam().setActivePony(i);
		else throw new IllegalArgumentException("[Battle.setActivePony()]: num is "+num);
	}

	public boolean setActivePony(int num,String name) {
		if(num == 1) return p1.getTeam().setActivePony(name);
		else if(num == 2) return p2.getTeam().setActivePony(name);
		else throw new IllegalArgumentException("[Battle.setActivePony()]: num is "+num);
	}

	/** Simulates a battle (not used) */
	public void start() {
		if(Debug.on) {
			printDebug("Started battle between "+p1+" and "+p2);
			printDebug("Teams: \nP1: "+p1.getTeam()+"\nP2: "+p2.getTeam());
		}
		p1.getTeam().setActivePony(0);
		p2.getTeam().setActivePony(0);
		
		//BattleTurn turn = new BattleTurn(p1,p2,weather);
		/*BattleTurn turn = new BattleTester(p1,p2,weather);

		do {
			winner = turn.performTurn();
		} while(winner == 0);

		switch(winner) {
			case -1:
				printMsg("Battle is draw between "+p1+" and "+p2+"!");
				break;
			case 1:
				printMsg(p1+" is the winner!");
				break;
			case 2:
				printMsg(p2+" is the winner!");
				break;
			default:
				throw new RuntimeException("winner is "+winner+"!");
		}*/
	}


	/////////////// PRIVATE METHODS / FIELDS ////////////////

	private Player p1;
	private Player p2;
	private Connection c1;
	private Connection c2;
	private WeatherHolder weather = new WeatherHolder(Weather.CLEAR,0);
	private int winner;
	private ExecutorService executor = Executors.newFixedThreadPool(2);
	//private BattleEngine engine;
	private Random rng = new Random();
}
