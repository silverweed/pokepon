//: move/SkyDive.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Inflicts good physical damage.
 *
 * @author Giacomo Parolini
 */

public class SkyDive extends Move {

	public SkyDive() {
		super("Sky Dive");
		type = Type.LOYALTY;
		moveType = Move.MoveType.PHYSICAL;
		maxpp = pp = 30;
		baseDamage = 90;
		accuracy = 100;
		priority = 0;
		description = "Swoop towards your enemy from the sky to inflict massive damage.";
		briefDesc = "Inflicts regular damage.";

		animation.put("name","Direct");
		animation.put("sprite","user");
		animation.put("passThrough",true);
	}

	public SkyDive(Pony p) {
		this();
		pony = p;
	}
}
