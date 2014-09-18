//: pony/MilkyWay.java

package pokepon.pony;

import pokepon.enums.*;
import pokepon.util.Meta;
import java.net.*;

/**	Milky Way
 *	Bulky but surprisingly fast (needless to say, much like Miltank)
 *
 * @author Tommaso Parolini
 */
public class MilkyWay extends Pony {
	
	public MilkyWay(int _level) {
		super(_level);
		
		name = "Milky Way";
		type[0] = Type.GENEROSITY;

		race = Race.EARTHPONY;

		baseHp = 100;
		baseAtk = 76;
		baseDef = 100;
		baseSpatk = 34;
		baseSpdef = 69;
		baseSpeed = 96;

		canon = false;
		
		/* learnableMoves ... */
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Startle",1);
		learnableMoves.put("Whine",1);
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Stampede",1);
		learnableMoves.put("Snuggle",1);
		learnableMoves.put("Sonic Barrier",1);
		learnableMoves.put("Shy Away",1);
		learnableMoves.put("Rock Throw",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Lullaby",1);
		learnableMoves.put("Love And Care",1);
		learnableMoves.put("Hopeful Strike",1);
		learnableMoves.put("Glomp",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Stealth Diamonds",1);
	}
}
