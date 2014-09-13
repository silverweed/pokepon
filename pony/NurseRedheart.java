//: pony/NurseRedheart.java

package pokepon.pony;

import pokepon.enums.*;

/**	NurseRedheart
 *	Has excellent HP and defensive stats, but lacks attack
 *	and speed.
 *
 * @author silverweed
 */

public class NurseRedheart extends Pony {
	
	public NurseRedheart(int _level) {
		super(_level);
		
		name = "Nurse Redheart";
		type[0] = Type.KINDNESS;
		type[1] = Type.GENEROSITY;
		
		race = Race.EARTHPONY;

		baseHp = 250; 
		baseAtk = 55;
		baseDef = 10;
		baseSpatk = 10;
		baseSpdef = 105;
		baseSpeed = 70;

		/* Learnable moves */
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Talk Out",1);
		learnableMoves.put("Nap",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Snuggle",1);
		learnableMoves.put("Hopeful Strike",1);
		learnableMoves.put("Bind Up",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Lullaby",24);
		learnableMoves.put("Love And Care",39);
		learnableMoves.put("Relax",45);

		/* possibleAbilities */
		possibleAbilities[0] = "Magic Heal";
	}
}
