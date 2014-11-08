//: move/BindUp.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.battle.*;
import pokepon.pony.Pony;

/**
 * Decreases enemy's speed by 1 and deals physical damage.
 *
 * @author silverweed
 */

public class BindUp extends Move {
	
	public BindUp() {
		super("Bind Up");
		
		type = Type.GENEROSITY;
		moveType = Move.MoveType.PHYSICAL;
		contactMove = true;
		maxpp = pp = 20;
		baseDamage = 50;
		accuracy = 100;
		priority = 0;
		description = "Tie your enemy so tightly it can't move.";
		briefDesc = "Lowers enemy Spe by 1.";

		animation.put("name","Shake");
		animation.put("sprite","target");
		animation.put("style","linear");
		animation.put("shakes",1);

		targetSpeed = addEntry(-1,1f);
			
	}

	public BindUp(Pony p) {
		this();
		pony = p;
	}
}
