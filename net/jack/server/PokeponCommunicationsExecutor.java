//: net/jack/server/PokeponCommunicationsExecutor.java

package pokepon.net.jack.server;

import pokepon.net.jack.*;
import static pokepon.util.MessageManager.*;
import java.util.*;

/** This class extends the normal CommunicationsExecutor implementing
 * pokepon-specific communications.
 *
 * @author silverweed
 */

class PokeponCommunicationsExecutor extends CommunicationsExecutor {

	@Override
	public int execute(String msg) {

		if(msg.charAt(0) != CMN_PREFIX) return 0;

		String[] token = msg.substring(1).split(" ");
		String cmd = token[0];
	
		if(connection.getVerbosity() >= 3) printDebug("cmd="+cmd+",token="+Arrays.asList(token).toString());

		if(cmd.equals("team")) {
			connection.lockReading();
			return 1;
		}
		return super.execute(msg);

	}
}
