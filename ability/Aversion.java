//: ability/Aversion.java

package pokepon.ability;

import pokepon.pony.*;
import pokepon.battle.*;
import pokepon.move.*;
import static pokepon.util.MessageManager.*;

/** Aversion
 * Whenever this pony is damaged by a contact move, opponent loses 1/8 HP.
 *
 * @author Giacomo Parolini
 */

public class Aversion extends Ability {

	public Aversion() {
		super("Aversion");
		briefDesc = "Damages opponent by 1/8 HP when hit by a contact move.";
	}

	@Override
	public void afterMoveHit(final BattleEngine be) {
		if(	pony == be.getDefender() && 
			be.getCurrentMove().isContactMove() &&
			be.getInflictedDamage() > 0
		) {
			int damage = be.getAttacker().damagePerc(12.5f);
			if(be.getBattleTask() != null) {
				be.getBattleTask().sendB(be.getAlly(),"|damage|opp|"+damage+"|"+
					be.getAttacker().getNickname()+" is damaged by "+pony.getNickname()+"'s Aversion!");
				be.getBattleTask().sendB(be.getOpp(),"|damage|ally|"+damage+"|"+
					be.getAttacker().getNickname()+" is damaged by "+pony.getNickname()+"'s Aversion!");
			}
		}
	}
}
