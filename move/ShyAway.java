//: move/ShyAway.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.battle.*;
import pokepon.pony.Pony;
import pokepon.gui.animation.*;
/** 
 * Status move which boosts user's evasion
 *
 * @author Tommaso Parolini
 */

public class ShyAway extends Move {
	
	public ShyAway() {
		super("Shy Away");

		type = Type.KINDNESS;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 24;
		accuracy = -1;
		priority = 0;
		description = "Keep a lower profile to make a difficult target.";
		briefDesc = "Raises user Evasion by 1.";

		animation.put("name","Shake");
		animation.put("sprite","user");

		userEvasion = addEntry(1,1f);
	}

	public ShyAway(Pony p) {
		this();
		pony = p;
	}

	@Override
	public boolean validConditions(final BattleEngine be) {
		return pony.evasionMod() < 6;
	}
}
