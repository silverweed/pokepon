//: pony/SilverSpoon.java

package pokepon.pony;

import pokepon.enums.*;

/**	Silver Spoon
 *	Nice bulk but low attacks and speed; mainly a supporter.
 *	
 *
 * @author Tommaso Parolini
 */
public class SilverSpoon extends Pony {
	
	public SilverSpoon(int _level) {
		super(_level);
		
		name = "Silver Spoon";
		type[0] = Type.LOVE; 
		type[1] = Type.SHADOW;

		race = Race.EARTHPONY;

		baseHp = 90;
		baseAtk = 50;
		baseDef = 75;
		baseSpatk = 45;
		baseSpdef = 95;
		baseSpeed = 65;

		/* Learnable Moves */
		learnableMoves.put("Bully",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Sonic Barrier",1);
		learnableMoves.put("Entrench",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Hopeful Strike",1);
		learnableMoves.put("Taunt",1);
		learnableMoves.put("Poison Joke",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Chatter",10);
		learnableMoves.put("Love And Care",40);

		possibleAbilities[0] = "Lovebird";
	}
}
