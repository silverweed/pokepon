//: ability/Ability.java

package pokepon.ability;

import pokepon.battle.*;
import pokepon.pony.*;
import static pokepon.util.MessageManager.*;

/** The base class for all Abilities; it is a child class from
 * EffectDealer, like Move and Item.
 *
 * @author silverweed
 */
public abstract class Ability extends TriggeredEffectDealer {

	public Ability() {
		super();
	}

	public Ability(String name) {
		super(name);
	}

	public Ability(String name,Pony p) {
		super(name,p);
	}

	public String toString() {
		return name;
	}
}
