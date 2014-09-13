//: ability/SelfConfidence.java

package pokepon.ability;

import pokepon.battle.*;
import pokepon.enums.Type;
import pokepon.pony.Pony;
import pokepon.util.Debug;
import static pokepon.util.MessageManager.*;

/** Adds +33% to the damage multiplier when pony uses same-type attacks.
 *
 * @author silverweed
 */

public class SelfConfidence extends Ability {

	public SelfConfidence() {
		super("Self Confidence");
		briefDesc = "Adds +33% bonus to STAB moves.";
	}
	
	@Override
	public float changeDamageDealtBy(Type moveType) {
		if(pony == null) 
			throw new NullPointerException("Pony is null for SelfConfidence!");
		for(int i = 0; i < Pony.MAX_TYPES; ++i) 
			if(pony.getType(i) == moveType) {
				return 1.33f;
			}
		return 1f;
	}		
}
