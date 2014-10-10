//: pokepon.net.jack/server/NameValidatingServer.java

package pokepon.net.jack.server;

import pokepon.net.jack.*;

/** Interface for a server with name validation policy.
 *
 * @author Giacomo Parolini
 */
public interface NameValidatingServer extends Server {
	
	public int maxNickLen();
	public boolean isValidName(String name);
	
}
