//: move/Stampede.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.battle.*;
import pokepon.pony.Pony;

/**
 * Much like Dragon Tail.
 *
 * @author Giacomo Parolini
 */

public class Stampede extends Move {
	
	public Stampede() {
		super("Stampede");
		
		type = Type.SPIRIT;
		moveType = Move.MoveType.PHYSICAL;
		maxpp = pp = 15;
		baseDamage = 90;
		accuracy = 90;
		priority = -6;
		description = "A reckless attack that forces your opponent to switch out, but always hits last.";
		briefDesc = "Switches enemy out to a<br>random pony. Priority -6.";

		animation.put("name","Direct");
		animation.put("sprite","user");
		animation.put("bounceBack",true);
		animation.put("accelerated",true);
		animation.put("delay",20);
	
		// forces switch to random opp on damage
		forceTargetSwitch = 2;	
	}

	public Stampede(Pony p) {
		this();
		pony = p;
	}
}
