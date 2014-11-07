//: pony/Fancypants.java

package pokepon.pony;

import pokepon.enums.*;

/** Fancypants
 * Quite balanced stats, mostly Def and Spatk.
 *
 * @author silverweed
 */
public class Fancypants extends Pony {
	
	public Fancypants(int _level) {
		super(_level);
		
		name = "Fancypants";
		type[0] = Type.HONESTY;
		type[1] = Type.NIGHT;
		
		race = Race.UNICORN;
		sex = Sex.MALE;

		baseHp = 80;
		baseAtk = 65;
		baseDef = 88;
		baseSpatk = 88;
		baseSpdef = 65;
		baseSpeed = 80;
		
		/* learnableMoves ... */
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Scorching Beam",1);
		learnableMoves.put("Eerie Sonata",1);
		learnableMoves.put("Surprise Hit",1);
		learnableMoves.put("Repeat",1);
		learnableMoves.put("Chatter",1);
		learnableMoves.put("Magic Shield",1);
		learnableMoves.put("Sonic Barrier",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Dodge",1);

		possibleAbilities[0] = "Nocturnality";
	}
}
