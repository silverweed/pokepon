//: player/TeamBuilder.java

package pokepon.player;

import pokepon.util.*;
import static pokepon.util.Meta.*;
import java.net.URL;

/** An utility class that allows a Player to build a team.
 *
 * @author silverweed
 */
 
// (will be extended for GUI team builder / CLI team builder) 
 
public abstract class TeamBuilder {

	public TeamBuilder() {
		team = new Team();
	}
	
	/** Abstract method to be overridden */
	public abstract void buildTeam();
	
	protected Team team;
}
