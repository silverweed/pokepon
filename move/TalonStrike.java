//: move/TalonStrike.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;
import static java.util.Arrays.asList;

/**
 * Physical move with 1 to 5 hits.
 *
 * @author silverweed
 */

public class TalonStrike extends Move {
	
	public TalonStrike() {
		super("Talon Strike");
	
		type = Type.SHADOW;
		moveType = Move.MoveType.PHYSICAL;
		maxpp = pp = 10;
		baseDamage = 25;
		accuracy = 90;
		priority = 0;
		description = "Strike with sharp talons. Hits 2-5 times.";
		briefDesc = "Hits 2 to 5 times in a row.";
		
		animation.put("name","Shake");
		animation.put("sprite","user");
		animation.put("shakes", 2);
		animation.put("delay", 10);

		hits = 5;
		/** Probability of doing 1,2,3,4 or 5 hits */
		hitsChance = asList( 0f, 33.3f, 33.3f, 16.7f, 16.7f ); 
	}

	public TalonStrike(Pony p) {
		this();
		pony = p;
	}
}
