//: move/Trailblazer.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Inflicts massive special damage with higher chance for a critical hit
 *
 * @author silverweed
 */

public class Trailblazer extends Move {
	
	public Trailblazer() {
		super("Trailblazer");
		
		type = Type.LOYALTY;
		moveType = Move.MoveType.SPECIAL;
		maxpp = pp = 8;
		baseDamage = 100;
		accuracy = 90;
		priority = 0;
		description = "";
		briefDesc = "Higher chance for critical hit";

		animation.put("name","Direct");
		animation.put("sprite","user");
		animation.put("passThrough",true);

		critical = 2;
	}
	
	public Trailblazer(Pony p) {
		this();
		pony = p;
	}
}
