//: move/Balefire.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.battle.*;
import pokepon.pony.Pony;
import pokepon.util.Debug;
import pokepon.net.jack.*;
import static pokepon.util.MessageManager.*;

/**
 * Inflicts huge special damage but kills the user.
 *
 * @author silverweed
 */

public class Balefire extends Move {

	public Balefire() {
		super("Balefire");
		type = Type.SPIRIT;
		moveType = Move.MoveType.SPECIAL;
		maxpp = pp = 5;
		baseDamage = 250;
		accuracy = 100;
		priority = 0;
		description = "A Megaspell that uses necromancy to enhance Dragon fire. Can destroy an entire city at once.";
		briefDesc = "The user faints.";

		animation.put("name","Fade");
		animation.put("sprite","fireball.png");
		animation.put("transparent",true);
		animation.put("initialOpacity",1f);
		animation.put("finalOpacity",1f);
		animation.put("initialPoint","opp -40Y");
		animation.put("finalPoint","opp +40Y");
		animation.put("delay",90);
	}

	public Balefire(Pony p) {
		this();
		pony = p;
	}

	@Override
	public BattleEvent[] getBattleEvents() {
		return new BattleEvent[] {
			new BattleEvent(1, name) {
				@Override
				public void afterMoveUsage(final BattleEngine be) {
					if(Debug.on) printDebug("[Balefire] triggering afterMoveUsage...");
					count = 0;
					if(source.isFainted()) return;	//should never happen
					if(be.getBattleTask() != null) {
						Connection ally = be.getConnection(be.getSide(source));
						Connection opp = be.getConnection(be.getOppositeSide(source));
						be.getBattleTask().sendB(ally,"|damage|ally|"+(source.hp()+1));
						be.getBattleTask().sendB(opp,"|damage|opp|"+(source.hp()+1));
						source.damagePerc(100f);
					}
				}
			}
		};
	}
}
