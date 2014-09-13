//: move/Startle.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;
import pokepon.battle.BattleEngine;

/**
 * Like Hex: damage doubles if enemy has a status condition
 *
 * @author silverweed
 */

public class Startle extends Move {
	
	public Startle() {
		super("Startle");
		
		type = Type.LAUGHTER;
		moveType = Move.MoveType.PHYSICAL;
		maxpp = pp = 25;
		baseDamage = 60;
		accuracy = 90;
		priority = 0;
		description = "Startle your enemy so much it may flinch. Power doubles if enemy is affected by a negative condition.";
		briefDesc = "10% to flinch enemy. Power doubles if target has status.";

		animation.put("name","Shake");
		animation.put("sprite","target");
		animation.put("delay", 10);

		targetFlinch = 0.1f;
	}
	
	public Startle(Pony p) {
		this();
		pony = p;
	}

	@Override
	public boolean validConditions(final BattleEngine be) {
		if(pony == null)
			throw new NullPointerException("Pony is null for Startle!");

		Pony opp = be.getTeam(be.getOppositeSide(pony)).getActivePony();
		if(opp.hasNegativeCondition())
			baseDamage = 120;
		else
			baseDamage = 60;
		return true;
	}
}
