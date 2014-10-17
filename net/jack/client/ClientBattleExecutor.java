//: net/jack/client/ClientBattleExecutor.java

package pokepon.net.jack.client;

import pokepon.net.jack.*;
import pokepon.util.*;
import pokepon.gui.*;
import static pokepon.util.MessageManager.*;
import java.util.concurrent.*;
import java.util.*;

/** This executor basically routes the messages to the correct BattlePanel
 * that interprets them.
 * 
 * @author Giacomo Parolini
 */
class ClientBattleExecutor extends ClientConnectionExecutor {

	protected PokeponClient pClient;

	@Override
	public int execute(String msg) {

		if(!(client instanceof PokeponClient)) return 0;
		pClient = (PokeponClient)client;

		if(connection.getVerbosity() >= 3) printDebug("Called ClientBattleExecutor (msg="+msg+")");
		
		if(msg.charAt(0) != BTL_PREFIX) return 0;
	
		/* Battle messages have this syntax: ~battleID |actual|message */
		String[] token = msg.substring(1).split(" ",2);
		String btlID = token[0];

		if(connection.getVerbosity() >= 3) printDebug("btlID="+btlID+",token="+Arrays.asList(token));

		if(pClient.getBattle(btlID) != null) {
			if(pClient.getOptions().containsKey("logBattle") && pClient.getOptions().get("logBattle").equals("true"))
				pClient.logBattleLine(msg);	
			pClient.getBattle(btlID).interpret(token[1]);
		}
		else if(Debug.on)
			printDebug("[ClientBattleExecutor]: battle not found: "+btlID);

		return 1;
	}
}
