//: ability/ShiningCoat.java

package pokepon.ability;

import pokepon.battle.*;

/** ShiningCoat
 * Like Battle Armor
 *
 * @author silverweed
 */

public class ShiningCoat extends Ability {

	public ShiningCoat() {
		super("Shining Coat");
		briefDesc = "Prevents critical hits.";

		ignoreCriticalHits = true;
	}
}
