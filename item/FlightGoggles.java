//: ability/FlightGoggles.java

package pokepon.item;

import pokepon.enums.*;
import static pokepon.util.MessageManager.*;

/** Flight Goggles 
 * Boosts Loyalty moves by 20%
 *
 * @author silverweed
 */

public class FlightGoggles extends Item {

	public FlightGoggles() {
		super("Flight Goggles");
		briefDesc = "Boosts Loyalty-type moves by 20%.";
	}

	@Override
	public float changeDamageDealtBy(Type mt) {
		if(mt == Type.LOYALTY) 
			return 1.2f;
		else
			return 1f;
	}
}
