//: ability/Bookworm.java

package pokepon.ability;

import pokepon.pony.*;
import pokepon.battle.*;
import pokepon.move.*;
import pokepon.enums.*;
import pokepon.net.jack.*;
import static pokepon.util.MessageManager.*;

/** Bookworm
 * This pony cannot be statused nor damaged by a Magic-type;
 * damaging Magic moves heal it by the same amount.
 *
 * @author silverweed
 */

public class Bookworm extends Ability {

	public Bookworm() {
		super("Bookworm");
		briefDesc = "Magic-type attacks heal instead of doing damage.<br>Immunity to Magic status moves.";
	}

	@Override
	public float preventNegativeCondition(final String which, final BattleEngine be) {
		if(be.getCurrentMove().getType() == Type.MAGIC) return 1f;
		return 0f;
	}

	@Override
	public float changeDamageTakenFrom(Type type) {
		return type == Type.MAGIC ? -1f : 1f;
	}
}
