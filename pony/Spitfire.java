//: pony/Spitfire.java

package pokepon.pony;

import pokepon.enums.*;

/**	Spitfire
 *	Excels in attack and speed;
 *	Poor defenses / HP.
 *
 * @author Giacomo Parolini
 */
public class Spitfire extends Pony {
	
	public Spitfire(int _level) {
		super(_level);
		
		name = "Spitfire";
		type[0] = Type.PASSION;
		type[1] = Type.LOYALTY;

		race = Race.PEGASUS;

		baseHp = 75;
		baseAtk = 125;
		baseDef = 65;
		baseSpatk = 40;
		baseSpdef = 85;
		baseSpeed = 135;

		/* Learnable Moves */
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Night Wind",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Trailblazer",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Crazy Stunt",1);
		learnableMoves.put("Practice",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Speed Up",25);
		learnableMoves.put("Charge",36);
		learnableMoves.put("Sky Dive",40);

		possibleAbilities[0] = "Devotion";
	}
}
