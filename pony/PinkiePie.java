//: pony/PinkiePie.java

package pokepon.pony;

import pokepon.enums.*;

/** Pinkie Pie
 * 	excellent Speed, good Attacks and decent HP;
 *	lacks Defense.
 *
 * @author Giacomo Parolini
 */

public class PinkiePie extends Pony {
	
	public PinkiePie(int _level) {
		super(_level);
		
		name = "Pinkie Pie";
		type[0] = Type.LAUGHTER;
		
		race = Race.EARTHPONY;
		sex = Sex.FEMALE;

		baseHp = 80;
		baseAtk = 95;
		baseDef = 55;
		baseSpatk = 105; 
		baseSpdef = 70;
		baseSpeed = 140;
		
		/* learnableMoves ... */
		learnableMoves.put("Jingle",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Get Hype",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Glomp",1);
		learnableMoves.put("Snuggle",1);
		learnableMoves.put("Startle",1);
		learnableMoves.put("Bubble Burst",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Chatter",5);
		learnableMoves.put("Party Cannon",42);	
		learnableMoves.put("Duck Face",59);

		possibleAbilities[0] = "Optimism";
	}

}
