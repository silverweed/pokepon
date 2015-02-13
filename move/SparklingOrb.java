//: move/SparklingOrb.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Special move which inflicts good damage and may lower
 * target's accuracy.
 *
 * @author silverweed
 */

public class SparklingOrb extends Move {
	
	public SparklingOrb() {
		super("Sparkling Orb");
		
		type = Type.GENEROSITY;
		moveType = Move.MoveType.SPECIAL;
		maxpp = pp = 20;
		baseDamage = 80;
		accuracy = 95;
		priority = 0;
		description = "";
		briefDesc = "10% to lower enemy's accuracy";

		animation.put("name","Ballistic");
		animation.put("bounceBack",false);
		animation.put("sprite","loveball.png");

		targetAccuracy = addEntry(-1, 0.1f);
	}
	
	public SparklingOrb(Pony p) {
		this();
		pony = p;
	}
}
