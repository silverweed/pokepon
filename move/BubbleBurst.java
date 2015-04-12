//: move/BubbleBurst.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;
import pokepon.gui.animation.AnimPresets;

/**
 * Special move which inflicts average damage and has 100% precision;
 * No additional effects.
 *
 * @author silverweed
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

		animation = new java.util.HashMap<>(AnimPresets.get("gatling"));
		animation.put("sprite","loveball.png");

		userSpatk = addEntry(1,0.2f);
	}
	
	public BubbleBurst(Pony p) {
		this();
		pony = p;
	}
}
