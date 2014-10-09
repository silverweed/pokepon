//: pony/PrincessErroria.java

package pokepon.pony;

import pokepon.enums.*;
import pokepon.util.Meta;
import static pokepon.util.Meta.*;
import java.net.*;

/**	Princess Erroria
 *	Foalish stats, but has access to any signature move.
 *
 * @author Tommaso Parolini
 */
public class PrincessErroria extends Pony {
	
	public PrincessErroria(int _level) {
		super(_level);
		
		name = "Princess Erroria";
		type[0] = Type.LOVE;
		type[1] = Type.CHAOS;

		race = Race.ALICORN;
		sex = Sex.FEMALE;

		baseHp = 90;
		baseAtk = 65;
		baseDef = 65;
		baseSpatk = 40;
		baseSpdef = 40;
		baseSpeed = 15;

		/* learnableMoves ... */
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Trailblazer",1);
		learnableMoves.put("Stare",1);
		learnableMoves.put("Sonic Rainboom",1);
		learnableMoves.put("Raise Sun",1);
		learnableMoves.put("Party Cannon",1);
		learnableMoves.put("Nasty Plot",1);
		learnableMoves.put("Mutate",1);
		learnableMoves.put("Love Burst",1);
		learnableMoves.put("Joke",1);
		learnableMoves.put("Gem Storm",1);
		learnableMoves.put("Furry Coat",1);
		learnableMoves.put("Eternal Night",1);
		learnableMoves.put("Bullet Shower",1);
		learnableMoves.put("Bizaam",1);
		learnableMoves.put("Bass Drop",1);
		learnableMoves.put("Bass Cannon",1);
		learnableMoves.put("Balefire",1);
		learnableMoves.put("Venom Potion",1);
		learnableMoves.put("Lullaby",1);
		learnableMoves.put("Freeze Spell",1);
		learnableMoves.put("Chatter",1);
		learnableMoves.put("Burning Powder",1);
	}
}
