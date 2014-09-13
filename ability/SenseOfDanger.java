//: ability/SenseOfDanger.java

package pokepon.ability;

import pokepon.pony.*;
import pokepon.battle.*;
import pokepon.move.*;
import pokepon.enums.Type;
import pokepon.util.Debug;
import static pokepon.util.MessageManager.*;

/** SenseOfDanger
 * Maud Pie's signature ability: Base Speed becomes 100 and Def halves while HP is less or equal to 1/3.
 *
 * @author Giacomo Parolini
 */

public class SenseOfDanger extends Ability {

	private float boost = 1f;
	private int origSpe = -1;
	private int origDef = -1;

	public SenseOfDanger() {
		super("Sense Of Danger");
		briefDesc = "Base Speed becomes 100 and Def halves when HP is less or equal than 1/3";
	}
	
	@Override
	public void beforeTurnStart(final BattleEngine be) {
		if(pony == null) throw new NullPointerException("Pony is null for Sense Of Danger!");
		if(origSpe == -1)
			origSpe = pony.getBaseSpeed();
		if(origDef == -1)
			origDef = pony.getBaseDef();

		if(pony.hp() <= pony.maxhp() / 3) {
			pony.setBaseStat("spe",100);
			pony.setBaseStat("def",origDef/2);
		} else {
			pony.setBaseStat("spe",origSpe);
			pony.setBaseStat("def",origDef);
		}
	}
}
