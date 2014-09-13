//: battle/TeamValidator.java

package pokepon.battle;

import pokepon.player.*;
import pokepon.pony.*;
import pokepon.move.*;
import pokepon.util.*;
import static pokepon.util.MessageManager.*;
import java.util.*;
import java.util.concurrent.*;

/** This class implements a task that (asynchronously) validates a team according to
 * certain game rules (which are established by the Format class passed);
 * call() yields True if the team is valid and False otherwise.
 *
 * @author silverweed
 */
public class TeamValidator implements Callable<Boolean> {

	public TeamValidator(final Team team,final Format rules) {
		this.team = team;
		this.rules = rules;
	}

	public synchronized Boolean call() {
		result = true;
		if(team.members() < 1) {
			result = false;
			reasons.add("Your team is empty!");
			return false;
		}
		/* Check if team violates species clause (if active) */
		if(rules.getSpecialFormats().contains("speciesclause") && team.containsDuplicate()) {
			result = false;
			reasons.add("Multiple copies of the same pony are not allowed in current format ("+rules+").");
		}
		/* Check if item clause is violated, if active */
		if(rules.getSpecialFormats().contains("itemclause") && team.containsDuplicateItems()) {
			result = false;
			reasons.add("Multiple copies of the same item are not allowed in current format ("+rules+").");
		}
		/* Check if team violates monotype clause, if active */
		if(rules.getSpecialFormats().contains("monotype") && !team.isMonotype()) {
			result = false;
			reasons.add("All the ponies must share at least 1 type ("+rules+").");
		}
		for(Pony p : team.getAllPonies()) {
			/* Check if this pony is banned in current format */
			if(rules.getBannedPonies().contains(p.getName())) {
				result = false;
				reasons.add("Pony \""+p.getName()+"\" is banned in current format ("+rules+").");
			}
			if(rules.getSpecialFormats().contains("canon") && !p.isCanon()) {
				result = false;
				reasons.add("Pony \""+p.getName()+"\" is banned in current format ("+rules+") because it's not canon.");
			}
			/* Check the moveset: check if there are moves which are
			 * banned by the current RuleSet or which the pony can't learn.
			 * We also check if a pony has the same
			 * move two or more times.
			 */
			int numht = 0;
			for(Move m : p.getMoves()) {
				if(m.getName().startsWith("Hidden Talent")) {
					if(++numht > 1) {
						result = false;
						reasons.add(p.getName()+" cannot have more than 1 Hidden Talent.");
					}
					adjustHiddenTalentIVs(p);
					continue;
				}
				if(!p.canLearn(m)) {
					result = false;
					reasons.add(p.getName()+" can't learn "+m.getName()+".");
				}
				if(rules.getBannedMoves().contains(m.getName())) {
					result = false;
					reasons.add(p.getName()+"'s move \""+m.getName()+"\" is banned in current format ("+rules+").");
				}
				if(Collections.frequency(p.getMovesNames(),m.getName()) > 1) {
					result = false;
					reasons.add(p.getName()+"'s move \""+m.getName()+"\" is repeated more than once.");
				}
			}

			/* Check the ability and the item */
			if(p.getItem() != null && rules.getBannedItems().contains(p.getItem().getName())) {
				result = false;
				reasons.add(p.getName()+"'s item \""+p.getItem()+"\" is banned in current format ("+rules+").");
			}
			if(p.getAbility() != null && rules.getBannedAbilities().contains(p.getAbility().getName())) {
				result = false;
				reasons.add(p.getName()+"'s ability \""+p.getAbility()+"\" is banned in current format ("+rules+").");
			}

			/* Check banned combos.
			 * A 'combo' is the simultaneous presence of a pony/item/move/ability.
			 * The format of a bannedCombo array is:
			 * combo = { "p:nameofpony", "m:nameofmove", ... }
			 */
			for(String[] combo : rules.getBannedCombos()) {
				int comboCount = 0;
				for(String s : combo) {
					char type = s.charAt(0);
					switch(type) {
						case 'p':
							if(p.getName().equals(s.substring(2,s.length())))
								++comboCount;
							break;
						case 'm':
							if(p.getMovesNames().contains(s.substring(2,s.length())))
								++comboCount;
							break;
						case 'i':
							if(p.getItem().equals(s.substring(2,s.length())))
								++comboCount;
							break;
						case 'a':
							if(p.getAbility().equals(s.substring(2,s.length())))
								++comboCount;
							break;
					}
				}
				if(comboCount == combo.length) {
					result = false;
					StringBuilder sb = new StringBuilder("Combo ");
					for(String cb : combo) {
						sb.append(cb.substring(2,cb.length())+"+");
					}
					sb.delete(sb.length()-1,sb.length());
					sb.append(" is banned.");
					reasons.add(sb.toString());
				}
			}
			if(Debug.on) printDebug("Pony: "+p.getName()+" - valid = "+result);
		}			
		
		return result;
	}
	
	public synchronized String getReasons() {
		StringBuilder sb = new StringBuilder("");
		for(String reason : reasons) {
			if(sb.length() > 0) sb.append("\n");
			sb.append(reason);
		}
		return sb.toString();
	}

	/** Ensure the pony's IVs are compatible with its Hidden Talent */
	private static void adjustHiddenTalentIVs(Pony p) {
		HiddenTalent mv = null;
		for(Move m : p.getMoves()) {
			if(m.getName().startsWith("Hidden Talent")) {
				mv = (HiddenTalent)m;
				if(mv.getTypeByIVs(p) == mv.getType())
					return; 
				else break;
			}
		}
		if(Debug.on) {
			printDebug("[TeamValidator] pony "+p+" has incompatible type / HT; adjusting it.");
			printDebug("Before: { "+p.getIV("hp")+", "+p.getIV("atk")+", "+p.getIV("def")+", "+p.getIV("spatk")+", "+p.getIV("spdef")+", "+p.getIV("speed")+" }");
		}
		// if we didn't return here, the type is incompatible with pony's IV.
		// So, adjust them.
		HiddenTalent.adjustIVs(p, mv.getType());
		if(Debug.on)
			printDebug("After: { "+p.getIV("hp")+", "+p.getIV("atk")+", "+p.getIV("def")+", "+p.getIV("spatk")+", "+p.getIV("spdef")+", "+p.getIV("speed")+" }");
	}

	private Boolean result;
	/** Reasons why the team was rejected, if any. */
	private Set<String> reasons = new HashSet<String>();
	private final Format rules;
	private final Team team;
}
