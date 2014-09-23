//: pony/ButtonsMom.java

package pokepon.pony;

import pokepon.enums.*;
import pokepon.util.Meta;
import static pokepon.util.Meta.*;
import java.net.*;

/**	ButtonsMom
 *	Good HP and physical stats.
 *
 * @author Giacomo Parolini
 */
public class ButtonsMom extends Pony {
	
	public ButtonsMom(int _level) {
		super(_level);
		
		name = "Button's Mom";
		type[0] = Type.LOVE;
		type[1] = Type.KINDNESS;

		race = Race.EARTHPONY;

		baseHp = 135;
		baseAtk = 89;
		baseDef = 90;
		baseSpatk = 48;
		baseSpdef = 50;
		baseSpeed = 63;

		canon = false;
		
		/* Learnable Moves */
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Heavy Massage",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Holler Out",1);
		learnableMoves.put("Love And Care",1);
		learnableMoves.put("Joke",1);
		learnableMoves.put("Snuggle",1);
		learnableMoves.put("Glomp",1);
		learnableMoves.put("Entrench",1);
		learnableMoves.put("Duck Face",1);
		learnableMoves.put("Talk Out",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Treat",1);
		learnableMoves.put("Hopeful Strike",1);
		learnableMoves.put("Dodge",1);
	}
}
