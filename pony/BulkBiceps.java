//: pony/BulkBiceps.java

package pokepon.pony;

import pokepon.enums.*;

/**	Bulk Biceps
 *	Uber Atk but low defenses and SpA;
 *	Still, has a decent amount of speed.
 *
 * @author silverweed
 */
public class BulkBiceps extends Pony {
	
	public BulkBiceps(int _level) {
		super(_level);
		
		name = "Bulk Biceps";
		type[0] = Type.PASSION;

		race = Race.PEGASUS;

		baseHp = 80;
		baseAtk = 170;
		baseDef = 65;
		baseSpatk = 60;
		baseSpdef = 55;
		baseSpeed = 60;

		/* Learnable Moves */
		learnableMoves.put("Charge",10);
		learnableMoves.put("Heavy Massage",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Raging Spree",1);
		learnableMoves.put("Holler Out",1);
		learnableMoves.put("Practice",1);
		learnableMoves.put("Relay Race",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Hind Kick",23);
		learnableMoves.put("Inner Focus",30);
	}
}
