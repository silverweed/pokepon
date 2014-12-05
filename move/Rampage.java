//: move/Rampage.java

package pokepon.move;

/**
 * Status move which increases Atk by 3 but lowers defs and confuses
 * user after 1-3 turns.
 *
 * @author silverweed
 */

import pokepon.enums.*;
import pokepon.pony.*;
import pokepon.player.*;
import pokepon.battle.*;

public class Rampage extends Move {
	
	public Rampage() {
		super("Rampage");
		
		type = Type.CHAOS;
		moveType = Move.MoveType.STATUS;
		contactMove = true;
		maxpp = pp = 10;
		accuracy = -1;
		priority = 0;

		description = "Enter in a rampaging state to raise your attack drastically in exchange of the defenses.";
		briefDesc = "Raises Atk by 3, lowers Def and SpD by 2.<br>Confuses user after 2-3 turns.";

		animation.put("name","Shake");
		animation.put("sprite","user");
		
		// immediate effects
		userAtk = addEntry(3,1f);
		userDef = addEntry(-1,1f);
		userSpdef = addEntry(-1,1f);

	}
	
	public Rampage(Pony p) {
		this();
		pony = p;
	}

	public BattleEvent[] getBattleEvents() {
		double rand = Math.random();
		return new BattleEvent[] {
			new BattleEvent(rand < 0.25 ? 2 : (rand < 0.75 ? 3 : 4),name) {
				@Override
				public void onTurnEnd(final BattleEngine be) {
					Pony ap = be.getTeam(be.getSide(source)).getActivePony();
					if(ap != source) {
						// suicide if the source is switched out
						count = 0;
						return;
					}
					if(--count == 0 && !source.isFainted() && !source.isConfused()) {
						source.setConfused(true);
						if(be.getBattleTask() != null) {
							be.getBattleTask().sendB("|battle|"+source.getNickname()+" is confused due to the fatigue!");
							be.getBattleTask().sendB(be.getConnection(be.getSide(source)),"|addstatus|ally|cnf");
							be.getBattleTask().sendB(be.getConnection(be.getOppositeSide(source)),"|addstatus|opp|cnf");
						}
					}
				}
			}
		};
	}

	/** Move will fail if user has already maxed Atk. */
	@Override
	public boolean validConditions(final BattleEngine be) {
		if(pony == null) throw new NullPointerException("pony is null for move "+this);
		return pony.atkMod() < 6;
	}
}
