//: pony/Fiddlesticks.java

package pokepon.pony;

import pokepon.enums.*;

/** Fiddlesticks
 * 
 * 
 *
 * @author 
 */
public class Fiddlesticks extends Pony {
	
	public Fiddlesticks(int _level) {
		super(_level);
		
		name = "Fiddlesticks";
		type[0] = Type.HONESTY;
		type[1] = Type.MUSIC;

		race = Race.EARTHPONY;

		baseHp = 94;
		baseAtk = 70;
		baseDef = 95;
		baseSpatk = 35;
		baseSpdef = 80;
		baseSpeed = 76; 

		/* Learnable Moves */
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Sonic Barrier",1);
		learnableMoves.put("Entrench",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Dissonance",8);
	}
}
