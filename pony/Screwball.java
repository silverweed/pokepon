//: pony/Screwball.java

package pokepon.pony;

import pokepon.enums.*;

/** Screwball
 * Good Specials and Speed, mediocre physicals and HP.	
 *
 * @author Giacomo Parolini
 */
public class Screwball extends Pony {
	
	public Screwball(int _level) {
		super(_level);
		
		name = "Screwball";
		type[0] = Type.CHAOS;
		
		race = Race.EARTHPONY;
		sex = Sex.FEMALE;

		baseHp = 60;
		baseAtk = 60;
		baseDef = 60;
		baseSpatk = 90;
		baseSpdef = 90;
		baseSpeed = 90;
		
		/* learnableMoves ... */
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Sonic Barrier",1);
		learnableMoves.put("Entrench",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Dimension Twist",1);
		learnableMoves.put("Stampede",1);
		learnableMoves.put("Wreak Havoc",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Joke",1);
		learnableMoves.put("Chaos Burst",20);

		/* abilities */
		possibleAbilities[0] = "Escapist";
	}
}
