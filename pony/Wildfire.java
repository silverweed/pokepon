//: pony/Wildfire.java

package pokepon.pony;

import pokepon.enums.*;

/**	Wildfire
 *	Very good HP, Atk and Def, but low specials and Speed.
 *
 * @author Giacomo Parolini
 */

public class Wildfire extends Pony {
	
	public Wildfire(int _level) {
		super(_level);
		
		name = "Wildfire";
		type[0] = Type.HONESTY;
		type[1] = Type.PASSION;

		race = Race.PEGASUS;
		sex = Sex.FEMALE;

		baseHp = 105;
		baseAtk = 95;
		baseDef = 105;
		baseSpatk = 60;
		baseSpdef = 50;
		baseSpeed = 65; 

		/* Learnable Moves */
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Daredevilry",1);
		learnableMoves.put("Treat",1);
		learnableMoves.put("Hopeful Strike",1);
		learnableMoves.put("Stampede",1);
		learnableMoves.put("Sneak Attack",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Hind Kick",30);
		learnableMoves.put("Sky Dive",42);
		learnableMoves.put("Rock Crush",56);
		learnableMoves.put("Inner Focus",70);

		possibleAbilities[0] = "Determination";
	}
}
