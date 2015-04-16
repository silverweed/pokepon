//: pony/DoubleDiamond.java

package pokepon.pony;

import pokepon.enums.*;

/**	Double Diamond
 *	High Speed,
 *	good bulk.
 *
 * @author silverweed
 */
public class DoubleDiamond extends Pony {
	
	public DoubleDiamond(int _level) {
		super(_level);
		
		name = "Double Diamond";
		type[0] = Type.SPIRIT;
		type[1] = Type.KINDNESS; 

		race = Race.EARTHPONY;
		sex = Sex.MALE;

		baseHp = 72;
		baseAtk = 88;
		baseDef = 84;
		baseSpatk = 41;
		baseSpdef = 84;
		baseSpeed = 111;

		/* Learnable Moves */
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Stalking",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Practice",1);
		learnableMoves.put("Hopeful Strike",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Crazy Stunt",1);
	}
}
