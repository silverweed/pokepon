//: move/RelayRace.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.battle.*;
import pokepon.pony.Pony;

/**
 * Like Baton Pass.
 *
 * @author silverweed
 */

public class RelayRace extends Move {
	
	public RelayRace() {
		super("Relay Race");
		
		type = Type.LOYALTY;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 20;
		accuracy = -1;
		priority = 0;
		description = "Switch your place with an ally of you choice, passing it all your stats modifications.";
		briefDesc = "Switch out the Active Pony passing all modifiers and volatiles to the new one.";

		forceUserSwitch = 1;	
		copyVolatiles = true;
	}

	public RelayRace(Pony p) {
		this();
		pony = p;
	}
}
