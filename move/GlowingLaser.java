//: move/GlowingLaser.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Special move which inflicts good damage and may drop opponent's SpD.
 *
 * @author Giacomo Parolini
 */

public class GlowingLaser extends Move {
	
	public GlowingLaser() {
		super("Glowing Laser");
		
		type = Type.LIGHT;
		moveType = Move.MoveType.SPECIAL;
		maxpp = pp = 20;
		baseDamage = 90;
		accuracy = 95;
		priority = 0;
		description = "A powerful laser erupts from your horn and hits your enemy.";
		briefDesc = "10% to lower target SpD.";

		animation.put("name","Direct");
		animation.put("sprite","electroball.png");
		animation.put("bounceBack",false);

		targetSpdef = addEntry(-1,0.1f);
	}

	public GlowingLaser(Pony p) {
		this();
		pony = p;
	}
}
