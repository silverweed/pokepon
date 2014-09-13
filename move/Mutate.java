//: move/Mutate.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.battle.*;
import pokepon.pony.*;
import pokepon.util.*;
import static pokepon.util.MessageManager.*;


/**
 * Changelings' status move which allows them to transform into the opponent;
 * this transformation doesn't change the user's EVs or IVs.
 *
 * @author silverweed
 */


public class Mutate extends Move {
	
	public Mutate() {
		super("Mutate");
		
		type = Type.SHADOW;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 15;
		accuracy = -1;
		priority = 0;
		description = "Transform into your opponent.";

		transformsUser = true;
	}
	
	public Mutate(Pony p) {
		this();
		pony = p;
	}

	/** Creates a clone of the Defender (except IVs and EVs) and returns it. */
	@Override
	public Pony transformInto(final BattleEngine be) {
		if(Debug.pedantic) printDebug("\nCalled Mutate.transformInto("+be+")");
		Pony cloned = null;
		try {
			cloned = be.getDefender().clone(false,false); // don't clone IVs and EVs.
		} catch(Exception e) {
			printDebug("Caught exception while cloning pony in Mutate.transformInto: "+e);
			e.printStackTrace();
			return null;
		}

		/* Don't copy level and nature */
		cloned.setLevel(be.getAttacker().getLevel());
		cloned.setNature(be.getAttacker().getNature());
		/* keep cloned pony's name as nickname */
		cloned.setNickname(be.getAttacker().getName());

		if(Debug.pedantic) printDebug("Mutate: returning "+cloned);
		return cloned;
	}
}
