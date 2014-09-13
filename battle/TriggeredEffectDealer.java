//: battle/TriggeredEffectDealer.java

package pokepon.battle;

import pokepon.pony.Pony;

/** An EffectDealer which has methods to respond to 'triggers'
 *
 * @author Giacomo Parolini
 */

public abstract class TriggeredEffectDealer extends EffectDealer {

	public TriggeredEffectDealer() {
		super();
	}

	public TriggeredEffectDealer(String name) {
		super(name);
	}

	public TriggeredEffectDealer(String name,Pony p) {
		super(name,p);
	}

	// Triggers, in order of activation //
	
	/** This is triggered "before" starting the turn (i.e before priority is calculated, etc) */
	public void beforeTurnStart(final BattleEngine be) {}

	/** This is triggered right *before* the switch out (so the active pony is not changed yet) */
	public void onSwitchOut(final BattleEngine be) {}

	/** This is triggered right after a switch-in (AP already changed, but opponent pony may still switch). */
	public void onSwitchIn(final BattleEngine be) {}

	/** This is triggered after both ponies have switched in, but before moves usage */
	public void afterSwitchIn(final BattleEngine be) {}

	/** This is like afterSwitchIn, but gets triggered each turn, even if no switch happened. */
	public void onTurnStart(final BattleEngine be) {}

	/** This is triggered after the move decision, but before its usage 
	 * (this gets called for both sides on each move usage)
	 */
	public void onMoveUsage(final BattleEngine be) {}

	/** This is triggered right before damage calculation, after move rolled dice for hit */
	public void beforeMoveHit(final BattleEngine be) {}

	/** This is triggered _after_ damage calculation, but _before_ applying the damage (only for the defender) */
	public void onDamage(final BattleEngine be) {}

	/** This is triggered right after damage application, if move hit */
	public void afterMoveHit(final BattleEngine be) {}

	/** This is triggered after the move usage (this gets called for both sides after each move usage) */
	public void afterMoveUsage(final BattleEngine be) {}
	
	/** This is triggered right before ending the turn. */
	public void onTurnEnd(final BattleEngine be) {}

}
