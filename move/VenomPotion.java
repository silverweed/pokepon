//: move/VenomPotion.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.battle.*;
import pokepon.pony.Pony;

/**
 * Status move which may put the enemy to sleep.
 *
 * @author silverweed
 */

public class VenomPotion extends Move {
	
	public VenomPotion() {
		super("Venom Potion");
		
		type = Type.SPIRIT;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 10;
		accuracy = 90;
		priority = 0;
		description = "Brew a poisonous potion against your enemy.";
		briefDesc = "Badly poisons target.";

		animation.put("name","Ballistic");
		animation.put("sprite","poisonwisp.png");
		animation.put("bounceBack",false);

		targetToxic = 1f;
	}

	public VenomPotion(Pony p) {
		this();
		pony = p;
	}

	@Override
	public boolean validConditions(final BattleEngine be) {
		return !be.getDefender().hasNegativeCondition();
	}
}
