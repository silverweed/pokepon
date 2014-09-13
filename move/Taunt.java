//: move/Taunt.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;
import pokepon.battle.*;
import pokepon.gui.animation.*;

/** 
 * Like in Pok&#233mon.
 *
 * @author Giacomo Parolini
 */

public class Taunt extends Move {
	
	public Taunt() {
		super("Taunt");

		type = Type.LAUGHTER;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 25;
		accuracy = -1;
		priority = 0;
		description = "Prevent your enemy from using non-damaging moves.";
		briefDesc = "Target cannot use status moves for 2-5 turns.";

		animation.put("name","Shake");
		animation.put("sprite","user");

		tauntTarget = true;
	}

	public Taunt(Pony p) {
		this();
		pony = p;
	}

	@Override
	public boolean validConditions(final BattleEngine be) {
		return !be.getDefender().isTaunted();
	}
}
