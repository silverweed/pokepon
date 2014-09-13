//: ability/Mildness.java

package pokepon.ability;

import pokepon.ability.*;
import pokepon.pony.*;
import pokepon.battle.*;
import pokepon.move.*;
import static pokepon.util.MessageManager.*;

/** Mildness
 * Like Technician
 *
 * @author silverweed
 */

public class Mildness extends Ability {

	private Move move;

	public Mildness() {
		super("Mildness");
		briefDesc = "Boosts by 50% moves with a base power equal or less than 60.";
	}

	@Override
	public void onMoveUsage(final BattleEngine be) {
		if(be.getAttacker() != pony) return;
		if(be.getCurrentMove().getMoveType() != Move.MoveType.STATUS && be.getCurrentMove().getBaseDamage() <= 60) {
			move = be.getCurrentMove();
			move.setDamageBoost((int)(move.getBaseDamage() / 2f));
		}
	}
}
