//: move/Daredevilry.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;
import pokepon.gui.animation.AnimPresets;

/**
 * Inflicts massive physical damage but lowers defenses afterwards. 
 *
 * @author silverweed
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

		animation = new java.util.HashMap<>(AnimPresets.get("ballistic-direct"));
		animation.put("sprite", "user");
		
		userDef = addEntry(-1, 1f);
		userSpdef = addEntry(-1, 1f);
	}
	
	public Daredevilry(Pony p) {
		this();
		pony = p;
	}
}
