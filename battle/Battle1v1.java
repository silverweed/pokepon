package pokepon.battle;

import pokepon.player.*;
import pokepon.pony.*;
import pokepon.util.*;
import pokepon.net.jack.*;
import static pokepon.util.MessageManager.*;
import java.util.*;
import java.io.*;
import java.util.concurrent.*;

/** Class implementing 1 vs 1 battle.
 *
 * @author silverweed
 */
public class Battle1v1 extends Battle {

	/** Constructor used for tests */
	public Battle1v1(Player p1, Player p2) {
		this.p1 = p1;
		this.p2 = p2;
	}

	/** Constructor used by the Pokepon Server: initialize() must be called before starting battle. */
	public Battle1v1(Connection c1,Connection c2) {
		this.c1 = c1;
		this.c2 = c2;
		p1 = new Player(c1.getName());
		p2 = new Player(c2.getName());
	}

	/** Makes pre-battle preparations: retrieves and validates teams and so on 
	 * @return true - if initialization succeeded; false - otherwise.
	 */
	@Override
	public boolean initialize(boolean randomBattle) throws InterruptedException {
		if(randomBattle) {
			// generate random teams
			p1.setTeam(this.randomTeam());
			p2.setTeam(this.randomTeam());
		} else {
			// concurrently retreive teams
			if(Debug.on) printDebug("[BATTLE "+p1+","+p2+"]: Retrieving teams...");
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
			if(Debug.on) printDebug("[BATTLE "+c1.getName()+" ~ "+c2.getName()+"] Teams retreived correctly.");
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
	
	protected static Team randomTeam() {
		/* We follow a PokemonShowdown-like algorithm:
		 * level is based on BST. Min level is 70, max is 99.
		 * 600+ BST is 70, 300 is 99, and intermediate between those values.
		 * More specifically, every 10.35 BST adds a level from 70 to 99.
		 */
		// TODO-es:
		// 1. Give items, abilities and moves with some logic, not totally random
		// 2. Check the team hasn't too many ubers or wimps.
		// Get a random initial team, with all levels at 100.
		Team team = Team.randomTeam();
		for(Pony p : team) {
			// Special cases
			if(p.getName().equals("Tirek")) {
				p.setLevel(70);
			} else {
				int bst = Math.min(600, Math.max(300, p.bst()));
				p.setLevel(70 + (int)Math.floor((600 - bst) / 10.35));
			}
		}
		
		return team;
	}

	private Player p1;
	private Player p2;
	private Connection c1;
	private Connection c2;
}
