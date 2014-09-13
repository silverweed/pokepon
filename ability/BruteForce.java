//: ability/BruteForce.java

package pokepon.ability;

import pokepon.battle.*;

/** Adds a % of flinching to all physical moves.
 *
 * @author silverweed
 */

public class BruteForce extends Ability {

	public BruteForce() {
		super("Brute Force");
		briefDesc = "Physical moves have +20% prob. to flinch.";
	}

	@Override
	public void afterMoveHit(final BattleEngine be) {
		if(pony == null)
			throw new NullPointerException("Pony is null for ability BruteForce!");

		if(be.getAttacker() == pony) {
			if(be.getRNG().nextFloat() < 0.2f)
				be.getDefender().setFlinched(true);
		}
	}		
}
