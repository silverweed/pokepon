//: pony/Applejack.java

package pokepon.pony;

import pokepon.enums.*;

/** Applejack
 * Very good physical defense, speed and HP, decent physical atk and spdef;
 * Lacks special attack. 
 *
 * @author Giacomo Parolini
 */
public class Applejack extends Pony {
	
	public Applejack(int _level) {
		super(_level);
		
		name = "Applejack";
		type[0] = Type.HONESTY;
		
		race = Race.EARTHPONY;

		baseHp = 90;
		baseAtk = 100;
		baseDef = 130;
		baseSpatk = 40;
		baseSpdef = 70;
		baseSpeed = 115; // AJ's supposed to be fast, see ep 113.

		/* Learnable Moves */
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Raging Spree",1);
		learnableMoves.put("Bind Up",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Wild Weed",1);
		learnableMoves.put("Applebuck",22);	
		learnableMoves.put("Duck Face",39);
		learnableMoves.put("Charge",40);
		learnableMoves.put("Hind Kick",45);

		possibleAbilities[0] = "Integrity";
		possibleAbilities[1] = "Simplicity";
	}
}
