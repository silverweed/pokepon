//: pony/Rarity.java

package pokepon.pony;

import pokepon.enums.*;

/**	Rarity
 *	Good Special Defense and decent Special Attack and HP;
 *	Lacks physical attack and speed.
 *
 * @author Giacomo Parolini
 */
public class Rarity extends Pony {
	
	public Rarity(int _level) {
		super(_level);
		
		name = "Rarity";
		type[0] = Type.GENEROSITY;

		race = Race.UNICORN;
		sex = Sex.FEMALE;

		baseHp = 90; 
		baseAtk = 60;
		baseDef = 85;
		baseSpatk = 100;
		baseSpdef = 135;
		baseSpeed = 75;

		/* Learnable Moves */
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Night Wind",1);
		learnableMoves.put("Spa Treatment",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Talk Out",1);
		learnableMoves.put("Enlighten",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Snuggle",1);
		learnableMoves.put("Bind Up",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Duck Face",19);
		learnableMoves.put("Stealth Diamonds",21);
		learnableMoves.put("Relax",33);
		learnableMoves.put("Gem Storm",45);
		learnableMoves.put("Martial Arts",58);

		possibleAbilities[0] = "Mastery";
		possibleAbilities[1] = "Charity";
		possibleAbilities[2] = "Shining Coat";
	}
}
