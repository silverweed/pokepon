//: move/RockThrow.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Higher chance for a critical hit and good damage, but not very accurate. 
 *
 * @author Giacomo Parolini
 */
public class RockCrush extends Move {
	
	public RockCrush() {
		super("Rock Crush");
		
		type = Type.HONESTY;
		moveType = Move.MoveType.PHYSICAL;
		contactMove = true;
		maxpp = pp = 15;
		baseDamage = 110;
		accuracy = 75;
		priority = 0;
		description = "Hit your enemy with a powerful attack. Critical hits land more easily.";
		briefDesc = "High critical ratio.";

		animation.put("name","Ballistic");
		animation.put("sprite","user");
		
		critical = 2;
	}

	public RockCrush(Pony p) {
		this();
		pony = p;
	}
}
