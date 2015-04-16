//: ability/HotHeaded.java

package pokepon.ability;

import pokepon.pony.*;
import pokepon.battle.*;
import pokepon.move.*;
import static pokepon.util.MessageManager.*;

/** HotHeaded
 * Like Huge Power, but also halves Def.
 *
 * @author silverweed
 */

public class HotHeaded extends Ability {

	private int origAtk = -1;
	private int origDef = -1;

	public HotHeaded() {
		super("Hot Headed");
		briefDesc = "Doubles Atk, halves Def.";
	}

	@Override
	public void onSwitchIn(final BattleEngine be) {
		origAtk = pony.getBaseAtk();
		pony.setBaseStat(Pony.Stat.ATK, origAtk * 2);
		origDef = pony.getBaseDef();
		pony.setBaseStat(Pony.Stat.DEF, origDef / 2);
	}

	@Override
	public void reset() {
		if(origAtk != -1)
			pony.setBaseStat(Pony.Stat.ATK, origAtk);
		if(origDef != -1)
			pony.setBaseStat(Pony.Stat.DEF, origDef);
	}
}
