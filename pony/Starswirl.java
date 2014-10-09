//: pony/Starswirl.java

package pokepon.pony;

import pokepon.enums.*;
import pokepon.util.Meta;
import static pokepon.util.Meta.*;
import java.net.*;

/** Starswirl
 * Very good balanced stats, with prevalently special movepool
 *
 * @author Giacomo Parolini
 */

public class Starswirl extends Pony {
	
	public Starswirl(int _level) {
		super(_level);
		
		name = "Starswirl";
		type[0] = Type.MAGIC;

		race = Race.UNICORN;
		sex = Sex.MALE;

		baseHp = 100;
		baseAtk = 100;
		baseDef = 100;
		baseSpatk = 100; 
		baseSpdef = 100;
		baseSpeed = 100;
		
		/* learnableMoves ... */
		learnableMoves.put("Rectify",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Wild Weed",1);
		learnableMoves.put("Nap",1);
		learnableMoves.put("Surprise Hit",1);
		learnableMoves.put("Stealth Diamonds",1);
		learnableMoves.put("Storm Cloud",1);
		learnableMoves.put("Wreak Havoc",1);
		learnableMoves.put("Talk Out",1);
		learnableMoves.put("Repeat",1);
		learnableMoves.put("Power Display",1);
		learnableMoves.put("Magic Shield",1);
		learnableMoves.put("Lullaby",1);
		learnableMoves.put("Dimension Twist",1);
		learnableMoves.put("Bind Up",1);
		learnableMoves.put("Sonic Barrier",1);
		learnableMoves.put("Meditation",1);
		learnableMoves.put("Enlighten",1);
		learnableMoves.put("Balefire",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Teleport Blast",1);
		learnableMoves.put("Dark Magic",1);
		learnableMoves.put("Scheme Up",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Magic Blast",15);
		learnableMoves.put("Horn Beam",24);
		learnableMoves.put("Freeze Spell",31);
		learnableMoves.put("Scorching Beam",40);
		learnableMoves.put("Glowing Laser",51);

		/* possibleAbilities */
		possibleAbilities[0] = "Self Confidence";
	}
}
