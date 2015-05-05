//: pony/TroubleShoes.java

package pokepon.pony;

import pokepon.enums.*;

/**	TroubleShoes
 *	Very good HP and atk, average defenses;
 *	Lacks speed and spatk.
 *
 * @author silverweed
 */
public class TroubleShoes extends Pony {
	
	public TroubleShoes(int _level) {
		super(_level);
		
		name = "Trouble Shoes";
		type[0] = Type.CHAOS;
		type[1] = Type.LAUGHTER;

		race = Race.EARTHPONY;
		sex = Sex.MALE;

		baseHp = 135;
		baseAtk = 90;
		baseDef = 88;
		baseSpatk = 42;
		baseSpdef = 65;
		baseSpeed = 59;

		/* Learnable Moves */
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Hind Kick",1);
		learnableMoves.put("Wreak Havoc",1);
		learnableMoves.put("Startle",1);
		learnableMoves.put("Shy Away",1);
		learnableMoves.put("Crazy Stunt",1);
		learnableMoves.put("Rampage",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Entrench",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Applebuck",15);
		learnableMoves.put("Relax",30);

	}
}
