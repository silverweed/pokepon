//: move/HollerOut.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Special move which inflicts good damage;
 * May confuse the enemy.
 *
 * @author Tommaso Parolini
 */

public class HollerOut extends Move {
	
	public HollerOut() {
		super("Holler Out");
		
		type = Type.PASSION;
		moveType = Move.MoveType.SPECIAL;
		maxpp = pp = 16;
		baseDamage = 85;
		accuracy = 95;
		priority = 0;
		description = "Shout out with enthusiasm; target may end up confused.";
		briefDesc = "10% to confuse the target.";
		
		targetConfusion = 0.1f;
	}

	public HollerOut(Pony p) {
		this();
		pony = p;
	}
}
