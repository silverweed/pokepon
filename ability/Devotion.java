//: ability/Devotion.java

package pokepon.ability;

import pokepon.pony.*;
import pokepon.battle.*;
import pokepon.move.*;
import pokepon.enums.Type;
import pokepon.util.Debug;
import static pokepon.util.MessageManager.*;

/** Devotion
 * Boosts Loyalty moves when HP are low
 *
 * @author Giacomo Parolini
 */

public class Devotion extends Ability {

	private float boost = 1f;

	public Devotion() {
		super("Devotion");
		briefDesc = "Boosts Loyalty moves' damage by 50% when HP is less or equal to 33%.";
	}
	
	@Override
	public void beforeMoveHit(final BattleEngine be) {
		if(pony != be.getAttacker()) return;
		if(pony.hp() <= pony.maxhp() / 3) 
			boost = 1.5f;
		else 
			boost = 1f;
	}

	@Override
	public float changeDamageDealtBy(Type moveType) {
		if(moveType == Type.LOYALTY) return boost;
		else return 1f;
	}
}
