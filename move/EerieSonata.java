//: move/EerieSonata.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;
import pokepon.gui.animation.AnimPresets;

/**
 * Inflicts special damage and may petrify.
 *
 * @author , silverweed
 */

public class EerieSonata extends Move {

	public EerieSonata() {
		super("Eerie Sonata");
		type = Type.MUSIC;
		moveType = Move.MoveType.SPECIAL;
		maxpp = pp = 20;
		baseDamage = 70;
		accuracy = 100;
		priority = 0;
		description = "A disturbing tone creeps out the opponent. Has a chance to petrify.";
		briefDesc = "10% to petrify the target.";

		animation = new java.util.HashMap<>(AnimPresets.get("direct-whirl"));
		animation.put("1:sprite","note.png");
		animation.put("2:sprite", "note.png");
		
		targetPetrify = 0.1f;
	}

	public EerieSonata(Pony p) {
		this();
		pony = p;
	}
}
