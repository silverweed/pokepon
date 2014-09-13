//: move/PowerDisplay.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Inflicts massive special damage, but lowers spa and spd after use.
 *
 * @author Giacomo Parolini
 */

public class PowerDisplay extends Move {

	public PowerDisplay() {
		super("Power Display");
		type = Type.SHADOW;
		moveType = Move.MoveType.SPECIAL;
		maxpp = pp = 25;
		baseDamage = 120;
		accuracy = 100;
		priority = 0;
		description = "";
		briefDesc = "Lowers user's SpA and SpD by 1";

		animation.put("name","Fade");
		animation.put("transparent",true);
		animation.put("sprite","shadowball.png");
		animation.put("fadeOut",false);
		animation.put("initialPoint","opp -50Y");
		animation.put("finalPoint","opp +20Y");

		userSpatk = addEntry(-1, 1f);
		userSpdef = addEntry(-1, 1f);
	}

	public PowerDisplay(Pony p) {
		this();
		pony = p;
	}
}
