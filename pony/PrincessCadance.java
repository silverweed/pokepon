//: pony/PrincessCadance.java

package pokepon.pony;

import pokepon.enums.*;

/** Princess Cadance
 * 	excels in Defense, HP and has good SpD;
 *	lacks physical Attack, Special Attack and has average Speed.
 *
 * @author silverweed
 */
public class PrincessCadance extends Pony {
	
	public PrincessCadance(int _level) {
		super(_level);
		
		name = "Princess Cadance";
		type[0] = Type.LOVE;

		race = Race.ALICORN;

		baseHp = 130;
		baseAtk = 55;
		baseDef = 145;
		baseSpatk = 80;
		baseSpdef = 125;
		baseSpeed = 65;
		
		/* learnableMoves ... */
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Enlighten",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Snuggle",1);
		learnableMoves.put("Hopeful Strike",1);
		learnableMoves.put("Relax",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Magic Blast",1);	
		learnableMoves.put("Magic Shield",11);
		learnableMoves.put("Freeze Spell",22);
		learnableMoves.put("Gem Storm",44);
		learnableMoves.put("Canterlot Voice",60);
		learnableMoves.put("Crystal Shield",71);
		learnableMoves.put("Love Burst",86);

		/* possibleAbilities */
		possibleAbilities[0] = "Magic Heal";
		possibleAbilities[1] = "Optimism";
	}
}
