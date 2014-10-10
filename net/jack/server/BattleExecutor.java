//: net/jack/server/BattleExecutor.java

package pokepon.net.jack.server;

import pokepon.net.jack.*;
import pokepon.util.*;
import static pokepon.util.MessageManager.*;
import java.util.*;

/** An Executor specialized for Pokepon Battles; it basically forwards the messages to
 * the correct BattleTask, which interprets it (with a similar mechanism as ClientBattleExecutor).
 *
 * @author silverweed
 */

public class BattleExecutor extends ServerConnectionExecutor {

	protected PokeponServer pServer;
	
	@Override
	public int execute(String msg) {
		
		if(!(server instanceof PokeponServer)) return 0;
		pServer = (PokeponServer)server;

		if(connection.getVerbosity() >= 2) printDebug("Called BattleExecutor.execute(msg="+msg+")");

		if(msg.charAt(0) != BTL_PREFIX) return 0;
	
		/* Battle messages have this syntax: ~battleID |actual|message */
		String[] token = msg.substring(1).split(" ",2);
		String btlID = token[0];

		if(connection.getVerbosity() >= 3) printDebug("btlID="+btlID+",token="+Arrays.asList(token));

		if(pServer.getBattle(btlID) != null)
			pServer.getBattle(btlID).pushMsg(token[1]);
		else if(Debug.on)
			printDebug("[BattleExecutor]: battle not found: "+btlID);


		return 1;
	}
}

	
