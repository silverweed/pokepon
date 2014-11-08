//: move/Applebuck.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Inflicts good physical damage.
 *
 * @author silverweed
 */

public class Applebuck extends Move {

	public Applebuck() {
		super("Applebuck");
		type = Type.HONESTY;
		moveType = Move.MoveType.PHYSICAL;
		contactMove = true;
		maxpp = pp = 25;
		baseDamage = 70;
		accuracy = 100;
		priority = 0;
		description = "Buck your enemies' muzzles like they were apple trees.";
		briefDesc = "Inflicts regular damage.";

		animation.put("name","Ballistic2");
		animation.put("sprite","user");
	}

	public Applebuck(Pony p) {
		this();
		pony = p;
	}
}
