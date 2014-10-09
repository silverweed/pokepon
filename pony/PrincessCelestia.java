//: pony/PrincessCelestia.java

package pokepon.pony;

import pokepon.enums.*;

/** Princess Celestia
 *	Excels in all stats.	
 *
 * @author silverweed
 */

public class PrincessCelestia extends Pony {
	
	public PrincessCelestia(int _level) {
		super(_level);
		
		name = "Princess Celestia";
		type[0] = Type.MAGIC;
		type[1] = Type.LIGHT;

		race = Race.ALICORN;
		sex = Sex.FEMALE;
		
		baseHp = 120;
		baseAtk = 120;
		baseDef = 120;
		baseSpatk = 120;
		baseSpdef = 120;
		baseSpeed = 120;
		
		/* learnableMoves ... */
		learnableMoves.put("Rectify",1);
		learnableMoves.put("Spa Treatment",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Sonic Barrier",1);
		learnableMoves.put("Enlighten",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Teleport Blast",1);
		learnableMoves.put("Dark Magic",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Magic Blast",1);	
		learnableMoves.put("Horn Beam",16);
		learnableMoves.put("Friendship Cannon",30);
		learnableMoves.put("Raise Sun",40);
		learnableMoves.put("Glowing Laser",50);

		possibleAbilities[0] = "Solar Magic";
	}
}
