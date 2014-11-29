//: pony/IronWill.java

package pokepon.pony;

import pokepon.enums.*;

/**	Iron Will
 *	Ludicrous Atk but low SpDef and SpA.
 *	
 *
 * @author Tommaso Parolini
 */
public class IronWill extends Pony {
	
	public IronWill(int _level) {
		super(_level);
		
		name = "Iron Will";
		type[0] = Type.PASSION;
		type[1] = Type.HONESTY;

		race = Race.MYTHICBEAST;
		sex = Sex.MALE;

		baseHp = 105;
		baseAtk = 160;
		baseDef = 80;
		baseSpatk = 40;
		baseSpdef = 35;
		baseSpeed = 105;

		/* Learnable Moves */
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Sharp Nails",1);
		learnableMoves.put("Taunt",1);
		learnableMoves.put("Charge",16);
		learnableMoves.put("Hind Kick",25);
		learnableMoves.put("Rampage",30);
		learnableMoves.put("Scare Away",33);
		learnableMoves.put("Martial Arts",37);
		learnableMoves.put("Bully",42);
		learnableMoves.put("Raging Spree",49);
		learnableMoves.put("Rock Crush",56);
		learnableMoves.put("Holler Out",61);

		possibleAbilities[0] = "Subjection";
		possibleAbilities[1] = "Appeal";
	}
}
