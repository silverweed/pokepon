//: move/hazard/SlimyTrapHazard.java

package pokepon.move.hazard;

import pokepon.battle.*;
import pokepon.move.hazard.*;
import pokepon.util.*;
import pokepon.pony.*;
import pokepon.enums.*;
import static pokepon.util.MessageManager.*;

/** Hazard which damages entering ponies of 12.5% x (type multiplier)
 *
 * @author Giacomo Parolini
 */
public class SlimyTrapHazard extends Hazard {

	public SlimyTrapHazard() {
		super("Slimy Trap");

		maxLayers = 1;
		token = "slimytrap.png";
	}

	@Override
	public String[] getSetupPhrase() {
		return new String[] {
			"Your opponent's field has a Slimy Trap on it!",
			"Your field has a Slimy Trap on it!"
		};
	}

	@Override
	public void onSwitchIn(final BattleEngine be) {
		Pony pony = null;
		if(side == 1) 
			pony = be.getTeam1().getActivePony();
		else if(side == 2)
			pony = be.getTeam2().getActivePony();
		else {
			printDebug("[SlimyTrapHazard] error: side is "+side+"!");
			return;
		}

		switch(pony.getRace()) {
			case GRYPHON: 
			case PEGASUS:
				return;
			default:
				pony.boost("speed", -1);
				if(be.getBattleTask() != null) {
					be.getBattleTask().sendB(be.getBattleTask().getConnection(side),"|boost|ally|speed|-1");
					be.getBattleTask().sendB(be.getBattleTask().getConnection(side == 1 ? 2 : 1),"|boost|opp|speed|-1");
				}
		}
	}
}
