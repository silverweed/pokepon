//: ability/Subjection.java

package pokepon.ability;

import pokepon.pony.*;
import pokepon.battle.*;
import pokepon.move.*;
import pokepon.net.jack.*;
import static pokepon.util.MessageManager.*;

/** Subjection
 * Like Intimidate.
 *
 * @author Giacomo Parolini
 */

public class Subjection extends Ability {

	private boolean activated;

	public Subjection() {
		super("Subjection");
		briefDesc = "Decreases enemy's atk on switch-in.";
	}

	@Override
	public void afterSwitchIn(final BattleEngine be) {
		if(pony == null)
			throw new NullPointerException("Pony is null for ability Subjection!");
		
		if(activated) return;

		// we must find out if we're currently on 'ally' or 'opponent' side
		int oppSide = be.getOppositeSide(pony);
		Pony opp = be.getTeam(oppSide).getActivePony();
		Connection allyC = be.getConnection(be.getSide(pony));
		Connection oppC = be.getConnection(oppSide);
		if(opp != null) {
			if(opp.hasSubstitute()) return;
			for(EffectDealer ed : opp.getEffectDealers())
				if(ed.ignoreStatusDrop() || ed.ignoreStatusChange()) return;

			opp.boostAtk(-1);
			printMsg(pony.getNickname()+"'s Subjection lowered "+opp.getNickname()+"'s Atk!");
			if(be.getBattleTask() != null) {
				be.getBattleTask().sendB(allyC,"|boost|opp|atk|-1|"+
					pony.getNickname()+"'s Subjection lowered "+
					opp.getNickname()+"'s Atk!");
				be.getBattleTask().sendB(oppC,"|boost|ally|atk|-1|"+
					pony.getNickname()+"'s Subjection lowered "+
					opp.getNickname()+"'s Atk!");
			}
			activated = true;
		} else throw new NullPointerException("Pony with Subjection is neither ally nor opponent!");
	}

	@Override
	public void onSwitchOut(final BattleEngine be) {
		activated = false;
	}
}
