//: move/BoulderBomb.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Not very accurate, but ensures enemy paralysis, along with a massive damage.
 *
 * @author silverweed
 */
public class BoulderBomb extends Move {
	
	public BoulderBomb() {
		super("Boulder Bomb");
		
		type = Type.HONESTY;
		moveType = Move.MoveType.PHYSICAL;
		maxpp = pp = 5;
		baseDamage = 120;
		accuracy = 50;
		priority = 0;
		description = "Throw a huge boulder to your enemy.";
		briefDesc = "100% to paralyze the user.";

		animation.put("name","Ballistic");
		animation.put("sprite","rock.png");
		animation.put("bounceBack",false);
		animation.put("horizOffset",10);
	
		targetParalysis = 1f;
	}

	public BoulderBomb(Pony p) {
		this();
		pony = p;
	}
}
