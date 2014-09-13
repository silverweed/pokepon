//: move/TalkOut.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/** 
 * Like Clear Smog
 *
 * @author silverweed
 */

public class TalkOut extends Move {
	
	public TalkOut() {
		super("Talk Out");

		type = Type.KINDNESS;
		moveType = Move.MoveType.SPECIAL;
		maxpp = pp = 24;
		baseDamage = 50;
		accuracy = -1;
		priority = 0;
		description = "";
		briefDesc = "Removes all target's stat changes";

		removeTargetPositiveStatModifiers = 1f;
		removeTargetNegativeStatModifiers = 1f;
	}
	
	public TalkOut(Pony p) {
		this();
		pony = p;
	}
}
