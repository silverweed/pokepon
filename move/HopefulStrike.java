//: move/HopefulStrike.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Inflicts good physical damage and may lower enemy atk by 1.
 *
 * @author Giacomo Parolini
 */

public class HopefulStrike extends Move {

	public HopefulStrike() {
		super("Hopeful Strike");
		type = Type.LOVE;
		moveType = Move.MoveType.PHYSICAL;
		contactMove = true;
		maxpp = pp = 15;
		baseDamage = 75;
		accuracy = 100;
		priority = 0;
		description = "Fight till the end against desperate situations. May lower enemy's attack.";
		briefDesc = "10% to lower target Atk.";

		animation.put("name","Ballistic2");
		animation.put("sprite","user");
		
		targetAtk = addEntry(-1,0.1f);
	}

	public HopefulStrike(Pony p) {
		this();
		pony = p;
	}
}
