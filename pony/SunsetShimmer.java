//: pony/SunsetShimmer.java

package pokepon.pony;

import pokepon.enums.*;

/**	Sunset Shimmer
 *	Very good SpA and other stats balanced;
 *	(Note: defs are weak because this typing has 5(!!) 1/4x resistances)
 *
 * @author Giacomo Parolini
 */
public class SunsetShimmer extends Pony {
	
	public SunsetShimmer(int _level) {
		super(_level);
		
		name = "Sunset Shimmer";
		type[0] = Type.LIGHT;
		type[1] = Type.SHADOW;

		race = Race.UNICORN;
		sex = Sex.FEMALE;

		baseHp = 75;
		baseAtk = 60;
		baseDef = 60;
		baseSpatk = 120;
		baseSpdef = 55;
		baseSpeed = 80;

		/* Learnable Moves */
		learnableMoves.put("Magic Blast",1);
		learnableMoves.put("Dark Magic",1);
		learnableMoves.put("Charge",1);
		learnableMoves.put("Night Wind",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Teleport Blast",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Scorching Beam",30);
		learnableMoves.put("Glowing Laser",60);

		possibleAbilities[0] = "Shining Coat";
		possibleAbilities[1] = "Aversion";
	}
}
