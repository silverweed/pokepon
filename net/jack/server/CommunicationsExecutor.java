//: pokepon.net.jack/server/CommunicationsExecutor.java

package pokepon.net.jack.server;

import pokepon.net.jack.*;
import pokepon.util.*;
import static pokepon.util.MessageManager.*;
import java.security.*;
import java.net.*;

/** An executor which parses commands starting with "!";
 * used for client-server communications
 */

public class CommunicationsExecutor extends ServerConnectionExecutor {

	@Override
	public int execute(String msg) {
		
		if(connection.getVerbosity() >= 3) printDebug("Called CommunicationsExecutor(msg="+msg+")");

		if(msg.charAt(0) != CMN_PREFIX) return 0;

		String[] token = msg.substring(1).split(" ");
		String cmd = token[0];
		
		switch(cmd) {
			case "pong":
				// fills this connection's pong queue with a new pong
				// (to be processed by the Connection's Pinger)
				((ServerConnection)connection).getPinger().add();
				return 1;
			case "mynick":
				//send to client the nick of its current connection
				connection.sendMsg(CMN_PREFIX+"setnick "+connection.getName());
				return 1;
			case "servername":
				//send to client the server name
				connection.sendMsg(CMN_PREFIX+server.getName());
				return 1;
			case "youros":
				//sends to client the server OS
				connection.sendMsg(CMN_PREFIX+"myos "+System.getProperty("os.name")+" "+
						System.getProperty("os.version"));
				return 1;
			case "myos": {
				if (token.length < 2) return 1;
				//receives and sets this connection's OS
				String os = ConcatenateArrays.merge(token, 1);
				connection.setOS(os);
				if(connection.getVerbosity() >= 2) 
					printDebug("Set "+connection.getName()+"'s OS to "+os);
				return 1;
			}
		}
		return 1;
	}
}
