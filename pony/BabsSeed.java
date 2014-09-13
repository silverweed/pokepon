//: pony/BabsSeed.java

package pokepon.pony;

import pokepon.enums.*;

/**	Babs Seed
 *	Good defensive stats and Atk; above-average HP but
 *	low spatk and below-average speed.
 *
 * @author Giacomo Parolini
 */
public class BabsSeed extends Pony {
	
	public BabsSeed(int _level) {
		super(_level);
		
		name = "Babs Seed";
		type[0] = Type.PASSION;
		type[1] = Type.SHADOW;

		race = Race.EARTHPONY;

		baseHp = 87;
		baseAtk = 100;
		baseDef = 90;
		baseSpatk = 45;
		baseSpdef = 93;
		baseSpeed = 70;

		/* Learnable Moves */
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Entrench",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Power Display",1);
		learnableMoves.put("Burning Powder",1);
		learnableMoves.put("Surprise Hit",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Bully",10);
		learnableMoves.put("Applebuck",40);
	}
}
