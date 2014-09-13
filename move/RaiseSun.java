//: move/RaiseSun.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;
import pokepon.battle.*;
import pokepon.gui.animation.*;

/**
 * Celestia's signature move which spawns SUNNY Weather and removes all
 * Status conditions on user.
 *
 * @author silverweed
 */

public class RaiseSun extends Move {
	
	public RaiseSun() {
		super("Raise Sun");
		
		type = Type.LIGHT;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 5;
		accuracy = -1;
		priority = 0;
		description = "Raise the Sun and remove all stats negative conditions.";
		briefDesc = "Spawns Sunny weather for 5 turns.<br>Resets user's negative boosts.";

		animation.put("name","Shake");
		animation.put("sprite","user");

		changeWeather = new WeatherHolder(Weather.SUNNY,5);
		removeUserNegativeStatModifiers = 1f;
	}

	public RaiseSun(Pony p) {
		this();
		pony = p;
	}
}
