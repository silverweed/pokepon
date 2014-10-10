//: pokepon.net.jack/server/ServerConnection.java

package pokepon.net.jack.server;

import pokepon.net.jack.*;
import static pokepon.util.MessageManager.*;
import pokepon.util.*;
import java.io.*;
import java.util.*;
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

	public ServerConnection(MultiThreadedServer server,Socket client,int... verbosityLvl) {
		super(client,verbosityLvl);
		
		this.server = server;
		
		try {
			name = socket.getInetAddress().getHostName().split("\\.")[0] + "-" + nConn++;
			// before noticing other clients about this, verify it in the run() method
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
		/* Shyly try to retreive the client's OS */
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
						|| token[0].equals("CONNECT")))
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
			printDebug(name+" disconnecting.");
			disconnect();
			return;
		} finally {
			try {
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

		/* After verifying this client is legit, send users list to client */
		if(verbosity >= 2) printDebug(name+": sending users data to client...");
		Iterable<Connection> clients = server.getClients();
		synchronized(clients) {
			Iterator<Connection> it = clients.iterator();
			while(it.hasNext()) {
				Connection conn = it.next();
				if(conn.getName().equals(name))
					sendMsg(CMN_PREFIX+"useradd "+name+" #0000FF");
				else
					sendMsg(CMN_PREFIX+"useradd "+conn.getName());
			}
		}
		/* The notify other clients that we've just connected */
		if(verbosity >= 1) printDebug("[Connection] Constructed connection with "+socket+" (name: "+name+")");
		if(verbosity >= 0) server.broadcast(socket,name+" connected to server.");
		server.broadcast(socket,CMN_PREFIX+"useradd "+name);

		/* Start receiving loop */
		try {
			receiveMsg();
			
		} catch(EOFException e) {
			printDebug("EOF: "+e.getMessage());
		} catch(IOException e) {
			printDebug("IOException: "+e);
		} catch(Exception e) {
			printDebug("Caught exception in Connection.run()");
			e.printStackTrace();
		} finally {
			server.broadcast(socket,name+" disconnected.");
			server.broadcast(socket,CMN_PREFIX+"userrm "+name);
			disconnect();
			return;
		}
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
		if(verbosity >= 2) printDebug("Assigning new nick \""+nick+"\" to "+name+" ("+socket.getInetAddress()+")");
		String newname = MessageManager.sanitize(nick);
		
		if(server instanceof NameValidatingServer) {
			if(verbosity >= 2) printDebug("Server supports names validation.");
			try {
				int maxNickLen = ((NameValidatingServer)server).maxNickLen();
				if(newname.length() > maxNickLen) {
					newname = newname.substring(0,maxNickLen-1);
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
		synchronized(clients) {
			Iterator<Connection> it = clients.iterator();
			while(it.hasNext()) {
				Connection conn = it.next();
				if(conn.getName().equals(newname)) {
					if(verbosity >= 1) printDebug(name+" requested already-in-use nick "+newname);
					sendMsg("Nickname already in use.");
					return false;
				}
			}
		}
		sendMsg(CMN_PREFIX+"setnick "+newname);
		sendMsg("Your nick is now "+newname);
		server.broadcast(null,CMN_PREFIX+"userrnm "+name+" "+newname);
		server.broadcast(socket,name+" changed its nick to "+newname);
		if(verbosity >= 0) printDebug(name+" changed its nick to "+newname);
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
			
			if(verbosity >= 0) printMsg(name+" said: "+msg);
			try {
				server.broadcast(socket,name+" said: "+msg);
			} catch(Exception e) {
				printDebug("Caught exception while pushing message: "+e);
			}
			return 1;
		}
	}

	@Override
	public synchronized void disconnect() {
		if(Debug.on) printDebug("Called "+name+".disconnect()");
		if(server instanceof PokeponServer)
			((PokeponServer)server).destroyAllBattles(name);
		if(server instanceof MultiThreadedServer) {
			List<Connection> clients = ((MultiThreadedServer)server).getClients();
			synchronized(clients) {
				if(clients.remove(this)) {
					if(verbosity >= 2)
						printDebug("ServerConnection "+name+" removed from clients list.");
				} else {
					printDebug("[ServerConnection "+name+"] Failed to remove myself from clients list!");
				}
			}
		}
		super.disconnect();
	}

	public void finalize() {
		if(Debug.on) printDebug("[INFO] ServerConnection "+name+" was finalized.");
	}
	
	/** Method used to create a self-destructing connection which sends a
	 * single message to a client and then disconnects from it.
	 */
	public static void dropWithMsg(Socket client,String msg) {
		printDebug("Sending drop message to "+client+": "+msg);

		try(PrintWriter pw = new PrintWriter(client.getOutputStream())) {
			pw.write(msg+"\n");
		} catch(IOException e) {
			printDebug("Caught exception while sending message: "+e);
		}
	}
}
	
