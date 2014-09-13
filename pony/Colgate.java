//: pony/Colgate.java

package pokepon.pony;

import pokepon.enums.*;

/** Colgate
 *
 * @author silverweed
 */

public class Colgate extends Pony {
	
	public Colgate(int _level) {
		super(_level);
		
		name = "Colgate";
		type[0] = Type.KINDNESS;
		type[1] = Type.MAGIC;
		
		race = Race.UNICORN;

		baseHp = 83;
		baseAtk = 60;
		baseDef = 90;
		baseSpatk = 60;
		baseSpdef = 98;
		baseSpeed = 69;
		
		/* learnableMoves ... */
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Sonic Barrier",1);
		learnableMoves.put("Meditation",1);
		learnableMoves.put("Enlighten",1);
		learnableMoves.put("Nap",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Teleport Blast",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Freeze Spell",38);
		learnableMoves.put("Horn Beam",30);
		learnableMoves.put("Crystal Shield",59);

		possibleAbilities[0] = "Ataraxy";
	}
}
