//: pony/Trenderhoof.java

package pokepon.pony;

import pokepon.enums.*;
import pokepon.util.Meta;
import static pokepon.util.Meta.*;
import java.net.*;

/** Trenderhoof
 * 	very good HP and SpA;
 *	lacking defenses and mediocre Speed.
 *
 * @author Tommaso Parolini
 */

public class Trenderhoof extends Pony {
	
	public Trenderhoof(int _level) {
		super(_level);
		
		name = "Trenderhoof";
		type[0] = Type.KINDNESS;
		
		race = Race.UNICORN;
		sex = Sex.MALE;

		baseHp = 120;
		baseAtk = 65;
		baseDef = 40;
		baseSpatk = 95; 
		baseSpdef = 40;
		baseSpeed = 60;
		
		/* learnableMoves ... */
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Nap",1);
		learnableMoves.put("Stalking",1);
		learnableMoves.put("Hopeful Strike",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Chatter",14);
	}
}
