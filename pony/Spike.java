//: pony/Spike.java

package pokepon.pony;

import pokepon.enums.*;

/** Spike
 *	Balanced average stats, with a bit more defs than atks and speed.
 *
 * @author Giacomo Parolini
 */

public class Spike extends Pony {
	
	public Spike(int _level) {
		super(_level);
		
		name = "Spike";
		type[0] = Type.LOYALTY;
		type[1] = Type.MAGIC;
		
		race = Race.MYTHICBEAST;
		sex = Sex.MALE;

		baseHp = 70;
		baseAtk = 65;
		baseDef = 80;
		baseSpatk = 70;
		baseSpdef = 75;
		baseSpeed = 60;
		
		/* learnableMoves ... */
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Wild Weed",1);
		learnableMoves.put("Sonic Barrier",1);
		learnableMoves.put("Entrench",1);
		learnableMoves.put("Nap",1);
		learnableMoves.put("Stalking",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Taunt",1);
		learnableMoves.put("Bubble Burst",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Sharp Nails",13);
		learnableMoves.put("Love And Care",40);

		/* Abilities */
		possibleAbilities[0] = "Dragon Scales";
		possibleAbilities[1] = "Mildness";
	}

}
