//: ability/Bookworm.java

package pokepon.ability;

import pokepon.pony.*;
import pokepon.battle.*;
import pokepon.move.*;
import pokepon.enums.*;
import pokepon.net.jack.*;
import static pokepon.util.MessageManager.*;

/** Bookworm
 * This pony cannot be statused nor damaged by a Magic-type;
 * damaging Magic moves heal it by the same amount.
 *
 * @author silverweed
 */

public class Bookworm extends Ability {

	public Bookworm() {
		super("Bookworm");
		briefDesc = "Magic-type attacks heal instead of doing damage.<br>Immunity to Magic status moves.";
	}

	@Override
/*	public void onMoveUsage(final BattleEngine be) {
		if(be.getDefender() != pony) return;
		Move curMove = be.getCurrentMove();
		if(curMove.getType() != Type.MAGIC) return;
		if(curMove.getMoveType() == Move.MoveType.STATUS || curMove.getDamage() <= 0) {
			be.setBreakCycle(true);
			if(be.getBattleTask() != null) {
				be.getBattleTask().sendB(be.getAlly(),"|immune|opp");
				be.getBattleTask().sendB(be.getOpp(),"|immune|ally");
				be.getBattleTask().sendB("|battle|"+pony.getNickname()+" is not affected by "+be.getCurrentMove()+"!");
			}
		}
	}*/

	public float preventNegativeCondition(final String which, final BattleEngine be) {
		if(be.getCurrentMove().getType() == Type.MAGIC) return 1f;
		return 0f;
	}

	@Override
	public float changeDamageTakenFrom(Type type) {
		return type == Type.MAGIC ? -1f : 1f;
	}
}
