//: pony/ScrewLoose.java

package pokepon.pony;

import pokepon.enums.*;

/** ScrewLoose
 * Good Physical and HP, mediocre specials and Speed.	
 *
 * @author Giacomo Parolini
 */
public class ScrewLoose extends Pony {
	
	public ScrewLoose(int _level) {
		super(_level);
		
		name = "Screw Loose";
		type[0] = Type.CHAOS;
		
		race = Race.EARTHPONY;
		sex = Sex.FEMALE;

		baseHp = 90;
		baseAtk = 90;
		baseDef = 90;
		baseSpatk = 60;
		baseSpdef = 60;
		baseSpeed = 60;
		
		/* learnableMoves ... */
		learnableMoves.put("One-Two Hit",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Get Hype",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Holler Out",1);
		learnableMoves.put("Surprise Hit",1);
		learnableMoves.put("Wreak Havoc",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Joke",1);
		learnableMoves.put("Chaos Burst",20);

		/* abilities */
		possibleAbilities[0] = "Spell Refractory";
	}
}
