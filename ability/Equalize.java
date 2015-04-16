//: ability/Equalize.java

package pokepon.ability;

import pokepon.pony.*;
import pokepon.battle.*;
import pokepon.util.*;
import static pokepon.util.MessageManager.*;
import java.util.Map;
import static java.util.AbstractMap.SimpleEntry;

/** Equalize
 * Starlight Glimmer's signature ability: on switch in, sets the
 * enemy's max stat to a value equal to its min stat (except HP)
 * Can only activate once.
 *
 * @author silverweed
 */

public class Equalize extends Ability {
	
	private boolean activated;

	public Equalize() {
		super("Equalize");
		briefDesc = "Sets opponent's max stat (not hp)<br>equal to its min stat on switch-in.";
	}
	
	@Override
	public void afterSwitchIn(final BattleEngine be) {
		if(activated) return;
		int oppSide = be.getOppositeSide(pony);
		Pony oppPony = be.getTeam(oppSide).getActivePony();
		SimpleEntry<Pony.Stat,Integer> maxStat = new SimpleEntry<>(null, 0), minStat = new SimpleEntry<>(null, 1000);
		for(Pony.Stat stat : Pony.Stat.core()) {
			if(stat == Pony.Stat.HP) continue;
			int val = oppPony.getBaseStat(stat);
			if(val > maxStat.getValue())
				maxStat = new SimpleEntry<>(stat, val);
			if(val < minStat.getValue())
				minStat = new SimpleEntry<>(stat, val);
		}
		oppPony.setBaseStat(maxStat.getKey(), minStat.getValue());
		if(Debug.on)
			printDebug("[Equalize] opponent's "+maxStat.getKey()+" set to "+oppPony.getBaseStat(maxStat.getKey()));
		if(be.getBattleTask() != null)
			be.getBattleTask().sendB("|battle|"+pony.getNickname()+"'s Equalize minimized "+
					oppPony.getNickname()+"'s "+maxStat.getKey()+"!");
		activated = true;
	}
}
