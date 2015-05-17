//: battle/BattleEvent.java

package pokepon.battle;

import pokepon.pony.*;

/** The TriggeredEffectDealers handled by the BattleEngine; these Events typically
 * have a duration or a delay after which they disappear, and can be triggered
 * like any TriggeredEffectDealer; the difference with normal TEDs is that a BattleEvent
 * is not linked with a particular pony, but is a 'global' effect, which normally acts
 * independently from its source;
 * BattleEvents can be used in several ways, e.g to spawn a delayed effect
 * or to setup a persistent effect which lasts N turns before disappearing.
 *
 * @author silverweed
 */
public class BattleEvent extends TriggeredEffectDealer {

	public BattleEvent(int delay) {
		super();
		count = this.delay = delay;
	}

	public BattleEvent(int delay, String name) {
		super(name);
		count = this.delay = delay;
	}

	public BattleEvent(int delay, String name, Pony source) {
		super(name, source);
		count = this.delay = delay;
	}

	public void delayInit(final BattleEngine be) {}

	public final int getCount() {
		return count;
	}

	public boolean survive() {
		return willSurvive;
	}

	public String toString() {
		if(pony == null) throw new NullPointerException("[BattleEvent] source is null for "+name);
		return name + " (source = "+pony.getName()+", count = "+count+" / "+delay+", willSurvive = "+willSurvive+")";
	}
	
	protected boolean willSurvive;
	protected final int delay;
	protected int count;
}
