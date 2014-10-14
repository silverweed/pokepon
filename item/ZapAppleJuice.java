//: item/ZapAppleJuice.java

package pokepon.item;

import pokepon.battle.*;
import pokepon.net.jack.*;
import static pokepon.util.MessageManager.*;

/** Like Focus Sash
 *
 * @author Giacomo Parolini
 */

public class ZapAppleJuice extends Item {

	public ZapAppleJuice() {
		super("Zap Apple Juice");
		briefDesc = "Prevents user OHKO";
	}

	@Override
	public void onDamage(final BattleEngine be) {
		if(pony.hp() == pony.maxhp() && be.getLatestInflictedDamage() > pony.hp() && !pony.hasSubstitute()) {
			be.setInflictedDamage(pony.hp() - 1);
			if(be.getBattleTask() != null)
				be.getBattleTask().sendB("|battle|"+pony.getNickname()+" drinks its Zap Apple Juice and resists!");
			// consume ourself
			pony.setItem(null);
		}
	}

	@Override
	// not really used
	public String getPhrase() {
		return "[pony] drinks its Zap Apple Juice and resists!";
	}
}
