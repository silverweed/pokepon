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
 * @author Giacomo Parolini
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

	private Player p1;
	private Player p2;
	private Connection c1;
	private Connection c2;
}
