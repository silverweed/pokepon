//: pokepon.net.jack/Connection.java

package pokepon.net.jack;

import pokepon.util.*;
import static pokepon.util.MessageManager.*;
import java.util.concurrent.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import java.net.*;

/** Abstract class to handle a single connection;
 * A Connection basically receives messages from one client and decides how to
 * handle them: this is done via ConnectionExecutors, that can be added with
 * addConnectionExecutor(ConnectionExecutor exec); the message is then processed
 * by *all* the ConnectionExecutors, starting from the latest added one; Even if
 * no ConnectionExecutor is explicitly added, the DefaultConnectionExecutor is
 * always used (it does nothing); To prevent the
 * next executor to process (part of) the message, ensure you produce the correct
 * return value;
 * Can be used both by a Server (ServerConnection) and a Client (ClientConnection)
 * to handle exchanged messages.
 *
 * @author silverweed
 */
 
public abstract class Connection implements Runnable {

	protected final static int SOCKET_TIMEOUT = 2000; //ms
	/** Length of history */
	protected final static int MSG_RETAIN_LIMIT = 50;
	protected BufferedReader input;
	protected PrintWriter output;
	protected Socket socket;
	protected String name;
	protected int verbosity;
	protected Date connectionTime;
	protected List<ConnectionExecutor> executors = new ArrayList<ConnectionExecutor>();
	protected String os;	
	protected FixedQueue<Map.Entry<Long,String>> latestMessages = 
			new FixedQueue<Map.Entry<Long,String>>(MSG_RETAIN_LIMIT);
	private volatile boolean readLocked = false;

	public Connection(Socket socket) {
		this(socket, 0);
	}

	public Connection(Socket socket,int verbosityLvl) {
		verbosity = verbosityLvl;
		
		if(verbosity >= 1) printDebug("{"+now()+"} Starting connection with "+socket+" with verbosity "+verbosity+".");

		try {
			this.socket = socket;
			connectionTime = new Date();
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			output = new PrintWriter(socket.getOutputStream(),true);
			name = "Unnamed connection";
		} catch(IOException e) {
			printDebug("Caught exception while constructing Connection: "+e);
			try {
				socket.close();
			} catch(Exception ee) {
				printDebug("Caught exception while closing socket: "+ee);
			}
		}
		
		addConnectionExecutor(new DefaultConnectionExecutor());
	}
	
	public void addConnectionExecutor(ConnectionExecutor exec) {
		exec.setConnection(this);
		synchronized(executors) {
			executors.add(exec);
		}
	}
	
	public void sendMsg(String msg) {
		if(verbosity >= 2) printDebug("[Connection ("+name+")] sending msg: "+msg);
		synchronized(output) {
			output.println(msg);
		}
	}

	public String getName() { return name; }
	public Socket getSocket() { return socket; }
	public Date getConnectionTime() { return connectionTime; }
	public PrintWriter getOutput() { return output; }
	public BufferedReader getInput() { return input; }
	public int getVerbosity() { return verbosity; }
	public List<ConnectionExecutor> getExecutors() { return executors; }
	public String getOS() { return os; }
	public FixedQueue<Map.Entry<Long,String>> getLatestMessages() { return latestMessages; }

	/** Blocks the connection receiveMsg() method, in order to let other processes to read from its socket. */
	public final synchronized void lockReading() {
		if(verbosity >= 2) printDebug("["+name+"]: reading locked.");
		readLocked = true;
	}
	/** Lets the connection receve messages normally. */
	public final synchronized void unlockReading() { 
		if(verbosity >= 2) printDebug("["+name+"]: reading unlocked.");
		readLocked = false; 
		notifyAll();
	}

	public void run() {
		/* Start receiving loop */
		try {
			receiveMsg();
			
		} catch(InterruptedException e) {
			printDebug("receiveMsg() was interrupted.");
		} catch(EOFException e) {
			printDebug("EOF: "+e.getMessage());
		} catch(IOException e) {
			printDebug("IOException: "+e);
		} catch(Exception e) {
			printDebug("Caught exception in Connection.run()");
			e.printStackTrace();
		} finally {
			disconnect();
		}
	}			

	public void disconnect() {
		if(verbosity >= 1) printDebug("{"+now()+"} Closing connection with "+name+" ("+socket.getInetAddress()+")");
		if(verbosity >= 0) printDebug("Disconnected from "+name+".");
		try {
			synchronized(socket) {
				socket.close();
				socket.notifyAll();
			}
		} catch(IOException e) {
			printDebug("IOException while closing socket: "+e);
		}
	}

	protected synchronized void receiveMsg() throws EOFException,IOException,InterruptedException {
		String msg = "";
		outer:
		do {	
			while(readLocked) {
				if(verbosity >= 1) printDebug("["+name+"] reading is locked. Waiting...");
				wait();
				if(verbosity >= 1) printDebug("["+name+"] notified: waking up...");
			}
			if(verbosity >= 3) printDebug("["+name+"] Reading line...");
			try {
				msg = input.readLine();
			} catch(SocketTimeoutException e) {
				if(verbosity >= 1) printDebug("Socket timeout for connection "+this+".");
				try {
					socket.setSoTimeout(0);
				} catch(SocketException ee) {
					printDebug("Socket exception: "+ee);
					printDebug(name+" disconnecting.");
					disconnect();
				}
				continue outer;
			}
			if(verbosity >= 2) printDebug("["+name+"] Read. msg is "+msg);
			
			if(msg == null) break;
			if(msg.length() == 0) continue;

			try {
				latestMessages.add(new AbstractMap.SimpleEntry<Long,String>(System.currentTimeMillis(),msg));
			} catch(Exception e) {
				e.printStackTrace();
			}

			if(verbosity >= 3) printDebug("before for; executors: "+executors.size());
			// executors are processed from the latest added one to the first
			// A return value > 0 means that the other executors shouldn't
			// be invoked for this msg.
			middle:
			for(int i = executors.size()-1; i > -1; --i) {
				if(verbosity >= 3) printDebug("Calling executor["+i+"]: "+executors.get(i).getClass().getSimpleName());
				int result = executors.get(i).execute(msg);
				if(verbosity >= 4) printDebug("Executor["+i+"] returned "+result);
				inner:
				//switch(executors.get(i).execute(msg)) {
				switch(result) {
					case 0: 
						if(verbosity >= 4) printDebug("execute("+msg+") returned 0");
						break inner;
					case 1: 
						if(verbosity >= 4) printDebug("execute("+msg+") returned 1");
						break middle;
					case 2: 
						if(verbosity >= 4) printDebug("execute("+msg+") returned 2");
						break outer;
					default: 
						if(verbosity >= 4) printDebug("execute("+msg+") returned default");
						break inner;
				}
			}
			if(verbosity >= 3) printDebug("["+name+"] End of cycle");
			Thread.yield();
		} while (msg != null);
		if(verbosity >= 1) printDebug("["+name+"] RECEIVED NULL");
	}
	
	/** This should be overridden by the ServerConnection */
	public boolean setName(String name) {
		this.name = name;
		return true;
	}

	public void setOS(String osName) {
		os = osName;
	}
	
	/** To be overridden by all children */
	protected class DefaultConnectionExecutor extends ConnectionExecutor {
		public int execute(String msg) {
			return 1;
		}
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder("");
		sb.append("name: "+name);
		sb.append(" {addr:"+socket.getInetAddress());
		if(verbosity >= 3) sb.append(",exctrs: "+executors.size());
		sb.append("}");
		return sb.toString();
	}
}
	
