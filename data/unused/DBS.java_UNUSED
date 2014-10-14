//: pokepon.net.jack/server/DatabaseServer.java

package pokepon.net.jack.server;

import pokepon.net.jack.*;

import java.io.*;
import java.util.*;

/** A server with a database of clients.
 *
 * @author silverweed
 */
public interface DatabaseServer extends Server {
	
	public boolean nickExists(String nick) throws FileNotFoundException;
	public int registerNick(String nick,char[] password) throws FileNotFoundException;
	public boolean checkPasswd(String nick,char[] password) throws FileNotFoundException;
	public List<String> getNicks() throws FileNotFoundException;
	
}
