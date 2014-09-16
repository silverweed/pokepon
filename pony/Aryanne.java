//: pony/Aryanne.java

package pokepon.pony;

import pokepon.enums.*;
import pokepon.util.Meta;
import static pokepon.util.Meta.*;
import java.net.*;

/**	Aryanne
 *	Viable for physical wall-breaking and revenge-killing
 *
 * @author 
 */
public class Aryanne extends Pony {
	
	public Aryanne(int _level) {
		super(_level);
		
		name = "Aryanne";
		type[0] = Type.LOYALTY;

		race = Race.EARTHPONY;

		baseHp = 65;
		baseAtk = 122;
		baseDef = 80;
		baseSpatk = 88;
		baseSpdef = 65;
		baseSpeed = 55;

		canon = false;
		
		/* learnableMoves ... */
		learnableMoves.put("Charge",1);
		learnableMoves.put("Surprise Hit",1);
		learnableMoves.put("Stalking",1);
		learnableMoves.put("Scheme Up",1);
		learnableMoves.put("Scare Away",1);
		learnableMoves.put("Raging Spree",1);
		learnableMoves.put("Power Display",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Inner Focus",1);
		learnableMoves.put("Hind Kick",1);
		learnableMoves.put("Entrench",1);
		learnableMoves.put("Bully",1);
		learnableMoves.put("Bind Up",1);
	}
}
