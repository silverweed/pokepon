//: move/Practice.java

package pokepon.move;

import pokepon.gui.animation.*;
/**
 * Status move which increases Atk and Def by 1.
 *
 * @author 
 */

import pokepon.enums.*;
import pokepon.pony.*;
import pokepon.player.*;
import pokepon.battle.*;

public class Practice extends Move {
	
	public Practice() {
		super("Practice");
		
		type = Type.PASSION;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 20;
		accuracy = -1;
		priority = 0;
		description = "Extensive training helps you raise both your Attack and Defense.";
		briefDesc = "Raises Atk and Def by 1.";

		animation.put("name","Shake");
		animation.put("sprite","user");
		
		userAtk = addEntry(1,1f);
		userDef = addEntry(1,1f);
		
	}

	public Practice(Pony p) {
		this();
		pony = p;
	}

	/** Move will fail if user has already maxed Atk and Def. */
	@Override
	public boolean validConditions(final BattleEngine be) {
		if(pony == null) throw new NullPointerException("pony is null for move "+this);			
		return (pony.atkMod() < 6 || pony.defMod() < 6);
	}
}
