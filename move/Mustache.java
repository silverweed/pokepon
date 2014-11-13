//: move/Mustache.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.util.*;
import pokepon.battle.*;
import pokepon.pony.Pony;

/**
 * If the opponent is female, deals +50% and may lower a random stat.
 *
 * @author silverweed
 */

public class Mustache extends Move {
	
	public Mustache() {
		super("Mustache");
		
		type = Type.LOVE;
		moveType = Move.MoveType.SPECIAL;
		maxpp = pp = 20;
		baseDamage = 60;
		accuracy = 95;
		priority = 0;
		description = "No mare can resist 'The Stache'. If opponent is female, inflicts 50% more damage and may lower a random stat.";
		briefDesc = "If opponent is female, deals +50%<br>and has 20% to lower a random stat.";

		animation.put("name", "Shake");
		animation.put("sprite", "user");
	}
	
	public Mustache(Pony p) {
		this();
		pony = p;
	}

	@Override
	public boolean validConditions(final BattleEngine be) {
		if(be.getDefender() != null && be.getDefender().getSex() == Pony.Sex.FEMALE) {
			damageBoost = (int)(baseDamage * 0.5);
			targetAtk = targetDef = targetSpatk = targetSpdef = targetAccuracy = targetEvasion = addEntry(0,0f);
			switch(be.getRNG().nextInt(7)) {
				case 0:
					targetAtk = addEntry(-1, 0.2f);
					break;
				case 1:
					targetDef = addEntry(-1, 0.2f);
					break;
				case 2:
					targetSpatk = addEntry(-1, 0.2f);
					break;
				case 3:
					targetSpdef = addEntry(-1, 0.2f);
					break;
				case 4:
					targetSpeed = addEntry(-1, 0.2f);
					break;
				case 5:
					targetAccuracy = addEntry(-1, 0.2f);
					break;
				case 6:
					targetEvasion = addEntry(-1, 0.2f);
					break;
			}
		} else {
			damageBoost = 0;
			targetAtk = targetDef = targetSpatk = targetSpdef = targetSpeed = targetAccuracy = targetEvasion = addEntry(0,0f);
		}
		return true;
	}
}
