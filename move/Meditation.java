//: move/Meditation.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;
import pokepon.battle.*;
import pokepon.gui.animation.*;
/**
 * Status move which raises user's Spatk and spD
 *
 * @author Giacomo Parolini
 */

public class Meditation extends Move {
	
	public Meditation() {
		super("Meditation");
		
		id = 1;
		type = Type.SPIRIT;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 15;
		accuracy = -1;
		priority = 0;
		description = "Meditate to increase your Special Attack and Special Defense";
		briefDesc = "Raises SpA and SpD by 1.";

		animation.put("name","Shake");
		animation.put("sprite","user");

		userSpatk = addEntry(1,1f);
		userSpdef = addEntry(1,1f);
	}
	
	public Meditation(Pony p) {
		this();
		pony = p;
	}

	@Override
	public boolean validConditions(final BattleEngine be) {
		if(pony == null) throw new NullPointerException("pony is null for move "+this);
		return pony.spatkMod() < 6 || pony.spdefMod() < 6;
	}
}
