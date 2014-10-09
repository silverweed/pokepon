//: pony/NightmareMoon.java

package pokepon.pony;

import pokepon.enums.*;

/** Nightmare Moon
 * 	excels in Special Attack, Special Defense and Defense;
 *	lacks physical Attack and has average speed.
 *
 * @author silverweed
 */

public class NightmareMoon extends Pony {
	
	public NightmareMoon(int _level) {
		super(_level);
		
		name = "Nightmare Moon";
		type[0] = Type.NIGHT;
		
		race = Race.ALICORN;
		sex = Sex.FEMALE;
		
		baseHp = 110;
		baseAtk = 90;
		baseDef = 90;
		baseSpatk = 145;
		baseSpdef = 120;
		baseSpeed = 95;
		
		/* learnableMoves ... */
		learnableMoves.put("Night Wind",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Scare Away",1);
		learnableMoves.put("Teleport Blast",1);
		learnableMoves.put("Dark Magic",1);
		learnableMoves.put("Surprise Hit",1);
		learnableMoves.put("Scheme Up",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Magic Blast",1);	
		learnableMoves.put("Shadow Mist",50);
		learnableMoves.put("Eternal Night",70);

		/* possibleAbilities */
		possibleAbilities[0] = "Subjection";
	}
}
