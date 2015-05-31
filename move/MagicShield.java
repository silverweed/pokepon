//: move/MagicShield.java

package pokepon.move;

/**
 * Status move which increases Def and SpD by 2 but decreases speed by 1.
 *
 * @author silverweed
 */

import pokepon.enums.*;
import pokepon.pony.*;
import pokepon.player.*;
import pokepon.battle.*;

public class MagicShield extends Move {
	
	public MagicShield() {
		super("Magic Shield");
		
		type = Type.MAGIC;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 20;
		accuracy = -1;
		priority = 0;
		description = "A magic shield increases your defenses.";
		briefDesc = "Raises Def and SpD by 1,<br>but lowers Spe by 1.";
	
		userDef = addEntry(1,1f);
		userSpdef = addEntry(1,1f);
		userSpeed = addEntry(-1,1f);
		
	}

	public MagicShield(Pony p) {
		this();
		pony = p;
	}

	/** Move will fail if user has already maxed Def and SpD. */
	@Override
	public boolean validConditions(final BattleEngine be) {
		if(pony == null) throw new NullPointerException("pony is null for move "+this);			
		return pony.defMod() < 6 || pony.spdefMod() < 6;
	}
}
