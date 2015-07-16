//: pony/Moondancer.java

package pokepon.pony;

import pokepon.enums.*;

/** Moondancer
 * 	Very good SpA and decent bulk; lacks Atk and Speed.
 *
 * @author silverweed
 */

public class Moondancer extends Pony {
	
	public Moondancer(int _level) {
		super(_level);
		
		name = "Moondancer";
		type[0] = Type.MAGIC;

		race = Race.UNICORN;
		sex = Sex.FEMALE;

		baseHp = 89;
		baseAtk = 42;
		baseDef = 66;
		baseSpatk = 140; 
		baseSpdef = 112;
		baseSpeed = 36;
		
		/* learnableMoves ... */
		learnableMoves.put("Icy Cannon",1);
		learnableMoves.put("Night Wind",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Sonic Barrier",1);
		learnableMoves.put("Meditation",1);
		learnableMoves.put("Enlighten",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Teleport Blast",1);
		learnableMoves.put("Scheme Up",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Magic Blast",15);
		learnableMoves.put("Horn Beam",24);
		learnableMoves.put("Freeze Spell",31);
		learnableMoves.put("Scorching Beam",40);

		/* possibleAbilities */
		possibleAbilities[0] = "Bookworm";
	}
}
