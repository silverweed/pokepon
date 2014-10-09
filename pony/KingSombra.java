//: pony/KingSombra.java

package pokepon.pony;

import pokepon.enums.*;

/** King Sombra
 * 	excels in Special Attack and Attack, and has good speed;
 *	lacks defenses and HP.
 *
 * @author Giacomo Parolini
 */
public class KingSombra extends Pony {
	
	public KingSombra(int _level) {
		super(_level);
		
		name = "King Sombra";
		type[0] = Type.SHADOW;

		race = Race.UNICORN;
		sex = Sex.MALE;

		baseHp = 81;
		baseAtk = 150;
		baseDef = 79;
		baseSpatk = 145;
		baseSpdef = 65;
		baseSpeed = 100;
		
		/* learnableMoves ... */
		learnableMoves.put("Night Wind",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Sonic Barrier",1);
		learnableMoves.put("Stalking",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Power Display",1);
		learnableMoves.put("Scare Away",1);
		learnableMoves.put("Dark Magic",1);
		learnableMoves.put("Surprise Hit",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Magic Blast",5);	
		learnableMoves.put("Freeze Spell",26);
		learnableMoves.put("Crystal Shield",35);
		learnableMoves.put("Shadow Mist",40);

		/* possibleAbilities */
		possibleAbilities[0] = "Subjection";
		possibleAbilities[1] = "Spell Refractory";
	}

}
