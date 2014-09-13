//: ability/DieHard.java

package pokepon.ability;

import pokepon.battle.*;
import pokepon.util.Debug;
import pokepon.net.jack.*;
import static pokepon.util.MessageManager.*;

/** Can heal negative status on turn end.
 *
 * @author silverweed
 */

public class DieHard extends Ability {

	private static final float PROB_HEAL = 0.25f;

	public DieHard() {
		super("Die Hard");
		briefDesc = (int)(PROB_HEAL*100) + "% to heal negative status at the end of each turn.";
	}

	@Override
	public void onTurnEnd(final BattleEngine be) {
		if(pony == null)
			throw new NullPointerException("Pony is null for ability DieHard!");
		
		if(pony.hasNegativeCondition() && be.getRNG().nextFloat() < PROB_HEAL) {
			if(be.getBattleTask() != null) {
				Connection allyC = be.getConnection(be.getSide(pony));
				Connection oppC = be.getConnection(be.getOppositeSide(pony));
				be.getBattleTask().sendB(allyC,"|rmstatus|ally||"+pony.getNickname()+" manages to heal thanks to its willpower!");
				be.getBattleTask().sendB(oppC,"|rmstatus|opp||"+pony.getNickname()+" manages to heal thanks to its willpower!");
			}
			pony.healStatus();
		}
	}		
}
