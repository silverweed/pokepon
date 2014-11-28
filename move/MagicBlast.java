//: move/MagicBlast.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Special move which inflicts average damage and has 100% precision;
 * No additional effects.
 *
 * @author silverweed
 */

public class MagicBlast extends Move {
	
	public MagicBlast() {
		super("Magic Blast");
		
		type = Type.MAGIC;
		moveType = Move.MoveType.SPECIAL;
		maxpp = pp = 25;
		baseDamage = 60;
		accuracy = 100;
		priority = 0;
		description = "Fire a magical projectile from the horn.";
		briefDesc = "Inflicts regular damage.";

		animation.put("name","Direct");
		animation.put("bounceBack",false);
		animation.put("sprite","shadowball.png");

		beamMove = true;
	}
	
	public MagicBlast(Pony p) {
		this();
		pony = p;
	}
}
