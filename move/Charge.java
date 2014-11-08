//: move/Charge.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.battle.*;
import pokepon.pony.Pony;

/**
 * Inflicts good physical damage and may make the target flinch.
 *
 * @author silverweed
 */

public class Charge extends Move {
	
	public Charge() {
		super("Charge");
		
		type = Type.HONESTY;
		moveType = Move.MoveType.PHYSICAL;
		contactMove = true;
		maxpp = pp = 15;
		baseDamage = 80;
		accuracy = 95;
		priority = 0;
		description = "Charge your enemy with a speedy rush. May make the target flinch.";
		briefDesc = "20% to flinch the target.";

		animation.put("name","Direct");
		animation.put("sprite","user");
		animation.put("bounceBack",true);
		animation.put("accelerated",true);
		animation.put("delay",20);

		targetFlinch = 0.2f;
			
	}

	public Charge(Pony p) {
		this();
		pony = p;
	}
}
