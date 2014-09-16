//: pony/Tracy.java

package pokepon.pony;

import pokepon.enums.*;
import pokepon.util.Meta;
import static pokepon.util.Meta.*;
import java.net.*;

/**	Tracy
 *	Aggressive mixed attacker.	
 *
 * @author Tommaso Parolini
 */
public class Tracy extends Pony {
	
	public Tracy(int _level) {
		super(_level);
		
		name = "Tracy";
		type[0] = Type.SHADOW;

		race = Race.EARTHPONY;

		baseHp = 60;
		baseAtk = 95;
		baseDef = 80;
		baseSpatk = 95;
		baseSpdef = 50;
		baseSpeed = 70;

		canon = false;
		
		/* learnableMoves ... */
		learnableMoves.put("Raging Spree",1);
		learnableMoves.put("Stalking",1);
		learnableMoves.put("Sharp Nails",1);
		learnableMoves.put("Scheme Up",1);
		learnableMoves.put("Scare Away",1);
		learnableMoves.put("Power Display",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Dissonance",1);
		learnableMoves.put("Chaos Burst",1);
		learnableMoves.put("Bully",1);
	}
}
