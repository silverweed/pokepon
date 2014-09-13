//: ability/ChaosMagic.java

package pokepon.ability;

import pokepon.battle.*;
import pokepon.pony.*;
import pokepon.enums.*;
import pokepon.util.*;
import pokepon.net.jack.Connection;
import static pokepon.util.MessageManager.*;

/** Chaos Magic
 * Discord's ability: acquire a random resistance during switch-in
 *
 * @author Giacomo Parolini
 */

public class ChaosMagic extends Ability {

	public ChaosMagic() {
		super("Chaos Magic");
		briefDesc = "Acquire a random resistance on switch-in.";
	}

	@Override
	public void onSwitchIn(final BattleEngine be) {
		if(pony.getWeaknesses().size() >= Type.values().length) return;
		boolean ok = true;
		mType = TypeDealer.randomType();
		do {
			for(Type t : pony.getTypes()) {
				if(t.equals(mType)) {
					ok = false;
					break;
				}
			}
		} while(!ok);
		pony.addVolatileEffectiveness(addEntry(mType,0.5f));
		printMsg(pony.getNickname()+" gained resistance to "+mType);
		if(be.getBattleTask() != null) {
			Connection ally = be.getConnection(be.getSide(pony));
			Connection opp = be.getConnection(be.getOppositeSide(pony));
			be.getBattleTask().sendB("|battle|"+pony.getNickname()+" gained resistance to "+mType+"!");
			be.getBattleTask().sendB(ally,"|addpseudo|ally|good|Res."+mType);
			be.getBattleTask().sendB(opp,"|addpseudo|opp|good|Res."+mType);
		}
		if(Debug.on) {
			printDebug(pony.getNickname()+" is now resistant to:");
			for(java.util.AbstractMap.Entry<Type,Integer> en : pony.getResistances().entrySet()) {
				printDebug(en.getKey()+" ("+en.getValue()+"x)");
			}
		}
	}

	@Override
	public void onSwitchOut(final BattleEngine be) {
		if(mType != null) {
			pony.removeVolatileEffectiveness(mType);
			if(Debug.on) printDebug(pony.getNickname()+" lost resistance to "+mType);
		}
	}

	private Type mType;
}
