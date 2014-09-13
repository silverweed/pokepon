//: pokepon.net.jack/server/ServerConnectionExecutor.java

package pokepon.net.jack.server;
import pokepon.net.jack.*;
import pokepon.net.jack.server.*;

abstract class ServerConnectionExecutor extends ConnectionExecutor {

	protected MultiThreadedServer server;
	
	public final void setServer(MultiThreadedServer server) {
		this.server = server;
	}
}
