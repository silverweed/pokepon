//: pony/StarlightGlimmer.java

package pokepon.pony;

import pokepon.enums.*;

/** StarlightGlimmer
 * 	Very good specials and good bulk;
 *	lacks atk and speed.
 *
 * @author silverweed
 */
public class StarlightGlimmer extends Pony {
	
	public StarlightGlimmer(int _level) {
		super(_level);
		
		name = "Starlight Glimmer";
		type[0] = Type.MAGIC;
		type[1] = Type.SHADOW;
		
		race = Race.UNICORN;
		sex = Sex.FEMALE;

		baseHp = 75;
		baseAtk = 60;
		baseDef = 75;
		baseSpatk = 120;
		baseSpdef = 110;
		baseSpeed = 50;
		
		/* learnableMoves ... */
		learnableMoves.put("Evil Plot",1);
		learnableMoves.put("Horn Beam",1);
		learnableMoves.put("Rectify",1);
		learnableMoves.put("Power Display",1);
		learnableMoves.put("Telekinesis",1);
		learnableMoves.put("Dark Magic",1);
		learnableMoves.put("Magic Shield",1);
		learnableMoves.put("Burning Powder",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Surprise Hit",1);
		learnableMoves.put("Sneak Attack",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Charge",20);
		learnableMoves.put("Shadow Mist",80);

		/* possibleAbilities */
		possibleAbilities[0] = "Equalize";
	}
}
