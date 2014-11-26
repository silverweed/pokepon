//: pony/DerpyHooves.java

package pokepon.pony;

import pokepon.enums.*;

/**	Derpy Hooves
 *	Very good Atk and Spdef; above average Speed. 
 *
 * @author silverweed
 */
public class DerpyHooves extends Pony {
	
	public DerpyHooves(int _level) {
		super(_level);
		
		name = "Derpy Hooves";
		type[0] = Type.GENEROSITY;
		type[1] = Type.LAUGHTER;

		race = Race.PEGASUS;
		sex = Sex.FEMALE;

		baseHp = 75;
		baseAtk = 95;
		baseDef = 77;
		baseSpatk = 40;
		baseSpdef = 115;
		baseSpeed = 98;

		/* Learnable Moves */
		learnableMoves.put("Hind Kick",25);
		learnableMoves.put("Flit About",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Get Hype",1);
		learnableMoves.put("Balefire",1);
		learnableMoves.put("Joke",1);
		learnableMoves.put("Tackle",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Treat",1);
		learnableMoves.put("Glomp",1);
		learnableMoves.put("Snuggle",1);
		learnableMoves.put("Bubble Burst",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Storm Cloud",34);
		learnableMoves.put("Speed Up",40);
		learnableMoves.put("Sky Dive",60);

		possibleAbilities[0] = "Indifference";
		possibleAbilities[1] = "Mildness";
	}
}
