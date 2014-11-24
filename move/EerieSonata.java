//: move/EerieSonata.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Inflicts special damage and may petrify.
 *
 * @author Tommaso Parolini
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

		animation.put("name", "Compound");
		animation.put("anims", java.util.Arrays.asList("Direct","Whirl"));
		animation.put("1:sprite","note.png");
		animation.put("1:initialPoint", "ally");
		animation.put("1:finalPoint", "opp");
		animation.put("1:bounceBack", false);
		animation.put("1:persistent", false);
		animation.put("2:sprite", "note.png");
		animation.put("2:center","opp +50X +50Y");
		animation.put("2:radius",50);
		
		targetPetrify = 0.1f;
	}

	public EerieSonata(Pony p) {
		this();
		pony = p;
	}
}
