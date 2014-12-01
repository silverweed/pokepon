//: ability/PestResilience.java

package pokepon.ability;

import pokepon.pony.*;
import pokepon.battle.*;
import static pokepon.util.MessageManager.*;

/** PestResilience
 * Immunizes from poison and bad poison.
 *
 * @author Giacomo Parolini
 */

public class PestResilience extends Ability {

	public PestResilience() {
		super("Pest Resilience");
		briefDesc = "This pony cannot be poisoned.";
	}

	@Override
	public float preventNegativeCondition(final String which) {
		if(which.equals("psn") || which.equals("tox")) return 1f;
		return 0f;
	}
}
