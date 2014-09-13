//: move/BubbleBurst.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Special move which inflicts average damage and has 100% precision;
 * No additional effects.
 *
 * @author Giacomo Parolini
 */

public class BubbleBurst extends Move {
	
	public BubbleBurst() {
		super("Bubble Burst");
		
		type = Type.LAUGHTER;
		moveType = Move.MoveType.SPECIAL;
		maxpp = pp = 25;
		baseDamage = 80;
		accuracy = 100;
		priority = 0;
		description = "Spray bubbles towards your enemies. Can increase your Special Attack.";
		briefDesc = "20% to increase SpA by 1.";

		animation.put("name","Direct");
		animation.put("bounceBack",false);
		// TODO
		animation.put("sprite","shadowball.png");

		userSpatk = addEntry(1,0.2f);
	}
	
	public BubbleBurst(Pony p) {
		this();
		pony = p;
	}
}
