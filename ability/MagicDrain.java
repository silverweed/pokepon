//: ability/MagicDrain.java

package pokepon.ability;

import pokepon.pony.*;
import static pokepon.pony.Pony.Stat.*;
import pokepon.battle.*;
import pokepon.net.jack.*;
import static pokepon.util.MessageManager.*;

/** MagicDrain
 * OP ability which boosts a stat or heals user at each turn end
 * depending on enemy's Race; Tirek's signature ability.
 *
 * @author silverweed
 */

public class MagicDrain extends Ability {

	public MagicDrain() {
		super("Magic Drain");
		briefDesc = "Boosts a stat per turn depending on opponent's race";
	}
	
	@Override
	public void onTurnEnd(final BattleEngine be) {
		if(pony == null)
			throw new NullPointerException("Pony is null for MagicDrain!");
		
		// don't trigger if the pony was switched in this turn
		if(pony.activeTurns < 1) return;

		Pony oppP = be.getTeam(be.getOppositeSide(pony)).getActivePony();
		
		if(oppP == null || oppP.isKO()) return;

		if(be.getBattleTask() != null)
			be.getBattleTask().sendB("|battle|"+pony.getNickname()+" absorbs magic and gets stronger!");

		switch(oppP.getRace()) {
			case UNICORN:
				be.tryStatChange(pony, SPATK, 1);
				break;
			case EARTHPONY:
				be.tryStatChange(pony, ATK, 1);
				break;
			case GRYPHON:
			case PEGASUS:
			case BREEZIE:
				be.tryStatChange(pony, SPEED, 1);
				break;
			case MYTHICBEAST:
				be.tryStatChange(pony, SPDEF, 1);
				break;
			case UNGULATE:
				be.tryStatChange(pony, DEF, 1);
				break;
			case ZEBRA: {
				int healed = pony.increaseHpPerc(12.5f);
				if(be.getBattleTask() != null) {
					Connection ally = be.getConnection(be.getSide(pony));
					Connection opp = be.getConnection(be.getOppositeSide(pony));
					be.getBattleTask().sendB(ally,"|damage|ally|"+healed+"|"+getPhrase());
					be.getBattleTask().sendB(opp,"|damage|opp|"+healed+"|"+getPhrase());
				}
				break;
			}
			case ALICORN:
				be.tryStatChange(pony, SPATK, 1);
				be.tryStatChange(pony, SPDEF, 1);
				break;
		}
	}

	@Override
	public String getPhrase() {
		return "[pony] drains magic from the enemy!";
	}
}
