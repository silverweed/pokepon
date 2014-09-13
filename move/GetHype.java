//: move/GetHype.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;
import pokepon.battle.*;
import pokepon.gui.animation.*;
/**
 * Status move which raises user's Spatk and Atk
 *
 * @author Giacomo Parolini
 */

public class GetHype extends Move {
	
	public GetHype() {
		super("Get Hype");
		
		id = 1;
		type = Type.LAUGHTER;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 15;
		accuracy = -1;
		priority = 0;
		description = "";
		briefDesc = "Raises Atk and SpA by 1.";

		animation.put("name","Shake");
		animation.put("sprite","user");
		animation.put("shakes", 6);
		animation.put("delay", 10);

		userAtk = addEntry(1,1f);
		userSpatk = addEntry(1,1f);
	}
	
	public GetHype(Pony p) {
		this();
		pony = p;
	}

	@Override
	public boolean validConditions(final BattleEngine be) {
		if(pony == null) throw new NullPointerException("pony is null for move "+this);
		return pony.atkMod() < 6 || pony.spatkMod() < 6;
	}
}
