//: ability/MysticMask.java

package pokepon.item;

import pokepon.enums.*;
import static pokepon.util.MessageManager.*;

/** MysticMask 
 * Boosts Spirit moves by 20%
 *
 * @author silverweed
 */

public class MysticMask extends Item {

	public MysticMask() {
		super("Mystic Mask");
		briefDesc = "Boosts Spirit-type moves by 20%.";
	}

	@Override
	public float changeDamageDealtBy(Type mt) {
		if(mt == Type.SPIRIT) 
			return 1.2f;
		else
			return 1f;
	}
}
