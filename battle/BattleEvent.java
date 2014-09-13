//: battle/BattleEvent.java

package pokepon.battle;

import pokepon.pony.*;

/** The TriggeredEffectDealers handled by the BattleEngine; these Events typically
 * have a duration or a delay after which they disappear, and can be triggered
 * like any TriggeredEffectDealer; the difference with normal TEDs is that a BattleEvent
 * is not linked with a particular pony, but is a 'global' effect, which normally acts
 * independently from its source.
 *
 * @author Giacomo Parolini
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

	public BattleEvent(int delay, String name, Pony p) {
		super(name, p);
		count = this.delay = delay;
	}

	public void delayInit(final BattleEngine be) {}

	public void setSource(Pony p) {
		source = p;
	}

	public Pony getSource() {
		return source;
	}

	public final int getCount() {
		return count;
	}

	public boolean survive() {
		return willSurvive;
	}

	public String toString() {
		if(source == null) throw new NullPointerException("[BattleEvent] source is null for "+name);
		return name + " (source = "+source.getName()+", count = "+count+" / "+delay+", willSurvive = "+willSurvive+")";
	}
	
	protected Pony source;
	protected boolean willSurvive;
	protected final int delay;
	protected int count;
}
