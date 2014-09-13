//: move/WreakHavoc.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Special move which inflicts massive damage. 
 *
 * @author silverweed
 */

public class WreakHavoc extends Move {
	
	public WreakHavoc() {
		super("Wreak Havoc");
		
		type = Type.CHAOS;
		moveType = Move.MoveType.SPECIAL;
		maxpp = pp = 15;
		baseDamage = 110;
		accuracy = 85;
		priority = 0;
		description = "Spread chaos all around. Massive damage will be dealt to enemies.";
		briefDesc = "No additional effect.";
	}

	public WreakHavoc(Pony p) {
		this();
		pony = p;
	}
}
