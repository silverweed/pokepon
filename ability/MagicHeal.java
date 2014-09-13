//: ability/MagicHeal.java

package pokepon.ability;

import pokepon.pony.*;
import pokepon.battle.*;
import pokepon.move.*;
import pokepon.net.jack.Connection;
import pokepon.util.Debug;
import static pokepon.util.MessageManager.*;

/** MagicHeal
 * Like Natural Cure
 *
 * @author silverweed
 */

public class MagicHeal extends Ability {

	public MagicHeal() {
		super("Magic Heal");
		briefDesc = "Heal negative status on switch-out.";
	}

	@Override
	public void onSwitchOut(final BattleEngine be) {
		Connection allyC = be.getConnection(be.getSide(pony));
		Connection oppC = be.getConnection(be.getOppositeSide(pony));
		pony.healStatus();
		if(be.getBattleTask() != null) {
			be.getBattleTask().sendB(allyC,"|rmstatus|ally||quiet");
			be.getBattleTask().sendB(oppC,"|rmstatus|opp||quiet");
		}
		if(Debug.on) printDebug("[MagicHeal] cured pony's status. "+pony.getStatus());
	}
}
