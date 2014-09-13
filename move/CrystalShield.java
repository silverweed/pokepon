//: move/CrystalShield.java

package pokepon.move;
import pokepon.gui.animation.*;

/**
 * Status move which increases SpD by 2 and may heal status.
 *
 * @author silverweed
 */

import pokepon.enums.*;
import pokepon.pony.*;
import pokepon.player.*;
import pokepon.battle.*;

public class CrystalShield extends Move {
	
	public CrystalShield() {
		super("Crystal Shield");
		
		type = Type.LOVE;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 15;
		accuracy = -1;
		priority = 0;
		description = "A crystal shield increases your Defense and may cure a negative condition.";
		briefDesc = "Raises Def by 2. 30% to heal status.";
		
		userDef = addEntry(2,1f);
		healUserStatus = 0.3f;

		animation.put("name","Shake");
		animation.put("sprite","user");
		
	}
	
	public CrystalShield(Pony p) {
		this();
		pony = p;
	}

	/** Move will fail if user has already maxed Def and SpD. */
	@Override
	public boolean validConditions(final BattleEngine be) {
		if(pony == null) throw new NullPointerException("pony is null for move "+this);
		return pony.defMod() < 6;
	}
}
