//: move/GemStorm.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;
import static java.util.Arrays.asList;

/**
 * Rarity's signature move;
 * Good special damage with multiple hits;
 * Can make the enemy flinch.
 *
 * @author Giacomo Parolini
 */

public class GemStorm extends Move {
	
	public GemStorm() {
		super("Gem Storm");
	
		type = Type.GENEROSITY;
		moveType = Move.MoveType.SPECIAL;
		maxpp = pp = 5;
		baseDamage = 25;
		accuracy = 100;
		priority = 0;
		description = "Hit your enemy with a shower of pointy gems. Hits 2-5 times.";
		briefDesc = "Hits 2 to 5 times in a row.";
		
		animation.put("name","Direct");
		animation.put("sprite","diamond.png");
		animation.put("bounceBack",false);

		hits = 5;
		/** Probability of doing 1,2,3,4 or 5 hits */
		hitsChance = asList( 0f, 33.3f, 33.3f, 16.7f, 16.7f ); 
	}

	public GemStorm(Pony p) {
		this();
		pony = p;
	}
}
