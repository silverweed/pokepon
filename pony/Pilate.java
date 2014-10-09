//: pony/Pilate.java

package pokepon.pony;

import pokepon.enums.*;
import pokepon.util.Meta;
import static pokepon.util.Meta.*;
import java.net.*;

/**	Pilate
 *	Good specials but lacks speed and attack.
 *
 * @author Tommaso Parolini
 */
public class Pilate extends Pony {
	
	public Pilate(int _level) {
		super(_level);
		
		name = "Pilate";
		type[0] = Type.KINDNESS;
		type[1] = Type.NIGHT;

		race = Race.ZEBRA;
		sex = Sex.MALE;

		baseHp = 84;
		baseAtk = 42;
		baseDef = 68;
		baseSpatk = 105;
		baseSpdef = 91;
		baseSpeed = 55;

		canon = false;
		
		/* learnableMoves ... */
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Talk Out",1);
		learnableMoves.put("Repeat",1);
		learnableMoves.put("Relay Race",1);
		learnableMoves.put("Nap",1);
		learnableMoves.put("Meditation",1);
		learnableMoves.put("Magic Blast",1);
		learnableMoves.put("Hind Kick",1);
		learnableMoves.put("Enlighten",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Daredevilry",1);
	}
}
