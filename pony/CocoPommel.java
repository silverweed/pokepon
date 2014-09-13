//: pony/CocoPommel.java

package pokepon.pony;

import pokepon.enums.*;
import pokepon.util.Meta;
import static pokepon.util.Meta.*;
import java.net.*;

/**	Coco Pommel
 *	Subpar attacks/speed and your average bulk;
 *	should be awarded with a nice support moveset.
 *
 * @author Tommaso Parolini
 */

public class CocoPommel extends Pony {
	
	public CocoPommel(int _level) {
		super(_level);
		
		name = "Coco Pommel";
		type[0] = Type.KINDNESS;
		
		race = Race.EARTHPONY;

		baseHp = 98; 
		baseAtk = 50;
		baseDef = 85;
		baseSpatk = 65;
		baseSpdef = 70;
		baseSpeed = 52;

		/* Learnable moves */
		learnableMoves.put("Shy Away",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Talk Out",1);
		learnableMoves.put("Stalking",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Snuggle",1);
		learnableMoves.put("Hopeful Strike",1);
		learnableMoves.put("Bind Up",1);
		learnableMoves.put("Dodge",1);
		
		possibleAbilities[0] = "Mildness";
	}
}
