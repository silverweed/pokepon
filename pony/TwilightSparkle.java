//: pony/TwilightSparkle.java

package pokepon.pony;

import pokepon.enums.*;

/** Twilight Sparkle
 * 	excels in Special Attack and has an above average Special Defense;
 *	lacks Attack and has low Defense, but average HP.
 *
 * @author silverweed
 */

public class TwilightSparkle extends Pony {
	
	public TwilightSparkle(int _level) {
		super(_level);
		
		name = "Twilight Sparkle";
		type[0] = Type.MAGIC;

		race = Race.UNICORN;

		baseHp = 88;
		baseAtk = 55;
		baseDef = 71;
		baseSpatk = 140; 
		baseSpdef = 127;
		baseSpeed = 64;
		
		/* learnableMoves ... */
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Sonic Barrier",1);
		learnableMoves.put("Meditation",1);
		learnableMoves.put("Enlighten",1);
		learnableMoves.put("Balefire",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Teleport Blast",1);
		learnableMoves.put("Dark Magic",1);
		learnableMoves.put("Scheme Up",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Magic Blast",15);
		learnableMoves.put("Horn Beam",24);
		learnableMoves.put("Freeze Spell",31);
		learnableMoves.put("Scorching Beam",40);
		learnableMoves.put("Friendship Cannon",80);

		/* possibleAbilities */
		possibleAbilities[0] = "Leadership";
	}

}
