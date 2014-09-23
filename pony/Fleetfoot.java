//: pony/Fleetfoot.java

package pokepon.pony;

import pokepon.enums.*;

/**	Fleetfoot
 *	Ridiculously high speed and SpA;
 *	weak bulk and atk.
 *
 * @author 
 */
public class Fleetfoot extends Pony {
	
	public Fleetfoot(int _level) {
		super(_level);
		
		name = "Fleetfoot";
		type[0] = Type.LOYALTY;
		type[1] = Type.LOVE; 

		race = Race.PEGASUS;

		baseHp = 80;
		baseAtk = 50;
		baseDef = 40;
		baseSpatk = 120;
		baseSpdef = 68;
		baseSpeed = 142;

		/* Learnable Moves */
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Heavy Massage",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Trailblazer",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Daredevilry",1);
		learnableMoves.put("Practice",1);
		learnableMoves.put("Hopeful Strike",1);
		learnableMoves.put("Relay Race",1);
		learnableMoves.put("Scheme Up",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Speed Up",17);
		learnableMoves.put("Storm Cloud",28);
		learnableMoves.put("Sky Dive",30);

		possibleAbilities[0] = "Self Confidence";
		possibleAbilities[1] = "Lovebird";
	}
}
