//: pokepon.net.jack/server/NameValidatingServer.java

package pokepon.net.jack.server;

import pokepon.net.jack.*;

/** Interface for a server with name validation policy.
 *
 * @author silverweed
 */
public interface NameValidatingServer extends Server {
	
	public boolean isValidName(String name);
	public int maxNickLen();
	public int minNickLen();
}
