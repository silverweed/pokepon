//: pony/LightningDust.java

package pokepon.pony;

import pokepon.enums.*;

/**	Lightning Dust
 *	Outclassing speed and good atks, but low
 *	defenses and HP.
 *
 * @author silverweed
 */

public class LightningDust extends Pony {
	
	public LightningDust(int _level) {
		super(_level);
		
		name = "Lightning Dust";
		type[0] = Type.PASSION;

		race = Race.PEGASUS;
		sex = Sex.FEMALE;

		baseHp = 64;
		baseAtk = 95;
		baseDef = 60;
		baseSpatk = 95;
		baseSpdef = 60;
		baseSpeed = 126; 

		/* Learnable Moves */
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Night Wind",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Scheme Up",1);
		learnableMoves.put("Inner Focus",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Raging Spree",1);
		learnableMoves.put("Daredevilry",1);
		learnableMoves.put("Practice",1);
		learnableMoves.put("Sneak Attack",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Speed Up",25);
		learnableMoves.put("Charge",30);
		learnableMoves.put("Sky Dive",33);

		possibleAbilities[0] = "Stubborn";
		possibleAbilities[1] = "Determination";
	}
}
