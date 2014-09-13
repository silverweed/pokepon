//: move/MirrorPond.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;
import pokepon.battle.*;
import pokepon.gui.animation.*;

/** 
 * Like in Pok&#233mon.
 *
 * @author silverweed
 */

public class MirrorPond extends Move {
	
	public MirrorPond() {
		super("Mirror Pond");

		type = Type.LAUGHTER;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 16;
		accuracy = -1;
		priority = 0;
		description = "";
		briefDesc = "Spend 1/4 HP to create a<br>dummy target for the enemy.";

		spawnSubstitute = true;
		//damageUserPerc = 25f;
	}

	public MirrorPond(Pony p) {
		this();
		pony = p;
	}

	@Override
	public boolean validConditions(final BattleEngine be) {
		return !be.getAttacker().hasSubstitute();
	}

	@Override
	public String getPhrase() {
		return "[pony] duplicated itself!";
	}
}
