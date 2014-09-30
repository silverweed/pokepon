//: pokepon.net.jack/client/GUIExecutor.java

package pokepon.net.jack.client;

import pokepon.net.jack.*;
import static pokepon.util.MessageManager.*;
import javax.swing.*;

class GUIExecutor extends ClientConnectionExecutor {

	@Override
	public int execute(String msg) {
		if(!(client instanceof GUIClient)) return 0;
		if(msg == null) {
			if(connection.getVerbosity() >= 1) printDebug("Received null from server.");
			return 2;
		}
		if(msg.length() == 0) {
			if(connection.getVerbosity() >= 3) printDebug("Message length = 0. Not printing.");
			return 1;
		}
		
		try {
			final String mesg = msg;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					((GUIClient)client).append(mesg);
				}
			});
		} catch(Exception e) {
			printDebug("[GUIExecutor] Caught exception while printing message: "+e);
			return 0;
		}
		return 1;	
	}	
}
