//: item/ChoiceBoots.java

package pokepon.item;

import pokepon.pony.*;
import pokepon.battle.*;
import pokepon.net.jack.*;
import pokepon.move.Move;
import pokepon.util.*;
import static pokepon.util.MessageManager.*;

/** Like Choice Scarf 
 *
 * @author silverweed
 */

public class ChoiceBoots extends Item {

	public int origSpe = -1;

	public ChoiceBoots() {
		super("Choice Boots");
		briefDesc = "User Speed is 1.5x, but can only use first move he selects.";
	}
	
	@Override
	public void beforeTurnStart(final BattleEngine be) {
		if(origSpe == -1)
			origSpe = pony.getBaseStat("speed");
		pony.setBaseStat(Pony.Stat.SPEED, (int)(origSpe * 1.5));	
		if(Debug.on) printDebug("[ChoiceBoots] Speed: "+origSpe+"->"+pony.getBaseStat(Pony.Stat.SPEED));
	}

	@Override
	public void onMoveUsage(final BattleEngine be) {
		if(be.getAttacker() != pony) return;
		
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
	public void onTurnEnd(final BattleEngine be) {
		if(origSpe != -1) 
			pony.setBaseStat(Pony.Stat.SPEED, origSpe);
	}

	@Override
	public void onSwitchOut(final BattleEngine be) {
		be.getLockingTurns()[be.getSide(pony)-1] = 0;
	}
}
