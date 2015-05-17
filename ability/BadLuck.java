//: ability/BadLuck.java

package pokepon.ability;

import pokepon.pony.*;
import pokepon.pony.Pony.Stat;
import pokepon.battle.*;
import pokepon.move.*;
import pokepon.net.jack.*;
import static pokepon.util.MessageManager.*;

/** BadLuck
 * Like Slow Start, but only for 3 turns; Trouble Shoes's signature ability
 *
 * @author silverweed
 */

public class BadLuck extends Ability {

	private final static byte DURATION = 3;
	private int origAtk;
	private int origSpeed;

	public BadLuck() {
		super("Bad Luck");
		briefDesc = "Upon switch-in, this pony has its Atk and Spe halved for 3 turns.";
	}
	
	@Override
	public void afterSwitchIn(final BattleEngine be) {
		if(pony == null) throw new NullPointerException("Pony is null for Bad Luck!");
		
		origAtk = pony.getBaseAtk();
		origSpeed = pony.getBaseSpeed();
		pony.setBaseStat(Stat.ATK, origAtk/2);
		pony.setBaseStat(Stat.SPEED, origSpeed/2);

		final Connection allyC = be.getConnection(be.getSide(pony));
		final Connection oppC = be.getConnection(be.getOppositeSide(pony));

		if(be.getBattleTask() != null) {
			be.getBattleTask().sendB(allyC, "|addpseudo|ally|bad|Bad Luck");
			be.getBattleTask().sendB(oppC, "|addpseudo|opp|bad|Bad Luck");
			be.getBattleTask().sendB("|battle|"+pony.getNickname()+" is oppressed by its Bad Luck!");
		}

		BattleEvent callback = new BattleEvent(DURATION + 1, name, pony) {
			@Override
			public void onTurnEnd(final BattleEngine be) {
				Pony ap = be.getTeam(be.getSide(pony)).getActivePony();
				if(ap != pony) {
					// suicide if the source is switched out
					count = 0;
					return;
				}
				if(--count == 0 && !pony.isFainted()) {
					pony.setBaseStat(Stat.ATK, origAtk);
					pony.setBaseStat(Stat.SPEED, origSpeed);
					if(be.getBattleTask() != null) {
						be.getBattleTask().sendB("|battle|"+pony.getNickname()+
								" is no more oppressed by Bad Luck!");
						be.getBattleTask().sendB(allyC, "|rmpseudo|ally|Bad Luck");
						be.getBattleTask().sendB(allyC, "|resultanim|ally|neutral|Bad Luck ended!");
						be.getBattleTask().sendB(oppC, "|rmpseudo|opp|Bad Luck");
						be.getBattleTask().sendB(oppC, "|resultanim|opp|neutral|Bad Luck ended!");
					}
				}
			}
		};
		
		be.addBattleEvent(callback);
	}
}
