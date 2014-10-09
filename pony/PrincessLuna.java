//: pony/PrincessLuna.java

package pokepon.pony;

import pokepon.enums.*;

/** Princess Luna
 * 	excels in Special Attack, Special Defense and Speed;
 *	lacks physical Attack, Defense and HP.
 *
 * @author Giacomo Parolini
 */
public class PrincessLuna extends Pony {
	
	public PrincessLuna(int _level) {
		super(_level);
		
		name = "Princess Luna";
		type[0] = Type.MAGIC;
		type[1] = Type.NIGHT;

		race = Race.ALICORN;
		sex = Sex.FEMALE;

		baseHp = 65;
		baseAtk = 66;
		baseDef = 99;
		baseSpatk = 150;
		baseSpdef = 145;
		baseSpeed = 125;
		
		/* learnableMoves ... */
		learnableMoves.put("Night Wind",1);
		learnableMoves.put("Rectify",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Sonic Barrier",1);
		learnableMoves.put("Stalking",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Power Display",1);
		learnableMoves.put("Scare Away",1);
		learnableMoves.put("Holler Out",1);
		learnableMoves.put("Dark Magic",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Magic Blast",5);	
		learnableMoves.put("Magic Shield",21);
		learnableMoves.put("Canterlot Voice",30);
		learnableMoves.put("Friendship Cannon",50);
		learnableMoves.put("Eternal Night",78);

		/* possibleAbilities */
		possibleAbilities[0] = "Subjection";
	}

}
