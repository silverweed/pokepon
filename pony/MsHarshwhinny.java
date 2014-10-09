//: pony/MsHarshwhinny.java

package pokepon.pony;

import pokepon.enums.*;

/**	Ms Harshwhinny
 *	Excellent HP and SpDef, low Speed and SpAtk though.
 *
 * @author 
 */
public class MsHarshwhinny extends Pony {
	
	public MsHarshwhinny(int _level) {
		super(_level);
		
		name = "Ms Harshwhinny";
		type[0] = Type.LOYALTY;

		race = Race.EARTHPONY;
		sex = Sex.FEMALE;

		baseHp = 100;
		baseAtk = 75;
		baseDef = 70;
		baseSpatk = 40;
		baseSpdef = 95;
		baseSpeed = 40;

		/* Learnable Moves */
		learnableMoves.put("Tackle",10);
		learnableMoves.put("Spa Treatment",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Whine",26);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Hind Kick",40);
		learnableMoves.put("Taunt",54);
	}
}
