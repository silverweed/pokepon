//: pokepon.net.jack/server/DatabaseServer.java

package pokepon.net.jack.server;

import pokepon.net.jack.*;
import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.util.*;
import java.util.regex.*;
import java.security.*;
import pokepon.util.Meta;
import static pokepon.util.MessageManager.*;

/** A MultiServer with a Database.
 *
 * @author silverweed
 */

public class DatabaseServer extends MultiThreadedServer {

	protected String dbName;
	protected URL dbURL;

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
		try {
			if(opts.database != null)
				setDatabaseLocation(opts.database);
		} catch(MalformedURLException e) {
			throw new RuntimeException(e);
		}
		return this;
	}

	/** Sets a new database location and (weakly) ensures the file exists by creating it. */
	public void setDatabaseLocation(String db) throws MalformedURLException {
		dbURL = new URL(db);
		dbName = dbURL.getPath();
		if(verbosity >= 1) printDebug("[DatabaseServer] Set database location to "+dbURL);
		if(!Files.exists(Paths.get(dbName))) {
			try {
				Files.createDirectories(Paths.get(dbName).getParent());
				Files.createFile(Paths.get(dbName));
				Files.write(Paths.get(dbName),
					Arrays.asList(new String[] { 
						"# Pokepon Server database",
						"# Created: " + new Date()
					}), 
					Charset.forName("UTF-8")
				);
				if(verbosity >= 1) printDebug("[DatabaseServer] Created database file.");
			} catch(IOException e) {
				printDebug("[ ERROR ] Caught IOException while creating database: "+e);
			}
		} else if(verbosity >= 1) printDebug("[DatabaseServer] Database file already exists.");
	}

	public static void main(String[] args) {
		DatabaseServer server = null;

		try {
			server = new DatabaseServer();
			args = server.loadPreConfig(args);
			server.loadOptions(readConfigFile(new URL(confFile)));
			server.loadOptions(parseServerOptions(args));
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
	public boolean nickExists(String nick) throws FileNotFoundException {
		try (BufferedReader scanner = new BufferedReader(new InputStreamReader(new FileInputStream(dbName)))) {
			if(verbosity >= 2) printDebug("Opening database: " + dbName + " ...");

			String input = null;
			while((input = scanner.readLine()) != null) {
				if(verbosity >= 3) printDebug("read line: "+input);
				if(input.charAt(0) == '#') {
					if(verbosity >= 3) printDebug("Read comment: continuing...");
					continue;
				}
				String[] tokens = input.trim().split("\t");
				if(verbosity >= 3) printDebug("tokens: "+Arrays.asList(tokens));
				if(tokens.length == 2) {
					if(verbosity >= 3) printDebug("Confronting given nick "+nick+" with database entry "+tokens[0]+";\nmatch? = "+tokens[0].equals(nick));

					if(tokens[0].equals(nick)) {	//nick already exists
						return true;
					}
				} else {
					if(verbosity >= 2) printDebug("Error: incorrect database entry: "+Arrays.asList(tokens).toString());
					continue;
				}
			}

		} catch(FileNotFoundException e) {	//we want this handled by upper context
			throw new FileNotFoundException("Thrown by DatabaseServer.nickExists("+nick+")");
		} catch(Exception e) {
			printDebug("Caught exception in nickExists: "+e);
			e.printStackTrace();
		} 
		return false;
	}
	/** @return List of registered names.
	 */
	public List<String> getNicks() throws FileNotFoundException {
		List<String> nicks = new ArrayList<String>();
		try (BufferedReader scanner = new BufferedReader(new InputStreamReader(new FileInputStream(dbName)))) {
			if(verbosity >= 2) printDebug("Opening database: " + dbName + " ...");

			String input = null;
			while((input = scanner.readLine()) != null) {
				if(verbosity >= 3) printDebug("read line: "+input);
				if(input.charAt(0) == '#') {
					if(verbosity >= 3) printDebug("Read comment: continuing...");
					continue;
				}
				String[] tokens = input.trim().split("\t");
				if(verbosity >= 3) printDebug("tokens: "+Arrays.asList(tokens));
				if(tokens.length == 2) {
					nicks.add(tokens[0]);
				} else {
					if(verbosity >= 2) printDebug("Error: incorrect database entry: "+Arrays.asList(tokens).toString());
					continue;
				}
			}

		} catch(FileNotFoundException e) {	//we want this handled by upper context
			throw new FileNotFoundException("Thrown by DatabaseServer.getNicks()");
		} catch(Exception e) {
			printDebug("Caught exception in getNicks(): "+e);
			e.printStackTrace();
		} 
		return nicks;
	}

	/** Checks if a given password is correct for nickname 'nick'
	 * @param nick The nickname whose password we want to match
	 * @param passwd The password (non-encrypted) to match
	 * @return true - if nickname exists and password matches; false - otherwise
	 */
	public boolean checkPasswd(String nick,char[] passwd) throws FileNotFoundException {
		
		try (BufferedReader scanner = new BufferedReader(new InputStreamReader(new FileInputStream(dbName)))) {
			String input = null;

			while((input = scanner.readLine()) != null) {
				if(verbosity >= 3) printDebug("read line: "+input);
				if(input.charAt(0) == '#') {
					if(verbosity >= 3) printDebug("Read comment: continuing...");
					continue;
				}
				String[] tokens = input.trim().split("\t");
				if(verbosity >= 3) printDebug("tokens: "+Arrays.asList(tokens));
				if(tokens.length == 2) {
					if(verbosity >= 3) printDebug("Confronting given nick "+nick+" with database entry "+tokens[0]+";\nmatch? = "+tokens[0].equals(nick));
					if(tokens[0].equals(nick)) {	//nick exists
						if(PasswordHash.validatePassword(passwd,tokens[1])) {
							if(verbosity >= 2) printDebug("Password matched for nick "+nick);
							return true;
						} else {
							if(verbosity >= 2) printDebug("Password match failed for nick "+nick);
							return false;
						}
					}
				} else {
					if(verbosity >= 2) printDebug("Error: incorrect database entry: "+Arrays.asList(tokens).toString());
					continue;
				}
			}
		} catch(FileNotFoundException e) {
			throw new FileNotFoundException("Thrown by: DatabaseServer.checkPasswd("+nick+",<passwd>)");
		} catch(IOException e) {
			printDebug("Caught IOException while reading input: "+e);
		} catch(NoSuchAlgorithmException e) {
			printDebug("No such algorithm: "+e);
		} catch(Exception e) {
			printDebug("Caught exception in checkPasswd: "+e);
		} 
		return false;

	}
	
	/** Attempts to register a nickname
	 * @param nick The nickname
	 * @param passwd The encrypted password
	 * @return 0: success; 1: already exists; 2: generic error
	 */
	public int registerNick(String nick,char[] passwd) {
		Writer writer = null;

		/* Load database */
		try {
			/* Read entries */
			if(nickExists(nick)) return 1;	//may throw a FileNotFoundException
		
			// to avoid infinite loops: at this point the database file MUST exist (it should've been created in the 'catch')
			if(!Files.exists(Paths.get(dbName))) {
				printDebug("UNEXPECTED ERROR: database was not created correctly. Quitting method registerNick.");
				return 2;
			}

			writer = new PrintWriter(new FileOutputStream(dbName,true));
			writer.append(nick+"\t"+PasswordHash.createHash(passwd)+"\n");
			printDebug("Registered nickname "+nick+" to database.");
			return 0;

		} catch(FileNotFoundException e) {
			printDebug("[DatabaseServer] Database "+dbName+" not found. Creating it...");
			try {
				//Files.createDirectories(Paths.get(getDataURL().getPath()));
				Files.createFile(Paths.get(dbName));
			} catch(Exception ee) {
				printDebug("Caught exception while creating database: "+ee);
			}
			if(Files.exists(Paths.get(dbName))) {
				printDebug("Successfully created database.");
				return registerNick(nick,passwd);
			} 
			return 2;
		} catch(Exception e) {
			printDebug("Caught exception in registerNick: "+e);
			e.printStackTrace();
		} finally {
			if(writer != null) {
				try {
					writer.close();
				} catch(Exception e) {
					printDebug("registerNick - Caught Exception while closing writer: "+e);
				}
			}
		}

		return 2;
	}	

	protected static void printUsage() {
		System.out.println("Usage: "+DatabaseServer.class.getSimpleName()+" [address (- for localhost)] [port] [verbosity]");
		System.exit(0);
	}
}
