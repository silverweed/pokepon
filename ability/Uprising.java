//: ability/Uprising.java

package pokepon.ability;

import pokepon.pony.*;
import pokepon.battle.*;
import pokepon.move.*;
import pokepon.enums.Type;
import pokepon.util.Debug;
import static pokepon.util.MessageManager.*;

/** Uprising
 * Whenever a stat is lowered, it is instead boosted by 2.
 *
 * @author silverweed
 */

public class Uprising extends Ability {

	public Uprising() {
		super("Uprising");
		briefDesc = "Whenever a stat is lowered,<br>it is instead boosted by 2.";
	}
	
	@Override
	public void onBoost(final BattleEngine be) {
		if(pony == null)
			throw new NullPointerException("Pony is null for Uprising!");
		if(be.getDefender() != pony) return;

		try {
			Pony.Stat stat = (Pony.Stat)be.getTriggerArg("stat");
			int boost = (int)be.getTriggerArg("boost");
			if(boost >= 0) return;
			pony.boost(stat, -boost + 2);
			if(be.getBattleTask() != null) {
				be.getBattleTask().sendB(be.getConnection(be.getSide(pony)),
					"|boost|ally|"+stat+"|"+(2-boost)+"|"+
					pony.getNickname()+"'s Uprising raises its "+stat+"!");
				be.getBattleTask().sendB(be.getConnection(be.getOppositeSide(pony)),
					"|boost|opp|"+stat+"|"+(2-boost)+"|"+
					pony.getNickname()+"'s Uprising raises its "+stat+"!");
			}
		} catch(Exception e) {
			printDebug("[Uprising] Exception: " +e);
			e.printStackTrace();
		}
	}
}
