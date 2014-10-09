//: pony/Flam.java

package pokepon.pony;

import pokepon.enums.*;

/** Flam
 * Quite balanced stats, with higher defs than atks,
 * and good hp.
 *
 * @author Giacomo Parolini
 */
public class Flam extends Pony {
	
	public Flam(int _level) {
		super(_level);
		
		name = "Flam";
		type[0] = Type.CHAOS;
		type[1] = Type.LAUGHTER;
		
		race = Race.UNICORN;
		sex = Sex.MALE;

		baseHp = 98;
		baseAtk = 60;
		baseDef = 75;
		baseSpatk = 65;
		baseSpdef = 92;
		baseSpeed = 75;
		
		/* learnableMoves ... */
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Sonic Barrier",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Taunt",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Chatter",18);
		learnableMoves.put("Magic Blast",20);
		learnableMoves.put("Applebuck",35);
		learnableMoves.put("Horn Beam",44);
		learnableMoves.put("Freeze Spell",56);
	}
}
