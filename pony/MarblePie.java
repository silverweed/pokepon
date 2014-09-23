//: pony/MarblePie.java

package pokepon.pony;

import pokepon.enums.*;

/**	MarblePie
 *	Very good defenses and HP, decent physical attack and speed;
 *	Lacks special attack.
 *
 * @author 
 */
public class MarblePie extends Pony {
	
	public MarblePie(int _level) {
		super(_level);
		
		name = "Marble Pie";
		type[0] = Type.KINDNESS;
		type[1] = Type.HONESTY;
		
		race = Race.EARTHPONY;

		baseHp = 90;
		baseAtk = 70;
		baseDef = 100;
		baseSpatk = 30;
		baseSpdef = 90;
		baseSpeed = 70;

		/* Learnable Moves */
		learnableMoves.put("Rock Throw",1);
		learnableMoves.put("Heavy Massage",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Talk Out",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Practice",1);
		learnableMoves.put("Sneak Attack",1);
		learnableMoves.put("Dodge",1);
	}
}
