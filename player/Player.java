//: player/Player.java

package pokepon.player;

import pokepon.pony.*;
import static pokepon.util.MessageManager.*;
import pokepon.util.*;
import pokepon.battle.*;
import java.util.*;
 
/** The main class for players' management;
 * each Player has a team (an ArrayList of Ponies).
 *
 * @author silverweed
 */

public class Player {
	
	///////////// PUBLIC METHODS / FIELDS ///////////////
	/** Default constructor: sets name to PlayerX */
	public Player() {
		id = count++;	//id is a final property of each player, equal to (count-1).
		name = "Player"+count;
	}
	
	/** Construct a player with a custom name */
	public Player(String name) {
		this.name = name;
		id = count++;
	}

	/** Copy-constructor; this only copies the name and the id, NOT the team. */
	public Player(final Player other) {
		name = other.name;
		id = other.id;
	}
	
	// GET METHODS //
	
	public String getName() {
		return name;
	}

	public int getID() {
		return id;
	}
	
	public Team getTeam() {
		return team;
	}

	/** @return True if player has at least 1 non-KO pony in team (except Active Pony), false otherwise */
	public boolean canSwitch() {
		for(Pony p : team) {
			if(p.equals(team.getActivePony()) || p.equals(team.getOriginalActivePony())) continue;
			if(!p.isKO()) return true;
		}
		return false;
	}
	
	/* Methods delegated to Team */
	public Pony getPony(int i) {
		return team.getPony(i);
	}
	
	public Pony getPony(String name) {
		return team.getPony(name);
	}

	public List<Pony> getAllPonies() {
		return team.getAllPonies();
	}
	
	public Pony getActivePony() {
		return team.getActivePony();
	}

	// SET METHODS //	
	public void setName(String name) {
		this.name = name;
	}

	public void addPony(Pony pony1) {
		team.add(pony1);
	}

	public void addPony(List<Pony> ponies) {
		team.add(ponies);
	}

	public void addPony(Pony pony1,Pony... ponies) {
		team.add(pony1,ponies);
	}
	
	public void setTeam(Team team) {
		this.team = team;
	}

	public boolean switchPony(int i,final BattleEngine be) {
		Pony curAP = team.getActivePony();
		if(!team.setActivePony(i)) {
			if(Debug.on) printDebug("Couldn't switch in pony #"+i+"!");
			return false;
		}
		if(curAP != null)
			curAP.removeVolatiles();
		//if(Debug.pedantic) printDebug("Calling trigger(onSwitchIn); AP: "+team.getActivePony());
		//team.getActivePony().trigger("onSwitchIn",be);
		return true;
	}

	public boolean switchPony(String name,final BattleEngine be) {
		Pony curAP = team.getActivePony();
		if(!team.setActivePony(name)) {
			if(Debug.on) printDebug("Couldn't switch in pony "+name+"!");
			return false;
		}
		if(curAP != null)
			curAP.removeVolatiles();
		//if(Debug.pedantic) printDebug("Calling trigger(onSwitchIn); AP: "+team.getActivePony());
		//team.getActivePony().trigger("onSwitchIn",be);
		return true;
	}

	public String toString() {
		return "["+id+"]"+name;
	}
	
	///////////// PRIVATE METHODS / FIELDS //////////////
	/** Number of created players. */
	private static byte count;	
	private final byte id;
	private String name;
	Team team = new Team();
	
}
