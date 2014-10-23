//: pokepon.net.jack/client/ClientConnection.java

package pokepon.net.jack.client;

import pokepon.net.jack.*;
import pokepon.gui.*;
import static pokepon.util.MessageManager.*;
import java.net.*;
import java.util.*;

/** The Connection used by a client */

class ClientConnection extends Connection {

	protected Client client;
	
	public ClientConnection(Client client,Socket server,int... verbosityLvl) {
		super(server,verbosityLvl);
		
		this.client = client;	

		addConnectionExecutor(new DefaultConnectionExecutor());
	}

	@Override
	public void run() {
		try {
			/* Allow the server to make some setups before starting loop.
			 * The setup will end as soon as a non-communication mesg is received
			 * OR the CommunicationsExecutor returns 2.
			 */
			String msg = null;
			outer:
			while((msg = input.readLine()).charAt(0) == CMN_PREFIX) {
				middle:
				for(int i = executors.size()-1; i > -1; --i) {
					int result = executors.get(i).execute(msg);
					inner:
					switch(result) {
						case -1:
							disconnect();
							throw new ConnectException();
						case 0:
							break inner;
						case 1:
							break middle;
						case 2:
							break outer;
						default:
							break inner;
					}
				}
			}
		} catch(Exception e) {
			printDebug("Caught exception in ClientConnection.run() <setup>: "+e);
			printDebug(name+" disconnecting.");
			disconnect();
			return;
		}

		super.run();
	}
				
	@Override
	// TODO: client should be able to reconnect without shutting down
	public synchronized void disconnect() {
		if(client instanceof PokeponClient) {
			PokeponClient pClient = (PokeponClient)client;
			Iterator<Map.Entry<String,BattlePanel>> it = pClient.getBattles().entrySet().iterator();
			while(it.hasNext()) {
				Map.Entry<String,BattlePanel> entry = (Map.Entry<String,BattlePanel>)it.next();
				entry.getValue().interpret("|disconnect");
				if(verbosity >= 2) printDebug("Removed battle #"+entry.getKey()+" from battles.");
				it.remove();
			}

		} 
		if(client instanceof GUIClient)	{
			((GUIClient)client).append("#### DISCONNECTED FROM SERVER ####");
			((GUIClient)client).append("To reconnect, quit and restart the client.");
		}

		super.disconnect();
	}

	@Override
	public void addConnectionExecutor(ConnectionExecutor exec) {
		exec.setConnection(this);
		if(exec instanceof ClientConnectionExecutor) {
			((ClientConnectionExecutor)exec).setClient(client);
		}
		executors.add(exec);
	}
	
	protected class DefaultConnectionExecutor extends ConnectionExecutor {
		
		@Override
		public int execute(String msg) {
			if(msg == null) {
				if(verbosity >= 1) printDebug("Received null from server.");
				return 2;
			}
			if(msg.length() == 0) {
				if(verbosity >= 3) printDebug("Message length = 0. Not printing.");
				return 1;
			}
		
			if(verbosity >= 3) printDebug("[DefaultConnExec] client is GUIClient? "+(client instanceof GUIClient));
			try {
				if(client instanceof GUIClient) 
					((GUIClient)client).append(msg);
				else
					System.out.println(msg);

			} catch(Exception e) {
				printDebug("[ClientConnection] Caught exception while printing message: "+e);
			}
			return 1;
		}
	}	
}
