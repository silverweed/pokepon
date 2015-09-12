//: pony/Greta.java

package pokepon.pony;

import pokepon.enums.*;

/** Greta
 * 	Good SpA and Speed.
 *
 * @author silverweed
 */
public class Greta extends Pony {
	
	public Greta(int _level) {
		super(_level);
		
		name = "Greta";
		type[0] = Type.SPIRIT;
		type[1] = Type.MUSIC;
		
		race = Race.GRYPHON;
		sex = Sex.FEMALE;

		baseHp = 75;
		baseAtk = 71;
		baseDef = 64;
		baseSpatk = 100;
		baseSpdef = 64;
		baseSpeed = 86;
		
		/* learnableMoves ... */
		learnableMoves.put("Night Wind",1);
		learnableMoves.put("Relax",1);
		learnableMoves.put("One-Two Hit",1);
		learnableMoves.put("Eerie Sonata",1);
		learnableMoves.put("Dissonance",1);
		learnableMoves.put("Meditation",1);
		learnableMoves.put("Talon Strike",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Sneak Attack",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Charge",20);
		learnableMoves.put("Sky Dive",40);

		/* possibleAbilities */
		possibleAbilities[0] = "Self Confidence";
		possibleAbilities[1] = "Tough Body";
	}
}
