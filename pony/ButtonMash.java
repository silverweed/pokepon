//: pony/ButtonMash.java

package pokepon.pony;

import pokepon.enums.*;
import pokepon.util.Meta;
import static pokepon.util.Meta.*;
import java.net.*;

/**	Button Mash
 *	Generally low stats, with exploitable HP and decent defenses.
 *
 * @author Giacomo Parolini
 */
public class ButtonMash extends Pony {
	
	public ButtonMash(int _level) {
		super(_level);
		
		name = "Button Mash";
		type[0] = Type.PASSION;

		race = Race.EARTHPONY;
		sex = Sex.MALE;

		baseHp = 85;
		baseAtk = 60;
		baseDef = 75;
		baseSpatk = 50;
		baseSpdef = 60;
		baseSpeed = 70;

		/* Learnable Moves */
		learnableMoves.put("Nap",1);
		learnableMoves.put("Mustache",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Burning Powder",1);
		learnableMoves.put("Surprise Hit",1);
		learnableMoves.put("Taunt",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Repeat",1);
		learnableMoves.put("Whine",1);
		learnableMoves.put("Dissonance",1);
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Crazy Stunt",1);
		learnableMoves.put("Rock Throw",1);

		possibleAbilities[0] = "High Scorer";
	}
}
