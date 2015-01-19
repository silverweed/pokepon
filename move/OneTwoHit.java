//: move/OneTwoHit.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;
import static java.util.Arrays.asList;

/**
 * Physical move with 2 hits; may flinch the target.
 *
 * @author Giacomo Parolini
 */

public class OneTwoHit extends Move {
	
	public OneTwoHit() {
		super("One-Two Hit");
	
		type = Type.SPIRIT;
		moveType = Move.MoveType.PHYSICAL;
		maxpp = pp = 15;
		baseDamage = 40;
		accuracy = 95;
		priority = 0;
		description = "A two-hit combo which may make your enemy flinch.";
		briefDesc = "10% to flinch the target. Hits 2 times in a row.";
		
		animation.put("name","Shake");
		animation.put("sprite","user");
		animation.put("shakes", 2);
		animation.put("delay", 10);

		hits = 2;
		/** Probability of doing 1,2,3,4 or 5 hits */
		hitsChance = asList( 0f, 1f);

		targetFlinch = 0.1f;
	}

	public OneTwoHit(Pony p) {
		this();
		pony = p;
	}
}
