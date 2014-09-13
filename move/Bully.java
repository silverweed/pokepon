//: move/Bully.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;
import pokepon.battle.*;
import pokepon.gui.animation.*;

/**
 * Like Frustration: stronger when happiness is low
 * (but inflicts more damage: division is by 2, not 2.5 as
 * Frustration).
 *
 * @author silverweed
 */

public class Bully extends Move {
	
	public Bully() {
		super("Bully");
		
		type = Type.SHADOW;
		moveType = Move.MoveType.PHYSICAL;
		baseDamage = 0;
		maxpp = pp = 25;
		accuracy = 100;
		priority = 0;
		description = "Bully your enemy. The lower the happiness of the user, the higher the damage inflicted.";
		briefDesc = "Inficts more damage if the user's<br>happiness is low.";

		animation.put("name","Whirl");
		animation.put("sprite","user");
	}

	public Bully(Pony p) {
		this();
		pony = p;
	}

	@Override
	public boolean validConditions(final BattleEngine be) {
		if(pony == null) throw new NullPointerException("pony is null for move "+this);
		baseDamage = Math.max(1,(int)((255 - pony.getHappiness()) / 2.5));
		return true;
	}
}
