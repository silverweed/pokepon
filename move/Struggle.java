//: move/Struggle.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.battle.BattleEngine;
import pokepon.pony.Pony;

/**
 * Move used when a pony has no more PP.
 *
 * @author Giacomo Parolini
 */

public class Struggle extends Move {
	
	public Struggle() {
		super("Struggle");
		
		type = Type.HONESTY;
		moveType = Move.MoveType.PHYSICAL;
		contactMove = true;
		maxpp = pp = -1; //-1 means infinite PPs.
		baseDamage = 40;
		accuracy = -1;
		priority = 0;
		description = "The move used when all other moves have no PP.";
		briefDesc = "Has 25% of user's HP as recoil.";

		animation.put("name","Ballistic2");
		animation.put("sprite","user");

		typeless = true;
		ignoreWeaknesses = true;
		ignoreResistances = true;
		ignoreImmunities = true;
	}
	
	public Struggle(Pony p) {
		this();
		pony = p;
	}
}
