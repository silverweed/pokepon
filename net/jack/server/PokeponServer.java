//: net/jack/server/PokeponServer.java

package pokepon.net.jack.server;

import pokepon.net.jack.*;
import pokepon.main.TestingClass;
import pokepon.util.*;
import pokepon.battle.*;
import static pokepon.util.MessageManager.*;
import java.net.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/** The main Server class.
 *
 * @author silverweed
 */
public class PokeponServer extends DatabaseServer implements TestingClass {
	
	public static final int DEFAULT_MAX_BATTLES = DEFAULT_MAX_CLIENTS;
	private static Set<Format> availableFormats = new LinkedHashSet<>();
	static {
		// The clients will see the available formats in this order.
		availableFormats.add(RuleSet.Predefined.CLASSIC);
		availableFormats.add(RuleSet.Predefined.RANDOMBATTLE);
		availableFormats.add(RuleSet.Predefined.CLASSIC_MONOTYPE);
		availableFormats.add(RuleSet.Predefined.SPECIESCLAUSE);
		availableFormats.add(RuleSet.Predefined.NOUBER);
		availableFormats.add(RuleSet.Predefined.CANON);
		availableFormats.add(RuleSet.Predefined.ITEMCLAUSE);
		availableFormats.add(RuleSet.Predefined.MONOTYPE);;
		availableFormats.add(RuleSet.Predefined.DEFAULT);

		// Additional server options
		serverOpts +=
			"\t--console [no]:                 enables/disables the server console\n"+
			"\t--max-battles <integer>:        set the limit of concurrent battles allowed by the server.\n";
	}
	private BattleSchedule battleSchedule = new BattleSchedule(); 
	private Map<String,BattleTask> battles = Collections.synchronizedMap(new HashMap<String,BattleTask>());
	private int battleID = 0;
	private int maxBattles = DEFAULT_MAX_BATTLES;
	private Welcomer welcomer = new Welcomer();
	private boolean enableConsole = true;

	public PokeponServer() throws IOException {
		this(ServerOptions.construct());
	}

	public PokeponServer(ServerOptions opts) throws IOException {
		super(opts);
		loadOptions(opts);
		if(verbosity >= 1) printDebug("["+serverName+"] Constructed.");
	}

	@Override
	public PokeponServer loadOptions(ServerOptions opts) {
		super.loadOptions(opts);
		if(opts.maxBattles != -1)
			maxBattles = opts.maxBattles;
		if(opts.serverName == null && !alreadySetName)
			serverName = getClass().getSimpleName();
		if(opts.enableConsole != null)
			enableConsole = opts.enableConsole;

		return this;
	}
	
	@Override
	public void start() throws IOException {
	
		initialize();

		welcomer.start();

		consoleHeader(new String[]{" "+serverName.toUpperCase()+" "," running on: "+myAddress+":"+port+" "},'*');
		printConfiguration();
		printMsg("************************");
		DataDealer.init();

		// Rename server broadcaster (initialized by MultiThreadedServer)
		broadcaster.setName("Pokepon Server Broadcaster");

		// Replace connection killer with pokepon-specific one
		connectionKiller = new Timer("Connection Killer", true);
		connectionKiller.scheduleAtFixedRate(new PokeponConnectionKiller(), 5 * 60 * 1000, 5 * 60 * 1000);

		// Start server console
		if(enableConsole) {
			Thread serverConsole = new Thread(new ServerConsole(this));
			serverConsole.setName("Pokepon Server Console");
			serverConsole.setDaemon(true);
			serverConsole.start();
		}

		while(!pool.isShutdown()) {
			if(verbosity >= 2) printDebug("Waiting for new connection...");
			try {
				Socket newClient = accept();
				if(clients.size() >= maxClients) {
					if(verbosity >= 1) printDebug("Client number exceeded: dropping connection from "+newClient+"...");
					ServerConnection.dropWithMsg(newClient,CMN_PREFIX+"drop Couldn't connect: server is full.");
					continue;
				}
				if(isBanned(newClient.getInetAddress().getHostAddress())) {
					if(verbosity >= 1) printDebug("Dropping connection with banned IP: "+newClient);
					ServerConnection.dropWithMsg(newClient,CMN_PREFIX+"drop Your IP is banned from this server.");
					continue;
				}
				welcomer.offer(newClient);
			} catch(SocketException e) {
				if(pool.isShutdown()) {
					if(verbosity >= 0) printDebug("["+serverName+"] Terminating.");
				} else {
					throw e;
				}
			}
		}
	}

	/** Class managing the battle schedule */
	public class BattleSchedule {
		/** The battle schedule:
		 * { client1: { (client2, format), (client3, format), etc }, 
		 *   client2: { (client4, format), etc },
		 *   etc
		 * }
		 */
		private Map<String,List<Map.Entry<String,Format>>> schedule = 
				Collections.synchronizedMap(new HashMap<String,List<Map.Entry<String,Format>>>());

		public synchronized boolean put(String client1, String client2, Format format) {
			if(schedule.get(client1) != null) {
				for(Map.Entry<String,Format> entry : schedule.get(client1)) {
					if(entry.getKey().equals(client2)) {
						// already scheduled battle client1 -> client2
						return false;
					}
				}
				schedule.get(client1).add(new AbstractMap.SimpleEntry<String,Format>(client2,format));
				return true;
			}
			List<Map.Entry<String,Format>> newEntry = new LinkedList<Map.Entry<String,Format>>();
			newEntry.add(new AbstractMap.SimpleEntry<String,Format>(client2,format));
			schedule.put(client1,newEntry);
			return true;
		}

		public synchronized List<Map.Entry<String,Format>> get(String key) {
			return schedule.get(key);
		}	
		
		/** @return If client1 -&gt; client2 is scheduled, its format, else null. */
		public synchronized Format getFormat(String client1, String client2) {
			if(schedule.get(client1) == null) return null;
			for(Map.Entry<String,Format> entry : schedule.get(client1)) {
				if(entry.getKey().equals(client2)) {
					return entry.getValue();
				}
			}
			return null;
		}

		public synchronized boolean isScheduled(String client1, String client2) {
			return isScheduled(client1, client2, true);
		}

		/** @param commutative If True, returns true even if client2 -&gt; client1 is scheduled, else only
		 * if client1 -&gt; client2 is. (default: true) 
		 */
		public synchronized boolean isScheduled(String client1, String client2, boolean commutative) {
			if(schedule.get(client1) != null) {
				for(Map.Entry<String,Format> entry : schedule.get(client1)) {
					if(entry.getKey().equals(client2)) {
						return true;
					}
				}
			}
			if(commutative) 
				if(schedule.get(client2) != null) {
					for(Map.Entry<String,Format> entry : schedule.get(client2)) {
						if(entry.getKey().equals(client1)) {
							return true;
						}
					}
				}
			return false;
		}

		public synchronized boolean containsKey(String client) {
			return schedule.containsKey(client);
		}

		public synchronized boolean remove(String client1, String client2) {
			return remove(client1, client2, true);
		}

		/** Removes one (or two) entries from the battleSchedule.
		 * @param commutative If true, remove both client1 -&gt; client2 and client2 -&gt; client1, 
		 * else just the first. (Default: True)
		 */
		public synchronized boolean remove(String client1, String client2, boolean commutative) {
			boolean tmp = schedule.remove(client1) != null;
			if(commutative)
				return schedule.remove(client2) != null || tmp;
			else
				return tmp;
		}

		public synchronized Set<Map.Entry<String,List<Map.Entry<String,Format>>>> entrySet() {
			return schedule.entrySet();
		}

		public synchronized String toString() {
			StringBuilder sb = new StringBuilder("{\n");
			for(Map.Entry<String,List<Map.Entry<String,Format>>> entry : schedule.entrySet()) {
				sb.append(entry.getKey() + ": [");
				for(Map.Entry<String,Format> lEntry : entry.getValue()) {
					sb.append("("+lEntry.getKey()+","+lEntry.getValue()+"), ");
				}
				sb.delete(sb.length() - 2, sb.length());
				sb.append("],\n");
			}
			sb.append("}");
			return sb.toString();
		}

		public synchronized int size() {
			return schedule.size();
		}
	}

	protected class Welcomer extends Thread {
		private LinkedBlockingQueue<Socket> queue = new LinkedBlockingQueue<>();

		public Welcomer() {
			super();
			setName("Pokepon Server Welcomer");
			setDaemon(true);
		}

		public void offer(Socket s) {
			queue.offer(s);
		}

		public void run() {
			while(!isInterrupted()) {
				try {
					welcome(queue.take());
				} catch(InterruptedException e) {
					printDebug("[Welcomer] interrupted: "+e);
				}
			}
		}

		private void welcome(Socket newClient) {
			Connection newConnection = new ServerConnection(PokeponServer.this, newClient, verbosity);
			newConnection.addConnectionExecutor(new BattleExecutor());
			newConnection.addConnectionExecutor(new PokeponCommandsExecutor());
			if(PokeponServer.this.chat != null)
				newConnection.addConnectionExecutor(new ChatCommandsExecutor());
			newConnection.addConnectionExecutor(new PokeponCommunicationsExecutor());
			clients.add(newConnection);
			pool.execute(newConnection);
			if(verbosity >= 1) printDebug("Constructed connection.");
			if(verbosity >= 2) {
				printDebug("Clients: "+clients.size()+" / "+maxClients+
				"\nThreads num: "+pool.getPoolSize()+
				"\nCurrently active: "+pool.getActiveCount()+
				"\nMax Thread Num so far: "+pool.getLargestPoolSize()+
				"\nMax Pool Size: "+pool.getMaximumPoolSize());
			}
		}
	}

	/** This method returns a battleID which is guaranteed to be unique;
	 * this is obtained simply by incrementing the returned ID by 1 each
	 * time an ID is requested.
	 */
	public synchronized String getBattleID() {
		return Integer.toString(battleID++);
	}

	public synchronized BattleTask getBattle(String id) {
		return battles.get(id);
	}

	public synchronized Map<String,BattleTask> getBattles() {
		return battles;
	}

	public int getMaxBattles() {
		return maxBattles;
	}

	public void setMaxBattles(int n) {
		maxBattles = n;
	}

	public BattleSchedule getBattleSchedule() { return battleSchedule; }

	/** Battle scheduling happens this way: 
	 * <ol>
	 *   <li>Client1: "/battle Client2"</li>
	 *   <li>Server: "!selectteam format1 &hellip; formatN" -&gt; Client1</li>
	 *   <li>Client1: "!ok ChosenFormat"</li>
	 *   <li>Server calls scheduleBattle(Client1, Client2, ChosenFormat)</li>
	 *   <li>Server: "!btlreq Client1" -&gt; Client2</li>
	 *   <li>Client2: "/acceptbtl Client1"</li>
	 *   <li>Server: "!selectteam &#64;ChosenFormat" -&gt; Client2</li>
	 *   <li>Client2: "!ok"</li>
	 *   <li>Server starts battle.</li>
	 * </ol>
	 */
	public boolean scheduleBattle(Connection client1,Connection client2,Format format) {
		synchronized(battles) {
			if(battles.size() >= maxBattles) {
				if(verbosity >= 0)
					printDebug("Dropping battle request "+client1.getName()+" -> "+client2.getName()+
						": too many active battles ("+battles.size()+" / "+maxBattles+")");
				client1.sendMsg(CMN_PREFIX+
					"html <font color=\"#FF2222\">Reached battles limit: cannot accept more battles." +
					"Please retry later.</font>");
				return false;
			}
			if(verbosity >= 2) printDebug(client1.getName()+" sent a battle request to "+client2.getName());
			if(battleSchedule.isScheduled(client2.getName(), client1.getName(), false)) {
				// retreive this battle's format
				Format fmt = battleSchedule.getFormat(client2.getName(),client1.getName());
				if(verbosity >= 1) 
					printDebug("Starting battle between "+client1.getName()+" and "+
							client2.getName()+" (format="+fmt.getName()+")");
				// remove this entry from the battleSchedule
				battleSchedule.remove(client2.getName(),client1.getName());	
				// start the battle server-side
				try {
					String btlID = getBattleID();
					if(fmt instanceof RuleSet)
						((RuleSet)fmt).addRule(":Pre-alpha release: some features are not available.");
					BattleTask bTask = new BattleTask(this,btlID,client1,client2,fmt);
					battles.put(btlID,bTask);
					pool.execute(bTask);
				} catch(Exception e) {
					e.printStackTrace();
					return false;
				}
			} else {
				if(verbosity >= 1) printDebug("Scheduled battle between "+client1.getName()+" and "+
						client2.getName()+".");
				battleSchedule.put(client1.getName(),client2.getName(),format);
				client2.sendMsg(CMN_PREFIX+"btlreq "+client1.getName());
			}

			return true;
		}
	}

	/** Check and remove battleSchedule entries client1 -&gt; client2 and client2 -&gt; client1; if neither found, return false. */
	public boolean dismissBattle(Connection client1,Connection client2) {
		return dismissBattle(client1.getName(), client2.getName());
	}

	public boolean dismissBattle(String client1, String client2) {
		synchronized(battleSchedule) {
			if(battleSchedule.remove(client1, client2, false)) {
				if(verbosity >= 2) printDebug("Dismissed battle between "+client1+" and "+client2+".");
				getClient(client2).sendMsg(client1+" dismissed battle request with you.");
				return true;
			}
			if(battleSchedule.remove(client2, client1, false)) {
				if(verbosity >= 2) printDebug("Dismissed battle between "+client2+" and "+client1+".");
				getClient(client2).sendMsg(client1+" dismissed battle request with you.");
				return true;
			}
		}
		return false;
	}

	/** Removes all battles involving client 'name' from battles and battleSchedule. 
	 * @return The number of battles destroyed.
	 */
	public int destroyAllBattles(String name) {
		int cnt = 0;
		if(verbosity >= 1)
			printDebug("[PokeponServer] Destroying all battles with player "+name);

		synchronized(battles) {
			Iterator<Map.Entry<String,BattleTask>> it = battles.entrySet().iterator();
			while(it.hasNext()) {
				Map.Entry<String,BattleTask> entry = it.next();
				BattleTask bt = entry.getValue();
				if(bt.getConnection(1).getName().equals(name) || bt.getConnection(2).getName().equals(name)) {
					bt.sendB("|leave|"+name);
					bt.terminate();
					if(verbosity >= 3) printDebug("[PokeponServer] Removed battle "+entry.getKey());
					it.remove();
				}
			}
		}

		synchronized(battleSchedule) {
			Iterator<Map.Entry<String,List<Map.Entry<String,Format>>>> it = battleSchedule.entrySet().iterator();
			while(it.hasNext()) {
				Map.Entry<String,List<Map.Entry<String,Format>>> entry = it.next();
				if(entry.getKey().equals(name)) {
					it.remove();
					++cnt;
					if(verbosity >= 3) printDebug("[PokeponServer] Removed entry "+entry+" from battleSchedule.");
				} else {
					for(Map.Entry<String,Format> lEntry : entry.getValue()) {
						if(lEntry.getKey().equals(name)) {
							it.remove();
							++cnt;
							if(verbosity >= 3) printDebug("[PokeponServer] Removed entry "+lEntry+" from battleSchedule.");
							break;
						}
					}
				}
			}
		}

		if(verbosity >= 2) printDebug("[PokeponServer] battles active: "+battles.size());
		return cnt;
	}

	public static Set<Format> getAvailableFormats() {
		return availableFormats;
	}

	/** Creates a default conf file from net/server.conf (if not existing) */
	public static void createDefaultConf() {
		try {
			Meta.ensureFileExists(confFile, Meta.complete2(Meta.NET_DIR)+"/server.conf");
		} catch(Exception e) {
			printDebug("[PokeponServer] Exception while creating server.conf:");
			e.printStackTrace();
		}
	}

	/** {@inheritDoc pokepon.net.jack.server.MultiThreadedServer#configure} */
	@Override
	public PokeponServer configure(String[] args, ServerOptions... opts) throws MalformedURLException, UnknownOptionException {
		createDefaultConf();
		return (PokeponServer)super.configure(args, opts);
	}

	@Override
	public void printConfiguration(final PrintStream s) {
		super.printConfiguration(s);
		s.println("- maxBattles: "+maxBattles);
		s.println("- consoleEnabled: "+enableConsole);
	}

	@Override
	public void printConfiguration() {
		super.printConfiguration();
		printMsg("- maxBattles: "+maxBattles);
		printMsg("- consoleEnabled: "+enableConsole);
	}
	
	class PokeponConnectionKiller extends TimerTask {
		public void run() {
			if(verbosity >= 2) printDebug("["+serverName+".ConnectionKiller] Started");
			int count = 0;
			synchronized(killableConnections) {
				Iterator<ServerConnection> it = killableConnections.iterator();
				while(it.hasNext()) {
					ServerConnection conn = it.next();
					if(verbosity >= 2) 
						printDebugnb("["+serverName+".ConnectionKiller] removing "+conn+"...");
					int dst = destroyAllBattles(conn.getName());
					if(dst > 0 && verbosity >= 2) printDebugnb("Removed "+dst+" battles. ");
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
			if(verbosity >= 2) printDebug("["+serverName+".ConnectionKiller] Ended; removed " + count + " connections.");
		}
	}

	public static void main(String[] args) {

		// first of all, check for a -h or --help flag
		for(String s : args) {
			if(s.equals("-h") || s.equals("--help")) {
				printUsage();
			}
		}

		PokeponServer server = null;
		
		try {
			args = loadPreConfig(args);
			server = new PokeponServer();
			server.configure(args).start();
		} catch(IOException e) {
			printDebug("Caught IOException while starting PokeponServer:");
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
}

class ServerConsole implements Runnable {
	
	private final static String HELP_STRING = 
		"Supported commands:\n stop\n ban <ip list>\n unban <ip list>\n flush (clear banlist)\n" +
		" say <message>\n banned [ip class]\n users\n kick <user>\n info\n roles\n reload";

	private final PokeponServer server;
	private final Scanner scanner;

	public ServerConsole(final PokeponServer server) {
		this.server = server;
		scanner = new Scanner(System.in);
	}
		
	@Override
	public void run() {
		String in = "";
		consoleMsg("** Minimal console initialized **");

		while(true) {
			try {
				consoleMsgnb("> ");
				in = scanner.nextLine();
				if(in.length() == 0) continue;
				if(in.charAt(0) == CMD_PREFIX) in = in.substring(1);
				String[] token = in.split(" ");
				if(in.equals("stop")) {
					synchronized(server) {
						server.shutdown();
					}
					System.exit(0);
				} else if(token[0].equals("users")) {
					consoleDebug(server.clients.toString());

				} else if(token[0].equals("banned")) {
					if(token.length > 1)
						for(int i = 1; i < token.length; ++i)
							consoleDebug("isBanned("+token[i]+"): "+server.isBanned(token[i]));
					else
						consoleDebug(server.banRules.toString());

				} else if(token[0].equals("?") || token[0].equals("help")) {
					consoleDebug(HELP_STRING);

				} else if(token[0].equals("info")) {
					consoleDebug(server.printInfo());

				} else if(token[0].equals("roles")) {
					if(server.chat != null)
						consoleDebug(server.chat.getRolesTable());
					else
						consoleDebug("advancedChat is disabled.");

				} else if(token[0].equals("flush")) {
					server.banRules.clear();
					consoleDebug("[ OK ] Banlist cleared.");

				} else if(token[0].equals("reload")) {
					if(server.chat != null) {
						if(server.loadDBEntries() && server.chat.reload()) {
							consoleDebug("[ OK ] chat roles reloaded successfully. New roles:");
							consoleDebug(server.chat.getRolesTable());
						} else {
							consoleDebug("Errors reloading chat roles: see server logs for details.");
						}
					} else {
						consoleDebug("advancedChat is disabled.");
					}

				} else {
					if(token.length < 2) {
						consoleDebug("Missing argument(s) ['?' for help]");
						continue;
					}
					if(token[0].equals("ban"))
						for(int i = 1; i < token.length; ++i)
							server.banIP(new IPClass(token[i]));

					else if(token[0].equals("unban"))
						for(int i = 1; i < token.length; ++i)
							server.unbanIP(new IPClass(token[i]));

					else if(token[0].equals("kick"))
						server.kickUser(ConcatenateArrays.merge(token, 1), null);

					else if(token[0].equals("say"))
						server.broadcast(null, CMN_PREFIX + "html <font color=\"purple\"><b>[SERVER] " +
							ConcatenateArrays.merge(token, 1) + "</b></font>");

					else
						consoleDebug(HELP_STRING);
				}
					
			} catch(NoSuchElementException e) {
				consoleMsg("[EOF] Console is dead!! Server is still up; to stop the server, Ctrl+C");
				return;
			} catch(Exception e) {
				consoleDebug("Caught exception: "+e);
				consoleDebug("...still reading from the console, though.");
			}
		}
	}
}
