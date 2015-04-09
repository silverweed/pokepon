//: move/CutieUnmark.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;
import pokepon.gui.animation.AnimPresets;

/**
 * Removes opponent's ability until its switch-out.
 *
 * @author silverweed
 */
public class CutieUnmark extends Move {
	
	public CutieUnmark() {
		super("Cutie Unmark");
		
		type = Type.MAGIC;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 35;
		accuracy = -1;
		priority = 0;
		description = "Use a forbidden spell to remove your opponent's special talent.";
		briefDesc = "Nullifies enemy's ability.";

		animation = new java.util.HashMap<>(AnimPresets.get("whirl-absorb"));
		animation.put("sprite", "shadowball.png");

		nullifyTargetAbility = 1f;
	}
	
	public CutieUnmark(Pony p) {
		this();
		pony = p;
	}
}
