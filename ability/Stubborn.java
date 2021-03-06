//: ability/Stubborn.java

package pokepon.ability;

import pokepon.pony.*;
import pokepon.battle.*;
import pokepon.move.*;
import static pokepon.util.MessageManager.*;

/** Stubborn
 * Prevents to be KO-ed in one hit.
 *
 * @author silverweed
 */

public class Stubborn extends Ability {

	public Stubborn() {
		super("Stubborn");
		briefDesc = "Prevents one-hit KO.";
		preventsUserOHKO = true;
	}

	@Override
	public String getPhrase() {
		return "[pony] resisted thanks to its stubborness!";
	}
}
