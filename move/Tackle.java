//: move/Tackle.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * The basic crappy move;
 * No additional effects.
 *
 * @author Giacomo Parolini
 */

public class Tackle extends Move {
	
	public Tackle() {
		super("Tackle");
		
		type = Type.HONESTY;
		moveType = Move.MoveType.PHYSICAL;
		contactMove = true;
		maxpp = pp = 35;
		baseDamage = 50;
		accuracy = 100;
		priority = 0;
		description = "Hit the enemy with a weak strike.";
		briefDesc = "Inflicts regular damage.";

		animation.put("name","Ballistic2");
		animation.put("sprite","user");
	}
	
	public Tackle(Pony p) {
		this();
		pony = p;
	}
}
