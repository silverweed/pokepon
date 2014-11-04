//: move/HeavyMassage.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.battle.*;
import pokepon.pony.Pony;

/**
 * Physical move; if enemy has a status condition, it gets healed, but
 * the move's base power doubles.
 *
 * @author Giacomo Parolini
 */

public class HeavyMassage extends Move {
	
	public HeavyMassage() {
		super("Heavy Massage");
		
		type = Type.KINDNESS;
		moveType = Move.MoveType.PHYSICAL;
		maxpp = pp = 25;
		baseDamage = 70;
		accuracy = 100;
		priority = 0;
		description = "";
		briefDesc = "Power doubles if enemy is statused.<br>Enemy heals from status.";

		healTargetStatus = 1f;

		animation.put("name", "Ballistic");
		animation.put("sprite", "user");
	}
	
	public HeavyMassage(Pony p) {
		this();
		pony = p;
	}

	@Override
	public boolean validConditions(final BattleEngine be) {
		if(be.getDefender() != null && be.getDefender().hasNegativeCondition())
			baseDamage = 140;
		else
			baseDamage = 70;
		return true;
	}
}
