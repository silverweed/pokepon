//: pokepon.net.jack/server/DatabaseServer.java

package pokepon.net.jack.server;

import pokepon.net.jack.*;
import pokepon.net.jack.chat.*;
import pokepon.util.Meta;
import static pokepon.util.MessageManager.*;
import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.util.*;
import java.util.regex.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

/** A MultiThreadedServer with a file database.
 *
 * @author silverweed
 */
public class DatabaseServer extends MultiThreadedServer {

	protected String dbName;
	protected URL dbURL;
	/** Map { nickname: [password, role] } */
	protected Map<String,String[]> dbEntries = Collections.synchronizedMap(new HashMap<String,String[]>());

	public DatabaseServer() throws IOException {
		this(ServerOptions.construct());
	}

	public DatabaseServer(ServerOptions opts) throws IOException {
		super(opts);
		loadOptions(opts);
		if(opts.database == null)
			setDatabaseLocation("file://"+Meta.getDataURL().getPath()+"/server.db"); 
		if(opts.serverName == null && !alreadySetName)
			serverName = getClass().getSimpleName();
		if(verbosity >= 0)
			printDebug("["+serverName+"] Constructed with database: "+dbURL+" (path: "+dbName+")");
	}

	@Override
	public DatabaseServer loadOptions(ServerOptions opts) {
		super.loadOptions(opts);
		if(verbosity >= 2) printDebug("[DatabaseServer] Called loadOptions");
		if(opts.database != null)
			setDatabaseLocation(opts.database);
		return this;
	}

	public URL getDatabaseURL() { return dbURL; }
	public Map<String,String[]> getDBEntries() { return dbEntries; }	

	/** Sets a new database location and ensures the file exists by creating it;
	 * also flush in-memory DB and reloads it (and the chat system, if any);
	 * if errors occur, the old database is kept (if existing).
	 * @return true if database was changed successfully, false otherwise.
	 */
	public synchronized boolean setDatabaseLocation(String db) {
		if(verbosity >= 1) printDebug("[DatabaseServer] Setting database location to "+db);
		try {
			URL tmpdbURL = new URL(db);
			String tmpdbName = tmpdbURL.getPath();
			if(!Files.exists(Paths.get(tmpdbName))) {
				try {
					Files.createDirectories(Paths.get(tmpdbName).getParent());
					Files.createFile(Paths.get(tmpdbName));
					Files.write(Paths.get(tmpdbName),
						Arrays.asList(new String[] { 
							"# Pokepon Server database",
							"# Created: " + new Date(),
							"# <name> <password-hash> <role>"
						}), 
						Charset.forName("UTF-8")
					);
					if(verbosity >= 1) printDebug("[DatabaseServer] Created database file.");
					dbURL = tmpdbURL;
					dbName = tmpdbName;
					loadDBEntries();
					if(chat != null)
						chat.reload();
					return true;
				} catch(IOException e) {
					printDebug("[ ERROR ] Caught IOException while creating database: "+e);
					return false;
				}
			} else {
				if(verbosity >= 1) printDebug("[DatabaseServer] Database file already exists.");
				dbURL = tmpdbURL;
				dbName = tmpdbName;
				loadDBEntries();
				if(chat != null)
					chat.reload();
				return true;
			}
		} catch(FileNotFoundException ee) {
			printDebug("[DatabaseServer.setDatabaseLocation] Error reloading DB entries after changing DB path!?" +
					"\n\t\tServer is in a stale state: restarting it is desirable.");
			return false;
		} catch(MalformedURLException ee) {
			printDebug("[DatabaseServer.setDatabaseLocation] Malformed URL: "+ee);
			return false;
		}
	}

	public static void main(String[] args) {
		DatabaseServer server = null;

		try {
			args = loadPreConfig(args);
			server = new DatabaseServer();
			server.loadOptions(readConfigFile(new URL(confFile)));
			server.loadOptions(ServerOptions.parseServerOptions(args));
			server.start();
		} catch(IOException e) {
			printDebug("Caught IOException while starting DatabaseServer: ");
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

	/** Reads the database and checks if given nick already exists or not
	 * @return true - if nick exists; false - otherwise
	 */
	public boolean nickExists(String nick) {
		return dbEntries.get(nick) != null;
	}

	/** @return List of registered names. */
	public Set<String> getNicks() {
		return dbEntries.keySet();
	}

	/** Checks if a given password is correct for nickname 'nick'
	 * @param nick The nickname whose password we want to match
	 * @param passwd The password (non-encrypted) to match
	 * @return true - if nickname exists, crypto algorithm is supported and password matches; false - otherwise
	 */
	public boolean checkPasswd(String nick, char[] passwd) {
		try {
			return dbEntries.get(nick) != null && PasswordHash.validatePassword(passwd, dbEntries.get(nick)[0]);
		} catch(NoSuchAlgorithmException|InvalidKeySpecException e) {
			printDebug("["+serverName+".checkPasswd] crypto error:");
			e.printStackTrace();
			return false;
		}
	}
	
	/** Attempts to register a nickname
	 * @param nick The nickname
	 * @param passwd The encrypted password
	 * @return 0: success; 1: already exists; 2: generic error
	 */
	public synchronized int registerNick(String nick, char[] passwd) {
		if(dbEntries.get(nick) != null) return 1;
		try {
			// Generate password hash
			String hash = PasswordHash.createHash(passwd);
			char role = '-';
			// If chatSystem is not null, generate role as well
			if(chat != null) {
				ChatUser user = chat.getUser(nick);
				if(user != null)
					role = user.getRole().getSymbol();
				else
					role = ChatUser.Role.USER.getSymbol();
			}

			// Append entry to file DB
			try (Writer output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dbName, true), "UTF-8"))) {
				output.append(nick + "\t" + hash + "\t" + role + "\n");
			} catch(IOException e) {
				printDebug("["+serverName+".registerNick] Error writing to database:");
				e.printStackTrace();
				return 2;
			}

			// If file DB was updated successfully, also update in-memory DB
			dbEntries.put(nick, new String[] { hash, "" + role });

		} catch(NoSuchAlgorithmException|InvalidKeySpecException ee) {
			printDebug("["+serverName+".registerNick] crypto error: ");
			ee.printStackTrace();
			return 2;
		}
		return 0;
	}	
	
	@Override
	public void printConfiguration(final PrintStream s) {
		super.printConfiguration(s);
		s.println("- database: "+dbURL);
	}

	@Override
	public void printConfiguration() {
		super.printConfiguration();
		printMsg("- database: "+dbURL);
	}

	@Override
	public void initialize() throws IOException {
		super.initialize();
		if(loadDBEntries()) {
			if(advancedChat)
				chat = new ChatSystem(this);
		} else {
			printDebug("["+serverName+"] ERROR: couldn't load database!");
		}
	}

	/** Reads entries from dbUrl and loads them into memory.
	 * @return True, if file was read correctly, False otherwise.
	 */
	protected boolean loadDBEntries() throws FileNotFoundException {
		// flush previous entries
		dbEntries.clear();
		try (BufferedReader scanner = new BufferedReader(new InputStreamReader(new FileInputStream(dbName), "UTF-8"))) {
			String input = null;
			int lineno = 1;
			while((input = scanner.readLine()) != null) {
				if(verbosity >= 3) printDebug("[loadDBEntries] read line: "+input);
				if(input.length() < 1) continue;
				if(input.charAt(0) == '#') continue;
				String[] tokens = input.split("\\s+");
				if(tokens.length != 2 && tokens.length != 3) {
					if(verbosity >= 1) printDebug("[loadDBEntries] Incorrect line in DB: "+input);
					continue;
				}
				if(dbEntries.get(tokens[0]) != null) {
					if(verbosity >= 1) printDebug("[loadDBEntries] Warning: line #" + lineno + " overrides previous entry!");
				}
				dbEntries.put(tokens[0], Arrays.copyOfRange(tokens, 1, tokens.length));
				++lineno;
			}
			printDebug("["+serverName+"] loaded DB entries with no errors.");
			return true;

		} catch(Exception e) {
			printDebug("["+serverName+"] Caught exception in loadDBEntries:");
			e.printStackTrace();
		}
		return false;			
	}

	protected static void printUsage() {
		System.out.println("Usage: "+DatabaseServer.class.getSimpleName()+" [address (- for localhost)] [port] [verbosity]");
		System.exit(0);
	}
}
