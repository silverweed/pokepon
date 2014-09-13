//: pokepon.net.jack/client/BasicGUIClient.java

package pokepon.net.jack.client;

import pokepon.net.jack.*;
import pokepon.gui.SwingConsole;
import static pokepon.util.MessageManager.*;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class BasicGUIClient extends JFrame implements GUIClient {

	private final static int MAX_HIST_SIZE = 50;
	private Socket s;
	private JTextArea inTxt = new JTextArea(10,80);
	private JTextField outTxt = new JTextField(80);
	private JButton exitButton = new JButton("quit");
	private BufferedReader stdIn;
	private BufferedReader in;
	private PrintWriter out;
	private GUIListener listener;
	private String hostname = "";
	private int port = -1;
	private LinkedList<String> history = new LinkedList<String>();
	private int index; //history index
	private String myNick = "Unknown";
	private volatile String fromServer = "";

	private KeyListener outKeyListener = new KeyListener() {
		public void keyPressed(KeyEvent e) {
			//sane history index
			if(index <= -1) index = 0;
			else if(index >= history.size()) index = history.size();

			switch(e.getKeyCode()) {
				case KeyEvent.VK_ENTER:
					if(outTxt.getText() != null && outTxt.getText().length() > 0) {
						if(outTxt.getText().charAt(0) != '/') inTxt.append(myNick+" said: "+outTxt.getText()+"\n");
						synchronized(out) {
							out.println(outTxt.getText());
						}
						history.add(outTxt.getText());
						if(history.size() > MAX_HIST_SIZE) history.removeFirst();
						index = history.size()-1;
						outTxt.setText("");
					}
					break;
				case KeyEvent.VK_UP:
					if(history.size() > 0 && index > -1) {
						outTxt.setText(history.get(index--));
					}
					break;
				case KeyEvent.VK_DOWN:
					if(history.size() > 0 && index < history.size()-1) {
						outTxt.setText(history.get(++index));
					} else {
						if(outTxt.getText() != null && outTxt.getText().length() > 0 && !history.get(history.size()-1).equals(outTxt.getText())) {
							history.add(outTxt.getText());
							if(history.size() > MAX_HIST_SIZE) history.removeFirst();
							index = history.size()-1;
						}
						outTxt.setText("");
					}
					break;
			}
		}
		public void keyReleased(KeyEvent e) {}
		public void keyTyped(KeyEvent e) {}
	};
	
	public JTextArea getInTxt() {
		return inTxt;
	}

	public void append(String str) {
		inTxt.append(str);
	}
	
	public JTextField getOutTxt() {
		return outTxt;
	}
	
	public void setNick(String nick) {
		printDebug("Set nick to "+nick);
		myNick = nick;
	}

	public BasicGUIClient() {
		//FIXME: resizing won't work
		addComponentListener(new ComponentAdapter() {
			public void componentResized() {	
				printDebug("Resized; new size: "+getSize());
				inTxt.setSize(getSize());
				outTxt.setSize(getSize());
			}
		});
		setLayout(new FlowLayout());
		stdIn = new BufferedReader(new InputStreamReader(System.in));
		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		add(exitButton);
		add(new JLabel("From server"));
		inTxt.setEditable(false);
		DefaultCaret caret = (DefaultCaret)inTxt.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		inTxt.setMaximumSize(getSize());
		add(inTxt);
		add(new JScrollPane(inTxt));
		add(new JLabel("To server"));
		outTxt.addKeyListener(outKeyListener);
		outTxt.setMaximumSize(getSize());
		add(outTxt);
	}

	public void start() {
		try {
			s = new Socket(hostname,port);
			out = new PrintWriter(s.getOutputStream(),true);
			in = new BufferedReader(new InputStreamReader(s.getInputStream()));

			consoleHeader(new String[]{" Java Awful Client-server Kit ","v 1.0"},'*');
			inTxt.append("Retreiving nickname from the server...\n");
			out.println("!mynick");
			myNick = in.readLine().substring(1);
			inTxt.append("Connected to "+hostname+":"+port+" with nick "+myNick+"\n");
			setTitle("Connected to "+hostname+":"+port);
			validate();

			/*listener = new GUIListener();
			(new Thread(listener)).start();*/
			//listener.run();
			Connection connection = new ClientConnection(this,s,3);
			//connection.addConnectionExecutor(new GUIExecutor());
			connection.addConnectionExecutor(new ClientCommunicationsExecutor());
			(new Thread(connection)).start();
			
			//FIXME
			while(!Thread.currentThread().isInterrupted()) {
				/*try {
					printDebug("Asking for nick...");
					myNick = askNick();	
					TimeUnit.SECONDS.sleep(5);
				} catch(InterruptedException e) {
					printDebug("Main thread interrupted.");
				}*/
			}
			
		} catch(ConnectException e) {
			printDebug("Couldn't connect to "+hostname+":"+port);
			System.exit(2);
		} catch(UnknownHostException e) {
			printDebug("Unknown host: "+hostname);
			System.exit(3);
		} catch(Exception e) {
			printDebug("Caught exception: "+e);
		} finally {
			try {
				in.close();
				out.close();
				stdIn.close();
				s.close();
			} catch(IOException e) {}
		}
	}

	public static void main(String[] args) {
		BasicGUIClient client = new BasicGUIClient();

		if(args.length != 1 && args.length != 2) {
			printDebug("Usage: BasicGUIClient <host>[:<port>| <port>]");
			System.exit(1);
		}

		if(args.length == 1) {
			client.hostname = args[0].split(":")[0];
			try {
				client.port = Integer.parseInt(args[0].split(":")[1]);
			} catch(IndexOutOfBoundsException|NumberFormatException e) {
				printDebug("Usage: BasicClient <host>[:<port>| <port>]");
				System.exit(1);
			}
		} else if(args.length == 2) {
			client.hostname = args[0];
			try {
				client.port = Integer.parseInt(args[1]);
			} catch(NumberFormatException e) {
				printDebug("Usage: BasicClient <host>[:<port>| <port>]");
				System.exit(1);
			}
		}
		
		if(client.hostname.equals("") || client.port == -1) {
			printDebug("Error: invalid hostname and/or port.");
			return;
		}

		SwingConsole.run(client,400,500);
		client.start();
	}

	class GUIListener implements Runnable {
		public void run() {
			printDebug("Started GUIListener.");
			fromServer = "";
			try {
				while(s.isConnected()) {
					synchronized(fromServer) {
						printDebug("before readline");
						fromServer = in.readLine();
						printDebug("read: "+fromServer);
						if(fromServer == null) 
							break;
					}
					if(fromServer.length() > 0 && fromServer.charAt(0) == '!')
						synchronized(fromServer) {
							fromServer.notifyAll();
						}
					else if(fromServer.length() > 0)
						inTxt.append(fromServer+"\n");
				}
				if(fromServer == null) {
					printDebug("Received null from server: quitting.");
				}
				s.close();
				System.exit(1);
			} catch(IOException e) {
				printDebug("Caught IOException while listening: "+e);
			}
		}
	}

	private String askNick() {
		try {
			synchronized(out) {
				out.println("!mynick");
			}
			synchronized(fromServer) {
				while(fromServer.length() < 1 || fromServer.charAt(0) != '!')
					fromServer.wait();
				return fromServer.substring(1);
			}
		} catch(InterruptedException e) {
			return "Unknown";
		}
	}
}


