//: move/Snuggle.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;
import pokepon.battle.*;
import pokepon.util.Debug;
import static pokepon.util.MessageManager.*;

/**
 * Like Fake Out
 *
 * @author 
 */
public class Snuggle extends Move {
	
	public Snuggle() {
		super("Snuggle");
		
		type = Type.LOVE;
		moveType = Move.MoveType.PHYSICAL;
		contactMove = true;
		maxpp = pp = 16;
		baseDamage = 35;
		accuracy = 100;
		priority = 3;
		description = "Show your enemy a little affection. Hits first, but fails if not used the first turn. Flinches target.";
		briefDesc = "Fails unless it's the user's<br>first turn on the field.<br>100% to flinch target. Priority +3";

		targetFlinch = 1f;

		animation.put("name","Fade");
		animation.put("sprite","user");
		animation.put("fadeOut",true);
		animation.put("initialPoint","ally");
		animation.put("finalPoint","opp");
		animation.put("bounceBack",false);
		animation.put("persistent",true);
		animation.put("rewind",true);
	}

	public Snuggle(Pony p) {
		this();
		pony = p;
	}

	@Override
	public boolean validConditions(final BattleEngine be) {
		if(pony == null)
			throw new NullPointerException("Pony is null for Snuggle!");
		if(Debug.on) printDebug("[Snuggle] activeTurns = "+pony.activeTurns);
		return pony.activeTurns <= 1;
	}
}
