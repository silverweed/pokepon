//: move/EternalNight.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.battle.*;
import pokepon.pony.Pony;

/**
 * Spawns Dark weather for 5 turns. 
 *
 * @author silverweed
 */


public class EternalNight extends Move {
	
	public EternalNight() {
		super("Eternal Night");
		
		type = Type.NIGHT;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 10;
		accuracy = -1;
		priority = 0;
		description = "The night will last forever! Changes Weather to Dark for 5 turns.";
		briefDesc = "Spawns Dark weather for 5 turns.";

		animation.put("name","Shake");
		animation.put("sprite","user");
		
		changeWeather = new WeatherHolder(Weather.DARK,5);
	}

	public EternalNight(Pony p) {
		this();
		pony = p;
	}
}
