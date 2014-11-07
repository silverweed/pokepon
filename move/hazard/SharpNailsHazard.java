//: move/hazard/SharpNailsHazard.java

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
public class SharpNailsHazard extends Hazard {

	public SharpNailsHazard() {
		super("Sharp Nails");

		maxLayers = 3;
		token = "nail.png";
	}

	@Override
	public String[] getSetupPhrase() {
		return new String[] {
			"Pointy nails were scattered through the opponent's field!",
			"Pointy nails were scattered on your field!"
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
			printDebug("[SharpNailsHazard] error: side is "+side+"!");
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
				int dmg = pony.damagePerc(100f / (10f - 2f*layers));
				printMsg(pony.getNickname()+" was damaged by sharp nails!");
				printDamageMsg(pony,dmg);
				if(be.getBattleTask() != null) {
					be.getBattleTask().sendB("|battle|"+pony.getNickname()+" is damaged by sharp nails!");
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
}
