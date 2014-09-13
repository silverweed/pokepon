//: ability/Charity.java

package pokepon.ability;

import pokepon.pony.*;
import pokepon.battle.*;
import pokepon.move.*;
import pokepon.enums.Type;
import pokepon.util.Debug;
import static pokepon.util.MessageManager.*;

/** Charity
 * Boosts Generosity moves when HP are low
 *
 * @author silverweed
 */

public class Charity extends Ability {

	private float boost = 1f;

	public Charity() {
		super("Charity");
		briefDesc = "Boosts Generosity moves' damage by 50% when HP is less or equal to 25%.";
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
		if(moveType == Type.GENEROSITY) return boost;
		else return 1f;
	}
}
