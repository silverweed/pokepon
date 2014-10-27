//: net/jack/server/ServerOptions.java

package pokepon.net.jack.server;

import pokepon.util.*;
import static pokepon.util.MessageManager.*;
import java.util.*;

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

	public ServerOptions minNickLen(int maxNL) {
		minNickLen = maxNL;
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

	public ServerOptions defaultNick(String dN) {
		defaultNick = dN;
		return this;
	}

	public ServerOptions welcomeMessage(String wM) {
		welcomeMessage = wM;
		return this;
	}

	public ServerOptions maxBattles(int mB) {
		maxBattles = mB;
		return this;
	}

	public ServerOptions advancedChat(Boolean b) {
		advancedChat = b;
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
		if(welcomeMessage != null) sb.append("welcomeMessage: "+welcomeMessage+", ");
		if(maxBattles != -1) sb.append("maxBattles: "+maxBattles+", ");
		sb.delete(sb.length()-1, sb.length());
		sb.append(" }");

		return sb.toString();
	}

	int port = -1;
	Integer verbosity = null;
	String address;
	int maxNickLen = -1;
	int minNickLen = -1;
	String serverName;
	int maxClients = -1;
	String database;
	Set<String> forbiddenNames;
	String confFile;
	MultiThreadedServer.ConnectPolicy connPolicy = null;
	String defaultNick;
	String welcomeMessage;
	int maxBattles = -1;
	Boolean advancedChat = null;


	protected static ServerOptions parseServerOptions(String[] args) throws UnknownOptionException {
		return parseServerOptions(args, 1);
	}

	/** Parses an array of strings and fills a ServerOptions object accordingly.
	 * @return The ServerOptions object with the configuration specified by the args
	 */
	protected static ServerOptions parseServerOptions(String[] args, int verbosity) throws UnknownOptionException {
		LinkedList<String> opts = new LinkedList<String>(Arrays.asList(args)); 
		ServerOptions srvopts = ServerOptions.construct();
		
		while(opts.size() > 0) {
			String token = opts.remove(0);
			if(verbosity >= 2)
				printDebug("token: ^"+token+"$");
			if(token.matches("^(-p|--port)$")) {
				try {
					srvopts.port = Integer.parseInt(opts.remove(0));
					if(verbosity >= 1) printDebug("port set to "+srvopts.port);
				} catch(IndexOutOfBoundsException|IllegalArgumentException e) {
					printDebug("[ ERROR ] expected integer after 'port' option.");
					System.exit(2);
				}
			} else if(token.matches("^--(serverN|n)ame$")) {
				try {
					srvopts.serverName = parseTillNextOpt(opts);
					if(verbosity >= 1) printDebug("name set to "+srvopts.serverName);
				} catch(IndexOutOfBoundsException e) {
					printDebug("[ ERROR ] expected token after 'name' option.");
				}
			} else if(token.matches("^-v+?$") || token.equals("--verbosity")) {
				if((token.equals("-v") || token.equals("--verbosity")) && opts.size() > 0 && !opts.get(0).startsWith("-")) {
					try {
						srvopts.verbosity = Integer.parseInt(opts.remove(0));
						if(verbosity >= 1) printDebug("verbosity set to "+srvopts.verbosity);
					} catch(IndexOutOfBoundsException|IllegalArgumentException e) {
						printDebug("[ ERROR ] expected integer after 'verbosity' option.");
						System.exit(2);
					}
				} else {
					srvopts.verbosity = token.substring(1).length();
					if(verbosity >= 1) printDebug("verbosity set to "+srvopts.verbosity);
				}
			} else if(token.matches("^(-m|--max-clients)$")) {
				try {
					srvopts.maxClients = Integer.parseInt(opts.remove(0));
					if(verbosity >= 1) printDebug("maxClients set to "+srvopts.maxClients);
				} catch(IndexOutOfBoundsException|IllegalArgumentException e) {
					printDebug("[ ERROR ] expected integer after 'max-clients' option.");
					System.exit(2);
				}
			} else if(token.matches("^(-d|--database)$")) {
				try {
					srvopts.database(parseTillNextOpt(opts));
				} catch(IndexOutOfBoundsException e) {	
					printDebug("[ ERROR ] expected token after 'database' option.");
					System.exit(2);
				}
			} else if(token.matches("^(-i|--ip)$")) {
				try {
					srvopts.address = opts.remove(0);
				} catch(IndexOutOfBoundsException e) {
					printDebug("[ ERROR ] expected ip address after 'ip' option.");
					System.exit(2);
				}
			} else if(token.equals("--forbid")) {
				try {
					srvopts = srvopts.forbiddenNames(new HashSet<String>(Arrays.asList(parseTillNextOpt(opts).split("\\s+"))));
					if(verbosity >= 1) printDebug("Added "+srvopts.forbiddenNames+" to forbidden names.");
				} catch(IndexOutOfBoundsException e) {
					printDebug("[ ERROR ] expected list of strings after 'forbid' option.");
					e.printStackTrace();
					System.exit(2);
				}
			} else if(token.matches("^(-c|--(connect(ion)?-)?policy)$")) {
				try {
					String pol = opts.remove(0);
					if(pol.toLowerCase().equals("paranoid")) 
						srvopts.connPolicy = MultiThreadedServer.ConnectPolicy.PARANOID;
					else if(pol.toLowerCase().equals("average") || 
						pol.toLowerCase().equals("default") || 
						pol.toLowerCase().equals("normal")
					) 
						srvopts.connPolicy = MultiThreadedServer.ConnectPolicy.AVERAGE;
					else if(pol.toLowerCase().equals("permissive")) 
						srvopts.connPolicy = MultiThreadedServer.ConnectPolicy.PERMISSIVE;
					else {
						printDebug("[ ERROR ] unknown value for 'policy' flag.");
						System.exit(2);
					}
				} catch(IndexOutOfBoundsException e) {
					printDebug("[ ERROR ] expected 'paranoid','average' or 'permissive' after 'policy' flag.");
					e.printStackTrace();
					System.exit(2);
				}

			} else if(token.equals("--default-nick")) {
				try {
					srvopts.defaultNick = opts.remove(0);
				} catch(IndexOutOfBoundsException e) {
					printDebug("[ ERROR ] expected string after 'default-nick' option.");
					e.printStackTrace();
					System.exit(2);
				}
			} else if(token.equals("--welcome-message") || token.equals("--motd")) {
				try {
					srvopts.welcomeMessage = parseTillNextOpt(opts); 
				} catch(IndexOutOfBoundsException e) {
					printDebug("[ ERROR ] expected string after 'welcome-message' option.");
					e.printStackTrace();
					System.exit(2);
				}
			} else if(token.equals("--max-nick-len")) {
				try {
					srvopts.maxNickLen = Integer.parseInt(opts.remove(0));
				} catch(IndexOutOfBoundsException|IllegalArgumentException e) {
					printDebug("[ ERROR ] expected integer after 'max-nick-len' option.");
					e.printStackTrace();
					System.exit(2);
				}
			} else if(token.equals("--min-nick-len")) {
				try {
					srvopts.minNickLen = Integer.parseInt(opts.remove(0));
				} catch(IndexOutOfBoundsException|IllegalArgumentException e) {
					printDebug("[ ERROR ] expected integer after 'min-nick-len' option.");
					e.printStackTrace();
					System.exit(2);
				}
			} else if(token.equals("--max-battles")) {
				try {
					srvopts.maxBattles = Integer.parseInt(opts.remove(0));
				} catch(IndexOutOfBoundsException|IllegalArgumentException e) {
					printDebug("[ ERROR ] expected integer after 'max-battles' option.");
					e.printStackTrace();
					System.exit(2);
				}
			} else if(token.matches("^(-C|--advanced-chat)$")) {
				try {
					String val = opts.remove(0);
					if(val != null && (val.equalsIgnoreCase("false") || val.equalsIgnoreCase("no")))
						srvopts.advancedChat = false;
					else
						srvopts.advancedChat = true;
				} catch(IndexOutOfBoundsException|IllegalArgumentException e) {
					srvopts.advancedChat = true;
				}
			} else {
				if(!token.matches("^(-h|--help)$"))
					throw new UnknownOptionException(token);
				else
					throw new UnknownOptionException();
			}
		}

		if(verbosity >= 2) printDebug("Opts to change after configuration: "+srvopts);
		return srvopts;
	}

	/** Given a list of options and arguments, like:
	 * --ip 192.168.0.100 --name My Server Name -vvv
	 * returns the string between the first argument and the next one
	 * (in the example, would return "My Server Name".
	 * The passed opts list gets stripped in the process.
	 */
	protected static String parseTillNextOpt(LinkedList<String> opts) {
		String arg = "";
		
		String tmp = opts.poll();
		if(!tmp.startsWith("-"))
			arg += tmp+" ";
		while((tmp = opts.poll()) != null && !tmp.startsWith("-"))
			arg += tmp+" ";
		
		arg = arg.trim();

		// if last string removed was the next option, re-insert it in the opts list.
		if(tmp != null)
			opts.add(0, tmp);

		return arg;
	}
}
