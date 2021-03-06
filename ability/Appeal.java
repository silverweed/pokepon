//: ability/Appeal.java

package pokepon.ability;

import pokepon.pony.*;
import pokepon.battle.*;
import pokepon.move.*;
import static pokepon.util.MessageManager.*;

/** Appeal
 * Negative statuses have a 50% chance to fail vs this pony.
 *
 * @author silverweed
 */

public class Appeal extends Ability {

	public Appeal() {
		super("Appeal");
		briefDesc = "This pony has 50% chance to avoid negative conditions.";	
	}
	
	@Override
	public float preventNegativeCondition(final String which) {
		return 0.5f;
	}
}
