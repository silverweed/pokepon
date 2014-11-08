//: move/Daredevilry.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Inflicts massive physical damage but lowers defenses afterwards. 
 *
 * @author Giacomo Parolini
 */

public class Daredevilry extends Move {
	
	public Daredevilry() {
		super("Daredevilry");
		
		type = Type.SPIRIT;
		moveType = Move.MoveType.PHYSICAL;
		contactMove = true;
		maxpp = pp = 15;
		baseDamage = 120;
		accuracy = 100;
		priority = 0;
		description = "";
		briefDesc = "Lowers user Def and SpD by 1.";

		animation.put("anims", java.util.Arrays.asList("Ballistic2", "Direct"));
		animation.put("name","Compound");
		animation.put("sprite", "user");
		animation.put("delay", 30);
		animation.put("2:passThrough", true);
		
		userDef = addEntry(-1, 1f);
		userSpdef = addEntry(-1, 1f);
	}
	
	public Daredevilry(Pony p) {
		this();
		pony = p;
	}
}
