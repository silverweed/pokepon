//: move/FreezeSpell.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.battle.*;
import pokepon.pony.Pony;

/**
 * Status move which paralyzes opponents
 *
 * @author silverweed
 */

public class FreezeSpell extends Move {
	
	public FreezeSpell() {
		super("Freeze Spell");
		
		type = Type.MAGIC;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 20;
		accuracy = 100;
		priority = 0;
		description = "Cast a powerful spell to paralyze your enemy.";
		briefDesc = "Paralyzes target.";

		animation.put("name","Direct");
		animation.put("sprite","electroball.png");
		animation.put("bounceBack",false);

		targetParalysis = 1f;
	}

	public FreezeSpell(Pony p) {
		this();
		pony = p;
	}

	@Override
	public boolean validConditions(final BattleEngine be) {
		return !be.getDefender().hasNegativeCondition();
	}
}
