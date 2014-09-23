//: pony/Octavia.java

package pokepon.pony;

import pokepon.enums.*;

/** Octavia
 * 	basically bulky, but with exploitable Atk as well
 *	lacks speed.
 *
 * @author 
 */
public class Octavia extends Pony {
	
	public Octavia(int _level) {
		super(_level);
		
		name = "Octavia";
		type[0] = Type.MUSIC;
		
		race = Race.EARTHPONY;

		baseHp = 120;
		baseAtk = 79;
		baseDef = 80;
		baseSpatk = 68;
		baseSpdef = 97;
		baseSpeed = 56;
		
		/* learnableMoves ... */
		learnableMoves.put("Rectify",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Eerie Sonata",1);
		learnableMoves.put("Practice",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Dissonance",1);
		learnableMoves.put("Overture",40);

		possibleAbilities[0] = "Indifference";
	}

}
