//: ability/GoldenLyre.java

package pokepon.item;

import pokepon.enums.*;
import static pokepon.util.MessageManager.*;

/** Golden Lyre 
 * Boosts Music moves by 20%
 *
 * @author Giacomo Parolini
 */

public class GoldenLyre extends Item {

	public GoldenLyre() {
		super("Golden Lyre");
		briefDesc = "Boosts Music-type moves by 20%.";
	}

	@Override
	public float changeDamageDealtBy(Type mt) {
		if(mt == Type.MUSIC) 
			return 1.2f;
		else
			return 1f;
	}
}
