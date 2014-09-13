//: move/SchemeUp.java

package pokepon.move;

import pokepon.gui.animation.*;
/**
 * Status move which increases Spatk by 2.
 *
 * @author silverweed
 */

import pokepon.enums.*;
import pokepon.pony.*;
import pokepon.player.*;
import pokepon.battle.*;

public class SchemeUp extends Move {
	
	public SchemeUp() {
		super("Scheme Up");
		
		type = Type.MAGIC;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 20;
		accuracy = -1;
		priority = 0;
		description = "Brainstorm about your next move in order to sharply raise your Special Attack.";
		briefDesc = "Raises SpA by 2.";

		animation.put("name","Shake");
		animation.put("sprite","user");
		
		userSpatk = addEntry(2,1f);
		
	}

	public SchemeUp(Pony p) {
		this();
		pony = p;
	}

	/** Move will fail if user has already maxed Def and SpD. */
	@Override
	public boolean validConditions(final BattleEngine be) {
		if(pony == null) throw new NullPointerException("pony is null for move "+this);			
		return pony.spatkMod() < 6;
	}
}
