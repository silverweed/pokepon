//: move/Dissonance.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;
import pokepon.gui.animation.AnimPresets;

/**
 * The basic crappy move (Music-typed, special);
 * No additional effects.
 *
 * @author , silverweed
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

		animation = new java.util.HashMap<>(AnimPresets.get("rise-from-below"));
		animation.put("sprite", "note.png");
	}

	public Dissonance(Pony p) {
		this();
		pony = p;
	}
}
