//: pokepon.net.jack/server/CommandsExecutor.java

package pokepon.net.jack.server;

import pokepon.net.jack.*;
import pokepon.util.*;
import static pokepon.util.MessageManager.*;
import java.util.*;
import java.io.*;

/** A ConnectionExecutor which parses commands and executes them; note that
 * some commands are restricted to a more specific class of servers than
 * MultiThreadedServer; This Executor can only be attached to a ServerConnection.
 *
 * @author Giacomo Parolini
 */
class CommandsExecutor extends ServerConnectionExecutor {
	
	/** If a connection gives more than this number of commands in a minute,
	 * ignore following.
	 */
	protected static final int ISSUED_CMD_BAN_LIMIT = 40;
	protected static StringBuilder help = new StringBuilder("");
	static {
		help.append("Available commands:\n");
		help.append(CMD_PREFIX+"help - get this help\n");
		help.append(CMD_PREFIX+"nick <nick> [passwd] - change your nick (password is required if nick is registered).\n");
		help.append(CMD_PREFIX+"register <nick> <passwd> - register nick in server database.\n");
		help.append(CMD_PREFIX+"list - list all registered nicks.\n");
		help.append(CMD_PREFIX+"users - list all connected users.\n");
		help.append(CMD_PREFIX+"whois <nick> - display information on user.\n");
		help.append(CMD_PREFIX+"whoami - display information about you.\n");
		help.append(CMD_PREFIX+"pm <user> <msg> - send a PM to an user.\n");
		help.append(CMD_PREFIX+"whisper <user> <msg> - alias for `pm`.\n");
		help.append(CMD_PREFIX+"serverinfo - display server information.\n");
		help.append(CMD_PREFIX+"disconnect - disconnect from the server.\n");
	}

	@SuppressWarnings("unchecked")
	@Override
	public int execute(String msg) {
			
		if(connection.getVerbosity() >= 2) printDebug("Called CommandsExecutor.execute(msg="+msg+")");

		if(msg.charAt(0) != CMD_PREFIX) return 0;
		// refuse to execute command if this connection sent more than ISSUED_CMD_BAN_LIMIT commands in the last minute.
		int issued = 0;
		long startTime = -1;
		Iterator<Map.Entry<Long,String>> it = connection.getLatestMessages().descendingIterator();
		while(it.hasNext()) {
			Map.Entry<Long,String> entry = it.next();
			if(startTime == -1) {
				startTime = entry.getKey();
				continue;
			}
			if(startTime - entry.getKey() > 60 * 1000)
				break;
			++issued;
		}
		if(issued >= ISSUED_CMD_BAN_LIMIT - 1) {
			if(connection.getVerbosity() >= 2) 
				printDebug("[CMDEXEC] connection "+connection.getName() + "issued " + issued +
					" commands in a minute. Ignoring next commands until queue is emptied.");
			return 1;
		}
			
		
		String[] token = msg.substring(1).split(" ");
		String cmd = token[0];
	
		if(connection.getVerbosity() >= 3) printDebug("cmd="+cmd+",token="+Arrays.asList(token).toString());

		if(cmd.equals("help")) {
			connection.sendMsg(help.toString());
			return 1;
		} else if(cmd.equals("list")) {
			if(!(server instanceof DatabaseServer)) {
				connection.sendMsg("This command is not supported by this server implementation.");
				return 1;
			}
			try {
				connection.sendMsg("Registered users:");
				List<String> nicks = ((DatabaseServer)server).getNicks();
				for(String s : nicks) {
					connection.sendMsg(s);
				}
			} catch(FileNotFoundException e) {
				printDebug("Caught exception in CommandsExecutor.execute(list): "+e);
				connection.sendMsg("[Server database error]");
			}
			return 1;
		} else if(cmd.equals("whois")) {
			if(token.length < 2) {
				connection.sendMsg("Syntax error. Correct syntax is "+CMD_PREFIX+"whois <nick>");
				return 1;
			}
			for(Connection conn : server.getClients()) {
				if(conn.getName().equals(token[1])) {
					StringBuilder sb = new StringBuilder("Info about: "+token[1]+"\n");
					// TODO: add admin roles and send sensible information only to admins.
					sb.append("  IP Address:        (not allowed)"/*+conn.getSocket().getInetAddress().getHostAddress()*/+"\n");
					sb.append("  Hostname:          (not allowed)"/*+conn.getSocket().getInetAddress().getHostName()*/+"\n");
					sb.append("  Connected since:   "+conn.getConnectionTime()+"\n");
					sb.append("  Connection time:   "+secondsToDate(-conn.getConnectionTime().getTime()/1000+(new Date()).getTime()/1000)+"\n");
					sb.append("  Operating System:  "+conn.getOS()+"\n");
					if(server instanceof DatabaseServer) {
						try {
							sb.append("  Nick registered:   "+(((DatabaseServer)server).nickExists(token[1]) ? "yes" : "no")+"\n");
						} catch(FileNotFoundException e) {
							printDebug("Caught exception in CommandsExecutor.execute(whois): "+e);
							connection.sendMsg("[Server database error]");
						}
					}
					connection.sendMsg(sb.toString());
					return 1;
				}
			}
			connection.sendMsg("Can't find user "+token[1]);
			return 1;
		} else if(cmd.equals("whoami")) {
			StringBuilder sb = new StringBuilder("Info about: "+connection.getName()+"\n");
			sb.append("  IP Address:        "+connection.getSocket().getInetAddress().getHostAddress()+"\n");
			sb.append("  Hostname:          "+connection.getSocket().getInetAddress().getHostName()+"\n");
			sb.append("  Connected since:   "+connection.getConnectionTime()+"\n");
			sb.append("  Connection time:   "+secondsToDate(-connection.getConnectionTime().getTime()/1000+(new Date()).getTime()/1000)+"\n");
			connection.sendMsg(CMN_PREFIX+"youros");
			String os = "Unknown";
			try {
				connection.getSocket().setSoTimeout(4000);
				os = connection.getInput().readLine();
				if(connection.getVerbosity() >= 2) printDebug("CommandsExecutor.execute(whoami#os): read line "+os);
				if(os.split(" ").length >= 2 && os.split(" ")[0].equals(CMN_PREFIX+"myos")) {
					os = os.replaceFirst(CMN_PREFIX+"myos ","");
					if(connection.getVerbosity() >= 3) printDebug("os is now: "+os);
				} else {
					if(connection.getVerbosity() >= 1) printDebug("Received unexpected string: "+os+" from "+connection.getSocket());
					os = "Unknown";
				}
			} catch(java.net.SocketTimeoutException e) {
				printDebug(connection.getName()+": Socket timeout. Setting OS to 'Unknown'.");
				os = "Unknown";
			} catch(IOException e) {
				printDebug("Caught exception while reading line from socket "+connection.getSocket());
				os = "Unknown";
			} finally {
				try {
					connection.getSocket().setSoTimeout(0);
				} catch(Exception e) {
					e.printStackTrace();
					return 1;
				}
			}
			sb.append("  Operating System:  "+os+"\n");
			if(server instanceof DatabaseServer) {
				try {
					sb.append("  Nick registered:   "+(((DatabaseServer)server).nickExists(connection.getName()) ? "yes" : "no")+"\n");
				} catch(FileNotFoundException e) {
					printDebug("Caught exception in CommandsExcecutor.execute(whoami): "+e);
					connection.sendMsg("[Server database error]");
				}
			}
			connection.sendMsg(sb.toString());
			return 1;
		} else if(cmd.equals("serverinfo")) {
			StringBuilder sb = new StringBuilder("Info about: "+server.getName()+" (server)\n");
			if(server instanceof BasicServer) {
				sb.append("  IP Address:   "+((BasicServer)server).getServerSocket().getInetAddress().getHostAddress()+"\n");
				sb.append("  Hostname:   "+((BasicServer)server).getServerSocket().getInetAddress().getHostName()+"\n");
				sb.append("  Uptime:   "+secondsToDate(-((BasicServer)server).getConnectionTime().getTime()/1000+(new Date()).getTime()/1000)+"\n");
			}
			if(server instanceof DatabaseServer) {
				sb.append("  ChatSystem enabled: "+((DatabaseServer)server).advancedChat+"\n");
			}
			sb.append("  Operating System:   "+System.getProperty("os.name")+" "+System.getProperty("os.version")+"\n");
			connection.sendMsg(sb.toString());
			return 1;
		} else if(cmd.equals("users")) {
			int n = 0;
			connection.sendMsg("-- Connected users:");
			for(Connection conn : server.getClients()) {
				connection.sendMsg(conn.getName());
				++n;
			}
			connection.sendMsg("Number of connected users: "+n);
			return 1;
		} else if(cmd.equals("register")) {
			return registerNick(token);
		} else if(cmd.equals("nick")) {
			return assignNick(token);
		} else if(cmd.equals("disconnect")) {
			connection.sendMsg(CMN_PREFIX+"disconnect");
			return 1;
		} else if(cmd.equals("pm") || cmd.equals("whisper")) {
			if(token.length < 3) {
				connection.sendMsg("Syntax error. Correct syntax is "+CMD_PREFIX+"pm <user> <msg>.");
				return 1;
			}
			String mesg = ConcatenateArrays.merge(token,2);

			for(Connection conn : server.getClients()) {
				if(conn.getName().equals(token[1])) {
					conn.sendMsg("["+connection.getName()+" whispered]: "+mesg);
					connection.sendMsg("You whispered to "+token[1]+": "+mesg);
					return 1;
				}
			}
			connection.sendMsg("User "+token[1]+" not found.");
			return 1;
		} else {
			connection.sendMsg("Unknown command.");
			return 1;
		}
	}

	/** If server has a database, check if nick is already registered: if so, ask for 
	 * password.
	 */
	private int assignNick(String[] token) {
		if(token.length < 2) {
			connection.sendMsg("Syntax error. Correct syntax is "+CMD_PREFIX+"nick <nick> [password]");
			return 1;
		}
		String nick = token[1];

		/* If server is a DatabaseServer, then do the proper checks on nick; else, just assign it. */
		if(server instanceof DatabaseServer) {
			try {
				if(((DatabaseServer)server).nickExists(nick)) {
					if(token.length > 2) {
						/* Token 2 is the password */
						if(((DatabaseServer)server).checkPasswd(nick,token[2].toCharArray())) {
							// OK
							connection.sendMsg("Successfully logged in.");
							token[2] = null;
							connection.setName(nick);
							return 1;
						} else {
							printDebug("Login failed for nickname "+nick);
							connection.sendMsg("Incorrect password.");
							token[2] = null;
							return 1;
						}
					} else { 	// prompt for password
						connection.sendMsg("Nickname is registered. Give password:");
						for(ConnectionExecutor ex : connection.getExecutors()) {
							if(ex instanceof CommunicationsExecutor) {
								connection.sendMsg(CMN_PREFIX+"givepasswd");
								try {
									String line = connection.getInput().readLine();
									printDebug("Read: "+line);
									String[] words = line.split(" ");
									if(words.length < 2 || words[0].equals(CMN_PREFIX+"abort")) {
										printDebug("Aborted login for nick "+nick);
										return 1;
									} else {
										printDebug("Received password. Logging in nick "+nick+"...");
										int ret = 42;
										try {
											if(((DatabaseServer)server).checkPasswd(nick,words[1].toCharArray())) {
												if(connection.getVerbosity() >= 2) 
													printDebug("Password matched for nick "+nick);
												words[1] = null;
												connection.sendMsg("Successfully logged in.");
												connection.setName(nick);
												return 1;
											} else {
												if(connection.getVerbosity() >= 2) 
													printDebug("Password mismatch for nick "+nick);
												connection.sendMsg("Incorrect password.");
												return 1;
											}
										} catch(FileNotFoundException e) {
											printDebug("Database not found: "+e);
											connection.sendMsg("Server database error. Assigning temporary nicknames.");
											connection.setName(nick);
											return 1;
										}
									}
								} catch(IOException e) {
									printDebug("IOException while registering name: ");
									e.printStackTrace();
									return 1;
								}
							}
						}
						connection.sendMsg("Your client does not support this kind of password validation. Please use /nick <nick> <password>.");
						return 1;
					}
				} else {
					connection.setName(nick);
					return 1;
				}
			} catch(FileNotFoundException e) {
				printDebug("Database not found: "+e);
				printDebug("Assigning temporary nicknames to clients.");
				connection.sendMsg("Server database error: assigning temporary nicknames.");
				connection.setName(nick);
				return 1;
			}
		/* If the server has no database, just assign the nick. */
		} else {
			connection.setName(nick);
			return 1;
		}
	}

	private int registerNick(String[] token) {
		if(token.length != 2 && token.length != 3) {
			connection.sendMsg("Syntax error. Correct syntax is "+CMD_PREFIX+"register <nick> [password]");
			return 1;
		}
		
		if(!(server instanceof DatabaseServer)) {
			connection.sendMsg("This command is not supported by this server implementation.");
			return 1;
		}
	
		String nick = token[1];
		try {
			if(((DatabaseServer)server).nickExists(nick)) {
				connection.sendMsg("Nickname already registered.");
				return 1;
			}
		} catch(FileNotFoundException e) {
			printDebug("Database not found: "+e);
			connection.sendMsg("Server database error. Cannot register nicknames.");
			return 1;
		}
		/* If token > 2 then token[2] is the password */
		if(token.length > 2) {
			int ret = 42;
			switch((ret = ((DatabaseServer)server).registerNick(nick,token[2].toCharArray()))) {
				case 0:
					if(connection.getVerbosity() >= 1) printDebug("Registered nickname "+nick+".");
					connection.sendMsg("Successfully registered nickname "+nick+".");
					token[2] = null;
					connection.setName(nick);
					return 1;
				case 1:	//nickname esists
					if(connection.getVerbosity() >= 1) printDebug("Nickname "+nick+" not registered: already exists.");
					connection.sendMsg("Nickname "+nick+" already exists.");
					token[2] = null;
					return 1;
				case 2:	//generic error
					if(connection.getVerbosity() >= 1) printDebug("Error: couldn't register "+nick+".");
					connection.sendMsg("Error: couldn't register nickname "+nick+".");
					token[2] = null;
					return 1;
				case 3: //illegal name
					if(connection.getVerbosity() >= 1) printDebug("Illegal nick: "+nick+".");
					connection.sendMsg("Illegal nick.");
					token[2] = null;
					return 1;
				default:
					if(connection.getVerbosity() >= 1) printDebug("registerNick("+nick+") returned "+ret+".");
					token[2] = null;
					return 1;
			}
		} else {	// else prompt for password
			for(ConnectionExecutor ex : connection.getExecutors()) {
				if(ex instanceof CommunicationsExecutor) {
					connection.sendMsg(CMN_PREFIX+"givepasswd");
					try {
						String line = connection.getInput().readLine();
						printDebug("Read: "+line);
						String[] words = line.split(" ");
						if(words.length < 2 || words[0].equals(CMN_PREFIX+"abort")) {
							printDebug("Aborted registration.");
							return 1;
						} else {
							printDebug("Received password. Registering...");
							int ret = 42;
							switch((ret = ((DatabaseServer)server).registerNick(nick,words[1].toCharArray()))) {
								case 0:
									if(connection.getVerbosity() >= 1) printDebug("Registered nickname "+nick+".");
									connection.sendMsg("Successfully registered nickname "+nick+".");
									words[1] = null;
									connection.setName(nick);
									return 1;
								case 1:	//nickname esists
									if(connection.getVerbosity() >= 1) printDebug("Nickname "+nick+" not registered: already exists.");
									connection.sendMsg("Nickname "+nick+" already exists.");
									words[1] = null;
									return 1;
								case 2:	//generic error
									if(connection.getVerbosity() >= 1) printDebug("Error: couldn't register "+nick+".");
									connection.sendMsg("Error: couldn't register nickname "+nick+".");
									words[1] = null;
									return 1;
								case 3: //illegal name
									if(connection.getVerbosity() >= 1) printDebug("Illegal nick: "+nick+".");
									connection.sendMsg("Illegal nick.");
									words[1] = null;
									return 1;
								default:
									if(connection.getVerbosity() >= 1) printDebug("registerNick("+nick+") returned "+ret+".");
									words[1] = null;
									return 1;
							}
						}
					} catch(IOException e) {
						printDebug("IOException while registering name: ");
						e.printStackTrace();
						return 1;
					}
				}
			} 
			connection.sendMsg("Sorry: password prompt not implemented for your client.");
			return 1;
		}
	}

	/** Given a number of _seconds, returns a human-readable date in
	 * format days : hours : minutes : seconds
	 */
	private String secondsToDate(long _seconds) {
		int days;
		int hours;
		int minutes;
		int seconds;

		days = (int)(_seconds / 86400);
		hours = (int)((_seconds % 86400) / 3600);
		minutes = (int)((_seconds % 3600) / 60);
		seconds = (int)(_seconds % 60);

		return 	(days > 0 ? days + "d" : "") +
			(hours > 0 ? hours + "h " : "") + 
			(minutes > 0 ? minutes + "m " : "") +
			seconds + "s";
	}
}
