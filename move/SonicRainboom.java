//: move/SonicRainboom.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Rainbow Dash's signature move;
 * Massive physical damage with recoil; Priority +1
 * Can heal party's conditions.
 *
 * @author silverweed
 */

public class SonicRainboom extends Move {
	
	public SonicRainboom() {
		super("Sonic Rainboom");
		
		type = Type.LOYALTY;
		moveType = Move.MoveType.PHYSICAL;
		maxpp = pp = 5;
		baseDamage = 120;
		accuracy = 80;
		priority = 1;
		description = "Inflicts massive damage to enemy but take damage from recoil. Usually go first.";
		briefDesc = "Has 33% recoil.";
	
		animation.put("name","Direct");
		animation.put("sprite","user");
		animation.put("passThrough",true);

		recoil = 0.33f;
		healAllTeamStatus = 0.2f;		
	}
	
	public SonicRainboom(Pony p) {
		this();
		pony = p;
	}
}
