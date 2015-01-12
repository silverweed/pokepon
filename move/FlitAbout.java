//: move/FlitAbout.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.battle.*;
import pokepon.pony.Pony;
/** 
 * Status move which boosts user's evasion
 *
 * @author silverweed
 */

public class FlitAbout extends Move {
	
	public FlitAbout() {
		super("Flit About");

		type = Type.CHAOS;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 24;
		accuracy = -1;
		priority = 0;
		description = "Randomly fly around to disorient your opponent.";
		briefDesc = "Raises user Evasion by 1.";

		animation.put("name","Whirl");
		animation.put("sprite","user");
		animation.put("shakes", 4);
		animation.put("delay", 10);

		userEvasion = addEntry(1,1f);
	}

	public FlitAbout(Pony p) {
		this();
		pony = p;
	}

	@Override
	public boolean validConditions(final BattleEngine be) {
		return pony.evasionMod() < 6;
	}
}
