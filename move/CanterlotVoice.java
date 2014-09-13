//: move/CanterlotVoice.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Move learnable only by Luna and Celestia;
 * Inflicts damage and may paralyze the target.
 *
 * @author silverweed
 */


public class CanterlotVoice extends Move {
	
	public CanterlotVoice() {
		super("Canterlot Voice");
		
		type = Type.PASSION;
		moveType = Move.MoveType.SPECIAL;
		maxpp = pp = 20;
		baseDamage = 75;
		accuracy = 95;
		priority = 0;
		description = "INFLICTS DAMAGE TO YOUR ENEMIES WITH A CHANCE TO PARALYZE THEM!!!";
		description = "30% TO PARALYZE THE ENEMY!";

		targetParalysis = 0.3f; 
	}

	public CanterlotVoice(Pony p) {
		this();
		pony = p;
	}
}
