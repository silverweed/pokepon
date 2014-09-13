//: ability/Indifference.java

package pokepon.ability;

import pokepon.pony.*;
import pokepon.battle.*;
import static pokepon.util.MessageManager.*;

/** Indifference
 * Ignores stats negative boosts.
 *
 * @author silverweed
 */

public class Indifference extends Ability {

	public Indifference() {
		super("Indifference");
		briefDesc = "This pony cannot have its stats lowered.";
	}

	@Override
	public boolean ignoreNegativeBoosts(String what) {
		return true;
	}
}
