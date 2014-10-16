//: ability/SpellRefractory.java

package pokepon.ability;

import pokepon.pony.*;
import pokepon.battle.*;
import pokepon.enums.*;
import pokepon.net.jack.*;
import static pokepon.util.MessageManager.*;

/** SpellRefractory
 * Immunizes from laser-based moves
 *
 * @author Giacomo Parolini
 */

public class SpellRefractory extends Ability {

	public SpellRefractory() {
		super("Spell Refractory");
		briefDesc = "This pony is immune from moves based on rays or beams.";
	}

	@Override
	public void beforeMoveHit(final BattleEngine be) {
		if(be.getCurrentMove().isBeamMove() && be.getDefender() == pony) {
			be.setBreakCycle(true);
			if(be.getBattleTask() != null) {
				be.getBattleTask().sendB(be.getAlly(),"|immune|opp");
				be.getBattleTask().sendB(be.getOpp(),"|immune|ally");
				be.getBattleTask().sendB("|battle|"+pony.getNickname()+" is not affected by "+be.getCurrentMove()+"!");
			}
		}
	}
}
