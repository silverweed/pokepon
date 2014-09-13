//: move/ShadowMist.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Special move which inflicts average damage and may raise evasion by 1;
 *
 * @author Giacomo Parolini
 */

public class ShadowMist extends Move {
	
	public ShadowMist() {
		super("Shadow Mist");
		
		type = Type.SHADOW;
		moveType = Move.MoveType.SPECIAL;
		maxpp = pp = 20;
		baseDamage = 90;
		accuracy = 100;
		priority = 0;
		description = "Magically summon a dark mist around your enemy to inflict great damage.";
		briefDesc = "Inflicts regular damage.";
	}

	public ShadowMist(Pony p) {
		this();
		pony = p;
	}
}
