//: pony/MaudPie.java

package pokepon.pony;

import pokepon.enums.*;
import pokepon.util.Meta;
import static pokepon.util.Meta.*;
import java.net.*;

/**	MaudPie
 *	Good balanced stats, with a high Atk but terrible Speed.
 *
 * @author silverweed
 */
public class MaudPie extends Pony {
	
	public MaudPie(int _level) {
		super(_level);
		
		name = "Maud Pie";
		type[0] = Type.HONESTY;
		
		race = Race.EARTHPONY;
		sex = Sex.FEMALE;

		baseHp = 90;
		baseAtk = 100;
		baseDef = 80;
		baseSpatk = 70;
		baseSpdef = 90;
		baseSpeed = 20;

		/* Learnable Moves */
		learnableMoves.put("Rock Throw",1);
		learnableMoves.put("Hidden Talent",1);
		learnableMoves.put("Mirror Pond",1);
		learnableMoves.put("Daredevilry",1);
		learnableMoves.put("Relax",1);
		learnableMoves.put("Dodge",1);
		learnableMoves.put("Rock Crush",40);
		learnableMoves.put("Boulder Bomb",60);

		possibleAbilities[0] = "Indifference";
		possibleAbilities[1] = "Sense Of Danger";
		possibleAbilities[2] = "Tough Body";
	}
}
