//: ability/SolarMagic.java

package pokepon.ability;

import pokepon.pony.*;
import pokepon.battle.*;
import pokepon.enums.*;
import static pokepon.util.MessageManager.*;

/** SolarMagic
 * Celestia's ability: spawns Sunny weather on switch-in
 *
 * @author Giacomo Parolini
 */

public class SolarMagic extends Ability {
	
	private int duration = 5;

	public SolarMagic() {
		super("Solar Magic");
		briefDesc = "Raises the Sun on switch-in.";
	}

	@Override
	public void afterSwitchIn(final BattleEngine be) {
		if(be.getBattleTask() != null) {
			be.getBattleTask().sendB("|battle|"+pony.getNickname()+"'s magic raises the Sun!");
		}
		be.setWeather(new WeatherHolder(Weather.SUNNY, duration));
	}
}
