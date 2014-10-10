//: pokepon.net.jack/server/MultiThreadedServer.java

package pokepon.net.jack.server;

import pokepon.net.jack.*;
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

	public static enum ConnectPolicy {
		PERMISSIVE, 	// allow all TCP connections
		AVERAGE,	// disallow HTTP requests
		PARANOID	// disallow all but requests compliant with JACK protocol 
	};
	protected int maxClients = 100;
	protected ThreadPoolExecutor pool;
	protected ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(200*maxClients);
	protected List<Connection> clients = Collections.synchronizedList(new LinkedList<Connection>());
	ConnectPolicy connectPolicy = ConnectPolicy.AVERAGE;
	
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
					synchronized(clients) {
						if(clients.remove((Connection)r) && verbosity >= 1) {
							printDebug("Connection "+c.getName()+" removed from clients list.");
						}
						if(verbosity >= 1) {
							if(clients.size() < 20)
								printDebug("clients: "+clients+" ("+clients.size()+" / "+maxClients+")");
							else 
								printDebug("clients: "+clients.size()+" / "+maxClients);
							printDebug("Threads num: "+pool.getPoolSize());
							printDebug("Currently active: "+pool.getActiveCount());
							printDebug("Max Thread Num so far: "+pool.getLargestPoolSize());
						}
					}
				}
			}
		};
		if(opts.serverName == null)
			serverName = MultiThreadedServer.class.getSimpleName();
		if(verbosity >= 0)
			printDebug("["+serverName+"] Constructed with maxClients = "+maxClients);
	}

	@Override
	public MultiThreadedServer loadOptions(ServerOptions opts) {
		if(opts.maxClients != -1)
			maxClients = opts.maxClients;
		super.loadOptions(opts);
		return this;
	}
	
	public static void main(String[] args) {
		MultiThreadedServer server = null;

		try {
			server = new MultiThreadedServer();
			args = server.loadPreConfig(args);
			server.loadConfiguration(readConfigFile(new URL(confFile)));
			server.loadConfiguration(parseServerOptions(args));
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
	public void start() throws IOException {
		initialize();
	
		consoleHeader(new String[] {" Java Awful Client-server Kit ","v 1.0"},'*');
		while(!pool.isShutdown()) {
			if(verbosity >= 2) printDebug("Waiting for new connection...");
			Socket newClient = accept();
			if(clients.size() >= maxClients) {
				if(verbosity >= 1) printDebug("Client number exceeded: dropping connection from "+newClient+"...");
				ServerConnection.dropWithMsg(newClient,CMN_PREFIX+"drop Couldn't connect: server is full.");
				continue;
			}
			Connection newConnection = new ServerConnection(this,newClient,verbosity);
			newConnection.addConnectionExecutor(new CommandsExecutor());
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
		for(Connection c : clients) 
			if(c.getName().equals(name)) 
				return c;
		return null;
	}
	
	public boolean isConnected(String name) {
		synchronized(clients) {
			Iterator<Connection> it = clients.iterator();
			while(it.hasNext())
				if(it.next().getName().equals(name)) return true;
		}
		return false;
	}

	/** Send msg to all clients but 'client'. */
	public void broadcast(Socket client,String msg) {
		if(verbosity >= 1) printDebug("["+serverName+"] Broadcasting message: "+msg);
		if(verbosity >= 3) printDebug("clients: "+clients);
		synchronized(clients) {
			Iterator<Connection> it = clients.iterator();
			while(it.hasNext()) {
				Connection conn = it.next();
				if(verbosity >= 3) printDebug("Connection: "+conn);
				if(client != null && conn.getSocket().equals(client)) {
					if(verbosity >= 3) printDebug("continuing.");
					continue;
				}
				if(verbosity >= 2) printDebug("["+serverName+"] Sending message to "+conn.getName()+" ("+conn.getSocket()+")");
				conn.getOutputStream().println(msg);
			}
		}
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
	public void loadConfiguration(ServerOptions opts) {
		super.loadConfiguration(opts);
		if(opts.maxClients != -1)
			maxClients = opts.maxClients;
	}

	protected static void printUsage() {
		consoleMsg("Usage: "+MultiThreadedServer.class.getSimpleName()+" [port] [verbosity]");
		System.exit(0);
	}
}
	
