//: move/MysteriousBrew.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;
import pokepon.battle.*;
/**
 * Status move which raises user's Atk and spD
 *
 * @author Giacomo Parolini
 */

public class MysteriousBrew extends Move {
	
	public MysteriousBrew() {
		super("Mysterious Brew");
		
		id = 1;
		type = Type.SPIRIT;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 15;
		accuracy = -1;
		priority = 0;
		description = "A mystic brew increases your Attack and Special Defense";
		briefDesc = "Raises Atk and SpD by 1.";

		animation.put("name","Shake");
		animation.put("sprite","user");

		userAtk = addEntry(1,1f);
		userSpdef = addEntry(1,1f);
	}
	
	public MysteriousBrew(Pony p) {
		this();
		pony = p;
	}

	@Override
	public boolean validConditions(final BattleEngine be) {
		if(pony == null) throw new NullPointerException("pony is null for move "+this);
		return pony.atkMod() < 6 || pony.spdefMod() < 6;
	}
}
