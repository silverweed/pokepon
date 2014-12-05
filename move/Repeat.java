//: move/Repeat.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;
import pokepon.battle.*;

/** 
 * Like in Pok&#233mon.
 *
 * @author Giacomo Parolini
 */

public class Repeat extends Move {
	
	public Repeat() {
		super("Repeat");

		type = Type.CHAOS;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 25;
		accuracy = -1;
		priority = 0;
		description = "";
		briefDesc = "Target repeats the last move for 3 turns.";

		animation.put("name","Shake");
		animation.put("sprite","user");

		locksTargetOn = "last";
		lockingTurns = 3;
	}

	public Repeat(Pony p) {
		this();
		pony = p;
	}

	@Override
	public boolean validConditions(final BattleEngine be) {
		return !be.getDefender().isLockedOnMove();
	}

	@Override
	public String getPhrase() {
		return "[pony]'s Repeat ended!";
	}
}
