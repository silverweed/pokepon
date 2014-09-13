//: item/SpareSnaks.java

package pokepon.item;

import pokepon.battle.*;
import pokepon.net.jack.*;
import pokepon.move.Move;
import static pokepon.util.MessageManager.*;

/** Like Life Orb 
 *
 * @author Giacomo Parolini
 */

public class AlicornAmulet extends Item {

	public AlicornAmulet() {
		super("Alicorn Amulet");
		briefDesc = "Boosts damage by 30%, but damages user of 10% of its HP after attacks.";
	}

	@Override
	public float changeDamageDealtBy(Move.MoveType mt) {
		if(mt != Move.MoveType.STATUS)
			return 1.3f;
		else return 1f;
	}
	
	@Override
	public void afterMoveUsage(final BattleEngine be) {
		if(	be.getAttacker() == pony && 
			be.getCurrentMove().getMoveType() != Move.MoveType.STATUS &&
			be.getLatestInflictedDamage() > 0 &&
			!be.getCurrentMove().isOHKO()
		) {
			Connection allyC = be.getConnection(be.getSide(pony));
			Connection oppC = be.getConnection(be.getOppositeSide(pony));
			int dam = pony.damagePerc(10f);
			if(be.getBattleTask() != null) {
				be.getBattleTask().sendB(allyC,"|damage|ally|" + dam + "|" + getPhrase());
				be.getBattleTask().sendB(oppC,"|damage|opp|" + dam + "|" + getPhrase());
			}
		}
	}

	@Override
	public String getPhrase() {
		return "Dark magic stole some of [pony]'s HP!";
	}
}
