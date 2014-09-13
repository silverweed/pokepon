//: move/Enlighten.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Special move which inflicts good damage and cannot miss
 *
 * @author Giacomo Parolini
 */

public class Enlighten extends Move {
	
	public Enlighten() {
		super("Enlighten");
		
		type = Type.LIGHT;
		moveType = Move.MoveType.SPECIAL;
		maxpp = pp = 32;
		baseDamage = 80;
		accuracy = -1;
		priority = 0;
		description = "Cast a magic orb which illuminates around and never misses the enemy.";
		briefDesc = "No secondary effect.";

		animation.put("name","Ballistic");
		animation.put("sprite","electroball.png");
		animation.put("bounceBack",false);

		targetSpdef = addEntry(-1,0.1f);
	}

	public Enlighten(Pony p) {
		this();
		pony = p;
	}
}
