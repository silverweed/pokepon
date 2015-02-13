//: battle/RuleSet.java

package pokepon.battle;

import static pokepon.util.MessageManager.*;
import pokepon.util.*;
import java.util.*;
import java.io.*;

/** Class that manages the rules for a battle.
 *
 * @author silverweed
 */
public class RuleSet implements Format {

	/** This enum contains the predefined rulesets; a RuleSet.Predefined has
	 * the same interface as the RuleSet, except the addRule method (the enum
	 * elements are final); use the RuleSet constructor directly to create
	 * customized rulesets.
	 */
	public static enum Predefined implements Format {
		DEFAULT (
			new RuleSet (
				"Default (No Rules)",
				null
			)
		),
		SPECIESCLAUSE (
			new RuleSet (
				"Species Clause Only",
				new String[] { ":speciesclause" }
			)
		),
		CANON (
			new RuleSet (
				"Canon Only",
				new String[] { ":canon" }
			)
		),
		ITEMCLAUSE (
			new RuleSet (
				"Item Clause Only",
				new String[] { ":itemclause" }
			)
		),
		MONOTYPE (
			new RuleSet (
				"Monotype Only",
				new String[] { ":monotype" }
			)
		),
		RANDOMBATTLE (
			new RuleSet (
				"Random Battle",
				new String[] { ":randombattle" }
			)
		),
		NOUBER (
			new RuleSet (
				"No Uber Only",
				new String[] {
					"p:Princess Celestia",
					"p:Princess Luna",
					"p:King Sombra",
					"p:Nightmare Moon",
					"p:Princess Cadance",
					"p:Chrysalis",
					"p:Discord",
					"p:Starswirl",
					"m:Friendship Cannon"
				}
			)
		),
		CLASSIC (
			new RuleSet(
				"Classic",
				null
			)
			.union(SPECIESCLAUSE)
			.union(NOUBER)
		),
		CLASSIC_MONOTYPE (
			new RuleSet(
				"Monotype",
				null
			)
			.union(CLASSIC)
			.union(MONOTYPE)
		);

		Predefined(final RuleSet r) {
			ruleSet = r;
		}
		
		public String toString() {
			return ruleSet.toString();
		}

		public String getName() {
			return ruleSet.getName();
		}

		public RuleSet copy() {
			return ruleSet.copy();
		}

		public RuleSet union(final Format... format) {
			return ruleSet.union(format);
		}

		public RuleSet intersect(final Format... format) {
			return ruleSet.intersect(format);
		}

		public static RuleSet forName(String name) {
			for(Predefined p : values())
				if(p.getName().equals(name)) return p.ruleSet;
			return null;
		}

		public Set<String> getBannedPonies() { return ruleSet.bannedPonies; }
		public Set<String> getBannedMoves() { return ruleSet.bannedMoves; }
		public Set<String> getBannedAbilities() { return ruleSet.bannedAbilities; }
		public Set<String> getBannedItems() { return ruleSet.bannedItems; }
		public Set<String[]> getBannedCombos() { return ruleSet.bannedCombos; }
		public Set<String> getSpecialFormats() { return ruleSet.specialFormats; }

		public RuleSet getRuleSet() { return ruleSet; }
		
		private RuleSet ruleSet;
	}

	/** RuleSet (
	 *	name,
	 *	banlist
	 * )
	 *
	 * 'banlist' is a list of strings, with the format:
	 * "p:nameOfPony" - or
	 * "m:nameOfMove" - or
	 * "a:nameOfAbility" - or
	 * "i:nameOfItem" - or
	 * "c:nameOfCombo" - or
	 * ":nameOfSpecialFormat" (or "S:nameOfSpecialFormat")
	 *
	 * Combos have the syntax:
	 * "c:{m:MoveName,m:OtherMoveName}" -or
	 * "c:{p:PonyName,i:ItemName,...}" - etc
	 *
	 * Special Formats are:
	 * 1) canon - allows only canon ponies;
	 * 2) speciesclause - allows only 1 copy of each pony per team;
	 * 3) itemclause - only 1 copy of each item per team
	 * 4) randombattle - uses Random Battle's special rules
	 */
	public RuleSet(String name,String[] rules) {
		this.name = name;
		if(rules != null) 
			for(String rule : rules)
				addRule(rule);
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public String getName() {
		return name;
	}

	public void addRule(String rule) {
		if(rule == null) return;
		String[] token = rule.split(":",2);
		if(token == null || token.length != 2 || token[0].length() > 1) {
			ignore(rule);
			return;
		}

		char type = token[0].length() == 0 ? 'S' : token[0].charAt(0);

		switch(type) {
			case 'p':
				bannedPonies.add(token[1]);
				break;
			case 'm':
				bannedMoves.add(token[1]);
				break;
			case 'a':
				bannedAbilities.add(token[1]);
				break;
			case 'i':
				bannedItems.add(token[1]);
				break;
			case 'S':
				specialFormats.add(token[1]);
				break;
			case 'c': {
				if(!(token[1].startsWith("{") && token[1].endsWith("}"))) {
					ignore(rule);
					return;
				}
				// format is c:{m:nameofmove,i:nameofitem,...}
				String[] tmp = token[1].substring(1,token[1].length()-1).split("\\s*,\\s*");
				if(tmp == null || tmp.length < 2) {
					ignore(rule);
					return;
				}
				if(Debug.on) printDebug("RuleSet: "+name+": added combo "+tmp);
				bannedCombos.add(tmp);
				break;
			}
		}
	}
	
	/** This method does the opposite thing as addRule(), returning an array of
	 * string which may be used to set the same rules as this RuleSet onto another one.
	 */
	public String[] getRules() {
		Set<String> rules = new HashSet<String>();
		for(String s : bannedPonies) 
			rules.add("p:"+s);
		for(String s : bannedMoves) 
			rules.add("m:"+s);
		for(String s : bannedItems) 
			rules.add("i:"+s);
		for(String s : bannedAbilities) 
			rules.add("a:"+s);
		for(String[] s : bannedCombos) {
			StringBuilder sb = new StringBuilder("c:{");
			for(String c : s) 
				sb.append(c+",");
			if(sb.length() < 4) continue;
			sb.delete(sb.length()-1,sb.length());
			sb.append("}");
			rules.add(sb.toString());
		}
		for(String s : specialFormats) 
			rules.add("S:"+s);
		
		return rules.toArray(new String[0]);
	}

	public Set<String> getBannedPonies() { return bannedPonies; }
	public Set<String> getBannedMoves() { return bannedMoves; }
	public Set<String> getBannedAbilities() { return bannedAbilities; }
	public Set<String> getBannedItems() { return bannedItems; }
	public Set<String[]> getBannedCombos() { return bannedCombos; }
	public Set<String> getSpecialFormats() { return specialFormats; }

	public RuleSet copy() {
		return new RuleSet(name,getRules());
	}

	/** @return A RuleSet whose rules are the union of this's ones and
	 * all the rules of the given Formats. 
	 */
	public RuleSet union(final Format... others) {
		RuleSet rs = this.copy();
		for(Format other : others) {
			rs.getBannedPonies().addAll(other.getBannedPonies());
			rs.getBannedMoves().addAll(other.getBannedMoves());
			rs.getBannedAbilities().addAll(other.getBannedAbilities());
			rs.getBannedItems().addAll(other.getBannedItems());
			rs.getBannedCombos().addAll(other.getBannedCombos());
			rs.getSpecialFormats().addAll(other.getSpecialFormats());
		}
		return rs;
	}

	/** @return A Format whose rules are the intersection of this's ones and
	 * all the rules of the given Formats. 
	 */
	public RuleSet intersect(final Format... others) {
		RuleSet rs = this.copy();
		for(Format other : others) {
			rs.getBannedPonies().retainAll(other.getBannedPonies());
			rs.getBannedMoves().retainAll(other.getBannedMoves());
			rs.getBannedAbilities().retainAll(other.getBannedAbilities());
			rs.getBannedItems().retainAll(other.getBannedItems());
			rs.getBannedCombos().retainAll(other.getBannedCombos());
			rs.getSpecialFormats().retainAll(other.getSpecialFormats());
		}
		return rs;
	}

	public void printInfo() {
		printMsg(getInfo());
	}

	public String getInfo() {
		StringBuilder sb = new StringBuilder
		(	"RuleSet "+name+":\n"+
			"bannedPonies: "+bannedPonies+"\n"+
			"bannedMoves: "+bannedMoves+"\n"+
			"bannedItems: "+bannedItems+"\n"+
			"bannedAbilities: "+bannedAbilities+"\n"+
			"bannedCombos: "
		);
		for(String[] c : bannedCombos) {
			sb.append("\n -"+Arrays.asList(c));
		}
		sb.append("\n");
		sb.append("specialFormats: ");
		for(String s : specialFormats) {
			sb.append("\n -"+s);
			if(s.equals("speciesclause")) 
				sb.append(" (only 1 pony of each species allowed)");
			else if(s.equals("canon"))
				sb.append(" (only show-canon characters are allowed)");
			else if(s.equals("itemclause"))
				sb.append(" (only 1 copy of each item is allowed per team)");
			else if(s.equals("monotype"))
				sb.append(" (all ponies in team must share a type)");
		}
		
		return sb.toString();
	}


	public static void main(String[] args) throws IOException {
		RuleSet rs = new RuleSet("Custom",null);
		String line = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while((line = br.readLine()) != null) {
			rs.addRule(line);
			rs.printInfo();
		}
	}

	private String name;
	private Set<String> bannedPonies = new HashSet<String>();
	private Set<String> bannedMoves = new HashSet<String>();
	private Set<String> bannedAbilities = new HashSet<String>();
	private Set<String> bannedItems = new HashSet<String>();
	private Set<String[]> bannedCombos = new HashSet<String[]>();
	private Set<String> specialFormats = new HashSet<String>();

	private void ignore(String rule) {
		printDebug("Ignoring malformed rule: "+rule);
	}
}

