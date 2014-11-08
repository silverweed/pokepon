//: move/Glomp.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Physical move which inflicts good damage and has 100% precision;
 * 30% to paralyze the target.
 *
 * @author 
 */

public class Glomp extends Move {
	
	public Glomp() {
		super("Glomp");
		
		type = Type.LOVE;
		moveType = Move.MoveType.PHYSICAL;
		maxpp = pp = 15;
		baseDamage = 85;
		accuracy = 100;
		priority = 0;
		description = "Squeeze your enemy into a tight hug; this may cause the target's paralysis.";
		briefDesc = "30% to paralyze the target";

		targetParalysis = 0.3f;

		animation.put("name", "Direct");
		animation.put("sprite", "user");
	}
	
	public Glomp(Pony p) {
		this();
		pony = p;
	}
}
