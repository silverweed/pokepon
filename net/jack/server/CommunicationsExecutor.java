//: pokepon.net.jack/server/CommunicationsExecutor.java

package pokepon.net.jack.server;

import pokepon.net.jack.*;
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
		
		if(cmd.equals("pong")) {
			// fills this connection's pong queue with a new pong
			// (to be processed by the Connection's Pinger)
			((ServerConnection)connection).getPinger().add();
			return 1;
		} else if(cmd.equals("mynick")) {
			//send to client the nick of its current connection
			connection.sendMsg(CMN_PREFIX+"setnick "+connection.getName());
			return 1;
		} else if(cmd.equals("servername")) {
			//send to client the server name
			connection.sendMsg(CMN_PREFIX+server.getName());
			return 1;
		} else if(cmd.equals("youros")) {
			//sends to client the server OS
			connection.sendMsg(CMN_PREFIX+"myos "+System.getProperty("os.name")+" "+System.getProperty("os.version"));
			return 1;
		} else if(cmd.equals("myos")) {
			//receives and sets this connection's OS
			if(token.length != 2) return 1;
			connection.setOS(token[1]);
			if(connection.getVerbosity() >= 2) printDebug("Set "+connection.getName()+"'s OS to "+token[1]);
			return 1;
		}
		return 1;
	}
}
