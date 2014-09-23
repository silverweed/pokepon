//: pony/BigMacintosh.java

package pokepon.pony;

import pokepon.enums.*;

/**	BigMacintosh
 *	Very good defenses/HP and average attack;
 *	Lacks speed.
 *
 * @author silverweed
 */
public class BigMacintosh extends Pony {
	
	public BigMacintosh(int _level) {
		super(_level);
		
		name = "BigMacintosh";
		type[0] = Type.HONESTY;
		type[1] = Type.LOYALTY;

		race = Race.EARTHPONY;

		baseHp = 115;
		baseAtk = 80;
		baseDef = 108;
		baseSpatk = 37;
		baseSpdef = 85;
		baseSpeed = 35;

		/* Learnable Moves */
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Heavy Massage",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Wild Weed",1);
		learnableMoves.put("Entrench",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Applebuck",15);
		learnableMoves.put("Relax",30);

		possibleAbilities[0] = "Brute Force";
		possibleAbilities[1] = "Integrity";
		possibleAbilities[2] = "Self Confidence";
	}
}
