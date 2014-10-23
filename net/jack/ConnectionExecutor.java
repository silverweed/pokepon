//: pokepon.net.jack/ConnectionExecutor.java

package pokepon.net.jack;

/** Abstract class for executors, a class which processes a received message from a client 
 * and 'executes' it (e.g sends it to other connected clients, interprets
 * commands, etc.)
 */
public abstract class ConnectionExecutor {
	
	/** The constructor doesn't set connection and server, because these are
	 * set by the Connection to which the executor is linked via addConnectionExecutor.
	 */
	public ConnectionExecutor() {}
	
	public abstract int execute(String msg);
	
	public final void setConnection(Connection connection) {
		this.connection = connection;
	}

	protected Connection connection;
	
}
