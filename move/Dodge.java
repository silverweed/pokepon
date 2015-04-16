//: move/Dodge.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.battle.*;
import pokepon.pony.Pony;
import pokepon.util.Debug;
import static pokepon.util.MessageManager.*;

/**
 * Like Protect.
 *
 * @author silverweed
 */

public class Dodge extends Move {
	
	public Dodge() {
		super("Dodge");
		
		type = Type.KINDNESS;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 16;
		accuracy = -1;
		priority = 4;
		description = "Brace for the next enemy's move and be protected by it.";
		briefDesc = "Protects from moves. Priority +4";

		protectUser = true;
	}

	public Dodge(Pony p) {
		this();
		pony = p;
	}

	@Override
	public void reset() {
		super.reset();
		if(pony != null)
			pony.protectCounter = 0;
	}

	@Override
	public boolean validConditions(final BattleEngine be) {
		if(pony == null)
			throw new NullPointerException("pony is null for Dodge!");

		if(Debug.on) printDebug("[Dodge] counter = "+pony.protectCounter);

		if(pony.protectCounter == 0)
			return true;
		else
			return be.getRNG().nextFloat() < 1f/(pony.protectCounter + 1);
	}
}
