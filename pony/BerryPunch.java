//: pony/BerryPunch.java

package pokepon.pony;

import pokepon.enums.*;

/** Berry Punch
 * 	good HP, Atk and Speed;
 *	Other stats are average.
 *
 * @author silverweed
 */
public class BerryPunch extends Pony {
	
	public BerryPunch(int _level) {
		super(_level);
		
		name = "Berry Punch";
		type[0] = Type.NIGHT;
		type[1] = Type.LAUGHTER;
	
		race = Race.EARTHPONY;
		sex = Sex.FEMALE;

		baseHp = 90;
		baseAtk = 86;
		baseDef = 75;
		baseSpatk = 64;
		baseSpdef = 69;
		baseSpeed = 81;
		
		/* learnableMoves ... */
		learnableMoves.put("Stalking",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Get Hype",1);
		learnableMoves.put("Nap",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Scare Away",1);
		learnableMoves.put("Treat",1);
		learnableMoves.put("Sneak Attack",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Venom Potion",30);

		possibleAbilities[0] = "Simplicity";
		possibleAbilities[1] = "Nocturnality";
		possibleAbilities[2] = "Pest Resilience";
	}
}
