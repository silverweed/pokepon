//: pony/FleurDeLis.java

package pokepon.pony;

import pokepon.enums.*;

/**	FleurDeLis
 *	Good defenses and SpA, with viable Speed.
 *
 * @author silverweed
 */
public class FleurDeLis extends Pony {
	
	public FleurDeLis(int _level) {
		super(_level);
		
		name = "Fleur De Lis";
		type[0] = Type.GENEROSITY;
		type[1] = Type.LIGHT;

		race = Race.UNICORN;

		baseHp = 70; 
		baseAtk = 50;
		baseDef = 85;
		baseSpatk = 85;
		baseSpdef = 105;
		baseSpeed = 70;

		/* Learnable Moves */
		learnableMoves.put("Love Burst",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Freeze Spell",1);
		learnableMoves.put("Overture",1);
		learnableMoves.put("Enlighten",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Snuggle",1);
		learnableMoves.put("Bind Up",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Duck Face",19);
		learnableMoves.put("Gem Storm",45);

		possibleAbilities[0] = "Shining Coat";
		possibleAbilities[1] = "Lovebird";
	}
}
