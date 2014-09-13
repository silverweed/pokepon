//: move/Dissonance.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * The basic crappy move (Music-typed, special);
 * No additional effects.
 *
 * @author 
 */
public class Dissonance extends Move {
	
	public Dissonance() {
		super("Dissonance");
		
		type = Type.MUSIC;
		moveType = Move.MoveType.SPECIAL;
		maxpp = pp = 35;
		baseDamage = 50;
		accuracy = 100;
		priority = 0;
		description = "The enemy is hurt by dissonant sounds.";
		briefDesc = "Inflicts regular damage.";
	}

	public Dissonance(Pony p) {
		this();
		pony = p;
	}
}
