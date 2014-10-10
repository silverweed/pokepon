//: net/jack/server/ServerOptions.java

package pokepon.net.jack.server;

import java.util.Set;

/** Utility class containing parameters to construct Servers;
 * construct an instance of this class with:
 * ServerOptions.construct().port(12344).address(192...) etc.
 *
 * @author silverweed
 */
public class ServerOptions {

	private ServerOptions() {}

	public static ServerOptions construct() {
		return new ServerOptions();
	}

	public ServerOptions port(int port) {
		this.port = port;
		return this;
	}

	public ServerOptions address(String addr) {
		address = addr;
		return this;
	}

	public ServerOptions verbosity(int verb) {
		verbosity = verb;
		return this;
	}

	public ServerOptions maxNickLen(int maxNL) {
		maxNickLen = maxNL;
		return this;
	}

	public ServerOptions serverName(String name) {
		serverName = name;
		return this;
	}

	public ServerOptions maxClients(int num) {
		maxClients = num;
		return this;
	}

	public ServerOptions database(String db) {
		database = db;
		return this;
	}

	public ServerOptions forbiddenNames(Set<String> fN) {
		if(forbiddenNames == null) {
			forbiddenNames = fN;
		} else {
			forbiddenNames.addAll(fN);
		}
		return this;
	}

	public ServerOptions confFile(String cf) {
		confFile = cf;
		return this;
	}

	public ServerOptions connectPolicy(MultiThreadedServer.ConnectPolicy cP) {
		connPolicy = cP;
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("{ ");
		if(port != -1) sb.append("port: "+port+", ");
		if(verbosity != null) sb.append("verbosity: "+verbosity+", ");
		if(address != null) sb.append("address: "+address+", ");
		if(maxNickLen != -1) sb.append("maxNickLen: "+maxNickLen+", ");
		if(serverName != null) sb.append("serverName: "+serverName+", ");
		if(maxClients != -1) sb.append("maxClients: "+maxClients+", ");
		if(database != null) sb.append("database: "+database+", ");
		if(forbiddenNames != null && !forbiddenNames.isEmpty()) sb.append("forbiddenNames: "+forbiddenNames+", ");
		if(confFile != null) sb.append("confFile: "+confFile+", ");
		if(connPolicy != null) sb.append("connectPolicy: "+connPolicy+", ");
		sb.delete(sb.length()-1, sb.length());
		sb.append(" }");

		return sb.toString();
	}

	int port = -1;
	Integer verbosity = null;
	String address;
	int maxNickLen = -1;
	String serverName;
	int maxClients = -1;
	String database;
	Set<String> forbiddenNames;
	String confFile;
	MultiThreadedServer.ConnectPolicy connPolicy = null;
}
