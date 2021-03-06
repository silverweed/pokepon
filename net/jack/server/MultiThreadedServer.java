//: pokepon.net.jack/server/MultiThreadedServer.java

package pokepon.net.jack.server;

import pokepon.net.jack.*;
import pokepon.net.jack.chat.*;
import pokepon.util.*;
import static pokepon.util.MessageManager.*;
import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.net.*;

/** A multi-threaded Server;
 * it uses the Connection class, which can be modularly extended via ConnectionExecutors.
 *
 * @author silverweed
 */
public class MultiThreadedServer extends BasicNameValidatingServer implements AutoCloseable {

	public static final int DEFAULT_MAX_CLIENTS = 100;
	public static enum ConnectPolicy {
		PERMISSIVE, 	// allow all TCP connections
		AVERAGE,	// disallow HTTP requests (but allow clients like netcat and telnet)
		PARANOID	// disallow all but requests compliant with our protocol 
	};

	protected static String serverOpts =
		"\t--conf <conf_file>:             use a different configuration file than the default one.\n"+
		"\t-i,--ip <ip>:                   bind the server to the ip <ip>\n"+
		"\t-p,--port <port>:               listen on port <port> (default: 12344)\n"+
		"\t-v(vv...),-v <verb.Lv>:         set verbosity to <verb.Lv> (-1~4)\n"+
		"\t-m,--max-clients <max-clients>: limit the number of clients allowed to <max-clients>\n"+
		"\t--name <name>:                  set the server name\n"+
		"\t--forbid <list of regexes>:     forbid patterns from being used as chat nicknames\n"+
		"\t-c,--policy <connect-policy>:   change the server connection policy\n"+
		"\t--default-nick <string>:        set the default nick to be given to anon clients\n"+
		"\t--welcome-message <string>:     set a welcome message to be given to clients\n"+
		"\t--min-nick-len <integer>:       set the minimum accepted nickname length\n"+
		"\t--max-nick-len <integer>:       longer nicknames will be truncated to this length\n"+
		"\t-C,--advanced-chat [no]:        enable/disable chat roles and the advanced chat system.\n"+
		"\t--cmd-ban-limit <integer>:      set maximum commands a client can issue in a minute (-1 = infinite)\n"+
		"\t--blacklist <rules_file>:       read IP ban rules from rules_file (default: none)\n"+
		"\t--conn-gc-rate <minutes>:       rate of connection GC in minutes; <= 0 means 'never' (default: 5)\n";
	protected int maxClients = DEFAULT_MAX_CLIENTS;
	protected ThreadPoolExecutor pool;
	/** Queue used by the ThreadPoolExecutor to store tasks */
	protected ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(200*maxClients);
	protected List<Connection> clients = Collections.synchronizedList(new LinkedList<Connection>());
	/** Map { ipclass: isBanned }; allows to override rules for ip subclasses, depending on the
	 * insertion order; for example, if one first bans '*', then unbans '10.0.0.0/24', the server
	 * will only allow connection from that subnet.
	 */
	protected LinkedHashMap<IPClass,Boolean> banRules = new LinkedHashMap<>();
	/** (Optional) File containing ban rules to preload */
	protected String blacklistFile;
	protected boolean advancedChat;
	/** Rate (in minutes) at which connections should be garbage-collected 
	 * by the ConnectionKiller; 0 or negative values means "never".
	 */
	protected int connGCRate = 5;
	/** Connections that should be killed */
	protected Set<ServerConnection> killableConnections = new HashSet<>();
	/** The policy for allowing clients to connect to this server: see server.conf for details */
	ConnectPolicy connectPolicy = ConnectPolicy.AVERAGE;
	/** If null, clients will get a default name of the form clientHostname-N, else
	 * defaultNick-N (with N being the number of connections the server has had so far);
	 * set this parameter in server.conf or with a command line option.
	 */
	String defaultNick;
	/** Message to send to users on connection */
	String welcomeMessage;
	/** ChatSystem used if advancedChat is set to true */
	ChatSystem chat;
	/** If a connection gives more than this number of commands in a minute,
	 * ignore following.
	 */
	int cmdBanLimit = 40;
	/** The queue of messages to broadcast */
	BlockingQueue<Map.Entry<Socket,String>> broadcastMsgQueue = new LinkedBlockingQueue<Map.Entry<Socket,String>>();
	/** Server daemon threads */
	protected Thread broadcaster;
	protected Timer connectionKiller;
	
	public MultiThreadedServer() throws IOException {
		this(ServerOptions.construct());
	}

	/** Accepted options: port, verbosity, address, maxClients, maxNickLen, serverName, confFile */
	public MultiThreadedServer(ServerOptions opts) throws IOException {
		super(opts);
		loadOptions(opts);
		pool = new ThreadPoolExecutor(5*maxClients,200*maxClients,60,TimeUnit.SECONDS,queue) {
			protected void afterExecute(Runnable r, Throwable t) {
				if(r instanceof Connection) {
					Connection c = (Connection)r;
					if(verbosity >= 1) printDebug("Closing connection with "+c.getName()+" ("+c.getSocket()+").");
					synchronized(clients) {
						Iterator<Connection> it = clients.iterator();
						while(it.hasNext()) {
							Connection client = it.next();
							if(client == c) {
								it.remove();
								printDebug("Connection "+c.getName()+" removed from clients list.");
								break;
							}
						}
					}
					if(verbosity >= 1) {
						if(clients.size() < 10)
							printDebug("clients: "+clients+" ("+clients.size()+" / "+maxClients+")");
						else 
							printDebug("clients: "+clients.size()+" / "+maxClients);
						printDebug("Threads num: "+pool.getPoolSize());
						printDebug("Currently active: "+pool.getActiveCount());
						printDebug("Max Thread Num so far: "+pool.getLargestPoolSize());
					}
				}
			}
		};
		if(opts.serverName == null && !alreadySetName)
			serverName = getClass().getSimpleName();
		if(verbosity >= 0)
			printDebug("[MultiThreadedServer] Constructed with maxClients = "+maxClients+" and connectPolicy = "+connectPolicy);
	}

	@Override
	public MultiThreadedServer loadOptions(ServerOptions opts) {
		super.loadOptions(opts);
		if(verbosity >= 2) printDebug("[MultiThreadedServer] Called loadOptions");
		if(opts.maxClients != -1) {
			maxClients = opts.maxClients;
			if(verbosity >= 2) printDebug("[MultiThreadedServer] maxClients set to "+maxClients);
		}
		if(opts.connPolicy != null) {
			connectPolicy = opts.connPolicy;
			if(verbosity >= 2) printDebug("[MultiThreadedServer] connectPolicy set to "+connectPolicy);
		}
		if(opts.defaultNick != null) {
			defaultNick = opts.defaultNick;
			if(verbosity >= 2) printDebug("[MultiThreadedServer] defaultNick set to "+defaultNick);
		}
		if(opts.welcomeMessage != null) {
			welcomeMessage = opts.welcomeMessage;
			if(verbosity >= 2) printDebug("[MultiThreadedServer] welcomeMessage set to "+welcomeMessage);
		}
		if(opts.advancedChat != null) {
			advancedChat = opts.advancedChat;
			if(verbosity >= 2) printDebug("[MultiThreadedServer] advancedChat set to "+advancedChat);
		}
		if(opts.cmdBanLimit != null) {
			cmdBanLimit = opts.cmdBanLimit;
			if(verbosity >= 2) printDebug("[MultiThreadedServer] cmdBanLimit set to "+cmdBanLimit);
		}
		if(opts.blacklistFile != null) {
			blacklistFile = opts.blacklistFile;
			if(verbosity >= 2) printDebug("[MultiThreadedServer] blacklistFile set to "+blacklistFile);
		}
		if(opts.connGCRate != null) {
			connGCRate = opts.connGCRate;
			if(verbosity >= 2) printDebug("[MultiThreadedServer] connGCRate set to "+connGCRate);
		}

		return this;
	}

	/** Reads pre config from CLI, config from conf file and CLI config;
	 * pre-config are options like --conf, which must be processed before reading the conf file;
	 * additional options may be passed via a ServerOptions object: these will be added after
	 * reading the conf file but before applying eventual CLI options.
	 * @param args The command line options
	 * @param opts (optional) additional options
	 */
	public MultiThreadedServer configure(String[] args, ServerOptions... opts) throws MalformedURLException, UnknownOptionException {
		if(verbosity >= 2) printDebug("["+serverName+"] CONFIGURING");

		loadOptions(readConfigFile(new URL("file://"+confFile)));
		for(ServerOptions o : opts) {
			printDebug("Loading additional option: "+o);
			loadOptions(o);
		}
		if(verbosity >= 1) printDebug("["+serverName+"] loaded "+opts.length+" additional options");
		loadOptions(ServerOptions.parseServerOptions(args));
		return this;
	}

	@Override
	protected void initialize() throws IOException {
		super.initialize();
		// Create ChatSystem
		if(advancedChat) {
			chat = new ChatSystem();
			try {
				String chatConf = (Meta.LAUNCHED_FROM_JAR
							? Meta.getDataURL()
							: Meta.getNetURL()
						).getPath() + Meta.DIRSEP + "chat.conf";
				Meta.ensureFileExists(chatConf, Meta.complete2(Meta.NET_DIR) + Meta.DIRSEP + "chat.conf");
				chat.loadConfFromFile(chatConf);
			} catch(Exception e) {
				printDebug("[MultiThreadedServer] Exception while creating chat conf:");
				e.printStackTrace();
			}
		}
		// Create default rules.conf
		Meta.ensureFileExists((Meta.LAUNCHED_FROM_JAR 
						? Meta.getDataURL()
						: Meta.getNetURL()
					).getPath() + Meta.DIRSEP + "rules.conf", Meta.complete2(Meta.NET_DIR + Meta.DIRSEP + "rules.conf"));
		if(blacklistFile != null) {
			File blFile = new File(blacklistFile);
			if(blFile.isFile()) {
				int num = loadBlacklist(blFile);
				if(verbosity >= 0) printDebug("["+serverName+"] loaded "+num+" ban rules from "+blacklistFile);
				if(verbosity >= 2) printDebug("Ban Rules: "+banRules.toString().replaceAll(",","\n "));
			} else {
				printDebug("["+serverName+"] WARNING: blacklist file " + blacklistFile + " couldn't be found!");
			}
		}
		// Start server broadcaster
		broadcaster = new Thread(new Broadcaster());
		broadcaster.setName("Broadcaster");
		broadcaster.setDaemon(true);
		broadcaster.start();

		// Start ConnectionKiller
		if(connGCRate > 0) {
			connectionKiller = new Timer("Connection Killer", true);
			connectionKiller.scheduleAtFixedRate(new ConnectionKiller(), connGCRate * 60 * 1000, connGCRate * 60 * 1000);
		}
	}

	@Override
	public void start() throws IOException {
		initialize();

		consoleHeader(new String[] { " Java Awful Client-server Kit ", "v 3.0" },'*');
		while(!pool.isShutdown()) {
			if(verbosity >= 2) printDebug("Waiting for new connection...");
			Socket newClient = accept();
			synchronized(clients) {
				if(clients.size() >= maxClients) {
					if(verbosity >= 1) printDebug("Client number exceeded: dropping connection from "+newClient+"...");
					ServerConnection.dropWithMsg(newClient,CMN_PREFIX+"drop Couldn't connect: server is full.");
					continue;
				}
			}
			if(isBanned(newClient.getInetAddress().getHostAddress())) {
				if(verbosity >= 1) printDebug("Dropping connection with banned IP: "+newClient);
				ServerConnection.dropWithMsg(newClient,CMN_PREFIX+"drop Your IP is banned from this server.");
				continue;
			}
			Connection newConnection = new ServerConnection(this,newClient,verbosity);
			newConnection.addConnectionExecutor(new CommandsExecutor());
			if(advancedChat)
				newConnection.addConnectionExecutor(new ChatCommandsExecutor());
			newConnection.addConnectionExecutor(new CommunicationsExecutor());
			synchronized(clients) {
				clients.add(newConnection);
			}
			pool.execute(newConnection);
			if(verbosity >= 2) printDebug("Constructed connection.");
		}
	}
	
	public List<Connection> getClients() {
		return clients;
	}

	public Connection getClient(String name) {
		synchronized(clients) {
			Iterator<Connection> it = clients.iterator();
			while(it.hasNext()) {
				Connection c = it.next();
				if(c.getName().equals(name)) 
					return c;
			}
		}
		return null;
	}

	public LinkedHashMap<IPClass,Boolean> getBanRules() { return banRules; }
	
	public boolean isConnected(String name) {
		synchronized(clients) {
			Iterator<Connection> it = clients.iterator();
			while(it.hasNext())
				if(it.next().getName().equals(name)) return true;
		}
		return false;
	}

	/** Send msg to all clients but 'client'. */
	public void broadcast(Socket client, String msg) {
		broadcastMsgQueue.add(new AbstractMap.SimpleEntry<Socket,String>(client, msg));
	}

	/** Forces an user to be disconnected from this server; useful with the advancedChat system. */
	public boolean kickUser(String name, String kicker) {
		synchronized(clients) {
			Iterator<Connection> it = clients.iterator();
			while(it.hasNext()) {
				Connection conn = it.next();
				if(conn.getName().equals(name)) {
					if(kicker != null)
						conn.sendMsg(CMN_PREFIX+"html <b><em><font color='red'>"+kicker+" kicked you out from the server.</font></em></b>");
					else
						conn.sendMsg(CMN_PREFIX+"html <b><em><font color='red'>You were kicked out from the server.</font></em></b>");
					conn.sendMsg(CMN_PREFIX+"disconnect");
					conn.disconnect();
					broadcast(null, name+" was kicked out of the server" + (kicker != null ? " by "+kicker : "")+".");
					if(verbosity >= 2) 
						printDebug("["+serverName+"] kicked "+name+" out of the server." + (kicker != null ? 
							"(kicked by "+kicker+")" : ""));
					return true;
				}
			} 
		}
		if(verbosity >= 2) {
			printDebug("["+serverName+"] kick attempt to "+name+" failed: client not found."+(kicker != null ?
				" (kick attempted by "+kicker+")" : ""));
		}
		return false;
	}

	public boolean kickUser(String name) {
		return kickUser(name, null);
	}

	// FIXME: is there a better approach than always adding the IP (even when already in the map)?
	public void banIP(IPClass ip) {
		synchronized(banRules) {
			banRules.put(ip, true);
		}
		if(verbosity >= 1) printDebug("["+serverName+"] Banned IP: "+ip);
	}

	/** If banRules contains a ban rule for the exact IPClass described by 'ip', remove it;
	 * else, add a banRule { ip, false } to the rules
	 */
	public void unbanIP(IPClass ip) {
		// special case: unban everything - in this case, just clear the rules, since no entry
		// means 'no banned IP'
		synchronized(banRules) {
			if(ip.getClassType() == IPClass.ClassType.EVERYTHING) {
				banRules.clear();
				return;
			}
			Iterator<Map.Entry<IPClass,Boolean>> it = banRules.entrySet().iterator();
			while(it.hasNext()) {
				Map.Entry<IPClass,Boolean> entry = it.next();
				if(entry.getKey().equals(ip) && entry.getValue().equals(true)) {
					it.remove();
					if(verbosity >= 1) printDebug("["+serverName+"] Unbanned IP: "+ip);
					return;
				}
			}
			banRules.put(ip, false);
			if(verbosity >= 1) printDebug("["+serverName+"] Added ALLOW rule for IP: "+ip);
		}
	}

	/** Checks whether the given IP / range is banned by the server
	 * rules; will always return true if an invalid IP is passed.
	 * @return false if the IP can connect to the server, true otherwise.
	 */
	public boolean isBanned(String ipclass) {
		try {
			boolean banned = false;
			synchronized(banRules) {
				// iteration will follow insertion order
				for(Map.Entry<IPClass,Boolean> entry : banRules.entrySet()) 
					if(entry.getKey().includes(ipclass))
						banned = entry.getValue();
			}
			return banned;
		} catch(IllegalArgumentException e) {
			printDebug("["+serverName+".isBanned("+ipclass+")] Illegal IP/range specified.");
			return true;
		}
	}

	/** Marks the given ServerConnection as killable: the server will remove it
	 * from its list when possible.
	 */
	public void scheduleKill(final ServerConnection conn) {
		synchronized(killableConnections) {
			killableConnections.add(conn);
		}
	}

	@Override
	public void close() {
		shutdown();
	}

	@Override
	public synchronized boolean shutdown() {
		if(clients.size() > 0) broadcast(null,"*** SERVER SHUTTING DOWN NOW ***");
		synchronized(clients) {
			Iterator<Connection> it = clients.iterator();
			while(it.hasNext()) {
				Connection client = it.next();
				printDebug(client+": sending disconnect");
				client.sendMsg(CMN_PREFIX+"disconnect");
				printDebug(client+": disconnecting");
				client.disconnect();
			}
		}
		printDebug("shutting down pool");
		pool.shutdownNow();
		try {
			pool.awaitTermination(5,TimeUnit.SECONDS);
		} catch(InterruptedException e) {
			printDebug("Interrupted while awaiting termination.");
		}
		return super.shutdown();
	}

	@Override
	public void printConfiguration(final PrintStream s) {
		super.printConfiguration(s);
		s.println("- maxClients: "+maxClients);
		s.println("- connectPolicy: "+connectPolicy);
		s.println("- welcomeMessage: "+welcomeMessage);
		s.println("- advancedChat: "+advancedChat);
		s.println("- cmdBanLimit: "+cmdBanLimit);
	}

	@Override
	public void printConfiguration() {
		super.printConfiguration();
		printMsg("- maxClients: "+maxClients);
		printMsg("- connectPolicy: "+connectPolicy);
		printMsg("- welcomeMessage: "+welcomeMessage);
		printMsg("- advancedChat: "+advancedChat);
		printMsg("- cmdBanLimit: "+cmdBanLimit);
	}
	
	@Override
	public String printInfo() {
		StringBuilder sb = new StringBuilder(super.printInfo());
		sb.append("  ChatSystem enabled: "+advancedChat+"\n");
		return sb.toString();
	}

	/** Reads a file with ban rules and adds them to banRules;
	 * rules are specified like this:
	 * <pre>
	 * ban
	 *   # this is a comment (middle-line comment are supported)
	 *   # put ONE IP class per line:
	 *   *  # ban all IPs
	 *
	 * unban
	 *   127.0.0.1  # unban local IP
	 *   192.168.0.1/24  # unban local LAN
	 * </pre>
	 * Note that insertion order _matters_.
	 * @return The number of rules added to the server banRules
	 */
	protected int loadBlacklist(File blFile) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(blFile), "UTF-8"))) {
			String line = null;
			boolean inStanza = false;
			boolean errors = false;
			LinkedHashMap<IPClass,Boolean> newRules = new LinkedHashMap<>();
			int stanzaRules = 0;
			boolean isBanStanza = false;
			while((line = reader.readLine()) != null) {
				line = line.trim();
				if(line.length() < 1 || line.charAt(0) == '#') continue;
				// having already considered full-line comments, we should have exactly 1 valid token per line,
				// which is the only one we consider.
				String token = line.split("#", 2)[0].trim();

				if(token.equals("ban") || token.equals("unban")) {
					if(inStanza && stanzaRules == 0) {
						printDebug("[loadBlacklist] WARNING: empty stanza found.");
					}
					inStanza = true;
					stanzaRules = 0;
					isBanStanza = token.equals("ban");
				} else {
					if(!inStanza) {
						printDebug("[loadBlacklist] ERROR: found "+token+" outside a stanza!");
						errors = true;
						break;
					}
					try {
						IPClass ip = new IPClass(token);
						for(IPClass ipc : newRules.keySet())
							if(ipc.equals(ip)) {
								printDebug("[loadBlacklist] WARNING: duplicate entry for IP "+ip+".");
								printDebug("	...(adding anyway, but consider optimizing the rules.)");
								break;
							}
						newRules.put(ip, isBanStanza);
						++stanzaRules;
					} catch(IllegalArgumentException ee) {
						printDebug(ee.getMessage());
						printDebug("[loadBlacklist] ignoring line");
					}
				}
			}
			if(errors) {
				printDebug("[loadBlacklist] ERRORS WERE FOUND: ignoring IP rules.");
				return 0;
			}
			banRules.putAll(newRules);
			return newRules.size();

		} catch(FileNotFoundException e) {
			printDebug("[loadBlacklist] Tried reading from non-existing file!");
		} catch(Exception e) {
			printDebug("[loadBlacklist] Caught exception while reading blacklist file:");
			e.printStackTrace();
		}
		return 0;
	}

	protected static void printUsage() {
		consoleMsg("Usage: "+MultiThreadedServer.class.getSimpleName()+" [opts]\n" +
			"Options are:\n"+ serverOpts + 
			"\nAll the long options can be used in the configuration file as well, with the format option: value(s)\n");
		System.exit(0);
	}

	public static void main(String[] args) {
		MultiThreadedServer server = null;

		try {
			args = loadPreConfig(args);
			server = new MultiThreadedServer();
			server.configure(args).start();
		} catch(IOException e) {
			printDebug("Caught IOException while starting MultiThreadedServer: ");
			server.shutdown();
			e.printStackTrace();
		} catch(UnknownOptionException e) {
			if(!e.isQuiet())
				printDebug("Unknown option: "+e);
			consoleMsg("");
			printUsage();
			consoleMsg("");
		}
	}

	/** This task asynchronously takes messages from the broadcasting queue and sends them
	 * to all clients.
	 */
	protected class Broadcaster implements Runnable {
		public void run() {
			if(verbosity >= 1) printDebug("["+serverName+".Broadcaster] Started.");
			while(true) {
				try {
					Map.Entry<Socket,String> pair = broadcastMsgQueue.take();
					Socket client = pair.getKey();
					String msg = pair.getValue();
					if(verbosity >= 1) {
						printDebug("["+serverName+"] Broadcasting message: "+msg);
						if(verbosity >= 3) printDebug("clients: "+clients);
					}
					synchronized(clients) {
						Iterator<Connection> it = clients.iterator();
						while(it.hasNext()) {
							Connection conn = it.next();
							if(verbosity >= 3) printDebug("Connection: "+conn);
							if(client != null && conn.getSocket().equals(client)) {
								if(verbosity >= 4) printDebug("continuing.");
								continue;
							}
							if(verbosity >= 2) 
								printDebug("["+serverName+"] Sending message to "+conn.getName()+" ("+conn.getSocket()+")");
							conn.getOutput().println(msg);
						}
					}
				} catch(InterruptedException e) {
					printDebug("["+serverName+".Broadcaster] interrupted on take()!");
				}
			}
		}
	}

	/** This task performs a sort of garbage collection of the killableConnections
	 * which may haven't been removed yet from the clients' list; in normal conditions,
	 * this shouldn't be needed, as the ThreadPoolExecutor.afterExecute function 
	 * does this as soon as the connection disconnects, but in rare cases it
	 * may happen that some ghost connection remains alive.
	 */
	protected class ConnectionKiller extends TimerTask {
		public void run() {
			if(verbosity >= 2) printDebug("["+serverName+".ConnectionKiller] Started");
			int count = 0;
			synchronized(killableConnections) {
				Iterator<ServerConnection> it = killableConnections.iterator();
				while(it.hasNext()) {
					ServerConnection conn = it.next();
					if(verbosity >= 2) 
						printDebugnb("["+serverName+".ConnectionKiller] removing "+conn+"...");
					synchronized(clients) {
						Iterator<Connection> cit = clients.iterator();
						while(cit.hasNext()) {
							Connection client = cit.next();
							if(client == conn) {
								if(verbosity >= 2) printDebug("Found.");
								cit.remove();
								it.remove();
								break;
							}
						}
					}
					if(verbosity >= 2) printDebug("Not found.");
					it.remove();
				}
			}
			if(verbosity >= 2) printDebug("["+serverName+".ConnectionKiller] Ended");
		}
	}
}
