//: move/NastyPlot.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Special move which inflicts average damage and may raise evasion by 1;
 *
 * @author Giacomo Parolini
 */

public class NastyPlot extends Move {
	
	public NastyPlot() {
		super("Nasty Plot");
		
		type = Type.SHADOW;
		moveType = Move.MoveType.PHYSICAL;
		maxpp = pp = 10;
		baseDamage = 100;
		accuracy = 90;
		priority = 0;
		description = "(see what I did here?) A shifty back-stabbing that deals massive damage and may raise evasion.";
		briefDesc = "20% to raise user Evasion by 1.";

		userEvasion = addEntry(1,0.2f);
	}

	public NastyPlot(Pony p) {
		this();
		pony = p;
	}
}
