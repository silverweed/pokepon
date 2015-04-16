//: ability/Tenacity.java

package pokepon.ability;

import pokepon.pony.*;
import pokepon.battle.*;
import pokepon.move.*;
import pokepon.enums.Type;
import pokepon.util.Debug;
import static pokepon.util.MessageManager.*;

/** Tenacity
 * Like Guts: if this pony has a negative condition, atk is 1.5x.
 *
 * @author silverweed
 */

public class Tenacity extends Ability {

	private int origAtk = -1;

	public Tenacity() {
		super("Tenacity");
		briefDesc = "Boosts Atk by 50% when user<br>has a negative condition.";
	}
	
	@Override
	public void onMoveUsage(final BattleEngine be) {
		if(pony == null)
			throw new NullPointerException("pony is null for Tenacity!");
		if(pony != be.getAttacker()) return;
		if(origAtk == -1)
			origAtk = pony.getBaseStat("atk");
		if(pony.hasNegativeCondition()) 
			pony.setBaseStat(Pony.Stat.ATK, (int)(origAtk*1.4f));
		else 
			pony.setBaseStat(Pony.Stat.ATK, origAtk);
		if(Debug.pedantic)
			printDebug("[Tenacity] pony's atk is "+pony.getBaseStat(Pony.Stat.ATK)+" (orig: "+origAtk+")");
	}
}
