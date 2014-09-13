//: pony/DaringDo.java

package pokepon.pony;

import pokepon.enums.*;

/**	Daring Do
 *	Excellent Atk and high Def and HP; lacks SpA and Spe
 *
 * @author silverweed
 */
public class DaringDo extends Pony {
	
	public DaringDo(int _level) {
		super(_level);
		
		name = "Daring Do";
		type[0] = Type.LIGHT;
		type[1] = Type.PASSION;

		race = Race.PEGASUS;

		baseHp = 90;
		baseAtk = 150;
		baseDef = 106;
		baseSpatk = 20;
		baseSpdef = 74;
		baseSpeed = 60;

		/* Learnable Moves */
		learnableMoves.put("Hind Kick",35);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Speed Up",40);
		learnableMoves.put("Rock Crush",42);
		learnableMoves.put("Charge",51);
		learnableMoves.put("Martial Arts",67);

		possibleAbilities[0] = "Brute Force";
		possibleAbilities[1] = "Determination";
	}
}
