//: move/Entrench.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;
import pokepon.battle.*;
import pokepon.move.*;
import pokepon.util.Debug;
import static pokepon.util.MessageManager.*;
import static java.util.AbstractMap.SimpleEntry;

/**
 * Like Protect
 * 
 * @author silverweed
 */
public class Entrench extends Move {
	
	public Entrench() {
		super("Entrench");
		
		type = Type.HONESTY;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 32;
		accuracy = -1;
		priority = 0;
		description = "Raise defenses before your team to halve all physical damage for 5 turns.";
		briefDesc = "Halves incoming physical<br>damage for 5 turns.";
	}
	
	public Entrench(Pony p) {
		this();
		pony = p;
	}

	@Override
	public BattleEvent[] getBattleEvents() {
		return new BattleEvent[] {
			new BattleEvent(5, name) {
				@Override
				public void delayInit(final BattleEngine be) {
					if(count < delay) return; 
					if(be.getBattleTask() != null) {
						be.getBattleTask().sendB("|battle|An Entrench was raised before the team!");
						be.getBattleTask().sendB(be.getConnection(be.getSide(pony)),"|persistent|ally|"+name);
						be.getBattleTask().sendB(be.getConnection(be.getOppositeSide(pony)),"|persistent|opp|"+name);
					}
				}
				@Override
				public void onDamage(final BattleEngine be) {
					if(Debug.on) printDebug("["+name+"] called onDamage("+be.getCurrentMove()+")");
					if(be.currentPlayer() != be.getSide(pony)) {
						if(be.getCurrentMove().getMoveType() == Move.MoveType.PHYSICAL)	{
							if(Debug.on) printDebug("["+name+"] reducing physical damage by half...");
							be.setInflictedDamage(be.getInflictedDamage() / 2);
						}
					}
				}
				@Override
				public void onTurnEnd(final BattleEngine be) {
					if(--count == 0) {
						if(be.getBattleTask() != null) {
							be.getBattleTask().sendB("|battle|The Entrench is no more effective!");
							be.getBattleTask().sendB(be.getConnection(be.getSide(pony)),"|rmpersistent|ally|"+name);
							be.getBattleTask().sendB(be.getConnection(be.getOppositeSide(pony)),"|rmpersistent|opp|"+name);
						}
					}
				}
			}
		};
	}

	@Override
	public boolean validConditions(final BattleEngine be) {
		for(BattleEvent evt : be.getBattleEvents()) 
			if(evt.getName().equals(name) && be.getSide(pony) == be.getSide(evt.getPony()))
				return false;
		return true;
	}
}
