//: move/SneakAttack.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Physical priority move of Night type.
 *
 * @author silverweed
 */

public class SneakAttack extends Move {

	public SneakAttack() {
		super("Sneak Attack");
		type = Type.NIGHT;
		moveType = Move.MoveType.PHYSICAL;
		contactMove = true;
		maxpp = pp = 25;
		baseDamage = 40;
		accuracy = 100;
		priority = 1;
		description = "Strike your enemy with a stealth attack. Usually hits first.";
		briefDesc = "Usually hits first.";

		animation.put("name","Direct");
		animation.put("sprite","user");
		animation.put("passThrough",true);
		animation.put("delay",15);
	}

	public SneakAttack(Pony p) {
		this();
		pony = p;
	}
}
