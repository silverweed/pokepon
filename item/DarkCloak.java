//: ability/DarkCloak.java

package pokepon.item;

import pokepon.enums.*;
import static pokepon.util.MessageManager.*;

/** DarkCloak 
 * Boosts Shadow moves by 20%
 *
 * @author silverweed
 */

public class DarkCloak extends Item {

	public DarkCloak() {
		super("Dark Cloak");
		briefDesc = "Boosts Shadow-type moves by 20%.";
	}

	@Override
	public float changeDamageDealtBy(Type mt) {
		if(mt == Type.SHADOW) 
			return 1.2f;
		else
			return 1f;
	}
}
