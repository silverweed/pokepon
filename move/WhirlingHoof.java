//: move/WhirlingHoof.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * The clone of Rapid Spin.
 *
 * @author Giacomo Parolini
 */

public class WhirlingHoof extends Move {

	public WhirlingHoof() {
		super("Whirling Hoof");
		type = Type.PASSION;
		moveType = Move.MoveType.PHYSICAL;
		maxpp = pp = 35;
		baseDamage = 20;
		accuracy = 100;
		priority = 0;
		description = "A rapid spinning attack damages your opponent and removes hazards from your field.";
		briefDesc = "Removes hazards from ally field.";

		animation.put("name","Ballistic2");
		animation.put("sprite","user");

		removesAllyHazards = true;
	}

	public WhirlingHoof(Pony p) {
		this();
		pony = p;
	}
}
