//: ability/DiamondRing.java

package pokepon.item;

import pokepon.enums.*;
import static pokepon.util.MessageManager.*;

/** Diamond Ring 
 * Boosts Love moves by 20%
 *
 * @author silverweed
 */

public class DiamondRing extends Item {

	public DiamondRing() {
		super("Diamond Ring");
		briefDesc = "Boosts Love-type moves by 20%.";
	}

	@Override
	public float changeDamageDealtBy(Type mt) {
		if(mt == Type.LOVE) 
			return 1.2f;
		else
			return 1f;
	}
}
