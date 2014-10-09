//: pony/Littlepip.java

package pokepon.pony;

import pokepon.enums.*;

/** Littlepip
 * 	balanced with good Atk and SpA, but also HP and 
 *	nice defs.
 *
 * @author silverweed
 */

public class Littlepip extends Pony {
	
	public Littlepip(int _level) {
		super(_level);
		
		name = "Littlepip";
		type[0] = Type.LIGHT;
		type[1] = Type.KINDNESS;

		race = Race.UNICORN;
		sex = Sex.FEMALE;
		canon = false;

		baseHp = 89;
		baseAtk = 85;
		baseDef = 76;
		baseSpatk = 90; 
		baseSpdef = 71;
		baseSpeed = 74;
		
		/* learnableMoves ... */
		learnableMoves.put("Rectify",1);
		learnableMoves.put("Heavy Massage",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Meditation",1);
		learnableMoves.put("Enlighten",1);
		learnableMoves.put("Balefire",1);
		learnableMoves.put("Stalking",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Daredevilry",1);
		learnableMoves.put("Hopeful Strike",1);
		learnableMoves.put("Surprise Hit",1);
		learnableMoves.put("Scheme Up",1);
		learnableMoves.put("Sneak Attack",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Magic Blast",15);
		learnableMoves.put("Telekinesis",22);
		learnableMoves.put("Sharp Nails",39);
		learnableMoves.put("Bullet Shower",42);
		
		possibleAbilities[0] = "Leadership";
		possibleAbilities[1] = "Die Hard";
	}
}
