//: pony/RainbowDash.java

package pokepon.pony;

import pokepon.enums.*;

/**	Rainbow Dash
 *	Outclassing speed and excellent attack, but low
 *	defenses and HP.
 *
 * @author Giacomo Parolini
 */

public class RainbowDash extends Pony {
	
	public RainbowDash(int _level) {
		super(_level);
		
		name = "Rainbow Dash";
		type[0] = Type.LOYALTY;

		race = Race.PEGASUS;
		sex = Sex.FEMALE;

		baseHp = 82;
		baseAtk = 130;
		baseDef = 78;
		baseSpatk = 50;
		baseSpdef = 60;
		baseSpeed = 145; 

		/* Learnable Moves */
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Night Wind",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Nap",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Crazy Stunt",1);
		learnableMoves.put("Holler Out",1);
		learnableMoves.put("Practice",1);
		learnableMoves.put("Relay Race",1);
		learnableMoves.put("Taunt",1);
		learnableMoves.put("Sneak Attack",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Speed Up",25);
		learnableMoves.put("Duck Face",33);
		learnableMoves.put("Sky Dive",42);
		learnableMoves.put("Sonic Rainboom",51);
		learnableMoves.put("Storm Cloud",56);

		possibleAbilities[0] = "Devotion";
		possibleAbilities[1] = "Determination";
	}
}
