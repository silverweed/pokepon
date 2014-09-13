//: ability/DragonScales.java

package pokepon.ability;

import pokepon.pony.*;
import pokepon.battle.*;
import pokepon.move.*;
import static pokepon.util.MessageManager.*;

/** DragonScales
 * Spike's ability; halves the damage coming from all special attacks.
 *
 * @author Giacomo Parolini
 */

public class DragonScales extends Ability {

	public DragonScales() {
		super("Dragon Scales");
		briefDesc = "Halves all special damage.";
	}

	@Override
	public float changeDamageTakenFrom(Move.MoveType mt) {
		if(mt == Move.MoveType.SPECIAL) 
			return 0.5f;
		else
			return 1f;
	}
}
