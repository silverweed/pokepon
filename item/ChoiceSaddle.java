//: item/ChoiceSaddle.java

package pokepon.item;

import pokepon.battle.*;
import pokepon.net.jack.*;
import pokepon.move.Move;
import static pokepon.util.MessageManager.*;

/** Like Choice Band 
 *
 * @author Giacomo Parolini
 */

public class ChoiceSaddle extends Item {

	public int origAtk = -1;

	public ChoiceSaddle() {
		super("Choice Saddle");
		briefDesc = "User Atk is 1.5x, but can only use first move he selects.";
	}
	
	@Override
	public void onMoveUsage(final BattleEngine be) {
		if(be.getAttacker() != pony) return;
		
		if(origAtk == -1)
			origAtk = pony.getBaseStat("atk");
		pony.setBaseStat("atk", (int)(origAtk * 1.5));	
		
		// prevent BattleTask to unlock this pony
		be.getLockingTurns()[be.getSide(pony)-1] = -1;
		
		// we've already locked pony
		if(be.getAttacker().isLockedOnMove()) return;

		be.getAttacker().setLockedOnMove(true);	
		if(be.getBattleTask() != null) {
			be.getBattleTask().sendB(be.getConnection(be.getSide(pony)),"|lockon|"+be.getCurrentMove());
		}
	}

	@Override
	public void afterMoveUsage(final BattleEngine be) {
		if(be.getAttacker() == pony)
			pony.setBaseStat("atk", origAtk);
	}

	@Override
	public void onSwitchOut(final BattleEngine be) {
		be.getLockingTurns()[be.getSide(pony)-1] = 0;
	}
}
