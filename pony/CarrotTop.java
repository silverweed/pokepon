//: pony/CarrotTop.java

package pokepon.pony;

import pokepon.enums.*;

/** CarrotTop
 * Unusually for an Earth Pony, has a lot of SpA and MAGIC type.
 *
 * @author silverweed
 */

public class CarrotTop extends Pony {
	
	public CarrotTop(int _level) {
		super(_level);
		
		name = "Carrot Top";
		type[0] = Type.MAGIC;
		type[1] = Type.GENEROSITY;
		
		race = Race.EARTHPONY;
		sex = Sex.FEMALE;

		baseHp = 57;
		baseAtk = 51;
		baseDef = 71;
		baseSpatk = 124;
		baseSpdef = 58;
		baseSpeed = 89;
		
		/* learnableMoves ... */
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Sonic Barrier",1);
		learnableMoves.put("Meditation",1);
		learnableMoves.put("Enlighten",1);
		learnableMoves.put("Nap",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Teleport Blast",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Freeze Spell",38);
		learnableMoves.put("Horn Beam",30);
		learnableMoves.put("Crystal Shield",59);

		possibleAbilities[0] = "Ataraxy";
		possibleAbilities[1] = "Pest Resilience";
	}
}
