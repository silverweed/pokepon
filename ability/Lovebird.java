//: ability/Lovebird.java

package pokepon.ability;

import pokepon.pony.*;
import pokepon.battle.*;
import pokepon.enums.*;
import pokepon.net.jack.*;
import static pokepon.util.MessageManager.*;

/** Lovebird
 * Renders the pony immune to Love; the pony also gets +1 SpA if hit by one.
 *
 * @author Giacomo Parolini
 */

public class Lovebird extends Ability {

	public Lovebird() {
		super("Lovebird");
		briefDesc = "This pony gets +1 SpA whenever<br>hit by a Love-type move.";
	}

	@Override
	public void onSwitchIn(final BattleEngine be) {
		pony.addVolatileEffectiveness(Type.LOVE, 0f);
	}

	@Override
	public void beforeMoveHit(final BattleEngine be) {
		if(be.getCurrentMove().getType() == Type.LOVE && be.getDefender() == pony) {
			Connection allyC = be.getConnection(be.getSide(pony));
			Connection oppC = be.getConnection(be.getOppositeSide(pony));
			if(pony.spatkMod() < 6) {
				pony.boostSpatk(1);
				if(be.getBattleTask() != null) {
					be.getBattleTask().sendB(allyC, "|boost|ally|spatk|1");
					be.getBattleTask().sendB(oppC, "|boost|opp|spatk|1");
				}
			}
		}
	}
}
