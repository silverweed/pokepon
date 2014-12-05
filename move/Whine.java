//: move/Whine.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;
import pokepon.battle.*;

/** 
 * Status move which reduces target's Def and SpDef
 *
 * @author Tommaso Parolini
 */

public class Whine extends Move {
	
	public Whine() {
		super("Whine");

		type = Type.LOVE;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 20;
		accuracy = -1;
		priority = 0;
		description = "The enemy is stressed out by incessant complaining which lowers his defenses.";
		briefDesc = "Lowers target's Def and SpD by 1.";

		animation.put("name","Shake");
		animation.put("sprite","user");

		targetDef = addEntry(-1,1f);
		targetSpdef = addEntry(-1,1f);
	}

	public Whine(Pony p) {
		this();
		pony = p;
	}

	/** Will fail if enemy already has -6 def and spdef */
	@Override
	public boolean validConditions(final BattleEngine be) {
		return be.getDefender().defMod() > -6 || be.getDefender().spdefMod() > -6;
	}
}
