//: pony/NightGlider.java

package pokepon.pony;

import pokepon.enums.*;

/**	Night Glider
 *	High Speed,
 *	weak bulk.
 *
 * @author silverweed
 */
public class NightGlider extends Pony {
	
	public NightGlider(int _level) {
		super(_level);
		
		name = "Night Glider";
		type[0] = Type.NIGHT;
		type[1] = Type.PASSION; 

		race = Race.PEGASUS;
		sex = Sex.FEMALE;

		baseHp = 55;
		baseAtk = 94;
		baseDef = 65;
		baseSpatk = 87;
		baseSpdef = 58;
		baseSpeed = 121;

		/* Learnable Moves */
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Surprise Hit",1);
		learnableMoves.put("Stalking",1);
		learnableMoves.put("Night Wind",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Daredevilry",1);
		learnableMoves.put("Practice",1);
		learnableMoves.put("Hopeful Strike",1);
		learnableMoves.put("Relay Race",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Speed Up",17);
		learnableMoves.put("Storm Cloud",28);
		learnableMoves.put("Sky Dive",30);
	
		possibleAbilities[0] = "Uprising";
	}
}
