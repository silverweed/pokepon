//: move/TeleportBlast.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.battle.*;
import pokepon.pony.Pony;

/**
 * Allows switching after hit, like Volt Switch.
 *
 * @author Giacomo Parolini
 */

public class TeleportBlast extends Move {
	
	public TeleportBlast() {
		super("Teleport Blast");
		
		type = Type.MAGIC;
		moveType = Move.MoveType.SPECIAL;
		maxpp = pp = 15;
		baseDamage = 70;
		accuracy = 100;
		priority = 0;
		description = "Teleport away after hitting your enemy with a magic blast.";
		briefDesc = "Switch out after hit.";

		animation.put("name","Direct");
		animation.put("sprite","shadowball.png");
		animation.put("bounceBack",false);

		// forces switch to chosen ally on damage
		forceUserSwitch = 1;	
		isBeamMove = true;
	}

	public TeleportBlast(Pony p) {
		this();
		pony = p;
	}
}
