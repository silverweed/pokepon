//: move/EerieSonata.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Inflicts good physical damage.
 *
 * @author 
 */

public class EerieSonata extends Move {

	public EerieSonata() {
		super("Eerie Sonata");
		type = Type.MUSIC;
		moveType = Move.MoveType.SPECIAL;
		maxpp = pp = 16;
		baseDamage = 90;
		accuracy = 100;
		priority = 0;
		description = "";
		briefDesc = "10% to petrify the target.";

		animation.put("name","Direct");
		animation.put("sprite","note.png");
		
		targetPetrify = 0.1f;
	}

	public EerieSonata(Pony p) {
		this();
		pony = p;
	}
}
