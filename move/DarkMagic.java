//: move/DarkMagic.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Special move which inflicts good damage and may paralyze enemy.
 *
 * @author Giacomo Parolini
 */

public class DarkMagic extends Move {
	
	public DarkMagic() {
		super("Dark Magic");
		
		type = Type.SHADOW;
		moveType = Move.MoveType.SPECIAL;
		maxpp = pp = 20;
		baseDamage = 85;
		accuracy = 95;
		priority = 0;
		description = "Use evil magic to numb your enemy's mind.";
		briefDesc = "30% to paralyze target.";

		animation.put("name","Direct");
		animation.put("sprite","shadowball.png");
		animation.put("bounceBack",false);

		targetParalysis = 0.3f;
		beamMove = true;
	}

	public DarkMagic(Pony p) {
		this();
		pony = p;
	}
}
