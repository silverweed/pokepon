//: move/RockThrow.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Basic move;
 * May reduce target's defence.
 *
 * @author Tommaso Parolini
 */
public class RockThrow extends Move {
	
	public RockThrow() {
		super("Rock Throw");
		
		type = Type.PASSION;
		moveType = Move.MoveType.PHYSICAL;
		maxpp = pp = 25;
		baseDamage = 40;
		accuracy = 100;
		priority = 0;
		description = "A rock is hurled that may also reduce the target's defense.";
		briefDesc = "10% to lower target's defense.";

		animation.put("name","Direct");
		animation.put("sprite","rock.png");
		animation.put("bounceBack",false);
		
		targetDef = addEntry(-1,0.1f);
	}

	public RockThrow(Pony p) {
		this();
		pony = p;
	}
}
