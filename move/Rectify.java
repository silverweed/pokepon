//: move/Rectify.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Physical move which inflicts good damage and can drop enemy's atk by 1.
 *
 * @author silverweed
 */

public class Rectify extends Move {
	
	public Rectify() {
		super("Rectify");
		
		type = Type.LIGHT;
		moveType = Move.MoveType.PHYSICAL;
		contactMove = true;
		maxpp = pp = 15;
		baseDamage = 90;
		accuracy = 90;
		priority = 0;
		description = "";
		briefDesc = "10% to reduce target's Atk by 1.";
		
		targetAtk = addEntry(-1, 0.1f);

		animation.put("name", "Ballistic2");
		animation.put("sprite", "user");
	}
	
	public Rectify(Pony p) {
		this();
		pony = p;
	}
}
