//: move/IcyCannon.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Special move which inflicts good damage and may paralyze.
 *
 * @author silverweed
 */

public class IcyCannon extends Move {
	
	public IcyCannon() {
		super("Icy Cannon");
		
		type = Type.MAGIC;
		moveType = Move.MoveType.SPECIAL;
		maxpp = pp = 20;
		baseDamage = 75;
		accuracy = 100;
		priority = 0;
		description = "A frozen beam bursts against your opponent.";
		briefDesc = "10% to paralyze the target.";

		animation.put("name","Direct");
		animation.put("sprite","icicle.png");
		animation.put("bounceBack",false);

		targetParalysis = 0.1f;
		beamMove = true;
	}

	public IcyCannon(Pony p) {
		this();
		pony = p;
	}
}
