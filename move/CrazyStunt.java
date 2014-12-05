//: move/CrazyStunt.java

package pokepon.move;

import pokepon.battle.*;
import pokepon.net.jack.*;
/**
 * Like High Jump Kick.
 *
 * @author Giacomo Parolini
 */

import pokepon.enums.*;
import pokepon.pony.*;
import pokepon.player.*;
import pokepon.battle.*;

public class CrazyStunt extends Move {
	
	public CrazyStunt() {
		super("Crazy Stunt");
		
		type = Type.PASSION;
		moveType = Move.MoveType.PHYSICAL;
		contactMove = true;
		maxpp = pp = 20;
		baseDamage = 130;
		accuracy = 85;
		priority = 0;
		description = "";
		briefDesc = "If hit isn't successful, user loses 50% of its HP.";

		animation.put("name","Ballistic");
		animation.put("sprite","user");
	}

	public CrazyStunt(Pony p) {
		this();
		pony = p;
	}

	@Override
	public void onMoveFail(final BattleEngine be) {
		if(pony == null) throw new NullPointerException("Pony is null for CrazyStunt!");
		int dam = pony.damagePerc(50f);
		Connection thisC = be.getConnection(be.getSide(pony));
		Connection thatC = be.getConnection(be.getOppositeSide(pony));
		if(be.getBattleTask() != null) {
			be.getBattleTask().sendB("|battle|"+pony.getNickname()+" failed the stunt and hurt itself!");
			be.getBattleTask().sendB(thisC,"|damage|ally|"+dam);
			be.getBattleTask().sendB(thatC,"|damage|opp|"+dam);
		}
	}
}
