//: move/SlimyTrap.java

package pokepon.move;

import pokepon.move.*;
import pokepon.enums.*;
import pokepon.move.hazard.*;
import pokepon.pony.*;

/** Hazard move which spreads SlimyTrapHazard through enemy field;
 * Like StickyWeb (doesn't affect Pegasi and Gryphons)
 *
 * @author silverweed
 */
public class SlimyTrap extends Move {

	public SlimyTrap() {
		super("Slimy Trap");

		type = Type.MAGIC;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 10;
		accuracy = -1;
		priority = 0;
		description = "Create a magical trap which entangles the enemy and slows it down.";
		briefDesc = "Enemy have -1 Spe on switch-in.<br>Pegasi and Gryphons are immune.";

		animation.put("name","Ballistic");
		animation.put("sprite","slimytrap.png");
		animation.put("bounceBack",false);

		hazard = new SlimyTrapHazard();				
	}
	
	public SlimyTrap(Pony p) {
		this();
		pony = p;
	}

	@Override
	public String getPhrase() {
		return hazard.getPhrase();
	}
}


