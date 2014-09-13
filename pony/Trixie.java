//: pony/Trixie.java

package pokepon.pony;

import pokepon.enums.*;

/** Trixie
 * 	good HP, SpA and SpD;
 *	Other stats are average.
 *
 * @author Giacomo Parolini
 */
public class Trixie extends Pony {
	
	public Trixie(int _level) {
		super(_level);
		
		name = "Trixie";
		type[0] = Type.MAGIC;
		type[1] = Type.PASSION;
	
		race = Race.UNICORN;

		baseHp = 90;
		baseAtk = 40;
		baseDef = 77;
		baseSpatk = 100;
		baseSpdef = 95;
		baseSpeed = 68;
		
		/* learnableMoves ... */
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Balefire",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Teleport Blast",1);
		learnableMoves.put("Hopeful Strike",1);
		learnableMoves.put("Taunt",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Magic Blast",15);
		learnableMoves.put("Horn Beam",30);
		learnableMoves.put("Freeze Spell",50);
		learnableMoves.put("Scorching Beam",63);

		/* abilities */
		possibleAbilities[0] = "Boasting";
	}
}
