//: ability/CowboyHat.java

package pokepon.item;

import pokepon.enums.*;
import static pokepon.util.MessageManager.*;

/** Cowboy Hat
 * Boosts Honesty moves by 20%
 *
 * @author silverweed
 */

public class CowboyHat extends Item {

	public CowboyHat() {
		super("Cowboy Hat");
		briefDesc = "Boosts Honesty-type moves by 20%.";
	}

	@Override
	public float changeDamageDealtBy(Type mt) {
		if(mt == Type.HONESTY) 
			return 1.2f;
		else
			return 1f;
	}
}
