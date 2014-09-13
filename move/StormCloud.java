//: move/StormCloud.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Special move which inflicts good damage and has 100% precision;
 * Can decrease enemy's SpD.
 *
 * @author silverweed
 */

public class StormCloud extends Move {
	
	public StormCloud() {
		super("Storm Cloud");
		
		type = Type.LOYALTY;
		moveType = Move.MoveType.SPECIAL;
		maxpp = pp = 25;
		baseDamage = 80;
		accuracy = 100;
		priority = 0;
		description = "Summon a stormy cloud to envelop your enemy and inflict damage. Can lower Special Defense.";
		briefDesc = "10% to lower enemy SpD by 1.";

		animation.put("name","Fade");
		animation.put("sprite","wisp.png");
		animation.put("transparent",true);
		animation.put("fadeOut",false);
		animation.put("initialPoint","opp -50Y");
		animation.put("finalPoint","opp +20Y");

		targetSpdef = addEntry(-1,0.1f);
	}
	
	public StormCloud(Pony p) {
		this();
		pony = p;
	}
}
