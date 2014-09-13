//: pony/Aloe.java

package pokepon.pony;

import pokepon.enums.*;

/** Aloe
 * Excellent bulk, viable Atk and mediocre Speed.
 *
 * @author silverweed
 */

public class Aloe extends Pony {
	
	public Aloe(int _level) {
		super(_level);
		
		name = "Aloe";
		type[0] = Type.LOVE;
		type[1] = Type.LIGHT;
		
		race = Race.EARTHPONY;

		baseHp = 90;
		baseAtk = 70;
		baseDef = 150;
		baseSpatk = 50;
		baseSpdef = 80;
		baseSpeed = 60;
		
		/* learnableMoves ... */
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Meditation",1);
		learnableMoves.put("Nap",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Snuggle",1);
		learnableMoves.put("Burning Powder",1);
		learnableMoves.put("Venom Potion",1);
		learnableMoves.put("Hind Kick",1);
		learnableMoves.put("Joke",1);
		learnableMoves.put("Taunt",1);
		learnableMoves.put("Love And Care",1);
		learnableMoves.put("Chatter",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Relax",1);

		possibleAbilities[0] = "Shining Coat";
	}
}
