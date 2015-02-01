//: player/Team.java

package pokepon.player;

import pokepon.pony.*;
import pokepon.item.*;
import pokepon.enums.Type;
import static pokepon.util.MessageManager.*;
import pokepon.util.*;
import java.util.*;
import java.lang.reflect.*;

/** The managing class for the team;
 * contains an Array of Ponies with fixed length, and insertions and
 * deletions are made so that all ponies are always adjacent (i.e
 * null entries are always in the tail of the team.)
 *
 * @author silverweed
 */
public class Team implements Iterable<Pony> {

	////////////// CONSTANT VALUES /////////////////
	public static final int MAX_TEAM_SIZE = 6;
	
	//////////// PUBLIC METHODS / FIELDS //////////////
	public Team() {}
	
	public Team(Pony... ponies) {
		for(Pony p : ponies) {
			if(p == null) continue;
			pony[members++] = p;
			p.setTeam(this);
			if(members >= MAX_TEAM_SIZE) return;
		}
	}
	
	public Team(List<Pony> ponies) {
		for(Pony p : ponies) {
			if(p == null) continue;
			pony[members++] = p;
			p.setTeam(this);
			if(members >= MAX_TEAM_SIZE) return;
		}
	}
	
	// GET METHODS //
	public Pony getPony(int i) {
		if(i < 0 || i >= MAX_TEAM_SIZE) return null;
		return pony[i];
	}
	
	/** Get a pony by nickname (if nickname is not set, it uses the regular name);
	 * will return null if no pony with that name is found; if more ponies share
	 * the same name, it will return the first one.
	 */
	public Pony getPony(String name) {
		for(Pony p : pony) {
			if(p == null) return null;
			if(p.getNickname().equals(name)) return p;
		}
		return null;
	}

	/** Like getPony(String), but if more ponies share the same name, return
	 * all of them (up to num)
	 */
	public List<Pony> getPonies(String name, int num) {
		int count = 0;
		List<Pony> list = new ArrayList<>();
		for(Pony p : pony) {
			if(p == null) return list;
			if(p.getNickname().equals(name)) {
				list.add(p);
				if(++count == num) return list;
			}
		}
		return list;
	}
	
	/** @return All ponies in team; guarantees not to return null members. */
	public List<Pony> getAllPonies() {
		List<Pony> list = new ArrayList<Pony>();
		for(Pony p : pony) {
			if(p == null) return list;
			list.add(p);
		}
		return list;
	}
	
	/** @return The number of non-KO ponies in this team (including the AP) */
	public int getViablePoniesCount() {
		int count = 0;
		for(Pony p : pony) {
			if(p == null) return count;
			if(!p.isKO())
				++count;
		}
		return count;
	}		

	public Pony getActivePony() {
		if(activePony != null) return activePony;
		/* If activePony is not set, try searching for one */
		for(Pony p : pony) {
			if(p == null) return null;
			if(p.isActive()) {
				if(Debug.pedantic) printDebug("Team.getActivePony() returning "+p);
				activePony = p; // save for future searches
				return activePony;
			}
		}
		return null;
	}

	/** @return The original active pony - if active pony is transformed; null otherwise. */
	public Pony getOriginalActivePony() {
		return activePony.getOriginal();
	}

	/** @return True if all ponies in this team are KO, false otherwise */
	public boolean allKO() {
		for(Pony p : pony) {
			if(p == null) return true;
			if(!p.isKO()) {
				if(Debug.on) printDebug("Team.allKO() returned false.");
				return false;
			}
		}
		if(Debug.on) printDebug("Team.allKO() returned true.");
		return true;
	}

	/** @return True if the team contains multiple copies of a pony, False otherwise. */
	public boolean containsDuplicate() {
		for(int i = 0; i < members; ++i) 
			for(int j = i + 1; j < members; ++j) 
				if(pony[i].getName().equals(pony[j].getName()))
					return true;
		return false;
	}
	/** @return True if more instances of the same item are owned by team members, False otherwise. */
	public boolean containsDuplicateItems() {
		Item[] itemlist = new Item[members];
		for(int i = 0; i < members; ++i) 
			if(pony[i].getItem() != null) { 
				itemlist[i] = pony[i].getItem();
				for(int j = 0; j < i; ++j) {
					if(itemlist[j] == null) continue;
					if(itemlist[j].getName().equals(itemlist[i].getName()))
						return true;
				}
			}
		return false;
	}
	/** @return True if all team members share at least 1 type, False otherwise. */
	public boolean isMonotype() {
		Map<Type,Integer> typeCnt = new EnumMap<>(Type.class);
		for(Pony pny : pony) {
			if(pny == null) break;
			for(int i = 0; i < Pony.MAX_TYPES; ++i) {
				if(pny.getType(i) == null) break;
				if(typeCnt.get(pny.getType(i)) == null)
					typeCnt.put(pny.getType(i), 1);
				else
					typeCnt.put(pny.getType(i), typeCnt.get(pny.getType(i)) + 1);
			}
		}
		for(Map.Entry<Type,Integer> entry : typeCnt.entrySet()) {
			if(entry.getValue() >= members) return true;
		}
		return false;
	}
	
	// SET METHODS //
	
	/** Add ponies to team until max number of members is reached; the first pony 
	 * added to the team becomes ActivePony if the team was empty.
	 * @return True if all given ponies were added, False otherwise.
	 */
	public boolean add(Pony pony1, Pony... otherPonies) {
		if(members >= MAX_TEAM_SIZE) return false;

		pony[members++] = pony1;
		pony1.setTeam(this);
		
		for(Pony p : otherPonies) {
			if(members >= MAX_TEAM_SIZE) return false;
			pony[members++] = p;
			p.setTeam(this);
		}

		return true;
	}
	
	/** Like add, but giving a List. */
	public boolean add(List<Pony> ponies) {
		int i = members;

		for(Pony p : ponies) {
			if(members >= MAX_TEAM_SIZE) return false;
			pony[members++] = p;
			p.setTeam(this);
		}
		
		return true;
	}

	/** Replace i-th pony with 'p'; if p is null, shift all ponies at
	 * its right by 1 to keep ponies adjacent.
	 * @return True if the operation was successful, false otherwise.
	 */
	public boolean setPony(int i, Pony p) {
		if(i < 0 || i > 5) return false;
		if(pony[i] == null) {
			if(p == null) return true; // both pony[i] and p are null: no-op.
			// this is actually an add()
			return add(p);
		}
		if(p == null) {
			// this is a remove(i)
			return remove(i) != null;
		}
		// this is a legit replace (members number is not modified)
		pony[i] = p;
		return true;
	}
	
	/** Remove pony in position i and left-shift all ponies at its right by 1.
	 * @return The removed Pony or null
	 */
	public Pony remove(int i) {
		if(i < 0 || i >= members) return null;
		Pony tmp = pony[i];
		pony[i] = null;
		// shift ponies to left
		for(int j = i + 1; j < MAX_TEAM_SIZE; ++j) {
			if(pony[j] == null) break;
			pony[j-1] = pony[j];
		}
		--members;
		return tmp;
	}
	
	/** (Try to) remove pony equal to given Pony. 
	 * @return True if the given pony was found, False otherwise.
	 */
	public boolean remove(Pony pony1) {
		for(int i = 0; i < MAX_TEAM_SIZE; ++i)
			if(pony[i] == pony1) {
				return remove(i) != null;
			}
		return false;
	}
	
	/** Remove first pony with name "name", if existing.
	 * @return True if at least 1 pony was removed, False otherwise.
	 */
	public boolean remove(String name) {
		boolean found = false;
		
		for(int i = 0; i < MAX_TEAM_SIZE; ++i) {
			if(pony[i].getName().equals(name)) {
				return remove(i) != null;
			}
		}
		return false;
	}

	/** Removes all ponies from team. */
	public void clear() {
		for(int i = 0; i < MAX_TEAM_SIZE; ++i)
			pony[i] = null;
		members = 0;
	}
	
	/** Sets i-th member of team as Active Pony, if exising; If another pony was already Active, it gets Inactive.
	 * @return Return true if a pony was set as Active Pony, else false.
	 */
	public boolean setActivePony(int i) {
		if(pony[i] != null) {
			if(pony[i].isKO()) {
				printMsg(pony[i].getNickname()+" is fainted and cannot fight!");
				return false;
			}
			if(pony[i].isActive()) {
				printMsg(pony[i].getNickname()+" is already on the field!");
				return false;
			}
			if(activePonyTransformed())
				transformBackActivePony();
			if(activePony != null)
				activePony.setActive(false);
			pony[i].setActive(true);
			activePony = pony[i];
			if(Debug.on) printDebug("Set active pony: "+pony[i]);
			if(Debug.pedantic) printDebug("[Team] successfully switched; new active pony is: "+getActivePony());
			return true;
		} else {
			if(Debug.on) printDebug("Error: member #"+i+" is null: not setting active.");
			return false;
		}
	}
	
	/** Tries to set a pony named "name" as Active Pony; If another pony was already Active, it get Inactive;
	 * Will fail if more than one pony is named "name" in team.
	 * @return Return true if a pony was set as Active Pony, else false.
	 */
	public boolean setActivePony(String name) {
		for(Pony p : pony) {
			if(p == null) return false;
			if(p.getName().equals(name)) {
				if(p.isKO()) {
					printMsg(p.getNickname()+" is fainted and cannot fight!");
					return false;
				}
				if(p.isActive()) {
					printMsg(p.getNickname()+" is already on the field!");
					return false;
				}
				if(activePonyTransformed())
					transformBackActivePony();
				if(activePony != null)
					activePony.setActive(false);
				p.setActive(true);
				activePony = p;
				if(Debug.on) printDebug("Set active pony: "+p);
				if(Debug.pedantic) printDebug("[Team] successfully switched; new active pony is: "+getActivePony());
				return true;
			} 
		}
		return false;
	}

	/** Transforms the Active pony into the given one, backing up the original reference (that can be restored via
	 * transformBackActivePony method - or automatically when the ActivePony changes.)
	 * @param cloned The pony in which the current active pony must transform
	 * @return True if active pony was successfully transformed, false otherwise.
	 */
	public boolean transformActivePonyInto(Pony cloned) {
		if(activePony == null) {
			if(Debug.on) printDebug("Team.transformActivePonyInto("+cloned+"): Active pony is null!");
			return false;
		}
		if(Debug.pedantic) printDebug("Called Team.transformActivePonyInto("+cloned+") [original: "+activePony+"]");
		try {
			activePony.transformInto(cloned);
		} catch(Exception e) {
			printDebug("Caught exception while calling clone in Team.transformActivePonyInto("+cloned+"): "+e);
			return false;
		}
		return true;
	}

	/** Resets the activePony to the original one manually.
	 * @return True - if the activePony was successfully restored, false otherwise (including when activePony is not transformed).
	 */
	public boolean transformBackActivePony() {
		if(activePony == null) {
			if(Debug.on) printDebug("Team.transformBackActivePony(): Active pony is null!");
			return false;
		}
		if(activePony.getOriginal() == null) {
			if(Debug.on) printDebug("Team.transformBackActivePony(): Active pony was not transformed.");
			return false;
		}
		if(Debug.pedantic) printDebug("Called Team.transformBackActivePony() [from "+activePony+" to "+activePony.getOriginal()+"]");
		try {
			activePony.transformBack();
		} catch(ClassCastException e) {
			printDebug("Caught ClassCastException in Team.transformBackActivePony(): "+e);
			return false;
		}
		return true;
	}

	/** @return True if active pony is currently transformed, false otherwise */
	public boolean activePonyTransformed() {
		return activePony != null && activePony.isTransformed();
	}

	public void healTeamStatus() {
		for(Pony p : pony) {
			if(p == null) return;
			p.healStatus();
			p.setConfused(false);
		}
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder("[");
		for(Pony p : pony) {
			if(p == null) {
				sb.append("empty, ");
			} else {
				sb.append(p.toString());
				if(p.isActive())
					sb.append("(A)");
				sb.append(", ");
			}
		}
		if(sb.length() > 1)
			sb.delete(sb.length()-2,sb.length());
		sb.append("]");

		return sb.toString();
	}

	// ITERABLE METHODS //
	/** Since in add(), remove() and set() methods we ensure all ponies are
	 * adjacent, this iterator guarantees not to return null ponies (in fact
	 * its hasNext() condition is next() != null).
	 */
	public Iterator<Pony> iterator() {
		return new Iterator<Pony>() {
			private int pos = 0;

			public boolean hasNext() {
				return pos < members;
			}

			public Pony next() throws NoSuchElementException {
				if(hasNext()) 
					return pony[pos++];
				throw new NoSuchElementException();
			}

			public void remove() {
				Team.this.remove(pos);
				// keep pos the same as before, since we shifted all other ponies.
			}
		};
	}

	public static Team randomTeam() {
		return randomTeam(MAX_TEAM_SIZE, false, true);
	}

	public static Team randomTeam(int members) {
		return randomTeam(members, false, true);
	}

	public static Team randomTeam(int members,boolean allowDuplicate) {
		return randomTeam(members, allowDuplicate, true);
	}

	/** Returns a team with 'members' random members.
	 * @param members Number of team members (between 0 and 6, default: 6)
	 * @param allowDuplicate Allow more ponies of the same species in team (default: false)
	 * @param randMovesAndAbilities Also assign random moves and abilities to ponies (default: true)
	 */
	public static Team randomTeam(int members,boolean allowDuplicate,boolean randMovesAndAbilities) {
		Team team = new Team();

		if(allowDuplicate) {
			for(int i = 0; i < Math.min(MAX_TEAM_SIZE,members); ++i) {
				try {
					team.add(PonyCreator.createRandom());
				} catch(ReflectiveOperationException e) {
					printDebug("[randomTeam]: failed to create pony: "+e);
				}
			}
		} else {
			for(int i = 0; i < Math.min(MAX_TEAM_SIZE,members); ++i) {
				Pony pony = null;
				int count = 0;
				do {
					try {
						pony = PonyCreator.createRandom();
					} catch(ReflectiveOperationException e) {
						printDebug("[randomTeam]: failed to create pony: "+e);
					}
					++count;
				} while(team.contains(pony.getName()) && count < 1024);	//prevent too many iterations
				team.add(pony);
			}
		}
		if(randMovesAndAbilities) {
			for(int j = 0; j < MAX_TEAM_SIZE; ++j) {
				Pony pony = team.getPony(j);
				List<String> moves = new LinkedList<>(pony.getLearnableMoves().keySet());
				Collections.shuffle(moves);
				for(String m : moves) {
					pony.learnMove(m);
				}
				if(Debug.on) printDebug(pony.getName()+": moves: "+pony.getMoves());
				if(pony.getPossibleAbilities().size() != 0) {
					try {
						pony.setAbility(AbilityCreator.create(pony.getPossibleAbilities().get(
							(new Random()).nextInt(pony.getPossibleAbilities().size()))));
						if(Debug.on) printDebug(pony.getName()+": set ability to "+pony.getAbility());
					} catch(ReflectiveOperationException e) {
						printDebug("[randomTeam]: Exception while creating ability: "+e);
					}
				}
			}
		}
		return team;
	}


	/** @return List of ponies' saveData (useful to send or save team data) */
	public List<String> getTeamData() {
		List<String> data = new ArrayList<String>();
		for(Pony p : pony) {
			if(p == null) return data;
			data.add(p.getSaveData());
		}
		return data;
	}

	/** @return True if memberlist contains the same object as the one referred by p. */
	public boolean contains(Pony p) {
		for(int i = 0; i < MAX_TEAM_SIZE; ++i) {
			if(pony[i] == null) return false;
			if(pony[i] == p) return true;
		}
		return false;
	}

	/** @return True if memberlist contains a pony with name 'name'. */
	public boolean contains(String name) {
		for(Pony p : pony) {
			if(p == null) return false;
			if(p.getName().equals(name)) return true;
		}
		return false;
	}

	public String getName() { return name; }
	public void setName(String newname) {
		name = newname;
	}

	/** Given a pony, returns its index in team, or -1 if not found (the comparison is made via ==) */
	public int indexOf(Pony pny) {
		for(int i = 0; i < members; ++i)
			if(pony[i] == pny) return i;
		return -1;
	}

	public int members() { return members; }

	///////////////////////////////////////// END PUBLIC
	
	//////////////// PRIVATE MEMBERS ////////////////
	/** Member ponies */
	private Pony[] pony = new Pony[MAX_TEAM_SIZE];
	/** Number of members */
	private int members;
	/** Active pony (must be non-null before entering battle) */
	private Pony activePony;
	/** This stores the original reference of a transformed pony (there may be up to 1 transformed
	 * pony in a Team, since transformation ends when it is not Active)
	 */
	private String name = "Untitled Team";
}
