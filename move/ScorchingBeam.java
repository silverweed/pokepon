//: move/ScorchingBeam.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Special move which inflicts good damage and has 100% precision;
 * 30% to burn the target.
 *
 * @author silverweed
 */

public class ScorchingBeam extends Move {
	
	public ScorchingBeam() {
		super("Scorching Beam");
		
		type = Type.MAGIC;
		moveType = Move.MoveType.SPECIAL;
		maxpp = pp = 25;
		baseDamage = 80;
		accuracy = 100;
		priority = 0;
		description = "Casts a bright ray that may burn your opponent.";
		briefDesc = "30% to burn the target";

		animation.put("name","Direct");
		animation.put("sprite","fireball.png");
		animation.put("bounceBack",false);

		targetBurn = 0.3f;
	}
	
	public ScorchingBeam(Pony p) {
		this();
		pony = p;
	}
}
