//: move/LoveAndCare.java

package pokepon.move;

/**
 * Like Wish.
 *
 * @author Giacomo Parolini
 */

import pokepon.enums.*;
import pokepon.pony.*;
import pokepon.player.*;
import pokepon.battle.*;
import pokepon.util.Debug;
import static pokepon.util.MessageManager.*;

public class LoveAndCare extends Move {
	
	public LoveAndCare() {
		super("Love And Care");
		
		type = Type.KINDNESS;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 16;
		accuracy = 100;
		priority = 0;
		description = "The user prepares to use some TLC on the Active Pony, healing him/her the following turn.";
		briefDesc = "Heals the active pony at the<br>end of the next turn.";
	}

	public LoveAndCare(Pony p) {
		this();
		pony = p;
	}

	/*@Override
	public void prepareDelayEffects(final BattleEngine be) {
		turnDelay = 1;
		countDelay = turnDelay;
	}
	
	@Override
	public EffectDealer delayEffects() {
		return new EffectDealer() {
			public float getUserHeal() { return 0.5f; }
		};
	};*/

	@Override
	public BattleEvent[] getBattleEvents() {
		return new BattleEvent[] {
			new BattleEvent(2, name) {
				@Override
				public void onTurnEnd(final BattleEngine be) {
					if(Debug.on) printDebug("[Love and Care] count = "+(count-1));
					if(--count == 0) {
						Pony pony = be.getTeam(be.getSide(source)).getActivePony();
						if(pony != null && !pony.isFainted()) {
							int heal = pony.increaseHpPerc(50f);	
							if(be.getBattleTask() != null) {
								be.getBattleTask().sendB(be.getConnection(be.getSide(source)),"|damage|ally|-"+heal);
								be.getBattleTask().sendB(be.getConnection(be.getOppositeSide(source)),"|damage|opp|-"+heal);
							}
						}
					}
				}
			}
		};
	}
}
