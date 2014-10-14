//: pokepon.net.jack/server/MultiThreadedServer.java

package pokepon.net.jack.server;

import pokepon.net.jack.*;
import java.net.*;

/** Interface for a concurrent server */
public interface MultiThreadedServer extends Server {

	public Iterable<Connection> getClients();
	/** Broadcast msg to all clients but 'client' (or all if client is null) */
	public void broadcast(Socket client,String msg);
}
