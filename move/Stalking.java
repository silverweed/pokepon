//: move/Stalking.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Like Pursuit; mechanics of this move are hard-coded in BattleTask, since its effects are unique
 *
 * @author Giacomo Parolini
 */

public class Stalking extends Move {

	public Stalking() {
		super("Stalking"); 
		type = Type.NIGHT;
		moveType = Move.MoveType.PHYSICAL;
		maxpp = pp = 32;
		baseDamage = 40;
		accuracy = 100;
		priority = 0;
		description = "";
		briefDesc = "Inflics doubled damage if used on opponent's switch-out.";

		animation.put("name","Ballistic2");
		animation.put("sprite","user");
	}

	public Stalking(Pony p) {
		this();
		pony = p;
	}
}
