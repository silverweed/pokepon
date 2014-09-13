//: move/FurryCoat.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;
import pokepon.battle.*;
import pokepon.gui.animation.*;

/**
 * Fluffle Puff's signature move which raises Def and SpD with priority +1.
 *
 * @author silverweed
 */

public class FurryCoat extends Move {
	
	public FurryCoat() {
		super("Furry Coat");
		
		id = 1;
		type = Type.LAUGHTER;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 15;
		accuracy = -1;
		priority = 1;
		description = "Use your fur to improve your defenses. Usually goes first.";
		briefDesc = "Raises Def and SpD by 1. Priority +1.";

		animation.put("name","Shake");
		animation.put("sprite","user");

		userDef = addEntry(1,1f);
		userSpdef = addEntry(1,1f);
	}

	public FurryCoat(Pony p) {
		this();
		pony = p;
	}
	
	@Override
	public boolean validConditions(final BattleEngine be) {
		if(pony == null) throw new NullPointerException("pony is null for move "+this);
		return pony.defMod() < 6 || pony.spdefMod() < 6;
	}
}
