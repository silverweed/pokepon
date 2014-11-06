//: move/KineticStrike.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Special move which inflicts massive damage and may also flinch target.
 *
 * @author Giacomo Parolini
 */

public class KineticStrike extends Move {
	
	public KineticStrike() {
		super("Kinetic Strike");
		
		type = Type.MAGIC;
		moveType = Move.MoveType.SPECIAL;
		maxpp = pp = 10;
		baseDamage = 110;
		accuracy = 80;
		priority = 0;
		description = "Throw a massive object against your enemy using telekinesis. May make the target flinch.";
		briefDesc = "15% to flinch target.";

		targetFlinch = 0.15f;

		animation.put("name","Ballistic");
		animation.put("bounceBack",false);
		animation.put("sprite","shadowball.png");
	}
	
	public KineticStrike(Pony p) {
		this();
		pony = p;
	}
}
