//: pony/DiamondTiara.java

package pokepon.pony;

import pokepon.enums.*;

/**	Diamond Tiara
 *	Excels in SpAtk but is otherwise quite feeble.
 *	
 *
 * @author Tommaso Parolini
 */
public class DiamondTiara extends Pony {
	
	public DiamondTiara(int _level) {
		super(_level);
		
		name = "Diamond Tiara";
		type[0] = Type.LAUGHTER; 
		type[1] = Type.SHADOW;

		race = Race.EARTHPONY;

		baseHp = 60;
		baseAtk = 65;
		baseDef = 80;
		baseSpatk = 98;
		baseSpdef = 80;
		baseSpeed = 67;

		/* Learnable Moves */
		learnableMoves.put("Bully",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Meditation",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Scare Away",1);
		learnableMoves.put("Taunt",1);
		learnableMoves.put("Sneak Attack",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Whine",20);
		learnableMoves.put("Stealth Diamonds",35);

		/* possibleAbilities */
		possibleAbilities[0] = "Subjection";
	}
}
