//: pony/Discord.java

package pokepon.pony;

import pokepon.enums.*;

/** Discord
 *	Excellent SpA, HP and Def.	
 *
 * @author silverweed
 */
public class Discord extends Pony {
	
	public Discord(int _level) {
		super(_level);
		
		name = "Discord";
		type[0] = Type.CHAOS;
		
		race = Race.MYTHICBEAST;
		sex = Sex.MALE;

		baseHp = 125;
		baseAtk = 105;
		baseSpatk = 130;
		baseDef = 120;
		baseSpdef = 85;
		baseSpeed = 85;
		
		/* learnableMoves ... */
		learnableMoves.put("Kinetic Strike",1);
		learnableMoves.put("Night Wind",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Dimension Twist",1);
		learnableMoves.put("Burning Powder",1);
		learnableMoves.put("Surprise Hit",1);
		learnableMoves.put("Wreak Havoc",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Joke",1);
		learnableMoves.put("Freeze Spell",11);
		learnableMoves.put("Chaos Burst",20);
		learnableMoves.put("Scorching Beam",40);
		learnableMoves.put("Rampage",60);

		/* abilities */
		possibleAbilities[0] = "Chaos Magic";
	}
}
