//: pony/Smooze.java

package pokepon.pony;

import pokepon.enums.*;

/** Smooze
 *	A strange guy with a Wonder Guard-like ability; 
 *	good SpA and decent Speed, but only 1 HP.
 *
 * @author silverweed
 */
public class Smooze extends Pony {
	
	public Smooze(int _level) {
		super(_level);
		
		name = "Smooze";
		type[0] = Type.CHAOS;
		type[1] = Type.MAGIC;
		
		race = Race.MYTHICBEAST;
		sex = Sex.MALE;

		baseHp = 1;
		baseAtk = 40;
		baseSpatk = 99;
		baseDef = 30;
		baseSpdef = 45;
		baseSpeed = 75;
		
		/* learnableMoves ... */
		learnableMoves.put("Slimy Trap",1);
		learnableMoves.put("Snuggle",1);
		learnableMoves.put("Bind Up",1);
		learnableMoves.put("Kinetic Strike",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Dimension Twist",1);
		learnableMoves.put("Surprise Hit",1);
		learnableMoves.put("Wreak Havoc",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Joke",1);
		learnableMoves.put("Chaos Burst",20);

		/* abilities */
		possibleAbilities[0] = "Shapeless";
	}
}
