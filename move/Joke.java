//: move/Joke.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * The basic move of Discord
 *
 * @author Giacomo Parolini
 */

public class Joke extends Move {
	
	public Joke() {
		super("Joke");
		
		type = Type.CHAOS;
		moveType = Move.MoveType.PHYSICAL;
		maxpp = pp = 25;
		baseDamage = 50;
		accuracy = 100;
		priority = 0;
		description = "Chaos everywhere! This move inverts the effects of Weaknesses and Resistances";
		briefDesc = "Swaps weakness and resistance<br>for damage calculation.";

		animation.put("name","Ballistic");
		animation.put("sprite","user");

		invertWeaknessAndResistance = true;
	}
	
	public Joke(Pony p) {
		this();
		pony = p;
	}
}
