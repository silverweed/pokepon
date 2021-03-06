//: net/PokeponClient.java

package pokepon.net.jack.client;

import pokepon.gui.*;
import pokepon.net.jack.*;
import pokepon.main.TestingClass;
import pokepon.pony.*;
import pokepon.battle.*;
import pokepon.sound.PresetBGM;
import static pokepon.util.MessageManager.*;
import pokepon.util.*;
import pokepon.player.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.regex.*;
import java.net.*;
import java.io.*;
import java.lang.reflect.*;

/** The Pokepon Client
 *
 * @author silverweed
 */
public class PokeponClient extends JPanel implements ChatClient, TestingClass {

	public final static int MAX_LOADABLE_TEAMS = 20;
	public final static int DEFAULT_PORT = 12344;

	protected Socket socket;
	protected String host = "";
	protected int port = -1;
	protected ClientConnection connection;
	protected PokeponChat chat = new PokeponChat();
	protected ButtonsPanel buttonsP = new ButtonsPanel(this);
	protected Team team;
	protected String format;
	protected List<Team> teams = new LinkedList<Team>();
	protected Map<String,BattlePanel> battles = new HashMap<String,BattlePanel>();
	protected TeamDealer teamDealer = new TeamDealer();
	protected boolean loadedTeams;
	protected Map<String,BattleLogger> battleLoggers = new HashMap<>();
	// Client options: must be Objects, not primitives!
	protected volatile Boolean optLogBattle;

	public PokeponClient(String host,int port) {
		super(true);
		this.host = host;
		this.port = port;
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = c.gridy = 0;
		c.weightx = 0;
		c.ipadx = c.ipady = 2;
		c.insets = new Insets(3, 3, 3, 3);
		c.anchor = GridBagConstraints.PAGE_START;
		add(buttonsP, c);
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 1;
		c.gridwidth = 3;
		c.weightx = 1;
		c.weighty = 1;
		add(chat, c);

		// Default options
		optLogBattle = true;
	}

	public synchronized void start() throws ConnectException, UnknownHostException {
		try {
			socket = new Socket(host,port);
			chat.initialize(socket);
			connection = new ClientConnection(this,socket,(Debug.pedantic ? 3 : (Debug.on ? 2 : 1)));
			connection.addConnectionExecutor(new ClientBattleExecutor());
			connection.addConnectionExecutor(new PokeponClientCommunicationsExecutor());
			connection.setName(chat.getNick());
			Thread connectionThread = new Thread(connection);
			connectionThread.setName("Pokepon Client Connection");
			connectionThread.start();
			JFrame frame = new JFrame();
			frame.add(this);
			frame.addWindowFocusListener(new WindowAdapter() {
				public void windowGainedFocus(WindowEvent e) {
					chat.getChatPanel().requestFocusInWindow();
				}
			});
			SwingConsole.run(frame,"Pokepon Client");
			// Load teams in a separate thread to speed up startup process.
			if(Debug.on) printDebug("[PokeponClient] loading teams...");
			new Thread() {
				public void run() {
					loadTeams();
				}
			}.start();
			wait(); //suspend this thread not to throttle CPU.
		} catch(ConnectException|UnknownHostException e) {
			throw e;
		} catch(Exception e) {
			printDebug("Caught exception: "+e);
			e.printStackTrace();
		} finally {
			chat.close();
			try {
				socket.close();
			} catch(Exception e) {
				printDebug("Exception while closing socket: "+e);
			}
		}
	}

	/** Command line: PokeponClient &lt;host[:port]&gt; [port] */
	public static void main(String[] args) {
		if(args.length < 1 || args.length > 2) 
			printUsage();
		String[] splitted = args[0].split(":");
		if(splitted.length > 2)
			printUsage();
		int port = DEFAULT_PORT;
		if(splitted.length == 1) {
			if(args.length > 2)
				printUsage();
			else if(args.length > 1)
				port = new Integer(args[1]);
		} else {
			port = new Integer(splitted[1]);
		}
		//SwingConsole.setSystemLookAndFeel();
		PokeponClient client = new PokeponClient(splitted[0],port);
		try {
			client.start();
		} catch(UnknownHostException e) {
			printDebug("Unknown host: "+splitted[0]);
		} catch(ConnectException e) {
			printDebug("Couldn't connect to host!");
		}
	}
	
	protected static void printUsage() {
		consoleMsg("Usage: PokeponClient <server[:port]> [port]");
		System.exit(1);
	}

	public ClientConnection getConnection() { return connection; }
	public PokeponChat getChat() { return chat; }
	public void append(String str) { 
		chat.getChatPanel().append(str); 
	}
	public void append(String str, boolean escapeHTML) { 
		chat.getChatPanel().append(str, escapeHTML); 
	}
	public void setName(String nick) { 
		chat.setNick(nick);
		connection.setName(nick);
	}
	public String getName() {
		return chat.getNick();
	}
	/** Uses reflection to retrieve the value of an option; options all have a name
	 * like optSomeNameHere, and you can call hasOptionEnabled(someNameHere) to
	 * retrieve it without using explicit getters; since this returns a Field, you
	 * should know the type of the retrieved option, so you can cast it with Field.get().
	 * @return the Object representing this option
	 */
	public Object getOption(String opt) {
		try {
			return getClass().getField("opt"+opt.substring(0,1).toUpperCase()+opt.substring(1)).get(this);
		} catch(NoSuchFieldException|SecurityException|IllegalArgumentException|IllegalAccessException e) {
			printDebug("[PokeponClient.getOption("+opt+")] exception: "+e);
			return null;
		}
	}
	/** Similar to getOption(String opt), but only for boolean values; works as a shortcut method 
	 * for getOption(opt).get() and all the exception handling implied.
	 * @return true if option exists, is boolean and is true, false otherwise
	 */
	public boolean hasOptionEnabled(String opt) {
		try {
			Field optField = getClass().getDeclaredField("opt"+opt.substring(0,1).toUpperCase()+opt.substring(1));
			if(optField.getType() != Boolean.class)
				return false;
			return (boolean)optField.get(this);
		} catch(NoSuchFieldException|SecurityException|ClassCastException|IllegalAccessException e) {
			printDebug("[PokeponClient.hasOptionEnabled("+opt+")] exception: "+e);
		}
		return false;
	}

	public Socket getSocket() { return socket; }
	public synchronized void stop() {
		for(BattlePanel bp : battles.values()) {
			bp.leave();
			bp.terminate();
		}
		try {
			chat.dispose();
			socket.close();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		System.exit(0);
	}

	public synchronized Team getTeam() { return team; }
	public synchronized List<Team> getTeams() { return teams; }
	public BattlePanel getBattle(String id) {
		return battles.get(id);
	}
	public Map<String,BattlePanel> getBattles() {
		return battles;
	}

	public synchronized String getFormat() { return format; }

	public void logBattleLine(final String line) {
		if(line.charAt(0) != BTL_PREFIX || line.length() < 2) return;
		BattleLogger logger = battleLoggers.get(Character.toString(line.charAt(1))); 
		if(logger != null)
			logger.addLine(line);
	}


	/** This method creates a BattlePanel and pushes it into the battles Map, which
	 * associates battle IDs with battlePanels. 
	 * @param id The unique ID of the battle (given by the server)
	 * @param format The battle's format
	 * @param asGuest Join the battle as guest (default: false)
	 * @param bgNum Background to use (normalized, default: random)
	 * @param bgmNum Background music to use (normalized, default: random)
	 */
	public boolean spawnBattle(final String id, final String format, final boolean asGuest, final float bgNum, final float bgmNum) {
		if(battles.containsKey(id)) throw new RuntimeException("Duplicate ID in battles map!");
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					final BattlePanel newbattle = new BattlePanel(
										connection,
										id, 
										new Player(connection.getName()),
										new Player("<Opponent>")
										);
					if(asGuest)
						newbattle.watchAsGuest(true);
					try {
						if(bgNum != -1f) {
							if(bgmNum != -1f)
								newbattle.initialize(
									format,
									(int)(bgNum*BattlePanel.BG_IMG_URL.length),
									(int)(bgmNum*PresetBGM.getPresets().size())
								);
							else
								newbattle.initialize(format,(int)(bgNum*BattlePanel.BG_IMG_URL.length));
						} else {
							newbattle.initialize(format);
						}
						battles.put(id, newbattle);
						if(optLogBattle) {
							if(Debug.on) printDebug("[spawnBattle] attaching FileBattleLogger to battle "+id+".");
							BattleLogger bl = new FileBattleLogger(newbattle);
							newbattle.setBattleLogger(bl);
							battleLoggers.put(id, bl);
						}
						final JFrame battleFrame = new JFrame();
						battleFrame.add(newbattle);
						battleFrame.addWindowListener(new WindowAdapter() {
							public void windowClosing(WindowEvent e) {
								if(Debug.on) printDebug("Battle window closing.");
								newbattle.leave();
								newbattle.terminate();
								battles.remove(id);
							}
						});
						battleFrame.setTitle("Battle ["+id+"]");
						battleFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
						battleFrame.pack();
						battleFrame.setVisible(true);
					} catch(Exception ee) {
						newbattle.terminate();
						printDebug("Caught exception while starting battle:");
						ee.printStackTrace();
						throw ee;
					}
				}
			});
		} catch(InterruptedException|java.lang.reflect.InvocationTargetException e) {
			printDebug("There was an exception: battle not started: "+e);
			printDebug("Caused by: "+e.getCause());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean spawnBattle(final String id, final String format, final boolean asGuest, final float bgNum) {
		return spawnBattle(id, format, asGuest, bgNum, -1f);
	}

	public boolean spawnBattle(final String id, final String format, final boolean asGuest) {
		return spawnBattle(id, format, asGuest, -1f);
	}

	public boolean spawnBattle(final String id, final String format) {
		return spawnBattle(id, format, false);
	}

	/** Look for teams in the save directory and load the valid ones into memory. */
	protected void loadTeams() {
		synchronized(teams) {
			int i = 0;
			for(File f : teamDealer.listSaveFiles()) {
				Team tmp = new Team();
				if(	teams.size() < MAX_LOADABLE_TEAMS &&
					teamDealer.load(tmp,Meta.getSaveURL().getPath()+Meta.DIRSEP+f.getPath()) &&
					tmp.members() > 0
				) {
					teams.add(tmp);
					++i;
				}
			}
			loadedTeams = true;
			if(Debug.on) printDebug("Loaded "+i+" teams");
		}
	}

	/** Brings up a message dialog to allow team and format choice;
	 * This gets called for the challenging player; the challenged one's
	 * client must call showTeamChoiceDialog(String chosenFormat)
	 */
	public boolean showTeamChoiceDialog(Set<String> formats) {
		final JComboBox<Team> teamList = new JComboBox<>();
		Object popup = teamList.getUI().getAccessibleChild(teamList, 0);  
		if(popup instanceof ComboPopup) {  
			JList<?> jlist = ((ComboPopup)popup).getList();  
			jlist.setFixedCellHeight(55);  
		}  
		synchronized(teams) {
			for(Team t : teams) 
				teamList.addItem(t);
		}

		teamList.setRenderer(new TeamRenderer());

		JPanel messagePanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		final JComboBox<String> formatList = new JComboBox<>();
		for(String s : formats)
			formatList.addItem(s);
		formatList.addItem("Custom");

		formatList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(formatList.getSelectedItem().equals("RandomBattle"))
					teamList.setEnabled(false);
				else
					teamList.setEnabled(true);
			}
		});

		JButton hintButton = new JButton("?");
		hintButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fmtName = (String)formatList.getSelectedItem();
				StringBuilder message = new StringBuilder();
				boolean known = false;
				for(RuleSet.Predefined p : RuleSet.Predefined.values()) {
					if(p.getName().replaceAll(" ","").equals(fmtName)) {
						known = true;
						message.append(p.getRuleSet().getInfo());
						break;
					}
				}
				if(!known)
					message.append("Format "+fmtName+"\n\nNo information available.");

				JOptionPane.showMessageDialog(PokeponClient.this,message.toString());
			}
		});
		
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		c.gridwidth = 5;
		c.fill = GridBagConstraints.HORIZONTAL;
		messagePanel.add(new JLabel("Format:"),c);
		c.gridy = 1;
		c.gridwidth = 4;
		c.weightx = 1;
		messagePanel.add(formatList,c);
		c.gridx = 4;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		messagePanel.add(hintButton,c);
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 5; 
		c.fill = GridBagConstraints.HORIZONTAL;
		messagePanel.add(new JLabel("Team:"),c);
		c.gridy = 3;
		c.gridheight = 2;
		messagePanel.add(teamList,c);

		int sel = JOptionPane.showOptionDialog(	
						this,
						messagePanel,
						"Choose a team",
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						null,
						null
						);
		if(Debug.on) 
			printDebug("selected team "+
					(sel == JOptionPane.CLOSED_OPTION || sel == JOptionPane.CANCEL_OPTION ? 
						"<none>" : 
						"#"+teamList.getSelectedIndex())
			);

		format = (String)formatList.getSelectedItem();
		// Don't return false if (teamlist index out-of-range AND format is randombattle) 
		if(	sel == JOptionPane.CLOSED_OPTION || 
			sel == JOptionPane.CANCEL_OPTION || 
			(
				(	teamList.getSelectedIndex() < 0 || 
					teamList.getSelectedIndex() > teams.size()
				) && (
					format == null ||
					!format.equals("RandomBattle")
				)
			)
		) {
			return false;
		}
		
		// here are the selected values:
		if(format != null && !format.equals("RandomBattle")) {
			team = teams.get(teamList.getSelectedIndex());
			if(format.equals("Custom")) {
				format = createCustomFormat();
				if(format == null)
					return false;
				if(Debug.on) printDebug("CUSTOM FORMAT: "+format);
			}
		}
		return true;
	}

	/** Show team choice dialog with a fixed Format; should be called when
	 * this player is challenged and the format has already been chosen.
	 */
	public boolean showTeamChoiceDialog(final String chosenFormat) {
		if(chosenFormat.equals("RandomBattle")) {
			// just show the yes/no dialog
			return JOptionPane.showConfirmDialog(
						this,
						new JLabel("Format: Random Battle"),
						"Choose a team",
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
		}
		JComboBox<Team> teamList = new JComboBox<>();
		Object popup = teamList.getUI().getAccessibleChild(teamList, 0);  
		if (popup instanceof ComboPopup) {  
			JList<?> jlist = ((ComboPopup)popup).getList();  
			jlist.setFixedCellHeight(55);  
		}  
		synchronized(teams) {
			for(Team t : teams) 
				teamList.addItem(t);
		}

		teamList.setRenderer(new TeamRenderer());

		JPanel messagePanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		JButton hintButton = new JButton("?");
		hintButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				StringBuilder message = new StringBuilder();
				boolean known = false;
				for(RuleSet.Predefined p : RuleSet.Predefined.values()) {
					if(p.getName().replaceAll(" ","").equals(chosenFormat)) {
						known = true;
						message.append(p.getRuleSet().getInfo());
						break;
					}
				}
				if(!known)
					message.append("Format "+chosenFormat+"\n\nNo information available.");

				JOptionPane.showMessageDialog(PokeponClient.this,message.toString());
			}
		});
		
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		c.gridwidth = 5;
		c.fill = GridBagConstraints.HORIZONTAL;
		messagePanel.add(new JLabel("Format:"),c);
		c.gridy = 1;
		c.gridwidth = 4;
		c.weightx = 1;
		messagePanel.add(new JLabel(chosenFormat),c);
		c.gridx = 4;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		messagePanel.add(hintButton,c);
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 5; 
		c.fill = GridBagConstraints.HORIZONTAL;
		messagePanel.add(new JLabel("Team:"),c);
		c.gridy = 3;
		c.gridheight = 2;
		messagePanel.add(teamList,c);

		int sel = JOptionPane.showOptionDialog(	
						this,
						messagePanel,
						"Choose a team",
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						null,
						null
						);
		if(Debug.on) 
			printDebug("selected team "+
				(sel == JOptionPane.CLOSED_OPTION || sel == JOptionPane.CANCEL_OPTION  
					? "<none>" 
					: "#"+teamList.getSelectedIndex()
				)
			);

		if(	sel == JOptionPane.CLOSED_OPTION || 
			sel == JOptionPane.CANCEL_OPTION || 
			teamList.getSelectedIndex() < 0 || 
			teamList.getSelectedIndex() > teams.size()
		) {
			return false;
		}
		
		// here are the selected values:
		team = teams.get(teamList.getSelectedIndex());
		// not really necessary, but...
		format = chosenFormat;
		return true;
	}

	// TODO: make this more user-friendly
	private String createCustomFormat() {
		JPanel panel = new JPanel(new BorderLayout());
		JTextArea area = new JTextArea(30,40);
		panel.add(area);
		JButton hint = new JButton("?");
		hint.addActionListener(new ActionListener() {
			private final static String helpURL = "https://github.com/silverweed/pokepon/#creating-custom-formats";
			public void actionPerformed(ActionEvent e) {
				JPanel p = new JPanel(new GridBagLayout());
				GridBagConstraints c = new GridBagConstraints();
				c.gridheight = 8;
				JLabel l = new JLabel("<html>Insert rules separated by newlines.<br>"+
				"Possible rule formats are:<br>"+
				"p:Name Of Pony<br>m:Name Of Move<br>i:Name Of Item<br>a:Name Of Ability<br>"+
				"c:{p:Name Of Pony,m:Name Of Move,[...]} (bans the described combo)<br>"+
				":speciesclause / :canon / :itemclause<br>"+
				"<br>(e.g. `p:Twilight Sparkle` -&gt; bans Twilight Sparkle)</html>");
				p.add(l,c);
				JButton b = new JButton("<html>More info on <font color=\"#000099\">"+
					"<u>"+helpURL+"</u></font></html>");
				b.setOpaque(false);
				b.setBackground(Color.WHITE);
				b.setBorderPainted(false);
				b.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(Desktop.isDesktopSupported()) {
							try {
								Desktop.getDesktop().browse(new URI(helpURL));
							} catch(IOException|URISyntaxException ee) {
								printDebug("Exception while opening URL: ");
								ee.printStackTrace();
								JOptionPane.showMessageDialog(PokeponClient.this, "Couldn't open URL!",
									"Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				});							
				b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				c.gridy = 8;
				c.gridheight = 1;
				p.add(b,c);
				JOptionPane.showMessageDialog(PokeponClient.this, p);
			}
		});
		panel.add(hint, BorderLayout.NORTH);
				
		int sel = JOptionPane.showOptionDialog(
						this,
						panel,
						"Insert format",
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						null,
						null
						);
		if(sel == JOptionPane.CLOSED_OPTION || sel == JOptionPane.CANCEL_OPTION)
			return null;		
		
		return "@Custom: " + area.getText().replaceAll("\n","\\$");
	}
}

class ButtonsPanel extends JPanel {
	private JButton bTeamBuilder = new JButton("Teambuilder");
	private JButton bExit = new JButton("Exit");
	private JButton bChallenge = new JButton("Challenge");
	private JButton bTypechart = new JButton("Typechart");
	private JButton bSettings = new JButton("Settings");
	private final PokeponClient client;

	public ButtonsPanel(PokeponClient _client) {
		super(true);	//enables double buffering
		client = _client;
		setLayout(new GridLayout(5,1));
		bExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				client.stop();
			}
		});
		add(bExit);
		bTeamBuilder.addActionListener(teamBuilderListener);
		add(bTeamBuilder);
		bChallenge.addActionListener(challengeListener);
		add(bChallenge);
		bTypechart.addActionListener(typechartListener);
		add(bTypechart);
		bSettings.addActionListener(settingsListener);
		add(bSettings);
	}
	
	// One method to get them all. 
	public JButton get(String b) {
		String a = b.toLowerCase();
		
		if(a.equals("teambuilder")) return bTeamBuilder;
		else if(a.equals("exit")) return bExit;
		else if(a.equals("challenge")) return bChallenge;
		else if(a.equals("typechart")) return bTypechart;
		else if(a.equals("settings")) return bSettings;
		else return null;
	}

	private ActionListener teamBuilderListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			consoleMsg("Opening TeamBuilder.\n");
			client.append("Opening TeamBuilder...");
			new GUITeamBuilder(client).buildTeam(false);
		}
	};
	private ActionListener challengeListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			String uName = JOptionPane.showInputDialog("Enter name of the user to challenge:");
			if(uName != null)
				client.getConnection().sendMsg(CMD_PREFIX+"battle "+uName);
		}
	};
	private ActionListener typechartListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			new Thread() {
				public void run() {
					(new Typechart()).display(false);
				}
			}.start();
		}
	};
	private ActionListener settingsListener = new ActionListener() {
		private JPanel panel = new JPanel();
		private JCheckBox 
			logBattleBtn = new JCheckBox("Enable battle logging"),
			soundBtn = new JCheckBox("Enable sound");
		private boolean initialized;

		// initialize GUI just once, not every time we click the button
		private void init() {
			panel.setLayout(new GridLayout(3, 1));
			// title
			JLabel l = new JLabel("Client Settings");
			l.setHorizontalAlignment(JLabel.CENTER);
			l.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
			panel.add(l);
			// enable/disable battle logging
			logBattleBtn.setToolTipText("<html>If checked, you'll be able to export the<br>"+
				"battle log by issuing the <b>"+CMD_PREFIX+"save</b> or <b>"+CMD_PREFIX+"export</b> command<br>"+
				"from the battle chat. Uncheck if you<br>"+
				"don't need this feature and you want<br>"+
				"to save some memory during battles.</html>");
			logBattleBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					client.optLogBattle = logBattleBtn.isSelected();
				}
			});
			panel.add(logBattleBtn);
			// enable/disable sound
			soundBtn.setToolTipText("Mute/unmute sound during battle.");
			soundBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					GUIGlobals.soundOn = soundBtn.isSelected();
				}
			});
			panel.add(soundBtn);
			initialized = true;
		}

		public void actionPerformed(ActionEvent e) {
			if(!initialized) 
				init();
			logBattleBtn.setSelected(client.optLogBattle);
			soundBtn.setSelected(GUIGlobals.soundOn);
			JOptionPane.showOptionDialog(
					client,
					panel,
					"Pokepon Client settings",
					JOptionPane.DEFAULT_OPTION,
					JOptionPane.PLAIN_MESSAGE,
					null,
					null,
					null
					);
		}
	};
}
