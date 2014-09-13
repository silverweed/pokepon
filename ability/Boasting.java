//: ability/Boasting.java

package pokepon.ability;

import pokepon.pony.*;
import pokepon.battle.*;
import static pokepon.util.MessageManager.*;

/** Boasting
 * Trixie's signature ability; it raises SpA by 1 if enemy's SpA is greater
 * than yours.
 *
 * @author Giacomo Parolini
 */

public class Boasting extends Ability {

	public Boasting() {
		super("Boasting");
		briefDesc = "Raises user's SpA if enemy's SpA is greater.";
	}

	@Override
	public void afterSwitchIn(final BattleEngine be) {
		Pony oppP = be.getOpponent(pony); 
		if(pony.spatk() < oppP.spatk() && pony.spatkMod() < 6) {
			printMsg(pony.getNickname()+"'s self-confidence increased its Special Attack!");
			pony.boostSpatk(1);
			if(be.getBattleTask() != null) {
				be.getBattleTask().sendB(be.getConnection(be.getSide(pony)),"|boost|ally|spatk|1|"+
					pony.getNickname()+"'s self-confidence increased its Special Attack!");
				be.getBattleTask().sendB(be.getConnection(be.getOppositeSide(pony)),"|boost|opp|spatk|1|"+
					pony.getNickname()+"'s self-confidence increased its Special Attack!");
			}

		}
	}
}
