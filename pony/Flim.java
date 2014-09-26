//: pony/Flim.java

package pokepon.pony;

import pokepon.enums.*;

/** Flim
 * Quite balanced stats, with higher atks than defs,
 * and good speed.
 *
 * @author Giacomo Parolini
 */
public class Flim extends Pony {
	
	public Flim(int _level) {
		super(_level);
		
		name = "Flim";
		type[0] = Type.CHAOS;
		type[1] = Type.PASSION;
		
		race = Race.UNICORN;

		baseHp = 75;
		baseAtk = 75;
		baseDef = 60;
		baseSpatk = 92;
		baseSpdef = 65;
		baseSpeed = 98;
		
		/* learnableMoves ... */
		learnableMoves.put("Night Wind",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Sonic Barrier",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Raging Spree",1);
		learnableMoves.put("Burning Powder",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Chatter",18);
		learnableMoves.put("Magic Blast",20);
		learnableMoves.put("Applebuck",35);
		learnableMoves.put("Scorching Beam",45);
	}

}
