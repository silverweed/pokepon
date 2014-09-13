//: move/WildWeed.java

package pokepon.move;

import pokepon.move.*;
import pokepon.enums.*;
import pokepon.move.hazard.*;
import pokepon.pony.*;

/** Hazard move which spreads WildWeedHazard through enemy field;
 * works like Leech Seed.
 *
 * @author silverweed
 */
public class WildWeed extends Move {

	public WildWeed() {
		super("Wild Weed");

		type = Type.HONESTY;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 16;
		accuracy = 90;
		priority = 0;
		description = "";
		briefDesc = "1/8 of target's HP is restored<br>to user every turn.";

		animation.put("name","Ballistic");
		animation.put("sprite","energyball.png");
		animation.put("bounceBack",false);

		hazard = new WildWeedHazard();				
	}
	
	public WildWeed(Pony p) {
		this();
		pony = p;
	}

	@Override
	public String getPhrase() {
		return hazard.getPhrase();
	}
}


