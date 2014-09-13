//: move/LoveBurst.java

package pokepon.move;

/**
 * Special move which inflicts huge damage ignoring resistances, but
 * takes 1 turn to charge up.
 *
 * @author silverweed
 */

import pokepon.enums.*;
import pokepon.pony.*;
import pokepon.player.*;
import pokepon.battle.*;
import static pokepon.util.MessageManager.*;

public class LoveBurst extends Move {
	
	public LoveBurst() {
		super("Love Burst");
		
		type = Type.LOVE;
		moveType = Move.MoveType.SPECIAL;
		baseDamage = 140;
		maxpp = pp = 5;
		accuracy = 100;
		priority = 0;
		description = "Use the power of love to bust away villains. Takes 1 turn to charge up, but ignores enemy resistances.";
		briefDesc = "Ignores target resistance.<br>1 turn to charge up.";
	
		animation.put("name","Direct");
		animation.put("sprite","loveball.png");
		animation.put("bounceBack",false);

		ignoreResistances = true;
		blockingDelayed = true;
		lockingTurns = 1;
	}

	public LoveBurst(Pony p) {
		this();
		pony = p;
	}

	@Override
	public String getPhrase() {
		return "[pony] is shining!";
	}
}
