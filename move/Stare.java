//: move/Stare.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/** Powerful move learnable only by Fluttershy
 *  Deals high damage and can Petrify the opponent.
 *
 * @author Giacomo Parolini
 */

public class Stare extends Move {
	
	public Stare() {
		super("Stare");

		type = Type.KINDNESS;
		moveType = Move.MoveType.SPECIAL;
		maxpp = pp = 5;
		baseDamage = 120;
		accuracy = 70;
		priority = 0;
		description = "Don't be a doormat! A terrible stare infuses fear into enemies and may petrify them.";
		briefDesc = "20% to petrify the target.";

		targetPetrify = 0.2f;	//20% prob. to petrify enemy.
	}
	
	public Stare(Pony p) {
		this();
		pony = p;
	}
}
