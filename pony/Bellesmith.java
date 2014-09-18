//: pony/Bellesmith.java

package pokepon.pony;

import pokepon.enums.*;
import pokepon.util.Meta;
import static pokepon.util.Meta.*;
import java.net.*;

/**	Bellesmith
 *	Lacks Spatk and isn't much bulky, but has good Atk and not-bad Speed.
 *
 * @author Tommaso Parolini
 */
public class Bellesmith extends Pony {
	
	public Bellesmith(int _level) {
		super(_level);
		
		name = "Bellesmith";
		type[0] = Type.LOYALTY;
		type[1] = Type.KINDNESS;

		race = Race.UNICORN;

		baseHp = 75;
		baseAtk = 90;
		baseDef = 55;
		baseSpatk = 50;
		baseSpdef = 79;
		baseSpeed = 91;

		canon = false;
		
		/* learnableMoves ... */
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Whirling Hoof",1);
		learnableMoves.put("Whine",1);
		learnableMoves.put("Treat",1);
		learnableMoves.put("Taunt",1);
		learnableMoves.put("Talk Out",1);
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Sneak Attack",1);
		learnableMoves.put("Sharp Nails",1);
		learnableMoves.put("Repeat",1);
		learnableMoves.put("Practice",1);
		learnableMoves.put("Love And Care",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Hopeful Strike",1);
		learnableMoves.put("Duck Face",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Crazy Stunt",1);
		learnableMoves.put("Bind Up",1);
	}
}
