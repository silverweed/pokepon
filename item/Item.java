//: item/Item.java

package pokepon.item;

import pokepon.battle.*;
import pokepon.pony.Pony;
import static pokepon.util.MessageManager.*;

/** The base class for Items; it is a child class of EffectDealer
 * like Move and Ability.
 *
 * @author silverweed
 */

public abstract class Item extends TriggeredEffectDealer {

	public Item() {
		super();
	}

	public Item(String name) {
		super(name);
	}

	public Item(String name, Pony pony) {
		super(name,pony);
	}

	@Override
	public String toString() {
		return name;
	}
}
