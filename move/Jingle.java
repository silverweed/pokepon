//: move/Jingle.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Inflicts decent damage and can lower enemy's SpD
 *
 * @author Giacomo Parolini
 */
public class Jingle extends Move {
	
	public Jingle() {
		super("Jingle");
		
		type = Type.MUSIC;
		moveType = Move.MoveType.SPECIAL;
		maxpp = pp = 25;
		baseDamage = 70;
		accuracy = 100;
		priority = 0;
		description = "A sweet tune that may lower target's SpD";
		briefDesc = "10% to lower target's SpD by 1.";

		targetSpdef = addEntry(-1, 0.1f);

		animation.put("name","Ballistic");
		animation.put("sprite","note.png");
		animation.put("bounceBack",false);
	}

	public Jingle(Pony p) {
		this();
		pony = p;
	}
}
