//: ability/MagicTiara.java

package pokepon.item;

import pokepon.enums.*;
import static pokepon.util.MessageManager.*;

/** Magic Tiara
 * Boosts Magic moves by 20%
 *
 * @author Giacomo Parolini
 */

public class MagicTiara extends Item {

	public MagicTiara() {
		super("Magic Tiara");
		briefDesc = "Boosts Magic-type moves by 20%.";
	}

	@Override
	public float changeDamageDealtBy(Type mt) {
		if(mt == Type.MAGIC) 
			return 1.2f;
		else
			return 1f;
	}
}
