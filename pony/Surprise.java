//: pony/Surprise.java

package pokepon.pony;

import pokepon.enums.*;

/**	Surprise
 *	Good HP, quite balanced attacks but poor defenses; good Speed.
 *
 * @author silverweed
 */
public class Surprise extends Pony {
	
	public Surprise(int _level) {
		super(_level);
		
		name = "Surprise";

		type[0] = Type.LOYALTY;
		type[1] = Type.LAUGHTER;

		race = Race.PEGASUS;
		sex = Sex.FEMALE;

		baseHp = 90;
		baseAtk = 78;
		baseDef = 44;
		baseSpatk = 78;
		baseSpdef = 49;
		baseSpeed = 131;

		/* Learnable Moves */
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Snuggle",1);
		learnableMoves.put("Relay Race",1);
		learnableMoves.put("Holler Out",1);
		learnableMoves.put("Get Hype",1);
		learnableMoves.put("Chatter",1);
		learnableMoves.put("Bubble Burst",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Trailblazer",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Practice",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Speed Up",25);
		learnableMoves.put("Sky Dive",46);
	}
}
