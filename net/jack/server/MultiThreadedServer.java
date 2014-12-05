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
 * @author Giacomo Parolini
 */

public class MultiThreadedServer extends BasicNameValidatingServer implements AutoCloseable {

	public static final int DEFAULT_MAX_CLIENTS = 100;
	public static enum ConnectPolicy {
		PERMISSIVE, 	// allow all TCP connections
		AVERAGE,	// disallow HTTP requests (but allow clients like netcat and telnet)
		PARANOID	// disallow all but requests compliant with our protocol 
	};
	protected int maxClients = DEFAULT_MAX_CLIENTS;
	protected ThreadPoolExecutor pool;
	/** Queue used by the ThreadPoolExecutor to store tasks */
	protected ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(200*maxClients);
	protected List<Connection> clients = Collections.synchronizedList(new LinkedList<Connection>());
	protected Set<String> bannedIP = new HashSet<>();
	protected boolean advancedChat;
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
	
	public MultiThreadedServer() throws IOException {
		this(ServerOptions.construct());
	}

	/** Accepted options: port, verbosity, address, maxClients, maxNickLen, serverName, confFile */
	public MultiThreadedServer(ServerOptions opts) throws IOException {
		super(opts);
		loadOptions(opts);
		pool = new ThreadPoolExecutor(5*maxClients,200*maxClients,60,TimeUnit.SECONDS,queue) {
			protected void afterExecute(Runnable r,Throwable t) {
				if(r instanceof Connection) {
					Connection c = (Connection)r;
					if(verbosity >= 1) printDebug("Closing connection with "+c.getName()+" ("+c.getSocket()+").");
					if(clients.remove((Connection)r) && verbosity >= 1) {
						printDebug("Connection "+c.getName()+" removed from clients list.");
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
			printDebug("["+serverName+"] Constructed with maxClients = "+maxClients+" and connectPolicy = "+connectPolicy);
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
			if(verbosity >= 2) printDebug("[MultiTheadedServer] defaultNick set to "+defaultNick);
		}
		if(opts.welcomeMessage != null) {
			welcomeMessage = opts.welcomeMessage;
			if(verbosity >= 2) printDebug("[MultiTheadedServer] welcomeMessage set to "+welcomeMessage);
		}
		if(opts.advancedChat != null) {
			advancedChat = opts.advancedChat;
			if(verbosity >= 2) printDebug("[MultiTheadedServer] advancedChat set to "+advancedChat);
		}
		if(opts.cmdBanLimit != null) {
			cmdBanLimit = opts.cmdBanLimit;
			if(verbosity >= 2) printDebug("[MultiTheadedServer] cmdBanLimit set to "+cmdBanLimit);
		}

		return this;
	}
	
	public static void main(String[] args) {
		MultiThreadedServer server = null;

		try {
			server = new MultiThreadedServer();
			args = server.loadPreConfig(args);
			server.loadOptions(readConfigFile(new URL(confFile)));
			server.loadOptions(ServerOptions.parseServerOptions(args));
			server.start();
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

	@Override
	public void initialize() throws IOException {
		super.initialize();
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
	}

	@Override
	public void start() throws IOException {
		initialize();
	
		consoleHeader(new String[] {" Java Awful Client-server Kit ","v 2.0"},'*');
		while(!pool.isShutdown()) {
			if(verbosity >= 2) printDebug("Waiting for new connection...");
			Socket newClient = accept();
			if(clients.size() >= maxClients) {
				if(verbosity >= 1) printDebug("Client number exceeded: dropping connection from "+newClient+"...");
				ServerConnection.dropWithMsg(newClient,CMN_PREFIX+"drop Couldn't connect: server is full.");
				continue;
			}
			if(bannedIP.contains(newClient.getInetAddress().getHostAddress())) {
				if(verbosity >= 1) printDebug("Dropping connection with banned IP: "+newClient);
				ServerConnection.dropWithMsg(newClient,CMN_PREFIX+"drop Your IP is banned from this server.");
				continue;
			}
			Connection newConnection = new ServerConnection(this,newClient,verbosity);
			newConnection.addConnectionExecutor(new CommandsExecutor());
			if(advancedChat)
				newConnection.addConnectionExecutor(new ChatCommandsExecutor());
			newConnection.addConnectionExecutor(new CommunicationsExecutor());
			clients.add(newConnection);
			pool.execute(newConnection);
			if(verbosity >= 2) printDebug("Constructed connection.");
		}
	}
	
	public List<Connection> getClients() {
		return clients;
	}

	public Connection getClient(String name) {
		for(Connection c : clients) 
			if(c.getName().equals(name)) 
				return c;
		return null;
	}

	public Set<String> getBannedIP() { return bannedIP; }
	
	public boolean isConnected(String name) {
		Iterator<Connection> it = clients.iterator();
		while(it.hasNext())
			if(it.next().getName().equals(name)) return true;
		return false;
	}

	/** Send msg to all clients but 'client'. */
	public void broadcast(Socket client,String msg) {
		if(verbosity >= 1) printDebug("["+serverName+"] Broadcasting message: "+msg);
		if(verbosity >= 3) printDebug("clients: "+clients);
		Iterator<Connection> it = clients.iterator();
		while(it.hasNext()) {
			Connection conn = it.next();
			if(verbosity >= 3) printDebug("Connection: "+conn);
			if(client != null && conn.getSocket().equals(client)) {
				if(verbosity >= 3) printDebug("continuing.");
				continue;
			}
			if(verbosity >= 2) printDebug("["+serverName+"] Sending message to "+conn.getName()+" ("+conn.getSocket()+")");
			conn.getOutput().println(msg);
		}
	}

	/** Forces an user to be disconnected from this server; useful with the advancedChat system. */
	public boolean kickUser(String name, String kicker) {
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
		if(verbosity >= 2) {
			printDebug("["+serverName+"] kick attempt to "+name+" failed: client not found."+(kicker != null ?
				" (kick attempted by "+kicker+")" : ""));
		}
		return false;
	}

	public boolean kickUser(String name) {
		return kickUser(name, null);
	}

	public void banIP(String ip) {
		bannedIP.add(ip);
		if(verbosity >= 1) printDebug("["+serverName+"] Banned IP: "+ip);
	}

	public void unbanIP(String ip) {
		if(bannedIP.remove(ip))
			if(verbosity >= 1) printDebug("["+serverName+"] Unbanned IP: "+ip);
	}

	@Override
	public void close() {
		if(clients.size() > 0) broadcast(null,"*** SERVER SHUTTING DOWN NOW ***");
		pool.shutdownNow();
		try {
			pool.awaitTermination(5,TimeUnit.SECONDS);
		} catch(InterruptedException e) {
			printDebug("Interrupted while awaiting termination.");
		} finally {
			shutdown();
		}
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

	protected static void printUsage() {
		consoleMsg("Usage: "+MultiThreadedServer.class.getSimpleName()+" [port] [verbosity]");
		System.exit(0);
	}
}
	
