//: pony/Zecora.java

package pokepon.pony;

import pokepon.enums.*;

/** Zecora
 * 	excels in Special Defense and has good Defense and Speed;
 *	Average SpA and above-average Atk.
 *
 * @author Giacomo Parolini
 */

public class Zecora extends Pony {
	
	public Zecora(int _level) {
		super(_level);
		
		name = "Zecora";
		type[0] = Type.SPIRIT;

		race = Race.ZEBRA;

		baseHp = 80;
		baseAtk = 95;
		baseDef = 80;
		baseSpatk = 70; 
		baseSpdef = 100;
		baseSpeed = 75;
		
		/* learnableMoves ... */
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Wild Weed",1);
		learnableMoves.put("Balefire",1);
		learnableMoves.put("Nap",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Practice",1);
		learnableMoves.put("Burning Powder",1);
		learnableMoves.put("Poison Joke",1);
		learnableMoves.put("Sneak Attack",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Mysterious Brew",10);
		learnableMoves.put("Martial Arts",35);
		learnableMoves.put("Hind Kick",39);
		learnableMoves.put("Venom Potion",44);

		possibleAbilities[0] = "Ataraxy";
	}
}
