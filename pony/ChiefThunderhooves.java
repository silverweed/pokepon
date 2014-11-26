//: pony/ChiefThunderhooves.java

package pokepon.pony;

import pokepon.enums.*;
import pokepon.util.Meta;
import static pokepon.util.Meta.*;
import java.net.*;

/**	Chief Thunderhooves
 *	Excels in physical defense and HP and has good speed;
 *	Poor SpA.
 *
 * @author Giacomo Parolini
 */
public class ChiefThunderhooves extends Pony {
	
	public ChiefThunderhooves(int _level) {
		super(_level);
		
		name = "Chief Thunderhooves";
		type[0] = Type.SPIRIT;
		type[1] = Type.LOYALTY;

		race = Race.UNGULATE;
		sex = Sex.MALE;

		baseHp = 100;
		baseAtk = 70;
		baseDef = 95;
		baseSpatk = 35;
		baseSpdef = 65;
		baseSpeed = 95;

		/* Learnable Moves */
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Rectify",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Wild Weed",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Holler Out",1);
		learnableMoves.put("Stampede",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Speed Up",25);
		learnableMoves.put("Charge",36);
		learnableMoves.put("Rampage",50);

		possibleAbilities[0] = "Brute Force";
		possibleAbilities[1] = "Stubborn";
		possibleAbilities[2] = "Ataraxy";
	}
}
