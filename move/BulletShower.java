//: move/BulletShower.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;
import pokepon.gui.animation.AnimPresets;
import static java.util.Arrays.asList;

/**
 * Littlepip's signature move;
 * Good special damage with multiple hits;
 * higher chance for critical hits.
 *
 * @author silverweed
 */

public class BulletShower extends Move {
	
	public BulletShower() {
		super("Bullet Shower");
	
		type = Type.SHADOW;
		moveType = Move.MoveType.SPECIAL;
		maxpp = pp = 10;
		baseDamage = 20;
		accuracy = 95;
		priority = 0;
		description = "Shot several bullets to your enemy. Hits 2-5 times with a higher chance for critical hits.";
		briefDesc = "Hits 2 to 5 times in a row.<br>High critical ratio.";
		
		animation = new java.util.HashMap<>(AnimPresets.get("gatling"));
		animation.put("sprite","fireball.png");

		hits = 5;
		/** Probability of doing 1,2,3,4 or 5 hits */
		hitsChance = asList( 0f, 33.3f, 33.3f, 16.7f, 16.7f ); 
		
		critical = 2;
	}

	public BulletShower(Pony p) {
		this();
		pony = p;
	}
}
