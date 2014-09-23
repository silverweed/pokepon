//: pony/ShiningArmor.java

package pokepon.pony;

import pokepon.enums.*;

/** Shining Armor
 * 	excellent defense and special defense;
 *	good SpA but lacks Speed.
 *
 * @author Giacomo Parolini
 */

public class ShiningArmor extends Pony {
	
	public ShiningArmor(int _level) {
		super(_level);
		
		name = "Shining Armor";
		type[0] = Type.LOVE;
		type[1] = Type.LOYALTY;

		race = Race.UNICORN;

		baseHp = 75;
		baseAtk = 65;
		baseDef = 125;
		baseSpatk = 90;
		baseSpdef = 130;
		baseSpeed = 40;
		
		/* learnableMoves ... */
		learnableMoves.put("Rectify",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Teleport Blast",1);
		learnableMoves.put("Hopeful Strike",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Magic Shield",13);	
		learnableMoves.put("Horn Beam",61);
		learnableMoves.put("Love Burst",90);
	}

}
