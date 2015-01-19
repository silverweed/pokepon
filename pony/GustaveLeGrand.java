//: pony/GustaveLeGrand.java

package pokepon.pony;

import pokepon.enums.*;

/** GustaveLeGrand
 * 	Good defenses and decent atk, but low speed.
 *
 * @author silverweed
 */
public class GustaveLeGrand extends Pony {
	
	public GustaveLeGrand(int _level) {
		super(_level);
		
		name = "Gustave Le Grand";
		type[0] = Type.PASSION;
		
		race = Race.GRYPHON;
		sex = Sex.MALE;

		baseHp = 88;
		baseAtk = 78;
		baseDef = 91;
		baseSpatk = 38;
		baseSpdef = 89;
		baseSpeed = 61;
		
		/* learnableMoves ... */
		learnableMoves.put("One-Two Hit",1);
		learnableMoves.put("Talon Strike",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Surprise Hit",1);
		learnableMoves.put("Sneak Attack",1);
		learnableMoves.put("Dodge",1);

		/* possibleAbilities */
		possibleAbilities[0] = "Mastery";
	}
}
