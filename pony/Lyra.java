//: pony/Lyra.java

package pokepon.pony;

import pokepon.enums.*;

/** Lyra Heartstrings
 * 	excels in Special Attack and has an above average Special Defense;
 *	lacks Attack and has low Defense, but average HP.
 *
 * @author 
 */

public class Lyra extends Pony {
	
	public Lyra(int _level) {
		super(_level);
		
		name = "Lyra";
		type[0] = Type.MUSIC;
		type[1] = Type.MAGIC;
		
		race = Race.UNICORN;
		sex = Sex.FEMALE;

		baseHp = 100;
		baseAtk = 50;
		baseDef = 85;
		baseSpatk = 120;
		baseSpdef = 60;
		baseSpeed = 85;
		
		/* learnableMoves ... */
		learnableMoves.put("Jingle",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Sonic Barrier",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Eerie Sonata",1);
		learnableMoves.put("Holler Out",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Dissonance",1);
		learnableMoves.put("Magic Blast",15);
		learnableMoves.put("Lullaby",20);
		learnableMoves.put("Freeze Spell",38);
		learnableMoves.put("Horn Beam",44);
		learnableMoves.put("Overture",49);
	}

}
