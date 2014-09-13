//: move/SurpriseHit.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.battle.*;
import pokepon.pony.Pony;
import pokepon.util.Debug;
import static pokepon.util.MessageManager.*;

/**
 * Like Sucker Punch.
 *
 * @author Giacomo Parolini
 */

public class SurpriseHit extends Move {
	
	public SurpriseHit() {
		super("Surprise Hit");
		
		type = Type.NIGHT;
		moveType = Move.MoveType.PHYSICAL;
		maxpp = pp = 8;
		baseDamage = 80;
		accuracy = 100;
		priority = 1;
		description = "Usually hits first, but fails if opponent used a non-damaging move.";
		briefDesc = "Priority +1. Fails if enemy used status move.";

		animation.put("name","Direct");
		animation.put("sprite","user");
		animation.put("passThrough",true);
	}

	public SurpriseHit(Pony p) {
		this();
		pony = p;
	}

	@Override
	public boolean validConditions(final BattleEngine be) {
		if(pony == null)
			throw new NullPointerException("pony is null for SurpriseHit!");
		int oppSide = be.getOppositeSide(pony);
		return !(be.getChosenMove(oppSide) == null || be.getChosenMove(oppSide).getMoveType() == Move.MoveType.STATUS);
	}
}
