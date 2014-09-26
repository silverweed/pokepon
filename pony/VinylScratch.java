//: pony/VinylScratch.java

package pokepon.pony;

import pokepon.enums.*;

/** Vinyl Scratch
 *   	oddly enough, she has great SpAtk and Def but little Atk and SpDef;
 *	speed and HP are good as well
 *
 * @author Tommaso Parolini
 */

public class VinylScratch extends Pony {
	
	public VinylScratch(int _level) {
		super(_level);
		
		name = "Vinyl Scratch";
		type[0] = Type.MUSIC;
		type[1] = Type.CHAOS;

		race = Race.UNICORN;

		baseHp = 95;
		baseAtk = 45;
		baseDef = 96;
		baseSpatk = 125;
		baseSpdef = 69;
		baseSpeed = 70;
		
		/* learnableMoves ... */
		learnableMoves.put("Night Wind",1);
		learnableMoves.put("Startle",1);
		learnableMoves.put("Snuggle",1);
		learnableMoves.put("nap",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Sonic Barrier",1);
		learnableMoves.put("Get Hype",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Raging Spree",1);
		learnableMoves.put("Holler Out",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Dissonance",1);
		learnableMoves.put("Bass Drop",36);
		learnableMoves.put("Bass Cannon",50);
	}

}
