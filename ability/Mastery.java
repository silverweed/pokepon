//: ability/Mastery.java

package pokepon.ability;

import pokepon.ability.*;
import pokepon.battle.*;

/** Like SkillLink, but works with any multi-hit move
 *
 * @author silverweed
 */

public class Mastery extends Ability {

	public Mastery() {
		super("Mastery");
		briefDesc = "Ensures maximum number of hits in a multi-hit move.";
		maximizeHits = true;
	}
}
