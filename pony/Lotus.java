//: pony/Lotus.java

package pokepon.pony;

import pokepon.enums.*;

/** Lotus
 * Great special wall with very good Atk; lacks a bit of HP.
 *
 * @author Giacomo Parolini
 */

public class Lotus extends Pony {
	
	public Lotus(int _level) {
		super(_level);
		
		name = "Lotus";
		type[0] = Type.GENEROSITY;
		type[1] = Type.LOVE;
		
		race = Race.EARTHPONY;

		baseHp = 60;
		baseAtk = 90;
		baseDef = 80;
		baseSpatk = 50;
		baseSpdef = 150;
		baseSpeed = 70;
		
		/* learnableMoves ... */
		learnableMoves.put("Spa Treatment",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Snuggle",1);
		learnableMoves.put("Bind Up",1);
		learnableMoves.put("Inner Focus",1);
		learnableMoves.put("Whine",1);
		learnableMoves.put("Love And Care",1);
		learnableMoves.put("Duck Face",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Relax",1);

		possibleAbilities[0] = "Ataraxy";
	}
}
