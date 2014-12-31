//: pony/Chrysalis.java

package pokepon.pony;

import pokepon.enums.*;

/** Chrysalis
 * 	excels in Special Attack and Special Defense.
 *
 * @author Giacomo Parolini
 */
public class Chrysalis extends Pony {
	
	public Chrysalis(int _level) {
		super(_level);
		
		name = "Chrysalis";
		type[0] = Type.SHADOW;
		type[1] = Type.LOVE;
		
		race = Race.MYTHICBEAST;
		sex = Sex.FEMALE;

		baseHp = 105;
		baseAtk = 105;
		baseDef = 86;
		baseSpatk = 139;
		baseSpdef = 130;
		baseSpeed = 85;
		
		/* learnableMoves ... */
		learnableMoves.put("Night Wind",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Meditation",1);
		learnableMoves.put("Balefire",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Power Display",1);
		learnableMoves.put("Scare Away",1);
		learnableMoves.put("Teleport Blast",1);
		learnableMoves.put("Surprise Hit",1);
		learnableMoves.put("Scheme Up",1);
		learnableMoves.put("Sneak Attack",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Magic Blast",15);
		learnableMoves.put("Sky Dive",20);
		learnableMoves.put("Horn Beam",38);
		learnableMoves.put("Evil Plot",46);
		learnableMoves.put("Mutate",59);
		learnableMoves.put("Glowing Laser",60);
	}

}
