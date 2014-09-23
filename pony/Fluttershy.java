//: pony/Fluttershy.java

package pokepon.pony;

import pokepon.enums.*;

/**	Fluttershy
 *	Has excellent HP and defensive stats, but lacks attack
 *	and speed.
 *
 * @author Giacomo Parolini
 */

public class Fluttershy extends Pony {
	
	public Fluttershy(int _level) {
		super(_level);
		
		name = "Fluttershy";
		type[0] = Type.KINDNESS;
		
		race = Race.PEGASUS;

		baseHp = 115; 
		baseAtk = 65;
		baseDef = 135;
		baseSpatk = 70;
		baseSpdef = 105;
		baseSpeed = 55;

		/* Learnable moves */
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Spa Treatment",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Wild Weed",1);
		learnableMoves.put("Talk Out",1);
		learnableMoves.put("Nap",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Practice",1);
		learnableMoves.put("Glomp",1);
		learnableMoves.put("Relay Race",1);
		learnableMoves.put("Poison Joke",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Shy Away",9);
		learnableMoves.put("Lullaby",24);
		learnableMoves.put("Love And Care",39);
		learnableMoves.put("Stare",70);	

		possibleAbilities[0] = "Compassion";
	}
}
