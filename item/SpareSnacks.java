//: item/SpareSnacks.java

package pokepon.item;

import pokepon.battle.*;
import pokepon.net.jack.*;
import static pokepon.util.MessageManager.*;

/** Like Leftovers 
 *
 * @author Giacomo Parolini
 */

public class SpareSnacks extends Item {

	public SpareSnacks() {
		super("Spare Snacks");
		briefDesc = "At the end of every turn, holder restores 1/16 of its max HP.";
	}
	
	@Override
	public void onTurnEnd(final BattleEngine be) {
		if(pony.hp() < pony.maxhp()) {
			Connection allyC = be.getConnection(be.getSide(pony));
			Connection oppC = be.getConnection(be.getOppositeSide(pony));
			int gain = -pony.increaseHpPerc(6.25f);
			if(be.getBattleTask() != null) {
				be.getBattleTask().sendB(allyC,"|damage|ally|" + gain + "|" + getPhrase());
				be.getBattleTask().sendB(oppC,"|damage|opp|" + gain + "|" + getPhrase());
			}
		}
	}

	@Override
	public String getPhrase() {
		return "[pony] eats its Spare Snacks and restores HP!";
	}
}
