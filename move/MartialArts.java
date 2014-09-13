//: move/MartialArts.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Inflicts good physical damage and may flinch the target,
 *
 * @author Giacomo Parolini
 */

public class MartialArts extends Move {
	
	public MartialArts() {
		super("Martial Arts");
		
		type = Type.SPIRIT;
		moveType = Move.MoveType.PHYSICAL;
		maxpp = pp = 15;
		baseDamage = 90;
		accuracy = 100;
		priority = 0;
		description = "Concentrate your spiritual energies into a powerful kick.";
		briefDesc = "10% to flinch the target.";

		animation.put("name","Ballistic");
		animation.put("sprite","user");
		
		targetFlinch = 0.1f;
	}
	
	public MartialArts(Pony p) {
		this();
		pony = p;
	}
}
