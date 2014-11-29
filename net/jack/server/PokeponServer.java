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
 * @author Giacomo Parolini
 */
public class PokeponServer extends DatabaseServer implements TestingClass {
	
	public static final int DEFAULT_MAX_BATTLES = DEFAULT_MAX_CLIENTS;
	private static Set<Format> availableFormats = new LinkedHashSet<>();
	static {
		// The clients will see the available formats in this order.
		availableFormats.add(RuleSet.Predefined.SPECIESCLAUSE);
		availableFormats.add(RuleSet.Predefined.RANDOMBATTLE);
		availableFormats.add(RuleSet.Predefined.NOUBER);
		availableFormats.add(RuleSet.Predefined.CANON);
		availableFormats.add(RuleSet.Predefined.ITEMCLAUSE);
		availableFormats.add(RuleSet.Predefined.MONOTYPE);
		availableFormats.add(RuleSet.Predefined.DEFAULT);
	}
	private BattleSchedule battleSchedule = new BattleSchedule(); 
	private Map<String,BattleTask> battles = Collections.synchronizedMap(new HashMap<String,BattleTask>());
	private int battleID = 0;
	private int maxBattles = DEFAULT_MAX_BATTLES;
	private Welcomer welcomer = new Welcomer();

	public PokeponServer() throws IOException {
		this(ServerOptions.construct());
	}

	public PokeponServer(ServerOptions opts) throws IOException {
		super(opts);
		loadOptions(opts);
		if(verbosity >= 2)
			printDebug("["+serverName+"] Constructed.");
	}

	@Override
	public PokeponServer loadOptions(ServerOptions opts) {
		super.loadOptions(opts);
		if(opts.maxBattles != -1)
			maxBattles = opts.maxBattles;
		if(opts.serverName == null && !alreadySetName)
			serverName = getClass().getSimpleName();

		return this;
	}
	
	@Override
	public void start() throws IOException {
	
		initialize();

		welcomer.start();

		consoleHeader(new String[]{" "+serverName.toUpperCase()+" "," running on: "+myAddress+":"+port+" "},'*');
		printConfiguration();
		printMsg("························");
		DataDealer.init();
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
			welcomer.offer(newClient);
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

	public synchronized BattleSchedule getBattleSchedule() { return battleSchedule; }

	/** Battle scheduling happens this way: 
	 * first, client1 makes a battle request to the server via '/battle client2';
	 * an entry (client1,client2) is added to the battleSchedule, and client2 is
	 * prompted;
	 * then, if client2 accepts, another battle request is sent to the server with
	 * '/battle client1': since this battle was already on schedule, the actual battle
	 * starts; if client2 refuses, the entry is removed from battleSchedule;
	 * note that only 1 battle per client is allowed simultaneously: a second battle 
	 * request will cancel the previous one.
	 * 
	 */
	public synchronized boolean scheduleBattle(Connection client1,Connection client2,Format format) {
		if(battles.size() >= maxBattles) {
			if(verbosity >= 0)
				printDebug("Dropping battle request "+client1.getName()+" -> "+client2.getName()+
					": too many active battles ("+battles.size()+" / "+maxBattles+")");
			client1.sendMsg(CMN_PREFIX+
				"html <font color=\"#FF2222\">Reached battles limit: cannot accept more battles. Please retry later.</font>");
			return false;
		}
		if(verbosity >= 2) printDebug(client1.getName()+" sent a battle request to "+client2.getName());
		if(battleSchedule.isScheduled(client2.getName(), client1.getName(), false)) {
			// retreive this battle's format
			Format fmt = battleSchedule.getFormat(client2.getName(),client1.getName());
			if(verbosity >= 1) 
				printDebug("Starting battle between "+client1.getName()+" and "+client2.getName()+" (format="+fmt.getName()+")");
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
			}
		} else {
			if(verbosity >= 1) printDebug("Scheduled battle between "+client1.getName()+" and "+client2.getName()+".");
			battleSchedule.put(client1.getName(),client2.getName(),format);
			client2.sendMsg(CMN_PREFIX+"btlreq "+client1.getName());
		}

		return true;
	}

	/** Check and remove battleSchedule entries client1 -&gt; client2 and client2 -&gt; client1; if neither found, return false. */
	public synchronized boolean dismissBattle(Connection client1,Connection client2) {
		if(battleSchedule.remove(client1.getName(), client2.getName(), false)) {
			if(verbosity >= 2) printDebug("Dismissed battle between "+client1.getName()+" and "+client2.getName()+".");
			client2.sendMsg(client1.getName()+" dismissed battle request with you.");
			return true;
		}
		if(battleSchedule.remove(client2.getName(), client1.getName(), false)) {
			if(verbosity >= 2) printDebug("Dismissed battle between "+client2.getName()+" and "+client1.getName()+".");
			client2.sendMsg(client1.getName()+" dismissed battle request with you.");
			return true;
		}
		return false;
	}

	public synchronized boolean dismissBattle(String client1, String client2) {
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
		return false;
	}

	/** Removes all battles involving client 'name' from battles and battleSchedule. */
	public synchronized void destroyAllBattles(String name) {
		if(verbosity >= 1)
			printDebug("[PokeponServer] Destroying all battles with player "+name);

		Iterator<Map.Entry<String,BattleTask>> it = battles.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry<String,BattleTask> entry = (Map.Entry<String,BattleTask>)it.next();
			BattleTask bt = entry.getValue();
			if(bt.getConnection(1).getName().equals(name) || bt.getConnection(2).getName().equals(name)) {
				bt.sendB("|leave|"+name);
				bt.terminate();
				if(verbosity >= 3) printDebug("[PokeponServer] Removed battle "+entry.getKey());
				it.remove();
			}
		}

		Iterator<Map.Entry<String,List<Map.Entry<String,Format>>>> it2 = battleSchedule.entrySet().iterator();
		while(it2.hasNext()) {
			Map.Entry<String,List<Map.Entry<String,Format>>> entry = (Map.Entry<String,List<Map.Entry<String,Format>>>)it2.next();
			if(entry.getKey().equals(name)) {
				it2.remove();
				if(verbosity >= 3) printDebug("[PokeponServer] Removed entry "+entry+" from battleSchedule.");
			} else {
				for(Map.Entry<String,Format> lEntry : entry.getValue()) {
					if(lEntry.getKey().equals(name)) {
						it2.remove();
						if(verbosity >= 3) printDebug("[PokeponServer] Removed entry "+lEntry+" from battleSchedule.");
						break;
					}
				}
			}
		}

		if(verbosity >= 2) printDebug("[PokeponServer] battles active: "+battles.size());

	}

	public static Set<Format> getAvailableFormats() {
		return availableFormats;
	}

	public static void printUsage() {
		consoleMsg("Usage: PokeponServer [opts]\n"+
		"Options are:\n"+
		"\t--conf <conf_file>:             use a different configuration file than the default one.\n"+
		"\t-i,--ip <ip>:                   bind the server to the ip <ip>\n"+
		"\t-p,--port <port>:               listen on port <port> (default: 12344)\n"+
		"\t-v(vv...),-v <verb.Lv>:         set verbosity to <verb.Lv> (-1~4)\n"+
		"\t-m,--max-clients <max-clients>: limit the number of clients allowed to <max-clients>\n"+
		"\t--name <name>:                  set the server name\n"+
		"\t--forbid <list of regexes>:     forbid patterns from being used as chat nicknames\n"+
		"\t-d,--database <dbUrl>:          change the server database file location\n"+
		"\t-c,--policy <connect-policy>:   change the server connection policy\n"+
		"\t--default-nick <string>:        set the default nick to be given to anon clients\n"+
		"\t--welcome-message <string>:     set a welcome message to be given to clients\n"+
		"\t--min-nick-len <integer>:       set the minimum accepted nickname length\n"+
		"\t--max-nick-len <integer>:       longer nicknames will be truncated to this length\n"+
		"\t--max-battles <integer>:        set the limit of concurrent battles allowed by the server.\n"+
		"\t-C,--advanced-chat [no]:        enable/disable chat roles and the advanced chat system.\n"+
		"\t--cmd-ban-limit <integer>:      set maximum commands a client can issue in a minute (-1 = infinite).\n"+
		"\nAll the long options can be used in the configuration file as well, with the format option: value(s)\n");
		System.exit(0);
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

	/** Ensures conf file exists, reads pre config from CLI, config from conf file and CLI config;
	 * pre-config are options like --conf, which must be processed before reading the conf file;
	 * additional options may be passed via a ServerOptions object: these will be added after
	 * reading the conf file but before applying eventual CLI options.
	 * @param args The command line options
	 * @param opts (optional) additional options
	 */
	public PokeponServer configure(String[] args, ServerOptions... opts) throws MalformedURLException, UnknownOptionException {
		if(verbosity >= 2) printDebug("["+serverName+"] CONFIGURING");
		createDefaultConf();
		args = loadPreConfig(args);
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
	public void printConfiguration(final PrintStream s) {
		super.printConfiguration(s);
		s.println("- maxBattles: "+maxBattles);
	}

	@Override
	public void printConfiguration() {
		super.printConfiguration();
		printMsg("- maxBattles: "+maxBattles);
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
