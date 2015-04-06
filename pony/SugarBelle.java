//: pony/SugarBelle.java

package pokepon.pony;

import pokepon.enums.*;

/** SugarBelle
 * 	Very good bulk and SpD,
 *	lacks speed.
 *
 * @author silverweed
 */

public class SugarBelle extends Pony {
	
	public SugarBelle(int _level) {
		super(_level);
		
		name = "Sugar Belle";
		type[0] = Type.LAUGHTER;
		type[1] = Type.KINDNESS;
		
		race = Race.UNICORN;
		sex = Sex.FEMALE;

		baseHp = 108;
		baseAtk = 68;
		baseDef = 70;
		baseSpatk = 75; 
		baseSpdef = 104;
		baseSpeed = 55;
		
		/* learnableMoves ... */
		learnableMoves.put("Entrench",1);
		learnableMoves.put("Sonic Barrier",1);
		learnableMoves.put("Horn Beam",1);
		learnableMoves.put("Talk Out",1);
		learnableMoves.put("Chatter",1);
		learnableMoves.put("Meditation",1);
		learnableMoves.put("Magic Shield",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Get Hype",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Glomp",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Tackle",1);

	}

}
