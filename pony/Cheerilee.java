//: pony/Cheerilee.java

package pokepon.pony;

import pokepon.enums.*;

/**	Cheerilee
 *	Balanced stats;
 *	Poor SpA but good HP.
 *
 * @author silverweed
 */
public class Cheerilee extends Pony {
	
	public Cheerilee(int _level) {
		super(_level);
		
		name = "Cheerilee";
		type[0] = Type.LOVE;
		type[1] = Type.KINDNESS;

		race = Race.EARTHPONY;
		sex = Sex.FEMALE;

		baseHp = 95;
		baseAtk = 76;
		baseDef = 90;
		baseSpatk = 50;
		baseSpdef = 85;
		baseSpeed = 74;

		/* Learnable Moves */
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Jingle",1);
		learnableMoves.put("Heavy Massage",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Talk Out",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Treat",1);
		learnableMoves.put("Hopeful Strike",1);
		learnableMoves.put("Bind Up",1);
		learnableMoves.put("Dodge",1);
	}
}
