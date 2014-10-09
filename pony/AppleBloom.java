//: pony/AppleBloom.java

package pokepon.pony;

import pokepon.enums.*;

/**	Apple Bloom
 *	Excellent defense, good HP and speed;
 *	Poor attack.
 *	
 *	@author 
 */
public class AppleBloom extends Pony {
	
	public AppleBloom(int _level) {
		super(_level);
		
		name = "Apple Bloom";
		type[0] = Type.HONESTY;
		type[1] = Type.SPIRIT;

		race = Race.EARTHPONY;
		sex = Sex.FEMALE;

		baseHp = 90;
		baseAtk = 60;
		baseDef = 110;
		baseSpatk = 50;
		baseSpdef = 85;
		baseSpeed = 90;

		/* Learnable Moves */
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Get Hype",1);
		learnableMoves.put("Repeat",1);
		learnableMoves.put("Stalking",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Practice",1);
		learnableMoves.put("Hopeful Strike",1);
		learnableMoves.put("Poison Joke",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Martial Arts",60);
	}
}
