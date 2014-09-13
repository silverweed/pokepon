//: move/RagingSpree.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Inflicts massive special damage, but lowers SpA afterwards.
 *
 * @author Giacomo Parolini
 */

public class RagingSpree extends Move {

	public RagingSpree() {
		super("Raging Spree");
		type = Type.HONESTY;
		moveType = Move.MoveType.SPECIAL;
		maxpp = pp = 8;
		baseDamage = 130;
		accuracy = 90;
		priority = 0;
		description = "";
		briefDesc = "Lowers user's SpA by 2";

		animation.put("name","Direct");
		animation.put("sprite","hoof.png");

		userSpatk = addEntry(-2, 1f);
	}

	public RagingSpree(Pony p) {
		this();
		pony = p;
	}
}
