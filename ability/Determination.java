//: ability/Determination.java

package pokepon.ability;

import pokepon.battle.*;
import pokepon.util.Debug;
import static pokepon.util.MessageManager.*;

/** Prevents from flinching
 *
 * @author Giacomo Parolini
 */

public class Determination extends Ability {

	public Determination() {
		super("Determination");
		briefDesc = "Prevents this pony from flinching.";
	}

	@Override
	public float preventNegativeCondition(final String which) {
		if(which.equals("flinch")) return 1f;
		return 0f;
	}
}
