//: move/PoisonJoke.java

package pokepon.move;

import pokepon.move.*;
import pokepon.enums.*;
import pokepon.move.hazard.*;
import pokepon.pony.*;

/** Hazard move which spreads PoisonJokeHazard through enemy field;
 * Poison Joke poisons non-flying enemies on switch-in, or badly
 * poison if stacked twice.
 *
 * @author silverweed
 */
public class PoisonJoke extends Move {

	public PoisonJoke() {
		super("Poison Joke");

		type = Type.SPIRIT;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 10;
		accuracy = -1;
		priority = 0;
		description = "Plant Poison Joke buds on the enemy fields to poison non-flying foes on switch-in.";
		briefDesc = "Enemies are poisoned on switch-in.<br>Flying races are immune.<br>Up to 2 layers.";

		animation.put("name","Ballistic");
		animation.put("sprite","poisonjoke_small.png");
		animation.put("bounceBack",false);

		hazard = new PoisonJokeHazard();				
	}

	public PoisonJoke(Pony p) {
		this();
		pony = p;
	}
	
	@Override
	public String getPhrase() {
		return hazard.getPhrase();
	}
}


