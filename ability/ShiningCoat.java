//: ability/ShiningCoat.java

package pokepon.ability;

import pokepon.battle.*;

/** ShiningCoat
 * Like Battle Armor
 *
 * @author Giacomo Parolini
 */

public class ShiningCoat extends Ability {

	public ShiningCoat() {
		super("Shining Coat");
		briefDesc = "Prevents critical hits.";

		ignoreCriticalHits = true;
	}
}
