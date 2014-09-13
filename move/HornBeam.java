//: move/HornBeam.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Special move which inflicts good damage and may confuse.
 *
 * @author silverweed
 */

public class HornBeam extends Move {
	
	public HornBeam() {
		super("Horn Beam");
		
		type = Type.MAGIC;
		moveType = Move.MoveType.SPECIAL;
		maxpp = pp = 20;
		baseDamage = 75;
		accuracy = 90;
		priority = 0;
		description = "A magical ray erupts from the horn. May confuse opponent.";
		briefDesc = "10% to confuse the target.";

		animation.put("name","Direct");
		animation.put("sprite","shadowball.png");
		animation.put("bounceBack",false);

		targetConfusion = 0.1f;
	}

	public HornBeam(Pony p) {
		this();
		pony = p;
	}
}
