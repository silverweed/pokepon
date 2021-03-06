//: ability/Escapist.java

package pokepon.ability;

import pokepon.pony.*;
import pokepon.battle.*;
import pokepon.enums.*;
import pokepon.net.jack.*;
import static pokepon.util.MessageManager.*;

/** Escapist
 * Whenever this pony is hit by a supereffective move, it gets +1 Evasion.
 *
 * @author silverweed
 */

public class Escapist extends Ability {

	public Escapist() {
		super("Escapist");
		briefDesc = "Evasion rises by 1 when hit by supereffective moves.";
	}

	@Override
	public void afterMoveHit(final BattleEngine be) {
		if(	be.getDefender() == pony &&
			pony.getWeaknesses().containsKey(be.getCurrentMove().getType()) &&
			pony.evasionMod() < 6
		) {
			pony.boost(Pony.Stat.EVASION, 1);
			if(be.getBattleTask() != null) {
				be.getBattleTask().sendB(be.getAlly(),"|boost|opp|eva|1");
				be.getBattleTask().sendB(be.getOpp(),"|boost|ally|eva|1");
			}
		}
	}
}
