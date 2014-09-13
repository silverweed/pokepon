//: move/BassDrop.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * another Vinyl's signature;
 * Special move which inflicts good damage and can confuse opponent (20%).
 *
 * @author Giacomo Parolini
 */

public class BassDrop extends Move {
	
	public BassDrop() {
		super("Bass Drop");
		
		type = Type.CHAOS;
		moveType = Move.MoveType.SPECIAL;
		maxpp = pp = 15;
		baseDamage = 90;
		accuracy = 95;
		priority = 0;
		description = "A powerful bass drop hits your enemy. May confuse the target.";
		briefDesc = "20% to confuse the target.";

		animation.put("name","Direct");
		animation.put("sprite","note.png");
		animation.put("bounceBack",false);
		
		targetConfusion = 0.2f;
	}

	public BassDrop(Pony p) {
		this();
		pony = p;
	}
}
