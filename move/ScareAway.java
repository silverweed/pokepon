//: move/ScareAway.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.battle.*;
import pokepon.pony.Pony;

/**
 * Much like Dragon Tail.
 *
 * @author silverweed
 */

public class ScareAway extends Move {
	
	public ScareAway() {
		super("Scare Away");
		
		type = Type.NIGHT;
		moveType = Move.MoveType.SPECIAL;
		maxpp = pp = 15;
		baseDamage = 60;
		accuracy = 90;
		priority = -5;
		description = "Scare you enemy making him or her run away.";
		briefDesc = "Switches enemy out to a<br>random pony. Priority -5.";

		animation.put("name","Shake");
		animation.put("sprite","user");
		animation.put("delay",20);

		forceTargetSwitch = 2;	
	}

	public ScareAway(Pony p) {
		this();
		pony = p;
	}
}
