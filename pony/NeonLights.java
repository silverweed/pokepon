//: pony/NeonLights.java

package pokepon.pony;

import pokepon.enums.*;

/** NeonLights
 *  Good Atk and Speed and very good SpA, but low bulk.
 *
 * @author silverweed
 */

public class NeonLights extends Pony {
	
	public NeonLights(int _level) {
		super(_level);
		
		name = "Neon Lights";
		type[0] = Type.MUSIC;
		type[1] = Type.NIGHT;

		race = Race.UNICORN;
		sex = Sex.MALE;

		baseHp = 65;
		baseAtk = 85;
		baseDef = 53;
		baseSpatk = 103;
		baseSpdef = 70;
		baseSpeed = 89;
		
		/* learnableMoves ... */
		learnableMoves.put("Drum Bang",1);
		learnableMoves.put("Surprise Hit",1);
		learnableMoves.put("Repeat",1);
		learnableMoves.put("Magic Blast",1);
		learnableMoves.put("Hind Kick",1);
		learnableMoves.put("Glowing Laser",1);
		learnableMoves.put("Crazy Stunt",1);
		learnableMoves.put("Bubble Burst",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Sonic Barrier",1);
		learnableMoves.put("Get Hype",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Holler Out",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Dissonance",1);
		learnableMoves.put("Bass Drop",36);
		learnableMoves.put("Bass Cannon",50);

		possibleAbilities[0] = "Nocturnality";
	}

}
