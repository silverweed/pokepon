//: move/Nap.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;
import pokepon.battle.*;

/**
 * Heal 50% of user's HP.
 *
 * @author silverweed
 */

public class Nap extends Move {
	
	public Nap() {
		super("Nap");
		
		type = Type.NIGHT;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 10;
		accuracy = -1;
		priority = 0;
		briefDesc = "User sleeps 2 turns to refill HP and cure status.";

		healUser = 1f;
		healUserStatus = 1f;
	}

	public Nap(Pony p) {
		this();
		pony = p;
	}

	@Override
	public BattleEvent[] getBattleEvents() {
		return new BattleEvent[] {
			new BattleEvent(1, name) {
				@Override
				public void afterMoveUsage(final BattleEngine be) {
					if(be.getAttacker() == pony && !pony.isFainted()) {
						pony.setAsleep(true);
						if(be.getBattleTask() != null) {
							be.getBattleTask().sendB("|battle|"+pony.getNickname()+" takes a nap and becomes healthy!");
							be.getBattleTask().sendB(be.getConnection(be.getSide(pony)),"|addstatus|ally|slp");
							be.getBattleTask().sendB(be.getConnection(be.getOppositeSide(pony)),"|addstatus|opp|slp");
						}
						pony.sleepCounter = 3;
						count = 0;
					}
				}
			}
		};
	}
}
