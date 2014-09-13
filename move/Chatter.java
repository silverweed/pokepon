//: move/Chatter.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;
import pokepon.battle.*;
import pokepon.gui.animation.*;

/** 
 * Status move which confuses enemy.
 *
 * @author Giacomo Parolini
 */

public class Chatter extends Move {
	
	public Chatter() {
		super("Chatter");

		type = Type.LAUGHTER;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 25;
		accuracy = 75;
		priority = 0;
		description = "Talk so quickly that your enemy gets confused.";
		briefDesc = "Confuses target.";

		animation.put("name","Shake");
		animation.put("sprite","user");

		targetConfusion = 1f;
	}

	public Chatter(Pony p) {
		this();
		pony = p;
	}

	@Override
	public boolean validConditions(final BattleEngine be) {
		return !be.getDefender().isConfused();
	}
}
