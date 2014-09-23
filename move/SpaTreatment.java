//: move/SpaTreatment.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;
import pokepon.battle.*;
import pokepon.gui.animation.*;

/**
 * Heals team's negative conditions.
 *
 * @author silverweed
 */

public class SpaTreatment extends Move {
	
	public SpaTreatment() {
		super("Spa Treatment");
		
		type = Type.LIGHT;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 25;
		accuracy = -1;
		priority = 0;
		description = "Nothing better than a day at Spa to heal bad conditions! Heals all team status.";
		briefDesc = "Heals team's negative statuses.";

		animation.put("name","Shake");
		animation.put("sprite","user");

		healAllTeamStatus = 1f;
	}

	public SpaTreatment(Pony p) {
		this();
		pony = p;
	}
}
