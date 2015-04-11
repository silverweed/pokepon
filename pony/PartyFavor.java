//: pony/PartyFavor.java

package pokepon.pony;

import pokepon.enums.*;

/**	Party Favor
 *	Good def and spa, lacks Hp and Speed
 *
 * @author silverweed
 */
public class PartyFavor extends Pony {
	
	public PartyFavor(int _level) {
		super(_level);
		
		name = "Party Favor";
		type[0] = Type.LAUGHTER;
		type[1] = Type.HONESTY; 

		race = Race.UNICORN;
		sex = Sex.MALE;

		baseHp = 60;
		baseAtk = 80;
		baseDef = 100;
		baseSpatk = 100;
		baseSpdef = 80;
		baseSpeed = 60;

		/* Learnable Moves */
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Dodge",1);
	}
}
