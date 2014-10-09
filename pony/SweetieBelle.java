//: pony/SweetieBelle.java

package pokepon.pony;

import pokepon.enums.*;

/**	SweetieBelle
 *	Good HP, defense and speed; Average attack;
 *	Poor defenses and special attack.
 *
 * @author 
 */

public class SweetieBelle extends Pony {
	
	public SweetieBelle(int _level) {
		super(_level);
		
		name = "Sweetie Belle";
		type[0] = Type.GENEROSITY;
		type[1] = Type.MUSIC;

		race = Race.UNICORN;
		sex = Sex.FEMALE;

		baseHp = 100;
		baseAtk = 62;
		baseDef = 94;
		baseSpatk = 74;
		baseSpdef = 55;
		baseSpeed = 100;

		/* Learnable Moves */
		learnableMoves.put("Dissonance",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Get Hype",1);
		learnableMoves.put("Nap",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Treat",1);
		learnableMoves.put("Snuggle",1);
		learnableMoves.put("Burning Powder",1);
		learnableMoves.put("Relay Race",1);
		learnableMoves.put("Scheme Up",1);
		learnableMoves.put("Bind Up",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Duck Face",20);
		learnableMoves.put("Stealth Diamonds",28);
	}
}
