//: move/DrumBang.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;
import static java.util.Arrays.asList;

/**
 * A physical Music-type move;
 * may land one or two hits.
 *
 * @author 
 */

public class DrumBang extends Move {
	
	public DrumBang() {
		super("Drum Bang");
	
		type = Type.MUSIC;
		moveType = Move.MoveType.PHYSICAL;
		maxpp = pp = 12;
		baseDamage = 60;
		accuracy = 100;
		priority = 0;
		description = "You can bang on the drums. As in, the opponent.";
		briefDesc = "50% to hit twice.";

		animation.put("name", "Ballistic");
		animation.put("sprite", "note.png");
		animation.put("bounceBack", false);
		
		hits = 2;
		/** Probability of doing 1 or 2 hits */
		hitsChance = asList( 0f, 50f ); 
	}

	public DrumBang(Pony p) {
		this();
		pony = p;
	}
}
