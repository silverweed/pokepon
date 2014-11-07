//: ability/Nocturnality.java

package pokepon.ability;

import pokepon.pony.*;
import pokepon.battle.*;
import pokepon.enums.*;
import static pokepon.util.MessageManager.*;

/** Nocturnality
 * This pony gains 1/8 hp per turn while under DARK weather; moreover,
 * this pony cannot be set Asleep by enemy.
 *
 * @author silverweed
 */

public class Nocturnality extends Ability {

	public Nocturnality() {
		super("Nocturnality");
		briefDesc = "This pony cannot be set Asleep. +1/8 HP per turn during DARK weather.";
	}

	@Override
	public float preventNegativeCondition(final String which) {
		if(which.equals("slp")) return 1f;
		return 0f;
	}

	@Override
	public void onTurnEnd(final BattleEngine be) {
		if(pony.isKO()) return;
		if(be.getWeather().get() == Weather.DARK) {
			int healed = pony.increaseHpPerc(12.5f);
			if(be.getBattleTask() != null) {
				be.getBattleTask().sendB(be.getAlly(),"|damage|ally|"+healed+"|"+pony.getNickname()+
					" gains health in DARK weather!");
				be.getBattleTask().sendB(be.getOpp(),"|damage|opp|"+healed+"|Opponent "+pony.getNickname()+
					" gains health in DARK weather!");
			}
		}
	}
}
