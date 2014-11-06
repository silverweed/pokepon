//: pony/Roxie.java

package pokepon.pony;

import pokepon.enums.*;
import static pokepon.util.Meta.*;
import java.net.*;

/** Roxie
 * Good Atk and Speed, but low bulk
 *
 * @author Giacomo Parolini
 */

public class Roxie extends Pony {
	
	public Roxie(int _level) {
		super(_level);
		
		name = "Roxie";
		type[0] = Type.LOVE;
		type[1] = Type.SPIRIT;
		
		race = Race.EARTHPONY;
		sex = Sex.FEMALE;

		baseHp = 60;
		baseAtk = 115;
		baseDef = 50;
		baseSpatk = 70;
		baseSpdef = 65;
		baseSpeed = 108;
		
		/* learnableMoves ... */
		learnableMoves.put("Heavy Massage",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Talk Out",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Snuggle",1);
		learnableMoves.put("Chatter",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Relax",1);
		learnableMoves.put("Tackle",4);
		learnableMoves.put("Sneak Attack",28);
		learnableMoves.put("Raging Spree",30);
		learnableMoves.put("Glomp",33);

		possibleAbilities[0] = "Appeal";
	}
}
