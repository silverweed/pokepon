//: ability/UpperHoof.java

package pokepon.ability;

import pokepon.ability.*;
import pokepon.pony.*;
import pokepon.battle.*;
import pokepon.move.*;
import pokepon.util.Debug;
import static pokepon.util.MessageManager.*;

/** UpperHoof
 * Like Prankster
 *
 * @author Giacomo Parolini
 */

public class UpperHoof extends Ability {

	private Move move;

	public UpperHoof() {
		super("Upper Hoof");
		briefDesc = "Gives +1 priority to Status moves";
	}

	@Override
	public void beforeTurnStart(final BattleEngine be) {
		if(pony == null) throw new NullPointerException("Pony is null for UpperHoof!");
		int side = be.getSide(pony);
		if(Debug.on) printDebug("[Upper Hoof] Chosen move = "+be.getChosenMove(side));
		if(be.getChosenMove(side).getMoveType() == Move.MoveType.STATUS) {
			be.getChosenMove(side).setBonusPriority((byte)1);
		}
	}
}
