//: move/LoveAndCare.java

package pokepon.move;

/**
 * Like Wish.
 *
 * @author silverweed
 */

import pokepon.enums.*;
import pokepon.pony.*;
import pokepon.player.*;
import pokepon.battle.*;
import pokepon.net.jack.*;
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

		animation.put("name", "Fade");
		animation.put("sprite", "wisp.png");
		animation.put("transparent",true);
		animation.put("fadeOut",true);
		animation.put("initialPoint","ally +20Y");
		animation.put("finalPoint","ally -50Y");
	}

	public LoveAndCare(Pony p) {
		this();
		pony = p;
	}

	@Override
	public BattleEvent[] getBattleEvents() {
		return new BattleEvent[] {
			new BattleEvent(2, name) {
				@Override
				public void onTurnEnd(final BattleEngine be) {
					if(Debug.on) printDebug("[Love and Care] count = "+(count-1));
					if(--count == 0) {
						Pony ap = be.getTeam(be.getSide(pony)).getActivePony();
						if(ap != null && !ap.isFainted()) {
							int heal = ap.increaseHpPerc(50f);	
							if(be.getBattleTask() != null) {
								Connection ally = be.getConnection(be.getSide(pony));
								be.getBattleTask().sendB(ally,"|damage|ally|-"+heal);
								be.getBattleTask().sendB(ally,"|anim|ally|"+
									"|name=Fade|sprite=wisp.png|transparent=(b)true"+
									"|fadeOut=(b)false|initialPoint=ally -50Y"+
									"|finalPoint=ally +20Y");
								be.getBattleTask().sendB(
									be.getConnection(be.getOppositeSide(pony)),"|damage|opp|-"+heal);
							}
						}
					}
				}
			}
		};
	}
}
