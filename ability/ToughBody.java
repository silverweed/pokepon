//: ability/ToughBody.java

package pokepon.ability;

import pokepon.pony.*;
import pokepon.enums.*;
import pokepon.battle.*;
import static pokepon.util.MessageManager.*;

/** ToughBody
 * Reduces super-effective damage by 25% (before applying modifiers);
 * therefore, without bonus, a 2x damage becomes 1.5x and a 4x becomes 3x.
 *
 * @author Giacomo Parolini
 */

public class ToughBody extends Ability {

	public ToughBody() {
		super("Tough Body");
		briefDesc = "Super-effective damage is reduced by 25%.";
	}

	@Override
	public float changeDamageTakenFrom(Type t) {
		if(pony == null) throw new NullPointerException("Pony is null for "+this+"!");
		if(pony.getWeaknesses().containsKey(t)) 
			return 0.75f;
		else
			return 1f;
	}
}
