//: move/SonicBarrier.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;
import pokepon.battle.*;
import pokepon.move.*;
import pokepon.util.Debug;
import static pokepon.util.MessageManager.*;
import static java.util.AbstractMap.SimpleEntry;

/**
 * Like Light Screen 
 * 
 * @author silverweed
 */

public class SonicBarrier extends Move {
	
	public SonicBarrier() {
		super("Sonic Barrier");
		
		type = Type.MUSIC;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 32;
		accuracy = -1;
		priority = 0;
		description = "An invisible wall of sound halves all special damage for 5 turns.";
		briefDesc = "Halves incoming special<br>damage for 5 turns.";
	}
	
	public SonicBarrier(Pony p) {
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
						be.getBattleTask().sendB("|battle|A Sonic Barrier was conjured before the team!");
						be.getBattleTask().sendB(be.getConnection(be.getSide(source)),"|persistent|ally|"+name);
						be.getBattleTask().sendB(be.getConnection(be.getOppositeSide(source)),"|persistent|opp|"+name);
					}
				}
				@Override
				public void onDamage(final BattleEngine be) {
					if(Debug.on) printDebug("["+name+"] called onDamage("+be.getCurrentMove()+")");
					if(be.currentPlayer() != be.getSide(source)) {
						if(be.getCurrentMove().getMoveType() == Move.MoveType.SPECIAL)	{
							if(Debug.on) printDebug("["+name+"] reducing special damage by half...");
							be.setInflictedDamage(be.getLatestInflictedDamage() / 2);
						}
					}
				}
				@Override
				public void onTurnEnd(final BattleEngine be) {
					if(--count == 0) {
						if(be.getBattleTask() != null) {
							be.getBattleTask().sendB("|battle|The Sonic Barrier disappeared!");
							be.getBattleTask().sendB(be.getConnection(be.getSide(source)),"|rmpersistent|ally|"+name);
							be.getBattleTask().sendB(be.getConnection(be.getOppositeSide(source)),"|rmpersistent|opp|"+name);
						}
					}
				}
			}
		};
	}
	/*@Override
	public SimpleEntry<String,PersistentEffect> spawnPersistentEffect() {

		return new SimpleEntry<String,PersistentEffect>(
			"ally",
	*///		new PersistentEffect(5 /* + pony.getItem().getName().equals("Light Clay") ? 3 : 0 */, "Sonic Barrier") {
	/*			@Override
				public float changeDamageTakenFrom(Move.MoveType mt) {
					if(Debug.on) printDebug("["+name+"] movetype = "+mt);
					return mt == Move.MoveType.SPECIAL ? 0.5f : 1f;
				}
				@Override
				public String getPhrase() {
					return "A Sonic Barrier was conjured before the team!";
				}
				@Override
				public String getEndPhrase() {
					return "The Sonic Barrier disappeared!";
				}
			}
		);
	}*/

	@Override
	public boolean validConditions(final BattleEngine be) {
		/*for(PersistentEffect pe : be.getPersistentEffects()) {
			if(pe.getSide() == be.getSide(pony) && pe.getName().equals(name))
				return false;
		}*/
		for(BattleEvent evt : be.getBattleEvents()) 
			if(evt.getName().equals(name) && be.getSide(pony) == be.getSide(evt.getSource()))
				return false;
		return true;
	}
}
