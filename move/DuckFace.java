//: move/DuckFace.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;
import pokepon.battle.*;

/**
 * Lowers enemy Def by 2.
 *
 * @author silverweed
 */

public class DuckFace extends Move {
	
	public DuckFace() {
		super("Duck Face");
		
		type = Type.GENEROSITY;
		moveType = Move.MoveType.STATUS;
		baseDamage = 0;
		maxpp = pp = 35;
		accuracy = 100;
		priority = 0;
		description = "Lowers target Def by 2.";
		briefDesc = "Lowers target Def by 2.";

		animation.put("name","Shake");
		animation.put("sprite","user");

		targetDef = addEntry(-2,1f);
	}

	public DuckFace(Pony p) {
		this();
		pony = p;
	}

	@Override
	public boolean validConditions(final BattleEngine be) {
		return be.getDefender().defMod() > -6; 
	}
}
