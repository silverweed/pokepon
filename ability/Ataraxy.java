//: ability/Ataraxy.java

package pokepon.ability;

import pokepon.battle.*;

/** Ataraxy
 * Like Magic Guard
 *
 * @author Giacomo Parolini
 */

public class Ataraxy extends Ability {

	public Ataraxy() {
		super("Ataraxy");
		briefDesc = "This pony can only take damage from damaging moves.";
		
		ignoreSecondaryDamage = true;
	}
}
