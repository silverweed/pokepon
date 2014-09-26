//: move/NightWind.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Special move which inflicts good damage and may drop opponent's SpD.
 *
 * @author Giacomo Parolini
 */

public class NightWind extends Move {
	
	public NightWind() {
		super("Night Wind");
		
		type = Type.NIGHT;
		moveType = Move.MoveType.SPECIAL;
		maxpp = pp = 20;
		baseDamage = 80;
		accuracy = 95;
		priority = 0;
		description = "A cold dark wind strikes your opponent. This may reduce enemy's Special Defense.";
		briefDesc = "20% to lower target SpD.";

		animation.put("name","Direct");
		animation.put("sprite","shadowball.png");
		animation.put("passThrough",true);

		targetSpdef = addEntry(-1,0.2f);
	}

	public NightWind(Pony p) {
		this();
		pony = p;
	}
}
