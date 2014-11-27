//: pony/SeaBreeze.java

package pokepon.pony;

import pokepon.enums.*;

/** Sea Breeze
 * Extremely fragile, but with unrivalled speed and good atks
 *
 * @author Giacomo Parolini
 */

public class SeaBreeze extends Pony {
	
	public SeaBreeze(int _level) {
		super(_level);
		
		name = "Sea Breeze";
		type[0] = Type.LOYALTY;
		type[1] = Type.LIGHT;

		race = Race.BREEZIE;
		sex = Sex.MALE;

		baseHp = 35;
		baseAtk = 75;
		baseDef = 35;
		baseSpatk = 95;
		baseSpdef = 35;
		baseSpeed = 125; 

		/* Learnable Moves */
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Flit About",1);
		learnableMoves.put("Sonic Barrier",1);
		learnableMoves.put("Dissonance",1);
		learnableMoves.put("Raging Spree",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Practice",1);
		learnableMoves.put("Taunt",1);
		learnableMoves.put("Sneak Attack",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Speed Up",25);

		possibleAbilities[0] = "Stubborn";
		possibleAbilities[1] = "Devotion";
		possibleAbilities[2] = "Swiftness";
	}
}
