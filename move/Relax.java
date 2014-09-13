//: move/Relax.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;
import pokepon.battle.*;
import pokepon.gui.animation.*;

/**
 * Heal 50% of user's HP.
 *
 * @author Giacomo Parolini
 */

public class Relax extends Move {
	
	public Relax() {
		super("Relax");
		
		type = Type.LIGHT;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 25;
		accuracy = -1;
		priority = 0;
		description = "Relax yourself to regain HP.";
		briefDesc = "Heals 50% HP.";

		animation.put("name","Shake");
		animation.put("sprite","user");

		healUser = 0.5f;
	}

	public Relax(Pony p) {
		this();
		pony = p;
	}

	@Override
	public boolean validConditions(final BattleEngine be) {
		if(pony == null) throw new NullPointerException("pony is null for move "+this);
		return pony.hp() < pony.maxhp();
	}
}
