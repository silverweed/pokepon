//: pony/Homage.java

package pokepon.pony;

import pokepon.enums.*;

/** Homage
 * Good HP, Def and SpA; lacks Atk and SpD and has average Speed.
 *
 * @author silverweed
 */

public class Homage extends Pony {
	
	public Homage(int _level) {
		super(_level);
		
		name = "Homage";
		type[0] = Type.HONESTY;
		type[1] = Type.LOVE;

		race = Race.UNICORN;
		canon = false;

		baseHp = 90;
		baseAtk = 69;
		baseDef = 99;
		baseSpatk = 86; 
		baseSpdef = 66;
		baseSpeed = 75;
		
		/* learnableMoves ... */
		learnableMoves.put("Talk Out",1);
		learnableMoves.put("Love And Care",1);
		learnableMoves.put("Joke",1);
		learnableMoves.put("Glomp",1);
		learnableMoves.put("Freeze Spell",1);
		learnableMoves.put("Bind Up",1);
		learnableMoves.put("Bass Drop",1);
		learnableMoves.put("Heavy Massage",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Enlighten",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Hopeful Strike",1);
		learnableMoves.put("Scheme Up",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Magic Blast",15);
		learnableMoves.put("Telekinesis",22);
		learnableMoves.put("Sharp Nails",39);
		
		possibleAbilities[0] = "Integrity";
	}
}
