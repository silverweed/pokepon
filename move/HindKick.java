//: move/HindKick.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Inflicts physical damage and may drop target's defence.
 *
 * @author Giacomo Parolini
 */

public class HindKick extends Move {
	
	public HindKick() {
		super("Hind Kick");
		
		type = Type.SPIRIT;
		moveType = Move.MoveType.PHYSICAL;
		maxpp = pp = 15;
		baseDamage = 80;
		accuracy = 95;
		priority = 0;
		description = "Buck your enemy with your hind legs. May lower enemy's defence.";
		briefDesc = "30% to lower enemy Def by 1.";

		animation.put("name","Direct");
		animation.put("sprite","hoof.png");
		animation.put("bounceBack",false);

		targetDef = addEntry(-1,0.3f);
	}
	
	public HindKick(Pony p) {
		this();
		pony = p;
	}
}
