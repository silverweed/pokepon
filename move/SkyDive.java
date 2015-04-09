//: move/SkyDive.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;
import pokepon.gui.animation.AnimPresets;

/**
 * Inflicts good physical damage.
 *
 * @author silverweed
 */

public class SkyDive extends Move {

	public SkyDive() {
		super("Sky Dive");
		type = Type.LOYALTY;
		moveType = Move.MoveType.PHYSICAL;
		contactMove = true;
		maxpp = pp = 30;
		baseDamage = 90;
		accuracy = 100;
		priority = 0;
		description = "Swoop towards your enemy from the sky to inflict massive damage.";
		briefDesc = "Inflicts regular damage.";

		animation = new java.util.HashMap<>(AnimPresets.get("cross"));
		animation.put("sprite","user");
	}

	public SkyDive(Pony p) {
		this();
		pony = p;
	}
}
