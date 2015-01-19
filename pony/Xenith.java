//: pony/Xenith.java

package pokepon.pony;

import pokepon.enums.*;
import pokepon.util.Meta;
import static pokepon.util.Meta.*;
import java.net.*;

/** Xenith
 * 	Very high speed and atk, and good defenses;
 *	lacks SpA.
 *
 * @author Giacomo Parolini
 */

public class Xenith extends Pony {
	
	public Xenith(int _level) {
		super(_level);
		
		name = "Xenith";
		type[0] = Type.SPIRIT;
		type[1] = Type.SHADOW;

		race = Race.ZEBRA;
		sex = Sex.FEMALE;
		canon = false;

		baseHp = 75;
		baseAtk = 115;
		baseDef = 72;
		baseSpatk = 46; 
		baseSpdef = 69;
		baseSpeed = 123;
		
		/* learnableMoves ... */
		learnableMoves.put("One-Two Hit",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Balefire",1);
		learnableMoves.put("Stalking",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Power Display",1);
		learnableMoves.put("Scare Away",1);
		learnableMoves.put("Daredevilry",1);
		learnableMoves.put("Surprise Hit",1);
		learnableMoves.put("Sneak Attack",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Mysterious Brew",10);
		learnableMoves.put("Martial Arts",35);
		learnableMoves.put("Venom Potion",50);

		possibleAbilities[0] = "Brute Force";
		possibleAbilities[1] = "Die Hard";
		possibleAbilities[2] = "Pest Resilience";
	}
}
