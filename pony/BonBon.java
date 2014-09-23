//: pony/BonBon.java

package pokepon.pony;

import pokepon.enums.*;

/** Bon Bon
 *	Balanced average stats.
 *
 * @author Giacomo Parolini
 */

public class BonBon extends Pony {
	
	public BonBon(int _level) {
		super(_level);
		
		name = "Bon Bon";
		type[0] = Type.LOYALTY;
		type[1] = Type.SPIRIT;
		
		race = Race.EARTHPONY;

		baseHp = 70;
		baseAtk = 70;
		baseDef = 70;
		baseSpatk = 70;
		baseSpdef = 70;
		baseSpeed = 70;
		
		/* learnableMoves ... */
		learnableMoves.put("Spa Treatment",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Get Hype",1);
		learnableMoves.put("Stalking",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Raging Spree",1);
		learnableMoves.put("Glomp",1);
		learnableMoves.put("Snuggle",1);
		learnableMoves.put("Burning Powder",1);
		learnableMoves.put("Bubble Burst",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Tackle",1);

		possibleAbilities[0] = "Self Confidence";
	}

}
