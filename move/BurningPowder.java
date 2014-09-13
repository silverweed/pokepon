//: move/BurningPowder.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.battle.*;
import pokepon.pony.Pony;

/**
 * Status move which may put the enemy to sleep.
 *
 * @author Giacomo Parolini
 */

public class BurningPowder extends Move {
	
	public BurningPowder() {
		super("Burning Powder");
		
		type = Type.LIGHT;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 24;
		accuracy = 85;
		priority = 0;
		description = "Scatter a strange powder on your enemy to cause it a burn.";
		briefDesc = "Burns the target.";

		animation.put("name","Ballistic");
		animation.put("sprite","fireball.png");
		animation.put("bounceBack",false);

		targetBurn = 1f;
	}

	public BurningPowder(Pony p) {
		this();
		pony = p;
	}

	@Override
	public boolean validConditions(final BattleEngine be) {
		return !be.getDefender().hasNegativeCondition();
	}
}
