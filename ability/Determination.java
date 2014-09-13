//: ability/Determination.java

package pokepon.ability;

import pokepon.battle.*;
import pokepon.util.Debug;
import static pokepon.util.MessageManager.*;

/** Prevents from flinching
 *
 * @author Giacomo Parolini
 */

public class Determination extends Ability {

	public Determination() {
		super("Determination");
		briefDesc = "Prevents this pony from flinching.";
	}

	@Override
	public void afterMoveUsage(final BattleEngine be) {
		if(pony == null)
			throw new NullPointerException("Pony is null for ability Determination!");
		
		if(Debug.pedantic) printDebug("[Determination::afterMoveUsage] pony == defender: "+(pony == be.getDefender()));
		if(be.getDefender() == pony) {
			if(Debug.pedantic) printDebug("pony is flinched = "+pony.isFlinched());
			if(pony.isFlinched()) {
				if(Debug.pedantic) printDebug("pony unflinched.");
				pony.setFlinched(false);
				if(be.getBattleTask() != null)
					be.getBattleTask().sendB("|battle|"+pony.getNickname()+
						"'s determination prevents it from flinching!");
			}
		}
	}		
}
