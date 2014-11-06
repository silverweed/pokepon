//: pony/Gilda.java

package pokepon.pony;

import pokepon.enums.*;

/** Gilda
 * 	Good bulk and excellent Atk, Speed and SpD;
 *	lacks SpA.
 *
 * @author silverweed
 */
public class Gilda extends Pony {
	
	public Gilda(int _level) {
		super(_level);
		
		name = "Gilda";
		type[0] = Type.SHADOW;
		type[1] = Type.LOYALTY;
		
		race = Race.GRYPHON;
		sex = Sex.FEMALE;

		baseHp = 71;
		baseAtk = 109;
		baseDef = 75;
		baseSpatk = 42;
		baseSpdef = 94;
		baseSpeed = 99;
		
		/* learnableMoves ... */
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Crazy Stunt",1);
		learnableMoves.put("Practice",1);
		learnableMoves.put("Surprise Hit",1);
		learnableMoves.put("Sneak Attack",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Bully",1);
		learnableMoves.put("Charge",20);
		learnableMoves.put("Sky Dive",40);
		learnableMoves.put("Rampage",70);
		learnableMoves.put("Shadow Mist",80);

		/* possibleAbilities */
		possibleAbilities[0] = "Subjection";
		possibleAbilities[1] = "Aversion";
	}
}
