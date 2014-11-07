//: move/hazard/StealthDiamondsHazard.java

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
public class StealthDiamondsHazard extends Hazard {

	public StealthDiamondsHazard() {
		super("Stealth Diamonds");

		maxLayers = 1;
		token = "diamond.png";
	}

	@Override
	public String[] getSetupPhrase() {
		return new String[] {
			"Magic diamonds are floating on the enemy field!",
			"Magic diamonds are floating on your field!"
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
			printDebug("[StealthDiamondsHazard] error: side is "+side+"!");
			return;
		}

		for(EffectDealer ed : pony.getEffectDealers())
			if(ed.ignoreSecondaryDamage())
				return;

		float perc = 12.5f;
		perc *= TypeDealer.getEffectiveness(Type.GENEROSITY,pony.getTypes());	
		int dmg = pony.damagePerc(perc);
		printMsg(pony.getNickname()+" was damaged by magic diamonds!");
		printDamageMsg(pony,dmg);
		if(be.getBattleTask() != null) {
			be.getBattleTask().sendB("|battle|"+pony.getNickname()+" is damaged by magic diamonds!");
			// discover if our side is attacking ('ally' for the BE) or defending ('opp')
			if(be.getTeam1() == be.getTeamAttacker()) {
				if(side == 1) { 
					be.getBattleTask().sendB(be.getAlly(),"|damage|ally|"+dmg);
					be.getBattleTask().sendB(be.getOpp(),"|damage|opp|"+dmg);
				} else {
					be.getBattleTask().sendB(be.getAlly(),"|damage|opp|"+dmg);
					be.getBattleTask().sendB(be.getOpp(),"|damage|ally|"+dmg);
				}
			} else {
				if(side == 1) { 
					be.getBattleTask().sendB(be.getAlly(),"|damage|opp|"+dmg);
					be.getBattleTask().sendB(be.getOpp(),"|damage|ally|"+dmg);
				} else {
					be.getBattleTask().sendB(be.getAlly(),"|damage|ally|"+dmg);
					be.getBattleTask().sendB(be.getOpp(),"|damage|opp|"+dmg);
				}
			}
		}

		return;
	}
}
