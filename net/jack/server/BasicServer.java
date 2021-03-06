//: pokepon.net.jack/server/BasicServer.java

package pokepon.net.jack.server;

import pokepon.net.jack.*;
import pokepon.util.*;
import static pokepon.util.MessageManager.*;
import java.net.*;
import java.io.*;
import java.util.*;

/** A basic implementation of a TCP server 
 * 
 * @author silverweed
 */
public class BasicServer implements Server {

	public static final int DEFAULT_PORT = 12344;
	public static final String DEFAULT_CONF_FILE = (Meta.LAUNCHED_FROM_JAR 
								? Meta.getDataURL()
								: Meta.getNetURL()
							).getPath() + Meta.DIRSEP + "server.conf";

	public static final int DEFAULT_VERBOSITY = 1;
	public static final int PING_DELAY = 120;	// seconds
	public static final int PONG_MAX_WAIT = 30;	// seconds

	protected static String confFile = DEFAULT_CONF_FILE;

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
		/*
		if(opts.address != null)
			myAddress = InetAddress.getByName(opts.address);
		else
			myAddress = InetAddress.getLocalHost();
		*/
		loadOptions(opts);
		if(opts.serverName == null && !alreadySetName)
			serverName = getClass().getSimpleName();
		if(verbosity >= 0)
			printDebug("[BasicServer] Constructed with address = "+myAddress+":"+port+", verbosity "+verbosity+" and conf-file "+confFile);
	}

	/** Given a ServerOptions object, applies all non-null options on this server
	 * and quietly rejects unknown ones.
	 */
	public BasicServer loadOptions(ServerOptions opts) {
		if(verbosity >= 2) printDebug("[BasicServer] Called loadOptions("+opts+")");
		try {
			if(opts.address != null)
				myAddress = InetAddress.getByName(opts.address);
			else
				myAddress = InetAddress.getLocalHost();
		} catch(IOException e) {
			printDebug("[BasicServer] WARNING: couldn't resolve address: "+e);
		}
		if(opts.serverName != null) {
			serverName = opts.serverName;
			alreadySetName = true;
		} else if(!alreadySetName) {
			serverName = getClass().getSimpleName();
		}
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
	protected void initialize() throws IOException {
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
			printDebug("Successfully bound socket to [" + ss.getInetAddress().getHostName() + "] " +
				ss.getInetAddress().getHostAddress() + ":" + ss.getLocalPort());
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

	/** (Synchronously) pings a client and waits for its response.
	 * @deprecated Use ServerConnection.Pinger to ping asynchronously.
	 */
	@Deprecated
	public synchronized boolean pingClient(Connection conn) {
		try {
			if(Debug.pedantic) printDebug("["+conn.getName()+"] PING ->");
			conn.getSocket().setSoTimeout(4000);
			conn.sendMsg(CMN_PREFIX+"ping");
			String response = conn.getInput().readLine();
			printDebug("RESPONSE: ^"+response+"$");
			if(response == null)
				return false;
			else if(!response.equals(CMN_PREFIX+"pong")) 
				printDebug("[pingClient("+conn.getName()+")] received "+response
							+ " in response to a ping.");
			if(Debug.pedantic) printDebug("["+conn.getName()+"] <- PONG");
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

	public Date getConnectionTime() { return connectionTime; }
	public InetAddress getAddress() { return myAddress; }
	public String getConfFile() { return confFile; }
	public void setConfFile(String path) {
		confFile = path;
	}
	public int getVerbosity() { return verbosity; }

	// SERVER CONFIGURATION FUNCTIONS
	
	public void printConfiguration(final PrintStream s) {
		s.println("************************");
		s.println("* Server configuration *");
		s.println("************************");
		s.println("- address: "+myAddress);
		s.println("- port: "+port);
		s.println("- serverName: "+serverName);
		s.println("- verbosity: "+verbosity);
		s.println("- confFile: "+confFile);
	}

	public void printConfiguration() {
		printMsg("************************");
		printMsg("* Server configuration *");
		printMsg("************************");
		printMsg("- address: "+myAddress);
		printMsg("- port: "+port);
		printMsg("- serverName: "+serverName);
		printMsg("- verbosity: "+verbosity);
		printMsg("- confFile: "+confFile);
	}

	/** Read command-line arguments and process all those that should be processed
	 * _BEFORE_ reading the conf file, then strip those arguments from args.
	 * @return The stripped array of cmdline arguments
	 */
	public static String[] loadPreConfig(String[] args) {
		List<String> nwargs = new ArrayList<>();
		for(int i = 0; i < args.length; ++i) {
			if(args[i].equals("--conf")) { 
				if(i + 1 < args.length) {
					confFile = args[i+1];
					if(Debug.on) printDebug("[BasicServer] Set conf file to "+confFile);
					++i;
				}
			} else {
				nwargs.add(args[i]);
			}
		}
		return nwargs.toArray(new String[0]);
	}

	protected static ServerOptions readConfigFile(URL file) throws UnknownOptionException {
		return readConfigFile(file, 1);
	}

	/** Reads the configuration file and return a ServerOptions object with the configuration. */
	protected static ServerOptions readConfigFile(URL file, int verbosity) throws UnknownOptionException {
		String line = null;
		List<String> lines = new LinkedList<>();
		if(verbosity >= 1) printDebug("["+MultiThreadedServer.class.getSimpleName()+"] Reading configuration file: "+file.getPath());
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file.getPath()), "UTF-8"))) {
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

		return ServerOptions.parseServerOptions(lines.toArray(new String[0]));
	}

	public String printInfo() {
		StringBuilder sb = new StringBuilder("Info about: "+serverName+" (server)\n");
		sb.append("  IP Address:   "+ss.getInetAddress().getHostAddress()+"\n");
		sb.append("  Hostname:   "+ss.getInetAddress().getHostName()+"\n");
		sb.append("  Uptime:   "+secondsToDate(-connectionTime.getTime()/1000+
			(new Date()).getTime()/1000)+"\n");
		sb.append("  Operating System:   "+System.getProperty("os.name")+" "+System.getProperty("os.version")+"\n");
		return sb.toString();
	}

	//////////////////////// END PUBLIC
	
	protected int port = DEFAULT_PORT;
	protected InetAddress myAddress;
	protected int verbosity = DEFAULT_VERBOSITY;
	protected String serverName;
	protected boolean alreadySetName;
	
	private ServerSocket ss;
	private Date connectionTime;
}


