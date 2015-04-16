//: ability/Swiftness.java

package pokepon.ability;

import pokepon.pony.*;
import pokepon.battle.*;
import static pokepon.util.MessageManager.*;

/** Swiftness
 * +1 Evasion on switch in
 *
 * @author silverweed
 */

public class Swiftness extends Ability {

	public Swiftness() {
		super("Swiftness");
		briefDesc = "Raises user's Evasion by 1 on switch in.";
	}

	@Override
	public void afterSwitchIn(final BattleEngine be) {
		if(pony.evasionMod() < 6) {
			pony.boost(Pony.Stat.EVASION, 1);
			if(be.getBattleTask() != null) {
				be.getBattleTask().sendB(be.getConnection(be.getSide(pony)), "|boost|ally|eva|1|"+
					pony.getNickname()+"'s Swiftness raised its Evasion!");
				be.getBattleTask().sendB(be.getConnection(be.getOppositeSide(pony)), "|boost|opp|eva|1|"+
					pony.getNickname()+"'s Swiftness raised its Evasion!");
			}
		}
	}
}
