//: move/DimensionTwist.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.battle.*;
import pokepon.pony.Pony;

/**
 * Allows switching after hit, like Volt Switch.
 *
 * @author silverweed
 */

public class DimensionTwist extends Move {
	
	public DimensionTwist() {
		super("Dimension Twist");
		
		type = Type.CHAOS;
		moveType = Move.MoveType.PHYSICAL;
		maxpp = pp = 10;
		baseDamage = 120;
		accuracy = 100;
		priority = 0;
		description = "";
		briefDesc = "Switch out to random ally after hit.";

		animation.put("name","Fade");
		animation.put("sprite","user");
		animation.put("fadeOut",true);
		animation.put("initialPoint","ally");
		animation.put("finalPoint","opp");
		animation.put("bounceBack",false);
		animation.put("persistent",true);
		animation.put("rewind",true);

		// switch to random pony on damage
		forceUserSwitch = 2;	
	}

	public DimensionTwist(Pony p) {
		this();
		pony = p;
	}
}
