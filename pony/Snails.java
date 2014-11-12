//: pony/Snails.java

package pokepon.pony;

import pokepon.enums.*;
import pokepon.util.Meta;
import static pokepon.util.Meta.*;
import java.net.*;

/**	Snails
 *	Generally low stats, with good Def but low Speed.
 *
 * @author silverweed
 */
public class Snails extends Pony {
	
	public Snails(int _level) {
		super(_level);
		
		name = "Snails";
		type[0] = Type.LOYALTY;
		type[1] = Type.SPIRIT;

		race = Race.UNICORN;
		sex = Sex.MALE;

		baseHp = 64;
		baseAtk = 39;
		baseDef = 119;
		baseSpatk = 77;
		baseSpdef = 68;
		baseSpeed = 33;

		/* Learnable Moves */
		learnableMoves.put("Nap",1);
		learnableMoves.put("Mustache",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Repeat",1);
		learnableMoves.put("Tackle",1);

	}
}
