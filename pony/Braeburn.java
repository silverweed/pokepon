//: pony/Braeburn.java

package pokepon.pony;

import pokepon.enums.*;

/** Braeburn
 * Quite balanced stats, with higher defs than atks,
 * and good hp.
 *
 * @author silverweed
 */
public class Braeburn extends Pony {
	
	public Braeburn(int _level) {
		super(_level);
		
		name = "Braeburn";
		type[0] = Type.HONESTY;
		type[1] = Type.GENEROSITY;
		
		race = Race.EARTHPONY;
		sex = Sex.MALE;

		baseHp = 75;
		baseAtk = 90;
		baseDef = 90;
		baseSpatk = 47;
		baseSpdef = 63;
		baseSpeed = 85;
		
		/* learnableMoves ... */
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Entrench",1);
		learnableMoves.put("Treat",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Taunt",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Applebuck",35);
	}
}
