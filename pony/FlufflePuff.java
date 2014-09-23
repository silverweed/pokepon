//: pony/FlufflePuff.java

package pokepon.pony;

import pokepon.enums.*;

/** Fluffle Puff
 * 	excellent Defense / HP and good Attack, but lacks
 *	specials and speed.
 *
 * @author silverweed
 */

public class FlufflePuff extends Pony {
	
	public FlufflePuff(int _level) {
		super(_level);
		
		name = "Fluffle Puff";
		type[0] = Type.LAUGHTER;
		type[1] = Type.LOVE;
		
		race = Race.EARTHPONY;
		canon = false;

		baseHp = 150;
		baseAtk = 70;
		baseDef = 125;
		baseSpatk = 20;
		baseSpdef = 60;
		baseSpeed = 50;
		
		/* learnableMoves ... */
		learnableMoves.put("Heavy Massage",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Entrench",1);
		learnableMoves.put("Nap",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Raging Spree",1);
		learnableMoves.put("Snuggle",1);
		learnableMoves.put("Hopeful Strike",1);
		learnableMoves.put("Bubble Burst",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Furry Coat",10);

		/* possibleAbilities */
		possibleAbilities[0] = "Magic Heal";
	}
}
