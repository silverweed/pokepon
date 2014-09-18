//: pony/Canni.java

package pokepon.pony;

import pokepon.enums.*;
import pokepon.util.Meta;
import static pokepon.util.Meta.*;
import java.net.*;

/**	Canni
 *	
 *
 * @author Tommaso Parolini
 */
public class Canni extends Pony {
	
	public Canni(int _level) {
		super(_level);
		
		name = "Canni";
		type[0] = Type.PASSION;
		type[1] = Type.LAUGHTER;

		race = Race.EARTHPONY;

		baseHp = 110;
		baseAtk = 39;
		baseDef = 55;
		baseSpatk = 97;
		baseSpdef = 55;
		baseSpeed = 94;

		canon = false;
		
		/* learnableMoves ... */
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Speed Up",1);
		learnableMoves.put("Startle",1);
		learnableMoves.put("Meditation",1);
		learnableMoves.put("Raging Spree",1);
		learnableMoves.put("Poison Joke",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Holler Out",1);
		learnableMoves.put("Get Hype",1);
		learnableMoves.put("Duck Face",1);
		learnableMoves.put("Chatter",1);
		learnableMoves.put("Bubble Burst",1);
		learnableMoves.put("Bizaam",100);
	}
}
