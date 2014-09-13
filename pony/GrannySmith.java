//: pony/GrannySmith.java

package pokepon.pony;

import pokepon.enums.*;

/** Granny Smith
 * Good HP and Def and decent Atk; 
 * Lacks special attack and speed.
 *
 * @author Giacomo Parolini
 */
public class GrannySmith extends Pony {
	
	public GrannySmith(int _level) {
		super(_level);
		
		name = "Granny Smith";
		type[0] = Type.HONESTY;
		
		race = Race.EARTHPONY;

		baseHp = 100;
		baseAtk = 78;
		baseDef = 87;
		baseSpatk = 37;
		baseSpdef = 73;
		baseSpeed = 25; 

		/* Learnable Moves */
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Wild Weed",1);
		learnableMoves.put("Entrench",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Crazy Stunt",1);
		learnableMoves.put("Hopeful Strike",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Applebuck",1);	
		learnableMoves.put("Love And Care",16);
		learnableMoves.put("Hind Kick",67);
		learnableMoves.put("Venom Potion",72);
	}
}
