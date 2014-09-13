//: ability/HighScorer.java

package pokepon.ability;

import pokepon.pony.*;
import pokepon.battle.*;
import static pokepon.util.MessageManager.*;
import java.util.Map;
import static java.util.AbstractMap.SimpleEntry;

/** HighScorer
 * Button Mash's signature ability: copies the opponent's highest base stat, except HP
 *
 * @author Giacomo Parolini
 */

public class HighScorer extends Ability {
	
	private Map.Entry<String,Integer> origStat;

	public HighScorer() {
		super("High Scorer");
		briefDesc = "Copies the opponent's highest base stat (except HP).";
	}
	
	@Override
	public void afterSwitchIn(final BattleEngine be) {
		int oppSide = be.getOppositeSide(pony);
		SimpleEntry<String,Integer> maxStat = new SimpleEntry<>("",0);
		for(String stat : Pony.statNames()) {
			if(stat.equalsIgnoreCase("hp")) continue;
			if(be.getTeam(oppSide).getActivePony().getStat(stat) > maxStat.getValue()) {
				maxStat = new SimpleEntry<>(stat, be.getTeam(oppSide).getActivePony().getStat(stat));
			}
		}
		origStat = new SimpleEntry<>(maxStat.getKey(), pony.getBaseStat(maxStat.getKey()));
		pony.setBaseStat(maxStat.getKey(), maxStat.getValue());
		if(be.getBattleTask() != null) 
			be.getBattleTask().sendB("|battle|"+pony.getNickname()+"'s "+name+" copied the opponent's "+maxStat.getKey()+"!");
	}

	@Override
	public void onSwitchOut(final BattleEngine be) {
		pony.setBaseStat(origStat.getKey(), origStat.getValue());
	}
}
