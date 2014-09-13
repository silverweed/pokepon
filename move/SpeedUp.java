//: move/SpeedUp.java

package pokepon.move;
import pokepon.gui.animation.*;

/**
 * Status move which increases Speed by 2.
 *
 * @author Giacomo Parolini
 */

import pokepon.enums.*;
import pokepon.pony.*;
import pokepon.battle.*;
import pokepon.player.*;

public class SpeedUp extends Move {
	
	public SpeedUp() {
		super("Speed Up");
		
		type = Type.LOYALTY;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 25;
		accuracy = -1;
		priority = 0;
		description = "Increase your Speed a lot!";
		briefDesc = "Raises Spe by 2.";

		animation.put("name","Shake");
		animation.put("sprite","user");
		
		userSpeed = addEntry(2,1f);
		
	}

	public SpeedUp(Pony p) {
		this();
		pony = p;
	}

	/** Move will fail if user has already maxed Def and SpD. */
	@Override public boolean validConditions(final BattleEngine be) {
		if(pony == null) throw new NullPointerException("pony is null for move "+this);
		return pony.speedMod() < 6;
	}
}
