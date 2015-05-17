//: pony/TreeHugger.java

package pokepon.pony;

import pokepon.enums.*;

/**	TreeHugger
 *	Good HP, SpD and SpA, lacks Atk.
 *
 * @author silverweed
 */

public class TreeHugger extends Pony {
	
	public TreeHugger(int _level) {
		super(_level);
		
		name = "Tree Hugger";
		type[0] = Type.SPIRIT;
		
		race = Race.EARTHPONY;
		sex = Sex.FEMALE;

		baseHp = 95; 
		baseAtk = 54;
		baseDef = 75;
		baseSpatk = 91;
		baseSpdef = 88;
		baseSpeed = 62;

		/* Learnable moves */
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Heavy Massage",1);
		learnableMoves.put("Spa Treatment",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Wild Weed",1);
		learnableMoves.put("Talk Out",1);
		learnableMoves.put("Nap",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Poison Joke",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Lullaby",24);
		learnableMoves.put("Love And Care",39);

		possibleAbilities[0] = "Natural Empathy";
		possibleAbilities[1] = "Ataraxy";
	}
}
