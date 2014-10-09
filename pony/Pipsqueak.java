//: pony/Pipsqueak.java

package pokepon.pony;

import pokepon.enums.*;

/**	Pipsqueak
 *	Generally low stats, with exploitable Speed and decent defenses
 *	and physical atk.
 *
 * @author Giacomo Parolini
 */
public class Pipsqueak extends Pony {
	
	public Pipsqueak(int _level) {
		super(_level);
		
		name = "Pipsqueak";
		type[0] = Type.NIGHT;
		type[1] = Type.GENEROSITY;

		race = Race.EARTHPONY;
		sex = Sex.MALE;

		baseHp = 60;
		baseAtk = 70;
		baseDef = 75;
		baseSpatk = 20;
		baseSpdef = 70;
		baseSpeed = 90;

		/* Learnable Moves */
		learnableMoves.put("Whirling Hoof",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Wild Weed",1);
		learnableMoves.put("Entrench",1);
		learnableMoves.put("Nap",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Scare Away",1);
		learnableMoves.put("Burning Powder",1);
		learnableMoves.put("Relay Race",1);
		learnableMoves.put("Surprise Hit",1);
		learnableMoves.put("Taunt",1);
		learnableMoves.put("Poison Joke",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Joke",13);
		learnableMoves.put("Charge",37);

		possibleAbilities[0] = "Stubborn";
		possibleAbilities[1] = "Upper Hoof";
	}
}
