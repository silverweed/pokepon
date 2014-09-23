//: pony/LimestonePie.java

package pokepon.pony;

import pokepon.enums.*;

/**	LimestonePie
 *	Very good HP, decent physical attack and speed;
 *	Lacks special attack.
 *
 * @author Giacomo Parolini
 */
public class LimestonePie extends Pony {
	
	public LimestonePie(int _level) {
		super(_level);
		
		name = "Limestone Pie";
		type[0] = Type.SPIRIT;
		type[1] = Type.NIGHT;
		
		race = Race.EARTHPONY;

		baseHp = 109;
		baseAtk = 81;
		baseDef = 65;
		baseSpatk = 40;
		baseSpdef = 72;
		baseSpeed = 83;

		/* Learnable Moves */
		learnableMoves.put("Rock Throw",1);
		learnableMoves.put("Rectify",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Surprise Hit",1);
		learnableMoves.put("Stealth Diamonds",1);
		learnableMoves.put("Relax",1);
		learnableMoves.put("Inner Focus",1);
		learnableMoves.put("Gem Storm",1);
		learnableMoves.put("Eerie Sonata",1);
		learnableMoves.put("Boulder Bomb",1);
		learnableMoves.put("Hopeful Strike",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Practice",1);
		learnableMoves.put("Sneak Attack",1);
		learnableMoves.put("Dodge",1);

		possibleAbilities[0] = "Ataraxy";
	}
}
