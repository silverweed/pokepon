//: move/hazard/WildWeedHazard.java

package pokepon.move.hazard;

import pokepon.battle.*;
import pokepon.move.hazard.*;
import pokepon.util.*;
import pokepon.pony.*;
import static pokepon.util.MessageManager.*;

/** 
 * Steals 1/8 of target's HP at the end of each turn.
 *
 * @author silverweed
 */
public class WildWeedHazard extends Hazard {

	public WildWeedHazard() {
		super("Wild Weed");

		maxLayers = 1;
		token = "energyball.png";
	}

	@Override
	public String[] getSetupPhrase() {
		return new String[] {
			"Your opponent is surrounded by Wild Weeds!",
			"Your pony is surrounded by Wild Weeds!"
		};
			
	}
	
	@Override
	public void onSwitchOut(final BattleEngine be) {
		if(be.getCurrentMove() != null && be.getCurrentMove().copyVolatiles())
			return;
		be.getHazards(side).remove(this);
		if(be.getBattleTask() != null) {
			be.getBattleTask().sendB(be.getAlly(),"|rmhazard|ally|"+name);	
			be.getBattleTask().sendB(be.getOpp(),"|rmhazard|opp|"+name);	
		}
	}

	@Override
	public void onTurnEnd(final BattleEngine be) {
		// only trigger if ponies on both side are alive
		if(	be.getDefender() == null || be.getDefender().isFainted() ||
			be.getAttacker() == null || be.getAttacker().isFainted()
		) {
			return;
		}
		for(EffectDealer ed : be.getTeam(side).getActivePony().getEffectDealers())
			if(ed.negateSecondaryDamage())
				return;
		int dmg = be.getTeam(side).getActivePony().damagePerc(12.5f);
		be.getTeam(side == 1 ? 2 : 1).getActivePony().increaseHp(dmg);
		if(be.getBattleTask() != null) {
			be.getBattleTask().sendB(be.getConnection(side),"|damage|ally|"+dmg+"|"+
				be.getTeam(side).getActivePony().getNickname()+"'s healt is sapped by "+name+"!");
			be.getBattleTask().sendB(be.getConnection(side == 1 ? 2 : 1),"|damage|opp|"+dmg+"|"+
				be.getTeam(side).getActivePony().getNickname()+"'s healt is sapped by "+name+"!");

			be.getBattleTask().sendB(be.getConnection(side),"|damage|opp|-"+dmg);
			be.getBattleTask().sendB(be.getConnection(side == 1 ? 2 : 1),"|damage|ally|-"+dmg);
		}
	}
}
