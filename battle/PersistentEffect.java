//: battle/PersistentEffect.java
package pokepon.battle;

/** A PersistentEffect is similar to a BattleEvent, because it's dealt with
 * by the BattleEngine rather than being associated to one pony, but 
 * it produces an effect affecting a specific side of the field.
 *
 * @author Giacomo Parolini
 * @deprecated Use BattleEvents instead.
 */
public class PersistentEffect extends TriggeredEffectDealer {
	
	public PersistentEffect(int duration) {
		super("Persistent Effect");
		count = this.duration = duration;
	}

	public PersistentEffect(int duration, String name) {
		super(name);
		count = this.duration = duration;
	}
	
	public int getSide() { return side; }
	public int getDuration() { return duration; }

	public void setSide(int i) { side = i; }
	public void setDuration(int i) { duration = i; }

	public String toString() {
		return name+" (side: "+side+", count: "+count+" / "+duration+")";
	}

	public String getEndPhrase() {
		return null;
	}

	public String getSprite() {
		return null;
	}
	
	public int count;

	private int duration;
	/** this gets set by the BattleEngine */
	private int side;
}

