//: pony/Scootaloo.java

package pokepon.pony;

import pokepon.enums.*;

/**	Scootaloo
 *	Excels in speed, very good attack;
 *	Poor defenses and special attack.
 *
 * @author Tommaso Parolini
 */
public class Scootaloo extends Pony {
	
	public Scootaloo(int _level) {
		super(_level);
		
		name = "Scootaloo";
		type[0] = Type.PASSION;

		race = Race.PEGASUS;
		sex = Sex.FEMALE;

		baseHp = 70;
		baseAtk = 110;
		baseDef = 65;
		baseSpatk = 50;
		baseSpdef = 60;
		baseSpeed = 130;

		/* Learnable Moves */
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Stalking",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Raging Spree",1);
		learnableMoves.put("Crazy Stunt",1);
		learnableMoves.put("Holler Out",1);
		learnableMoves.put("Practice",1);
		learnableMoves.put("Relay Race",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Speed Up",37);
		learnableMoves.put("Duck Face",49);
	}
}
