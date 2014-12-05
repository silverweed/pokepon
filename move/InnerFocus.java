//: move/InnerFocus.java

package pokepon.move;

/**
 * Status move which increases Atk by 2.
 *
 * @author silverweed
 */

import pokepon.enums.*;
import pokepon.pony.*;
import pokepon.player.*;
import pokepon.battle.*;

public class InnerFocus extends Move {
	
	public InnerFocus() {
		super("Inner Focus");
		
		type = Type.SPIRIT;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 20;
		accuracy = -1;
		priority = 0;
		description = "Focus on your inner self to highly raise your attack.";
		briefDesc = "Raises Atk by 2.";

		animation.put("name","Shake");
		animation.put("sprite","user");
		
		userAtk = addEntry(2,1f);
		
	}

	public InnerFocus(Pony p) {
		this();
		pony = p;
	}

	/** Move will fail if user has already maxed Def and SpD. */
	@Override
	public boolean validConditions(final BattleEngine be) {
		if(pony == null) throw new NullPointerException("pony is null for move "+this);			
		return pony.atkMod() < 6;
	}
}
