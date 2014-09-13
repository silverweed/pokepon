//: move/Telekinesis.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.player.*;
import pokepon.pony.*;
import pokepon.battle.*;
import pokepon.gui.animation.*;

/**
 * Status move which reduces enemy's SpD and Spe by 1.
 *
 * @author silverweed
 */

public class Telekinesis extends Move {
	
	public Telekinesis() {
		super("Telekinesis");
		
		type = Type.MAGIC;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 10;
		accuracy = -1;
		priority = 1;
		description = "Use your magic power to control your enemy's body and reduce its Special Defense and Speed.";
		briefDesc = "Lowers target's SpD and Spe by 1.";

		animation.put("name","Shake");
		animation.put("sprite","target");
		animation.put("delay",8);
		animation.put("shakes",4);

		targetSpeed = addEntry(-1,1f);
		targetSpdef = addEntry(-1,1f);
	}
	
	public Telekinesis(Pony p) {
		this();
		pony = p;
	}

	@Override
	public boolean validConditions(final BattleEngine be) {
		return be.getDefender().speedMod() > -6 && be.getDefender().spdefMod() > -6;
	}
}
