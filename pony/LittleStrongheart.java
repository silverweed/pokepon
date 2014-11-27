//: pony/LittleStrongheart.java

package pokepon.pony;

import pokepon.enums.*;

/**	Little Strongheart 
 *	Very good Speed and good Atk; 
 *	Poor SpA.
 *
 * @author silverweed
 */
public class LittleStrongheart extends Pony {
	
	public LittleStrongheart(int _level) {
		super(_level);
		
		name = "Little Strongheart";
		type[0] = Type.SPIRIT;
		type[1] = Type.PASSION;

		race = Race.UNGULATE;
		sex = Sex.FEMALE;

		baseHp = 75;
		baseAtk = 90;
		baseDef = 70;
		baseSpatk = 45;
		baseSpdef = 65;
		baseSpeed = 105;

		/* Learnable Moves */
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Whirling Hoof",1);
		learnableMoves.put("Stalking",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Holler Out",1);
		learnableMoves.put("Stampede",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Speed Up",25);
		learnableMoves.put("Practice",27);
		learnableMoves.put("Hind Kick",30);
	}
}
