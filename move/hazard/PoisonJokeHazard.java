//: move/hazard/PoisonJokeHazard.java

package pokepon.move.hazard;

import pokepon.battle.*;
import pokepon.move.hazard.*;
import pokepon.util.*;
import pokepon.pony.*;
import static pokepon.util.MessageManager.*;

/** Hazard which damages any entering pony by 12.5% hp x number of layers (max 3);
 * does not affect Gryphons, Alicorns and Pegasi.
 *
 * @author Giacomo Parolini
 */
public class PoisonJokeHazard extends Hazard {

	public PoisonJokeHazard() {
		super("Poison Joke");

		maxLayers = 2;
		token = "poisonjoke_small.png";
	}

	@Override
	public String[] getSetupPhrase() {
		return new String[] {
			"Poison Joke blossomed on the opponent's field!",
			"Poison Joke blossomed on your field!"
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
			printDebug("[PoisonJokeHazard] error: side is "+side+"!");
			return;
		}

		for(EffectDealer ed : pony.getEffectDealers())
			if(ed.ignoreSecondaryDamage())
				return;

		switch(pony.getRace()) {
			case GRYPHON:
			case ALICORN:
			case PEGASUS:
				return;
			default:
				if(pony.hasNegativeCondition()) {
					return;
				}

				if(layers == 1) 
					pony.setStatus(Pony.Status.POISONED, true);
				else if(layers == 2)
					pony.setStatus(Pony.Status.INTOXICATED, true);

				printMsg(pony.getNickname()+" got poisoned from the Poison Joke!");
				String status = pony.hasStatus(Pony.Status.INTOXICATED) ? "tox" : "psn";

				if(be.getBattleTask() != null) {
					be.getBattleTask().sendB("|battle|"+pony.getNickname()+" got poisoned from the Poison Joke!");
					// discover if our side is attacking ('ally' for the BE) or defending ('opp')
					if(be.getTeam1() == be.getTeamAttacker() ^ side == 1) {
						be.getBattleTask().sendB(be.getAlly(),"|addstatus|opp|"+status);
						be.getBattleTask().sendB(be.getOpp(),"|addstatus|ally|"+status);
					} else {
						be.getBattleTask().sendB(be.getAlly(),"|addstatus|ally|"+status);
						be.getBattleTask().sendB(be.getOpp(),"|addstatus|opp|"+status);
					}
				}
		}
	}
}
