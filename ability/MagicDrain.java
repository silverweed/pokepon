//: ability/MagicDrain.java

package pokepon.ability;

import pokepon.pony.*;
import pokepon.battle.*;
import pokepon.net.jack.*;
import static pokepon.util.MessageManager.*;

/** MagicDrain
 * Ignores stats negative boosts.
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

		Pony oppP = be.getTeam(be.getOppositeSide(pony)).getActivePony();
		Connection ally = be.getConnection(be.getSide(pony));
		Connection opp = be.getConnection(be.getOppositeSide(pony));
		
		if(oppP == null || oppP.isKO()) return;

		if(be.getBattleTask() != null)
			be.getBattleTask().sendB("|battle|"+pony.getNickname()+" absorbs magic and gets stronger!");

		switch(oppP.getRace()) {
			case UNICORN:
				be.tryStatChange(pony,"spatk",1);
				break;
			case EARTHPONY:
				be.tryStatChange(pony,"atk",1);
				break;
			case GRYPHON:
			case PEGASUS:
				be.tryStatChange(pony,"speed",1);
				break;
			case MYTHICBEAST:
				be.tryStatChange(pony,"spdef",1);
				break;
			case BUFFALO:
				be.tryStatChange(pony,"def",1);
				break;
			case ZEBRA:
				int healed = pony.increaseHpPerc(12.5f);
				if(be.getBattleTask() != null) {
					be.getBattleTask().sendB(ally,"|damage|ally|"+healed+"|"+getPhrase());
					be.getBattleTask().sendB(opp,"|damage|opp|"+healed+"|"+getPhrase());
				}
				break;
			case ALICORN:
				be.tryStatChange(pony,"spatk",1);
				be.tryStatChange(pony,"spdef",1);
				break;
		}
	}

	@Override
	public String getPhrase() {
		return "[pony] drains magic from the enemy!";
	}

}
