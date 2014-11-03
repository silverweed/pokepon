//: ability/PartyHat.java

package pokepon.item;

import pokepon.enums.*;
import static pokepon.util.MessageManager.*;

/** Party Hat 
 * Boosts Laughter moves by 20%
 *
 * @author silverweed
 */

public class PartyHat extends Item {

	public PartyHat() {
		super("Party Hat");
		briefDesc = "Boosts Laughter-type moves by 20%.";
	}

	@Override
	public float changeDamageDealtBy(Type mt) {
		if(mt == Type.LAUGHTER) 
			return 1.2f;
		else
			return 1f;
	}
}
