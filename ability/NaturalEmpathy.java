//: ability/NaturalEmpathy.java

package pokepon.ability;

import pokepon.pony.*;
import pokepon.pony.Pony.Status;
import pokepon.battle.*;
import pokepon.move.*;
import pokepon.net.jack.*;
import static pokepon.util.MessageManager.*;

/** NaturalEmpathy
 * When this pony is Burned, Petrified, Poisoned or Paralyzed,
 * the status is transferred to the enemy.
 *
 * @author silverweed
 */

public class NaturalEmpathy extends Ability {

	public NaturalEmpathy() {
		super("Natural Empathy");
		briefDesc = "When this pony is statused (except Slp), the status is transferred on enemy.";	
	}

	@Override
	public void afterMoveHit(final BattleEngine be) {
		if(pony == null) throw new NullPointerException("Pony is null for Natural Empathy!");
		Status status = pony.getStatus();
		if(status != null && status != Status.ASLEEP) {
			pony.setStatus(status, false);
			Pony opp = be.getOpponent(pony);
			if(opp.hasNegativeCondition()) return;
			opp.setStatus(status, true);
			if(be.getBattleTask() != null) {
				Connection allyC = be.getConnection(be.getSide(pony));
				Connection oppC = be.getConnection(be.getOppositeSide(pony));
				be.getBattleTask().sendB(allyC, "|rmstatus|ally|" + status);
				be.getBattleTask().sendB(oppC, "|rmstatus|opp|" + status);
				be.getBattleTask().sendB(allyC, "|addstatus|opp|" + status +
					"|"+pony.getNickname()+"'s Natural Empathy transfers the bad status!");
				be.getBattleTask().sendB(oppC, "|addstatus|ally|" + status +
					"|"+pony.getNickname()+"'s Natural Empathy transfers the bad status!");
			}
		}
	}
}
