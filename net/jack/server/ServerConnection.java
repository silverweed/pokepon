//: pokepon.net.jack/server/ServerConnection.java

package pokepon.net.jack.server;

import pokepon.net.jack.*;
import pokepon.net.jack.chat.*;
import static pokepon.util.MessageManager.*;
import pokepon.util.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.lang.reflect.*;
import java.net.*;

/** Class that handles a single connection with a client;
 * A Connection basically receives messages from one client and decides how to
 * handle them: this is done via ConnectionExecutors, that can be added with
 * addConnectionExecutor(ConnectionExecutor exec); the message is then processed
 * by *all* the ConnectionExecutors, starting from the latest added one; Even if
 * no ConnectionExecutor is explicitly added, the DefaultConnectionExecutor is
 * always used (it just echoes the message to all other clients); To prevent the
 * next executor to process (part of) the message, ensure you produce the correct
 * return value.
 *
 * @author silverweed
 */
 
class ServerConnection extends Connection {

	MultiThreadedServer server;
	private static int nConn = 1;
	private Pinger pinger;

	public ServerConnection(MultiThreadedServer server, Socket client) {
		this(server, client, 0);
	}

	public ServerConnection(MultiThreadedServer server, Socket client, int verbosityLvl) {
		super(client, verbosityLvl);
		
		this.server = server;
		
		try {
			if(server.defaultNick != null)
				name = server.defaultNick + "-" + nConn++;
			else
				name = socket.getInetAddress().getHostName().split("\\.")[0] + "-" + nConn++;
			// before notifying other clients about this, verify it in the run() method
		} catch(Exception e) {
			printDebug("Caught exception while constructing Connection: "+e);
			try {
				socket.close();
			} catch(Exception ee) {
				printDebug("Caught exception while closing socket: "+ee);
			}
		}
		
		addConnectionExecutor(new DefaultConnectionExecutor());
	}
	
	@Override
	public void addConnectionExecutor(ConnectionExecutor exec) {
		exec.setConnection(this);
		if(exec instanceof ServerConnectionExecutor) {
			((ServerConnectionExecutor)exec).setServer(server);
		}
		executors.add(exec);
	}
	
	@Override
	public void run() {
		/// CONNECTION HANDSHAKE ///
		// Step 1: Try to retreive client's OS:
		//   S: !youros -> C
		//   C: !myos <os> -> S
		// If client doesn't respond with !myos and connPolicy is paranoid, reject it.
		if(verbosity >= 2) printDebug(name+": Retreiving client OS information...");
		try {
			socket.setSoTimeout(SOCKET_TIMEOUT);
			sendMsg(CMN_PREFIX+"youros");
			String[] token = input.readLine().split(" ");
			if(token.length >= 2) {
				if(token[0].equals(CMN_PREFIX+"myos")) {
					os = ConcatenateArrays.merge(token,1);
					if(verbosity >= 2) printDebug(name+": OK. OS set to "+os);
				} else if(
					server.connectPolicy == MultiThreadedServer.ConnectPolicy.PARANOID || 
					(server.connectPolicy == MultiThreadedServer.ConnectPolicy.AVERAGE && 
						(token[0].equals("GET") || token[0].equals("POST") || token[0].equals("HEAD")
						|| token[0].equals("DELETE") || token[0].equals("PUT") || token[0].equals("TRACE")
						|| token[0].equals("CONNECT"))
					)
				) {
					printDebug("[ServerConnection] Received invalid response `"+token[0]+"`: dropping connection with "+name);
					printDebug("  (Server.connectPolicy is set to "+server.connectPolicy+")");
					disconnect();
					return;
				}
			} else {
				if(verbosity >= 2) printDebug(name+": received invalid OS data: "+Arrays.asList(token));
				if(server.connectPolicy == MultiThreadedServer.ConnectPolicy.PARANOID) {
					printDebug("[ServerConnection] Received invalid response `"+token[0]+"`: dropping connection with "+name);
					printDebug("  (Server.connectPolicy is set to "+server.connectPolicy+")");
					disconnect();
					return;
				}
			}
		} catch(SocketTimeoutException e) {
			// os unknown
			if(server.connectPolicy == MultiThreadedServer.ConnectPolicy.PARANOID) {
				printDebug("[ServerConnection] Timeout on OS request. Dropping connection with "+name);
				printDebug("  (Server.connectPolicy is set to "+server.connectPolicy+")");
				disconnect();
				return;
			}
			if(verbosity >= 2) printDebug(name+": timeout. OS set to Unknown.");
		} catch(Exception e) {
			printDebug("Unexpected exception in ServerConnection.run(): "+e);
			e.printStackTrace();
			printDebug(name+" disconnecting.");
			disconnect();
			return;
		} finally {
			try {
				// Step 2: If we arrived here it means we accept client connection: conclude handshake.
				if(socket != null)
					socket.setSoTimeout(0);
				sendMsg(CMN_PREFIX+"ok");
			} catch(SocketException e) {
				printDebug("Unexpected exception in Connection.run(): "+e);
				printDebug(name+" disconnecting.");
				disconnect();
				return;
			}	
		}
		/// HANDSHAKE ENDS HERE ///

		// After verifying this client is legit, set its nick 
		sendMsg(CMN_PREFIX + "setnick " + name);

		if(server.chat != null) 
			server.chat.addUser(new ChatClient(this, new ChatUser(name)));

		// ... and send it data about currently connected users.
		if(verbosity >= 2) printDebug(name+": sending users data to client...");

		Iterable<Connection> clients = server.getClients();
		Iterator<Connection> it = clients.iterator();
		StringBuilder sb = new StringBuilder();
		while(it.hasNext()) {
			Connection conn = it.next();
			if(sb.length() < 1) {
				sb.append(CMN_PREFIX);
				sb.append("useradd ");
			}
			String role = "--";
			if(server.chat != null && server.chat.getUser(conn.getName()) != null && server.chat.getUser(conn.getName()).getRole() != ChatUser.Role.USER)
				role = Character.toString(server.chat.getUser(conn.getName()).getRole().getSymbol());

			sb.append(conn.getName() + " " + role + " ");
		}
		if(sb.length() > 0) {
			sendMsg(sb.toString().trim());
		}

		// Then notify other clients that we've just connected 
		if(verbosity >= 1) printDebug("[Connection] Constructed connection with "+socket+" (name: "+name+")");
		if(verbosity >= 0) server.broadcast(socket,name+" connected to server.");
		server.broadcast(socket, CMN_PREFIX+"useradd "+name + " --");
		
		// Send welcome message 
		if(server.welcomeMessage != null) {
			String[] messages = server.welcomeMessage.split("\\\\n");
			for(String msg : messages)
				sendMsg(CMN_PREFIX+"motd "+msg);
		}

		// Start ping routine
		pinger = new Pinger();
		pinger.setName(name + ".Pinger");
		pinger.setDaemon(true);
		pinger.start();

		// Start receiving loop
		try {
			receiveMsg();
			
		} catch(EOFException e) {
			printDebug("EOF: "+e.getMessage());
		} catch(IOException e) {
			printDebug("IOException: "+e);
		} catch(Exception e) {
			printDebug("Caught exception in Connection.run()");
			e.printStackTrace();
		} 
		server.broadcast(socket,name+" disconnected.");
		server.broadcast(socket,CMN_PREFIX+"userrm "+name);
		disconnect();
		return;
	}			
	
	/** Attempts to assign nick 'nick' to client; this operation will fail if:
	 * 1 - a client already has that nick;
	 * 2 - the given nick is illegal for the server
	 * @return True - if the nick was assigned; False - otherwise.
	 */
	@Override
	public boolean setName(String nick) {
		if(nick == null || nick.length() == 0) {
			sendMsg("Syntax error.");
			return false;
		}
		if(nick.length() < server.minNickLen()) {
			sendMsg("Nickname too short. Nicknames should have at least "+server.minNickLen()+" characters.");
			return false;
		}
		if(verbosity >= 2) printDebug("Assigning new nick \""+nick+"\" to "+name+" ("+socket.getInetAddress()+")");
		String newname = MessageManager.sanitize(nick);
		
		if(server instanceof NameValidatingServer) {
			if(verbosity >= 2) printDebug("Server supports names validation.");
			try {
				int maxNickLen = ((NameValidatingServer)server).maxNickLen();
				if(newname.length() > maxNickLen) {
					newname = newname.substring(0,maxNickLen);
				}
				if(!((NameValidatingServer)server).isValidName(newname)) {
					sendMsg("illegal name.");
					if(verbosity >= 1) printDebug(name+" requested illegal nick "+newname);
					return false;
				}
			} catch(Exception e) {
				printDebug("Caught exception in Connection.assignNick: "+e);
				e.printStackTrace();
			}
		} else {
			if(verbosity >= 2) printDebug("[INFO] Server doesn't support names validation.");
		}
		
		Iterable<Connection> clients = server.getClients();
		Iterator<Connection> it = clients.iterator();
		while(it.hasNext()) {
			Connection conn = it.next();
			if(conn.getName().equals(newname)) {
				if(verbosity >= 1) printDebug(name+" requested already-in-use nick "+newname);
				sendMsg("Nickname already in use.");
				return false;
			}
		}
		// change chat user name and role
		if(server.chat != null) {
			if(!server.chat.renameUser(name, newname))
				printDebug("[ServerConnection] Error: couldn't rename "+name+" to "+newname);
			printDebug("User role: "+server.chat.getUser(newname).getRole());
		}
		// notify clients about the change
		sendMsg(CMN_PREFIX+"setnick "+newname + (server.chat != null ? " " + server.chat.getUser(newname).getRole().getSymbol() : ""));
		sendMsg("Your nick is now "+newname);
		server.broadcast(null,CMN_PREFIX+"userrnm "+name+" "+newname
			+ (server.chat != null ? " " + server.chat.getUser(newname).getRole().getSymbol() : ""));
		server.broadcast(socket,name+" changed its nick to "+newname);
		if(verbosity >= 0) printDebug(name+" changed its nick to "+newname);
		// change this connection's name
		name = newname;
		return true;			
	}
	
	/** Echoes all messages received to all other clients */
	protected class DefaultConnectionExecutor extends ConnectionExecutor {

		@Override
		public int execute(String msg) {
			if(msg == null) {
				if(verbosity >= 1) printDebug("Received null from client.");
				return 2;
			}
			if(msg.length() == 0) {
				if(verbosity >= 3) printDebug("Message length = 0. Not broadcasting.");
				return 1;
			}
			// check if this user is muted
			if(server.chat != null && !server.chat.hasPermission(name, ChatUser.Permission.CAN_TALK)) {
				sendMsg("You are not allowed to talk on this server.");
				return 1;
			}
			
			if(verbosity >= 2) printMsg(name+" said: "+msg);
			server.broadcast(null, "["+now("HH:mm:ss")+"] "+name+" said: "+msg);
			return 1;
		}
	}

	@Override
	public void disconnect() {
		if(verbosity >= 2) printDebug("Called "+name+".disconnect()");
		if(server instanceof PokeponServer)
			((PokeponServer)server).destroyAllBattles(name);
		synchronized(server) {
			List<Connection> clients = server.getClients();
			if(clients.remove(this)) {
				if(verbosity >= 2)
					printDebug("ServerConnection "+name+" removed from clients list.");
			} else {
				printDebug("[ServerConnection "+name+"] Failed to remove myself from clients list!");
			}
		}
		super.disconnect();
	}

	public void finalize() {
		if(verbosity >= 2) printDebug("[INFO] ServerConnection "+name+" was finalized.");
	}
	
	/** Method used to create a self-destructing connection which sends a
	 * single message to a client and then disconnects from it.
	 */
	public static void dropWithMsg(Socket client,String msg) {
		if(Debug.on) printDebug("{"+now()+"} Sending drop message to "+client+": "+msg);

		try(PrintWriter pw = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), "UTF-8"))) {
			pw.write(msg+"\n");
		} catch(IOException e) {
			printDebug("Caught exception while sending message: "+e);
		}
	}

	public Pinger getPinger() { return pinger; }

	class Pinger extends Thread {
		private BlockingQueue<Byte> pongQueue = new ArrayBlockingQueue<>(5);

		public void add() {
			try {
				pongQueue.add((byte)1);
			} catch(IllegalStateException e) {
				printDebug("["+name+".Pinger] Pongs are accumulating in this Pinger! Disconnecting to prevent flooding.");
				disconnect();
			}
		}

		public void run() {
			if(Debug.on) 
				printDebug("["+name+"] Started Pinger. Ping delay: "+BasicServer.PING_DELAY+" s.");
			while(true) {
				try {
					// send the ping message and wait for a pong to appear in the
					// pong queue. If a pong does not arrive within
					// PONG_MAX_WAIT seconds, disconnect with the client.
					sendMsg(CMN_PREFIX+"ping");
					Byte response = pongQueue.poll(BasicServer.PONG_MAX_WAIT, TimeUnit.SECONDS);
					if(response == null) {
						printDebug("["+name+".Pinger] Pong timeout: disconnecting");
						break;
					}
					Thread.sleep(BasicServer.PING_DELAY * 1000);
				} catch(InterruptedException e) {
					printDebug("["+name+".Pinger] Timeout. Disconnecting.");
					break;
				} catch(Exception e) {
					e.printStackTrace();
					break;
				}
			}
			disconnect();
		}
	}
}
	
