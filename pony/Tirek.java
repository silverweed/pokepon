//: pony/Tirek.java

package pokepon.pony;

import pokepon.enums.*;
import pokepon.util.Meta;
import static pokepon.util.Meta.*;
import java.net.*;

/** Tirek
 * 	Relatively meager stats, but with OP ability.	
 *
 * @author Giacomo Parolini
 */
public class Tirek extends Pony {
	
	public Tirek(int _level) {
		super(_level);
		
		name = "Tirek";
		type[0] = Type.SHADOW;
		type[1] = Type.MAGIC;

		race = Race.MYTHICBEAST;
		sex = Sex.MALE;

		baseHp = 90;
		baseAtk = 60;
		baseDef = 80;
		baseSpatk = 70;
		baseSpdef = 80;
		baseSpeed = 85;
		
		/* learnableMoves ... */
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Power Display",1);
		learnableMoves.put("Daredevilry",1);
		learnableMoves.put("Dark Magic",1);
		learnableMoves.put("Surprise Hit",1);
		learnableMoves.put("Scheme Up",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Magic Blast",1);	
		learnableMoves.put("Freeze Spell",10);
		learnableMoves.put("Telekinesis",35);
		learnableMoves.put("Shadow Mist",50);

		/* possibleAbilities */
		possibleAbilities[0] = "Magic Drain";
	}
}
