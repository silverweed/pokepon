//: ability/SheepsEyes.java

package pokepon.ability;

import pokepon.pony.*;
import pokepon.battle.*;
import pokepon.move.*;
import pokepon.net.jack.*;
import static pokepon.util.MessageManager.*;

/** SheepsEyes
 * Lowers Enemy Spdef by 1 on switch-in
 *
 * @author silverweed
 */

public class SheepsEyes extends Ability {

	private boolean activated;

	public SheepsEyes() {
		super("Sheep's Eyes");
		briefDesc = "Lowers enemy's SpD by 1 on switch-in.";
	}

	@Override
	public void afterSwitchIn(final BattleEngine be) {
		if(pony == null)
			throw new NullPointerException("Pony is null for ability Sheep's Eyes!");
		
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

			opp.boost("spdef", -1);
			printMsg(pony.getNickname()+"'s Sheep's Eyes lowered "+opp.getNickname()+"'s SpD!");
			if(be.getBattleTask() != null) {
				be.getBattleTask().sendB(allyC,"|boost|opp|spdef|-1|"+
					pony.getNickname()+"'s Sheep's Eyes lowered "+
					opp.getNickname()+"'s SpD!");
				be.getBattleTask().sendB(oppC,"|boost|ally|spdef|-1|"+
					pony.getNickname()+"'s Sheep's Eyes lowered "+
					opp.getNickname()+"'s SpD!");
			}
			activated = true;
		} else throw new NullPointerException("Pony with Sheep's Eyes is neither ally nor opponent!");
	}

	@Override
	public void onSwitchOut(final BattleEngine be) {
		activated = false;
	}
}
