//: ability/Leadership.java

package pokepon.ability;

import pokepon.pony.*;
import pokepon.battle.*;
import pokepon.move.*;
import pokepon.enums.Type;
import pokepon.util.Debug;
import static pokepon.util.MessageManager.*;

/** Leadership
 * Boosts Magic moves when HP are low
 *
 * @author silverweed
 */

public class Leadership extends Ability {

	private float boost = 1f;

	public Leadership() {
		super("Leadership");
		briefDesc = "Boosts Magic moves' damage by 50% when HP is less or equal to 33%.";
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
		if(moveType == Type.MAGIC) return boost;
		else return 1f;
	}
}
