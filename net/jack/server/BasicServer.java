//: pokepon.net.jack/server/BasicServer.java

package pokepon.net.jack.server;

import pokepon.net.jack.*;
import pokepon.util.*;
import static pokepon.util.MessageManager.*;
import java.net.*;
import java.io.*;
import java.util.*;

/** A basic implementation of TCP server 
 * 
 * @author Giacomo Parolini
 */
public class BasicServer implements Server {

	public static final int DEFAULT_PORT = 12344;
	protected static final int DEFAULT_VERBOSITY = 1;

	protected static String confFile = (Meta.LAUNCHED_FROM_JAR ? Meta.getDataURL() : Meta.getNetURL()).getPath() +
					Meta.DIRSEP + "server.conf";
	static {
		if(Debug.on) printDebug("[BasicServer] CONF_FILE = "+confFile);
	}

	public BasicServer() throws IOException {
		this(ServerOptions.construct()
			.port(DEFAULT_PORT)
			.verbosity(DEFAULT_VERBOSITY)
		);
	}

	/** Create ServerSocket and assign port, but don't bind it yet;
	 * Accepted opts: port, address, verbosity, confFile, serverName.
	 */
	public BasicServer(ServerOptions opts) throws IOException {
		ss = new ServerSocket();
		if(opts.address != null)
			myAddress = InetAddress.getByName(opts.address);
		else
			myAddress = InetAddress.getLocalHost();
		loadOptions(opts);
		if(verbosity >= 0)
			printDebug("["+serverName+"] Constructed with address = "+myAddress+":"+port+", verbosity "+verbosity+" and conf-file "+confFile);
	}

	/** Given a ServerOptions object, applies all non-null options on this server
	 * and quietly rejects unknown ones.
	 */
	public BasicServer loadOptions(ServerOptions opts) {
		if(Debug.on) printDebug("["+serverName+"] Called loadOptions("+opts+")");
		try {
			if(opts.address != null)
				myAddress = InetAddress.getByName(opts.address);
			else
				myAddress = InetAddress.getLocalHost();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		if(opts.serverName != null)
			serverName = opts.serverName;
		else
			serverName = BasicServer.class.getSimpleName();
		if(opts.port != -1)
			port = opts.port;
		if(opts.verbosity != null)
			verbosity = opts.verbosity;
		if(opts.confFile != null)
			confFile = opts.confFile;
		return this;
	}

	public void setAddress(String address) throws IOException {
		myAddress = InetAddress.getByName(address);
	}

	/** Bind socket to port (only works if port was already given in constructor, else
	 * use create(int port); Note that you can only start the server on the local host.	
	 */
	public void initialize() throws IOException {
		if(verbosity >= 1) printDebug("Attempting to bind server to port "+port+"...");
		if(port == -1) {
			throw new RuntimeException("Error: port was not selected.");
		}
		if(ss.isBound()) {
			throw new RuntimeException("Error: server already bound to port " + ss.getLocalPort());
		}

		try {
			ss.bind(new InetSocketAddress(myAddress,port)); 
			connectionTime = new Date();
		} catch(IOException e) {
			throw new IOException("caught in BasicServer.start()",e);
		}

		if(ss.isBound()) {
			printDebug("Successfully bound socket to [" + ss.getInetAddress().getHostName() + "] " + ss.getInetAddress().getHostAddress() + ":" + ss.getLocalPort());
		} else {
			throw new RuntimeException("Error: couldn't bind socket to port " + port);
		}
	}

	/** Assign port to 'port' and bind socket */
	public void initialize(int port) throws IOException {
		if(verbosity >= 1) printDebug("Attempting to bind server to port "+port+"...");
		if(ss.isBound()) {
			throw new RuntimeException("Error: server already bound to port " + ss.getLocalPort());
		}
		
		this.port = port;
		
		try {
			printDebug("Creating server socket...");
			ss.bind(new InetSocketAddress(myAddress,port));
			connectionTime = new Date();
		} catch(IOException e) {
			throw new IOException("caught in BasicServer.start("+port+")",e);
		}

		if(ss.isBound()) {
			printDebug("Successfully bound socket to [" + ss.getInetAddress().getHostName() + "] " + ss.getInetAddress().getHostAddress() + ":" + ss.getLocalPort());
		} else {
			throw new RuntimeException("Error: couldn't bind socket to port " + port);
		}
	}

	public void start() throws IOException {
		initialize();
	}

	public Socket accept() throws IOException {
		if(!ss.isBound()) {
			printDebug("Cannot accept connections: server is not active.");
			return null;
		}
		Socket s = ss.accept();
		if(verbosity >= 1) printDebug("Received connection from " + s);
		return s;
	}
	
	public boolean shutdown() {
		if(!ss.isBound()) {
			printDebug("Cannot close server: was not active.");
			return false;
		}
		else {
			try {
				ss.close();
			} catch(IOException e) {
				printDebug("Caught IOException in shutdown(): " + e);
			}
			printDebug("Server successfully closed.");
			return true;
		}
	}

	public void status(String... pre) {
		String p = "";
		for(String s : pre) p += s;
		consoleMsg(p+"ServerSocket: " + ss);
		consoleMsg(p+"ss.isBound: "+ss.isBound());
	}

	public String getName() {
		return serverName;
	}

	public synchronized boolean pingClient(Connection conn) {
		try {
			conn.getSocket().setSoTimeout(4000);
			conn.sendMsg(CMN_PREFIX+"ping");
			String response = conn.getInput().readLine();
			if(response == null)
				return false;
			else if(!response.equals(CMN_PREFIX+"pong")) 
				consoleDebug("[pingClient("+conn.getName()+")] received "+response
							+ " in response to a ping.");
			return true;
		} catch(SocketTimeoutException e) {
			consoleDebug("[pingClient("+conn.getName()+")] Timeout.");
			return false;
		} catch(IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				conn.getSocket().setSoTimeout(0);
			} catch(Exception e) {
				e.printStackTrace();
				return false;
			}
		}
	}

	public ServerSocket getServerSocket() { return ss; }
	public Date getConnectionTime() { return connectionTime; }
	public InetAddress getAddress() { return myAddress; }

	// SERVER CONFIGURATION FUNCTIONS
	public void loadConfiguration(ServerOptions opts) {
		if(opts.port != -1) {
			if(verbosity >= 2) printDebug("[loadConfiguration] set port to "+opts.port);
			port = opts.port;
		}
		if(opts.address != null) {
			try {
				myAddress = InetAddress.getByName(opts.address);
				if(verbosity >= 2) printDebug("[loadConfiguration] set address to "+opts.address);
			} catch(IOException e) {
				printDebug("[loadConfiguration] invalid address: "+e);
			}
		}
		if(opts.serverName != null)
			serverName = opts.serverName;
		if(opts.verbosity != null)
			verbosity = opts.verbosity;

		if(verbosity >= 1) printDebug("[BasicServer] loaded configuration.");
	}

	/** Read command-line arguments and process all those that should be processed
	 * _BEFORE_ reading the conf file, then strip those arguments from args.
	 * @return The stripped array of cmdline arguments
	 */
	public String[] loadPreConfig(String[] args) {
		List<String> nwargs = new ArrayList<>();
		for(int i = 0; i < args.length; ++i) {
			if(args[i].equals("--conf")) { 
				if(i + 1 < args.length) {
					confFile = args[i+1];
					++i;
				}
			} else {
				nwargs.add(args[i]);
			}
		}
		return nwargs.toArray(new String[0]);
	}

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
			} else {
				if(!token.matches("^(-h|--help)$"))
					throw new UnknownOptionException(token);
				else
					throw new UnknownOptionException();
			}
		}

		if(verbosity >= 0) printDebug("Opts to change after configuration: "+srvopts);
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

		// if last string removed was the next option, re-insert it in the opts list.
		if(tmp != null)
			opts.add(0, tmp);

		return arg;
	}

	protected static ServerOptions readConfigFile(URL file) throws UnknownOptionException {
		return readConfigFile(file, 1);
	}

	/** Reads the configuration file and return a ServerOptions object with the configuration. */
	protected static ServerOptions readConfigFile(URL file, int verbosity) throws UnknownOptionException {
		String line = null;
		List<String> lines = new LinkedList<>();
		if(verbosity >= 1) printDebug("["+MultiServerTest.class.getSimpleName()+"] Reading configuration file: "+file.getPath());
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file.getPath())))) {
			if(verbosity >= 1) printDebug("[ OK ] Configuration file is readable.");
			while((line = reader.readLine()) != null) {
				line = line.trim();
				if(line.startsWith("#") || line.length() == 0) continue;
				
				String[] token = line.split(":",2);
				
				lines.add("--"+token[0].trim());
				lines.add(token[1].trim());
			}
		} catch(FileNotFoundException e) {
			printDebug("[ WARNING ] Configuration file not found: "+e);
		} catch(IOException e) {
			printDebug("[ WARNING ] IOException while reading conf file: "+e);
		}

		return parseServerOptions(lines.toArray(new String[0]));
	}
	//////////////////////// END PUBLIC
	
	protected int port = DEFAULT_PORT;
	protected InetAddress myAddress;
	protected int verbosity = DEFAULT_VERBOSITY;
	protected String serverName;
	
	private ServerSocket ss;
	private Date connectionTime;
}


