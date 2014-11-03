//: ability/Scooter.java

package pokepon.item;

import pokepon.enums.*;
import static pokepon.util.MessageManager.*;

/** Scooter 
 * Boosts Passion moves by 20%
 *
 * @author Giacomo Parolini
 */

public class Scooter extends Item {

	public Scooter() {
		super("Scooter");
		briefDesc = "Boosts Passion-type moves by 20%.";
	}

	@Override
	public float changeDamageDealtBy(Type mt) {
		if(mt == Type.PASSION) 
			return 1.2f;
		else
			return 1f;
	}
}
