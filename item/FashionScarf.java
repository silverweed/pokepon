//: ability/FashionScarf.java

package pokepon.item;

import pokepon.enums.*;
import static pokepon.util.MessageManager.*;

/** Fashion Scarf 
 * Boosts Generosity moves by 20%
 *
 * @author Giacomo Parolini
 */

public class FashionScarf extends Item {

	public FashionScarf() {
		super("Fashion Scarf");
		briefDesc = "Boosts Generosity-type moves by 20%.";
	}

	@Override
	public float changeDamageDealtBy(Type mt) {
		if(mt == Type.GENEROSITY) 
			return 1.2f;
		else
			return 1f;
	}
}
