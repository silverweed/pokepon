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
						be.getBattleTask().sendB(be.getConnection(be.getSide(source)),"|persistent|ally|"+name);
						be.getBattleTask().sendB(be.getConnection(be.getOppositeSide(source)),"|persistent|opp|"+name);
					}
				}
				@Override
				public void onDamage(final BattleEngine be) {
					if(Debug.on) printDebug("["+name+"] called onDamage("+be.getCurrentMove()+")");
					if(be.currentPlayer() != be.getSide(source)) {
						if(be.getCurrentMove().getMoveType() == Move.MoveType.PHYSICAL)	{
							if(Debug.on) printDebug("["+name+"] reducing physical damage by half...");
							be.setInflictedDamage(be.getLatestInflictedDamage() / 2);
						}
					}
				}
				@Override
				public void onTurnEnd(final BattleEngine be) {
					if(--count == 0) {
						if(be.getBattleTask() != null) {
							be.getBattleTask().sendB("|battle|The Entrench is no more effective!");
							be.getBattleTask().sendB(be.getConnection(be.getSide(source)),"|rmpersistent|ally|"+name);
							be.getBattleTask().sendB(be.getConnection(be.getOppositeSide(source)),"|rmpersistent|opp|"+name);
						}
					}
				}
			}
		};
	}
	/*public SimpleEntry<String,PersistentEffect> spawnPersistentEffect() {
		return new SimpleEntry<String,PersistentEffect>(
			"ally",
	*///		new PersistentEffect(5 /* + pony.getItem().getName().equals("Light Clay") ? 3 : 0 */, "Entrench") {
	/*			@Override
				public float changeDamageTakenFrom(Move.MoveType mt) {
					if(Debug.on) printDebug("["+name+"] movetype = "+mt);
					return mt == Move.MoveType.PHYSICAL ? 0.5f : 1f;
				}
				@Override
				public String getPhrase() {
					return "An Entrench was raised before the team!";
				}
				@Override
				public String getEndPhrase() {
					return "The Entrench is no more effective!";
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
