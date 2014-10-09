//: pony/Soarin.java

package pokepon.pony;

import pokepon.enums.*;

/**	Soarin
 *	Quite balanced attacks and defenses; good Speed.
 *
 * @author silverweed
 */
public class Soarin extends Pony {
	
	public Soarin(int _level) {
		super(_level);
		
		name = "Soarin";

		type[0] = Type.LOYALTY;

		race = Race.PEGASUS;
		sex = Sex.MALE;

		baseHp = 68;
		baseAtk = 88;
		baseDef = 85;
		baseSpatk = 60;
		baseSpdef = 76;
		baseSpeed = 123;

		/* Learnable Moves */
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Trailblazer",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Crazy Stunt",1);
		learnableMoves.put("Practice",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Speed Up",25);
		learnableMoves.put("Charge",36);
		learnableMoves.put("Sky Dive",46);
	}
}
