//: ability/Shapeless.java

package pokepon.ability;

import pokepon.pony.*;
import pokepon.battle.*;
import pokepon.move.*;
import pokepon.enums.Type;
import pokepon.util.Debug;
import static pokepon.util.MessageManager.*;
import java.util.*;

/** Shapeless
 * Like Wonder Guard (Smooze's signature ability)
 *
 * @author silverweed
 */

public class Shapeless extends Ability {

	private float boost = 1f;
	private Map<Type,Integer> wks;

	public Shapeless() {
		super("Shapeless");
		briefDesc = "This pony can only be damaged by super-effective moves.";
	}
	
	@Override
	public float changeDamageTakenFrom(Type moveType) {
		if(pony == null) throw new NullPointerException("Pony is null for Shapeless!");
		if(wks == null) wks = TypeDealer.getWeaknesses(pony.getTypes().toArray(new Type[0]));
		printDebug("wks["+moveType+"] = "+wks.get(moveType));
		if(wks.containsKey(moveType)) 
			return (float)wks.get(moveType);
		return 0f;
	}
}
