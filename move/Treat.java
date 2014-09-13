//: move/Treat.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Special move which inflicts average damage and has 100% precision;
 * 20% to burn the target.
 *
 * @author Tommaso Parolini
 */

public class Treat extends Move {
	
	public Treat() {
		super("Treat");
		
		type = Type.GENEROSITY;
		moveType = Move.MoveType.SPECIAL;
		maxpp = pp = 20;
		baseDamage = 65;
		accuracy = 100;
		priority = 0;
		description = "Offer your target a tasty, possibly overcooked delicacy. May cause burn.";
		briefDesc = "20% to burn the target";

		targetBurn = 0.2f;
	}
	
	public Treat(Pony p) {
		this();
		pony = p;
	}
}
