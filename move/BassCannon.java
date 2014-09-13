//: move/BassCannon.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Vinyl Scratch's signature move;
 * Special move which inflicts good damage and ignores resistances;
 * Can Confuse user.
 *
 * @author Giacomo Parolini
 */

public class BassCannon extends Move {
	
	public BassCannon() {
		super("Bass Cannon");
		
		type = Type.MUSIC;
		moveType = Move.MoveType.SPECIAL;
		maxpp = pp = 5;
		baseDamage = 120;
		accuracy = 100;
		priority = 0;
		description = "Hit opponents with WUBS! There is a chance to confuse yourself in the process. Ignores weaknesses and resistances.";
		briefDesc = "30% to confuse the user.<br>Ignore weakness and resistance.";

		animation.put("name","Direct");
		animation.put("sprite","note.png");
		animation.put("bounceBack",false);
		animation.put("delay",70);
		
		ignoreWeaknesses = true;
		ignoreResistances = true;
		userConfusion = 0.3f;	//30% to confuse yourself

	}

	public BassCannon(Pony p) {
		this();
		pony = p;
	}
}
