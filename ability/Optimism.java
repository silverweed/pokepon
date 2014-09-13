//: ability/Optimism.java

package pokepon.ability;

import pokepon.pony.*;
import pokepon.battle.*;
import pokepon.move.*;
import pokepon.enums.Type;
import pokepon.util.Debug;
import static pokepon.util.MessageManager.*;

/** Optimism
 * Boosts Laughter moves when HP are low
 *
 * @author silverweed
 */

public class Optimism extends Ability {

	private float boost = 1f;

	public Optimism() {
		super("Optimism");
		briefDesc = "Boosts Laughter moves' damage by 50% when HP is less or equal to 25%.";
	}
	
	@Override
	public void beforeMoveHit(final BattleEngine be) {
		if(pony != be.getAttacker()) return;
		if(pony.hp() <= pony.maxhp() / 4) 
			boost = 1.5f;
		else 
			boost = 1f;
	}

	@Override
	public float changeDamageDealtBy(Type moveType) {
		if(moveType == Type.LAUGHTER) return boost;
		else return 1f;
	}
}
