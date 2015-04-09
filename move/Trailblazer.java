//: move/Trailblazer.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;
import pokepon.gui.animation.AnimPresets;

/**
 * Inflicts massive special damage with higher chance for a critical hit
 *
 * @author silverweed
 */

public class Trailblazer extends Move {
	
	public Trailblazer() {
		super("Trailblazer");
		
		type = Type.LOYALTY;
		moveType = Move.MoveType.SPECIAL;
		maxpp = pp = 8;
		baseDamage = 100;
		accuracy = 90;
		priority = 0;
		description = "";
		briefDesc = "Higher chance for critical hit";

		critical = 2;

		animation = new java.util.HashMap<>(AnimPresets.get("cross"));
		animation.put("sprite","user");
	}
	
	public Trailblazer(Pony p) {
		this();
		pony = p;
	}
}
