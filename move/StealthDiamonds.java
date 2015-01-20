//: move/StealthDiamonds.java

package pokepon.move;

import pokepon.move.*;
import pokepon.enums.*;
import pokepon.move.hazard.*;
import pokepon.pony.*;

/** Hazard move which spreads StealthDiamondsHazard through enemy field;
 * Works like Stealth Rock with type Generosity.
 *
 * @author Giacomo Parolini
 */
public class StealthDiamonds extends Move {

	public StealthDiamonds() {
		super("Stealth Diamonds");

		type = Type.GENEROSITY;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 10;
		accuracy = -1;
		priority = 0;
		description = "Spread magic diamonds on the enemy field. The diamonds damage entering ponies depending on their type.";
		briefDesc = "Damages enemies on switch-in;<br>based on Generosity.";

		animation.put("name","Ballistic");
		animation.put("sprite","diamond.png");
		animation.put("bounceBack",false);

		hazard = new StealthDiamondsHazard();				
	}
	
	public StealthDiamonds(Pony p) {
		this();
		pony = p;
	}

	@Override
	public String getPhrase() {
		return hazard.getPhrase();
	}
}


