//: pokepon.net.jack/client/BasicClient.java

package pokepon.net.jack.client;

import pokepon.net.jack.*;
import static pokepon.util.MessageManager.*;
import java.net.*;
import java.io.*;

/** A basic TCP client with concurrent listening from the server.
 *
 * @author Giacomo Parolini
 */
public class BasicClient implements Client {

	public BasicClient() {
		stdIn = new BufferedReader(new InputStreamReader(System.in));
	}

	public static void main(String[] args) {
		BasicClient client = new BasicClient();

		if(args.length != 1 && args.length != 2 && args.length != 3) {
			System.err.println("Usage: BasicClient <host>[:<port>| <port>] [verbosity]");
			System.exit(1);
		}

		if(args.length == 1) {
			client.hostname = args[0].split(":")[0];
			try {
				client.port = Integer.parseInt(args[0].split(":")[1]);
			} catch(IndexOutOfBoundsException|NumberFormatException e) {
				System.err.println("Usage: BasicClient <host>[:<port>| <port>] [verbosity]");
				System.exit(1);
			}
		} else if(args.length == 2) {
			client.hostname = args[0];
			try {
				client.port = Integer.parseInt(args[1]);
			} catch(NumberFormatException e) {
				System.err.println("Usage: BasicClient <host>[:<port>| <port>] [verbosity]");
				System.exit(1);
			}
		} else {
			client.hostname = args[0];
			try {
				client.port = Integer.parseInt(args[1]);
				client.verbosity = Integer.parseInt(args[2]);
			} catch(NumberFormatException e) {
				System.err.println("Usage: BasicClient <host>[:<port>| <port>] [verbosity]");
				System.exit(1);
			}		
		}

		if(client.hostname.equals("") || client.port == -1) {
			System.err.println("Error: invalid hostname and/or port.");
			return;
		}

		client.start();
	}
	
	public void start() {
		try {
			s = new Socket(hostname,port);
			out = new PrintWriter(s.getOutputStream(),true);
			in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			
			/* Construct connection and connection executors */
			Connection connection = new ClientConnection(this,s,verbosity);
			connection.addConnectionExecutor(new ClientCommunicationsExecutor());
			(new Thread(connection)).start();

			//System.out.println("Retreiving nickname from the server...");
			//out.println(CMN_PREFIX+"mynick");
			//myNick = in.readLine().substring(1);
			//System.out.println("Connected to "+hostname+":"+port+" with nick "+myNick);

			String userInput;
			while((userInput = stdIn.readLine()) != null) {
				if(userInput.length() == 0) continue;
				out.println(userInput);
			}
			
		} catch(ConnectException e) {
			System.err.println("Couldn't connect to "+hostname+":"+port);
			System.exit(2);
		} catch(UnknownHostException e) {
			System.err.println("Unknown host: "+hostname);
			System.exit(3);
		} catch(Exception e) {
			System.err.println("Caught exception: "+e);
		} finally {
			try {
				in.close();
				out.close();
				stdIn.close();
				s.close();
			} catch(IOException e) {}
		}
	}
	
	public BufferedReader getStdIn() {
		return stdIn;
	}

	public void setStdIn(BufferedReader stdin) {
		stdIn = stdin;
	}

	public BufferedReader getIn() {
		return in;
	}

	public void setName(String nick) {
		myNick = nick;
	}

	public String getName() {
		return myNick;
	}

	private String hostname = "";
	private int port = -1;
	private Socket s;
	private PrintWriter out;
	private BufferedReader in;
	private BufferedReader stdIn;
	private String myNick = "Unknown";
	private int verbosity = 1;
}
