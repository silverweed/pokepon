//: move/Trailblazer.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Inflicts massive special damage with higher chance for a critical hit
 *
 * @author Giacomo Parolini
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

		critical = 2;

		animation.put("name","Compound");
		animation.put("anims", java.util.Arrays.asList("Fade","Fade"));
		animation.put("sprite","user");
		animation.put("transparent",true);
		animation.put("fadeOut",true);
		animation.put("persistent",true);
		animation.put("1:iterations",40f);
		animation.put("1:delay",50);
		animation.put("1:accelerated",true);
		animation.put("1:initialPoint","ally");
		animation.put("1:finalPoint","opp b450X b450Y");
		animation.put("1:rewind",true);
		animation.put("2:initialPoint","opp -300X -300Y");
		animation.put("2:finalPoint","opp +300X +250Y");
		animation.put("2:rewindTo","ally");
	}
	
	public Trailblazer(Pony p) {
		this();
		pony = p;
	}
}
