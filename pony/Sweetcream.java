//: pony/Sweetcream.java

package pokepon.pony;

import pokepon.enums.*;

/** Sweetcream
 * 	excellent Speed, good Attacks and decent HP;
 *	lacks Defense.
 *
 * @author silverweed
 */

public class Sweetcream extends Pony {
	
	public Sweetcream(int _level) {
		super(_level);
		
		name = "Sweetcream";
		type[0] = Type.PASSION;
		type[1] = Type.GENEROSITY;
		
		race = Race.UNICORN;
		sex = Sex.FEMALE;

		baseHp = 85;
		baseAtk = 56;
		baseDef = 90;
		baseSpatk = 91; 
		baseSpdef = 70;
		baseSpeed = 72;
		
		/* learnableMoves ... */
		learnableMoves.put("Sparkling Orb",1);
		learnableMoves.put("Meditation",1);
		learnableMoves.put("Icy Cannon",1);
		learnableMoves.put("Magic Shield",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Get Hype",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Glomp",1);
		learnableMoves.put("Snuggle",1);
		learnableMoves.put("Startle",1);
		learnableMoves.put("Bubble Burst",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Duck Face",59);

		possibleAbilities[0] = "Sheep's Eyes";
	}

}
