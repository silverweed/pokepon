//: ability/Simplicity.java

package pokepon.ability;

import pokepon.pony.*;
import pokepon.battle.*;
import pokepon.move.*;
import static pokepon.util.MessageManager.*;

/** Simplicity
 * A less-extreme version of Hustle: trade -15% to accuracy for a +40% Attack on physical moves.
 *
 * @author silverweed
 */

public class Simplicity extends Ability {

	private int origAtk = -1;
	private int origAcc;
	private Move move;

	public Simplicity() {
		super("Simplicity");
		briefDesc = "-15% accuracy, +40% power to physical moves.";
	}

	@Override
	public void onMoveUsage(final BattleEngine be) {
		if(be.getAttacker() != pony) return;
		if(be.getCurrentMove().getMoveType() == Move.MoveType.PHYSICAL) {
			move = be.getCurrentMove();
			origAcc = move.getAccuracy();
			move.setAccuracy((int)(origAcc*0.85));
			if(origAtk == -1)
				origAtk = pony.getBaseStat("atk");
			pony.setBaseStat("Atk", (int)(origAtk*1.4f));
		}
	}

	@Override
	public void reset() {
		pony.setBaseStat("Atk", origAtk);
		if(move != null)
			move.setAccuracy(origAcc);
	}

	@Override
	public void afterMoveUsage(final BattleEngine be) {
		pony.setBaseStat("Atk", origAtk);
		if(move != null)
			move.setAccuracy(origAcc);
	}
}
