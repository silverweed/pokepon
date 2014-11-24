//: move/Overture.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.battle.*;
import pokepon.pony.Pony;

/**
 * A more powerful version of Eerie Sonata.
 *
 * @author Giacomo Parolini
 */

public class Overture extends Move {
	
	public Overture() {
		super("Overture");
		
		type = Type.MUSIC;
		moveType = Move.MoveType.SPECIAL;
		maxpp = pp = 15;
		baseDamage = 90;
		accuracy = 100;
		priority = 0;
		description = "Play a powerful and nostalgic tune that may petrify your opponent.";
		briefDesc = "10% to petrify the target.";

		targetPetrify = 0.1f;
	}

	public Overture(Pony p) {
		this();
		pony = p;
	}
}
