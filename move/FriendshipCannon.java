//: move/FriendshipCannon.java

package pokepon.move;

import pokepon.pony.Pony;
import pokepon.enums.*;

/**
 * OP move learnable only by Twalot, Celestia and Luna;
 * Can OHKO opponent.
 *
 * @author Giacomo Parolini
 */
public class FriendshipCannon extends Move {
	
	public FriendshipCannon() {
		super("Friendship Cannon");
		
		type = Type.MAGIC;
		moveType = Move.MoveType.SPECIAL;
		maxpp = pp = 5;
		priority = 0;
		description = "Fire the Friendship Cannon against the villain to turn him to stone! Or...to dust, as you like most. (OHKO move)";
		briefDesc = "Can One-Hit-KO the target.";
	
		OHKO = true;	/* One Hit KO moves are calculated directly in BattleEngine.java. */		
	}

	public FriendshipCannon(Pony p) {
		this();
		pony = p;
	}
}
