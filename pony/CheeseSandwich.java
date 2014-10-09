//: pony/CheeseSandwich.java

package pokepon.pony;

import pokepon.enums.*;

/** Cheese Sandwich
 * 	excellent Speed and attack; decent SpA too;
 *	lacks Defense and HP.
 *
 * @author Giacomo Parolini
 */

public class CheeseSandwich extends Pony {
	
	public CheeseSandwich(int _level) {
		super(_level);
		
		name = "Cheese Sandwich";
		type[0] = Type.LAUGHTER;
		type[1] = Type.CHAOS;
		
		race = Race.EARTHPONY;
		sex = Sex.MALE;

		baseHp = 61;
		baseAtk = 110;
		baseDef = 48;
		baseSpatk = 76; 
		baseSpdef = 60;
		baseSpeed = 130;
		
		/* learnableMoves ... */
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Get Hype",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Holler Out",1);
		learnableMoves.put("Glomp",1);
		learnableMoves.put("Burning Powder",1);
		learnableMoves.put("Surprise Hit",1);
		learnableMoves.put("Bubble Burst",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Joke",1);
		learnableMoves.put("Chatter",14);
		learnableMoves.put("Speed Up",28);
		learnableMoves.put("Charge",36);
		learnableMoves.put("Chaos Burst",49);
		learnableMoves.put("Party Cannon",62);	
	}

}
