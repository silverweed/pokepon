//: main/GUILauncher.java

package pokepon.main;

import pokepon.util.*;
import pokepon.gui.*;
import pokepon.net.jack.client.*;
import pokepon.net.jack.server.*;
import pokepon.player.*;
import static pokepon.util.MessageManager.*;
import javax.swing.*;
import javax.imageio.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;

/** This class provides a launcher window.
 *
 * @author Giacomo Parolini
 */

class GUILauncher extends JFrame {

	private static final int BTN_X = 200, BTN_Y = 60, BTN_FONT_SIZE = 30;
	private static final Color BTN_BG1 = new Color(0xFFFF55), BTN_BG2 = Color.WHITE, BTN_FG = Color.BLACK;

	public GUILauncher() {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		// import Celestia Medium Redux font
		final JLabel titleLabel = new JLabel("<html>Pok&#233pon</html>");
		final JLabel byLabel = new JLabel(" by Silverweed91");
		Font celestia = null;
		try {
			URL cmrUrl = getClass().getResource(Meta.complete2(Meta.RESOURCE_DIR+"/fonts/CelestiaMediumRedux1.55.ttf"));
			printDebug(cmrUrl+"");
			celestia = Font.createFont(Font.TRUETYPE_FONT, cmrUrl.openStream());
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			if(!ge.registerFont(celestia))
				printDebug("[GUILauncher] Warning: couldn't register font!");
		} catch(IOException|FontFormatException e) {
			printDebug("[GUILauncher] Caught exception while loading font: ");
			e.printStackTrace();
			celestia = null;
		}
		if(celestia != null) {
			titleLabel.setFont(celestia.deriveFont(Font.PLAIN, 45));
			byLabel.setFont(celestia.deriveFont(Font.PLAIN, 24));
		}
		// add title labels
		c.ipadx = c.ipady = 5;
		add(titleLabel, c);
		c.gridx = 1;
		//c.anchor = GridBagConstraints.LINE_END;
		c.insets = new Insets(10,20,0,0);
		add(byLabel, c);
		// add launcher buttons
		c.gridheight = 2;
		c.gridx = 0;
		c.gridy = 1;
		c.insets = new Insets(4,4,4,4);
		c.anchor = GridBagConstraints.CENTER;
		
		GradientButton btn = new GradientButton(BTN_BG1, BTN_BG2, "Client");
		btn.setToggleable(false);
		btn.setToolTipText("Connect to a running Pokepon Server");
		if(celestia != null) btn.setFont(celestia.deriveFont(Font.PLAIN, BTN_FONT_SIZE));
		btn.setPreferredSize(new Dimension(BTN_X, BTN_Y));
		btn.addMouseListener(new HoverListener(btn));
		btn.addActionListener(clientListener);
		btn.setForeground(BTN_FG);
		add(btn, c);
		
		btn = new GradientButton(BTN_BG1, BTN_BG2, "Server");
		btn.setToggleable(false);
		btn.setToolTipText("Starts the Pokepon Server.");
		if(celestia != null) btn.setFont(celestia.deriveFont(Font.PLAIN, BTN_FONT_SIZE));
		btn.setPreferredSize(new Dimension(BTN_X, BTN_Y));
		btn.setForeground(BTN_FG);
		btn.addMouseListener(new HoverListener(btn));
		btn.addActionListener(serverListener);
		c.gridx = 1;
		add(btn, c);
		
		btn = new GradientButton(BTN_BG1, BTN_BG2, "Teambuilder");
		btn.setToggleable(false);
		btn.setToolTipText("Starts the GUI TeamBuilder");
		if(celestia != null) btn.setFont(celestia.deriveFont(Font.PLAIN, BTN_FONT_SIZE));
		btn.setPreferredSize(new Dimension(BTN_X, BTN_Y));
		btn.addMouseListener(new HoverListener(btn));
		btn.addActionListener(tbListener);
		btn.setForeground(BTN_FG);
		c.gridx = 0;
		c.gridy = 3;
		add(btn, c);
		
		btn = new GradientButton(BTN_BG1, BTN_BG2, "Typechart");
		btn.setToggleable(false);
		btn.setToolTipText("Shows the typechart");
		if(celestia != null) btn.setFont(celestia.deriveFont(Font.PLAIN, BTN_FONT_SIZE));
		btn.setPreferredSize(new Dimension(BTN_X, BTN_Y));
		btn.addMouseListener(new HoverListener(btn));
		btn.addActionListener(chartListener);
		btn.setForeground(BTN_FG);
		c.gridx = 1;
		add(btn, c);
		
		// add footer and logo
		JLabel footerLabel = new JLabel("<html>I know this launcher sucks,<br> it's a Pre-alpha release :P</html>");
		footerLabel.setFont(new Font("Sans-serif", Font.PLAIN, 12));
		c.gridx = 0;
		c.gridy = 5;
		add(footerLabel, c);
		try {
			Image logo = ImageIO.read(getClass().getResource(
					Meta.complete2(Meta.RESOURCE_DIR)+"/misc/inle_studios_logo.png"))
					.getScaledInstance(60, -1, Image.SCALE_SMOOTH);
			c.gridx = 1;
			c.anchor = GridBagConstraints.LINE_END;
			add(new JLabel(new ImageIcon(logo)), c);
		} catch(IOException e) {
			printDebug("[GUILauncher] Error loading image: "+e);
		}
	}

	public static void main(String[] args) {
		GUILauncher gl = new GUILauncher();
		SwingConsole.run(gl);
	}
	
	private final ActionListener
		serverListener = new ActionListener() {
			
			private JFileChooser fChooser = new JFileChooser(BasicServer.DEFAULT_CONF_FILE);

			public void actionPerformed(ActionEvent e) {
				JPanel msgPanel = new JPanel(new GridBagLayout());
				GridBagConstraints c = new GridBagConstraints();
				c.gridx = c.gridy = 0;
				c.gridwidth = 3;
				c.insets = new Insets(1, 2, 1, 2);
				msgPanel.add(new JLabel("Unspecified opts will be loaded from:"), c);

				// conf file path
				final JTextField confPath = new JTextField(BasicServer.DEFAULT_CONF_FILE, 30);

				final JButton changeConfBtn = new JButton("Change");
				changeConfBtn.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						switch(fChooser.showOpenDialog(getParent())) {
							case JFileChooser.APPROVE_OPTION:
								if(fChooser.getSelectedFile() != null) 
									confPath.setText(fChooser.getSelectedFile().getPath());
								break;
						}
					}
				});
				++c.gridy;
				c.gridx = 0;
				c.gridwidth = 1;
				msgPanel.add(changeConfBtn, c);

				c.gridwidth = 3;
				c.gridx = 1;
				msgPanel.add(confPath, c);

				final JTextField srvIp = new JTextField(20), srvPort = new JTextField(6),
						srvName = new JTextField(20), srvMaxClients = new JTextField(4),
						srvDB = new JTextField(25), srvMaxBattles = new JTextField(4),
						srvMOTD = new JTextField(25), srvDefaultNick = new JTextField(20);
				final ButtonGroup chatBGroup = new ButtonGroup();
				final JRadioButton chatOn = new JRadioButton("ON"), chatOff = new JRadioButton("OFF"),
						chatDefault = new JRadioButton("Use default");
				// advanced chat
				chatBGroup.add(chatOn);
				chatBGroup.add(chatOff);
				chatBGroup.add(chatDefault);
				chatDefault.setSelected(true);
				c.gridwidth = 1;
				c.gridx = 0;
				++c.gridy;
				c.anchor = GridBagConstraints.EAST;
				msgPanel.add(new JLabel("Enable chat roles: "), c);
				++c.gridx;
				c.gridwidth = 1;
				c.anchor = GridBagConstraints.WEST;
				msgPanel.add(chatOn, c);
				++c.gridx;
				msgPanel.add(chatOff, c);
				++c.gridx;
				msgPanel.add(chatDefault, c);
				// ip
				c.gridwidth = 1;
				++c.gridy;
				c.gridx = 0;
				c.anchor = GridBagConstraints.EAST;
				msgPanel.add(new JLabel("IP: "), c);
				srvPort.setText(""+BasicServer.DEFAULT_PORT);
				c.gridx = 1;
				c.gridwidth = 3;
				c.anchor = GridBagConstraints.WEST;
				msgPanel.add(srvIp, c);
				try {
					srvIp.setText(""+InetAddress.getLocalHost().getHostAddress());
				} catch(UnknownHostException ignore) {}
				// port
				c.gridx = 0;
				++c.gridy;
				c.gridwidth = 1;
				c.anchor = GridBagConstraints.EAST;
				msgPanel.add(new JLabel("Port: "), c);
				c.gridx = 1;
				c.gridwidth = 3;
				c.anchor = GridBagConstraints.WEST;
				msgPanel.add(srvPort, c);
				++c.gridy;
				c.gridwidth = 4;
				msgPanel.add(new JLabel("<html>(<i>Optional parameters</i>)</html>"), c);
				// name
				c.gridx = 0;
				++c.gridy;
				c.gridwidth = 1;
				c.anchor = GridBagConstraints.EAST;
				msgPanel.add(new JLabel("Name: "), c);
				c.gridx = 1;
				c.gridwidth = 3;
				c.anchor = GridBagConstraints.WEST;
				msgPanel.add(srvName, c);
				// max clients
				c.gridx = 0;
				++c.gridy;
				c.gridwidth = 1;
				c.anchor = GridBagConstraints.EAST;
				msgPanel.add(new JLabel("Max clients: "), c);
				c.gridx = 1;
				c.gridwidth = 3;
				c.anchor = GridBagConstraints.WEST;
				msgPanel.add(srvMaxClients, c);
				// max battles
				c.gridx = 0;
				++c.gridy;
				c.gridwidth = 1;
				c.anchor = GridBagConstraints.EAST;
				msgPanel.add(new JLabel("Max battles: "), c);
				c.gridx = 1;
				c.gridwidth = 3;
				c.anchor = GridBagConstraints.WEST;
				msgPanel.add(srvMaxBattles, c);
				// database
				c.gridx = 0;
				++c.gridy;
				c.gridwidth = 1;
				c.anchor = GridBagConstraints.EAST;
				msgPanel.add(new JLabel("Database: "), c);
				c.gridx = 1;
				c.gridwidth = 3;
				c.anchor = GridBagConstraints.WEST;
				msgPanel.add(srvDB, c);
				// welcome message
				c.gridx = 0;
				++c.gridy;
				c.gridwidth = 1;
				c.anchor = GridBagConstraints.EAST;
				msgPanel.add(new JLabel("Welcome msg: "), c);
				c.gridx = 1;
				c.gridwidth = 3;
				c.anchor = GridBagConstraints.WEST;
				msgPanel.add(srvMOTD, c);
				// default users nick
				c.gridx = 0;
				++c.gridy;
				c.gridwidth = 1;
				c.anchor = GridBagConstraints.EAST;
				msgPanel.add(new JLabel("Default nick: "), c);
				c.gridx = 1;
				c.gridwidth = 3;
				c.anchor = GridBagConstraints.WEST;
				msgPanel.add(srvDefaultNick, c);

				int sel = JOptionPane.showOptionDialog(
						GUILauncher.this,
						msgPanel,
						"Select server options",
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						null,
						null
						);

				if(	sel == JOptionPane.CLOSED_OPTION ||
					sel == JOptionPane.CANCEL_OPTION 
				) {

					return;
				}
				/*if(!useConf.isSelected() && 
					(	srvIp.getText().length() == 0 ||
						srvPort.getText().length() == 0
					)
				) {
					JOptionPane.showMessageDialog(null, 
						"One ore more mandatory options were not specified.\n"+
						"Not starting server.", "Unspecified option(s)",
						JOptionPane.ERROR_MESSAGE);
					return;
				}*/
				
				try {
					final PokeponServer server = new PokeponServer();
					ServerOptions opts = ServerOptions.construct();

					if(srvIp.getText().length() > 0)
						opts.address(srvIp.getText());
					if(srvPort.getText().length() > 0)
						opts.port(Integer.parseInt(srvPort.getText()));
					if(srvName.getText().length() > 0)
						opts.serverName(srvName.getText());
					if(srvMaxClients.getText().length() > 0)
						opts.maxClients(Integer.parseInt(srvMaxClients.getText()));
					if(srvMaxBattles.getText().length() > 0)
						opts.maxBattles(Integer.parseInt(srvMaxBattles.getText()));
					if(srvDB.getText().length() > 0)
						opts.database(srvDB.getText());
					if(srvMOTD.getText().length() > 0)
						opts.welcomeMessage(srvMOTD.getText());
					if(srvDefaultNick.getText().length() > 0)
						opts.defaultNick(srvDefaultNick.getText());
					if(!confPath.getText().equals(BasicServer.DEFAULT_CONF_FILE))
						opts.confFile(confPath.getText());
					
					if(Debug.on) printDebug("Opts: "+opts);

					server.configure(opts.confFile != null ? new String[] { opts.confFile } : new String[0], opts);

					new Thread() {
						public void run() {
							try {
								if(System.console() == null) {
									// redirect messages to a text pane
									MessageProxy proxy = new MessageProxy();
									MessageManager.setAltOut(proxy.getAltOut());
									MessageManager.setAltErr(proxy.getAltErr());
									proxy.startGUI("Pokepon Server");
								}
								server.start();
							} catch(Exception ee) {
								StringWriter sw = new StringWriter();
								ee.printStackTrace(new PrintWriter(sw));
								JOptionPane.showMessageDialog(null,  
									"There was an exception while starting server:\n"+
									sw, "Error", JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
					}.start();
					// suicide
					GUILauncher.this.dispose();
				} catch(IOException|UnknownOptionException ex) {
					StringWriter sw = new StringWriter();
					ex.printStackTrace(new PrintWriter(sw));
					JOptionPane.showMessageDialog(null, 
						"There was an exception while parsing options:\n"+
						sw, "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		},
		clientListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JPanel msgPanel = new JPanel(new GridBagLayout());
				GridBagConstraints c = new GridBagConstraints();
				JCheckBox msgbox = new JCheckBox("Open debug window");
				final JTextField srvIp = new JTextField(20), srvPort = new JTextField(6);
				// ip
				c.gridwidth = 1;
				c.gridy = 1;
				c.anchor = GridBagConstraints.EAST;
				msgPanel.add(new JLabel("Server address: "), c);
				srvPort.setText(""+BasicServer.DEFAULT_PORT);
				c.gridx = 1;
				c.gridwidth = 2;
				c.anchor = GridBagConstraints.WEST;
				msgPanel.add(srvIp, c);
				try {
					srvIp.setText(""+InetAddress.getLocalHost().getHostAddress());
				} catch(UnknownHostException ignore) {}
				// port
				c.gridx = 0;
				c.gridy = 2;
				c.gridwidth = 1;
				c.anchor = GridBagConstraints.EAST;
				msgPanel.add(new JLabel("Server port: "), c);
				c.gridx = 1;
				c.gridwidth = 2;
				c.anchor = GridBagConstraints.WEST;
				msgPanel.add(srvPort, c);
				// messages
				c.gridx = 0;
				c.gridy = 3;
				c.gridwidth = 2;
				msgPanel.add(msgbox, c);

				int sel = JOptionPane.showOptionDialog(
						GUILauncher.this,
						msgPanel,
						"Select server",
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						null,
						null
						);

				if(	sel == JOptionPane.CLOSED_OPTION ||
					sel == JOptionPane.CANCEL_OPTION 
				) {

					return;
				}
				GUILauncher.this.dispose();
				if(msgbox.isSelected()) {
					MessageProxy proxy = new MessageProxy(JFrame.DISPOSE_ON_CLOSE);
					MessageManager.setAltOut(proxy.getAltOut());
					MessageManager.setAltErr(proxy.getAltErr());
					proxy.startGUI("Pokepon Client - messages");
				}
				final PokeponClient client = new PokeponClient(srvIp.getText(), Integer.parseInt(srvPort.getText()));	
				// FIXME: the error panels don't show up!!
				new Thread() {
					public void run() {
						try {
							client.start();
						} catch(ConnectException e) {
							JOptionPane.showMessageDialog(
									null,
									"Couldn't connect to host!",
									"Connection Error",
									JOptionPane.ERROR_MESSAGE
							);
						} catch(UnknownHostException e) {
							JOptionPane.showMessageDialog(
									null,
									"Unknown host: " + srvIp.getText(),
									"Unknown host",
									JOptionPane.ERROR_MESSAGE
							);
						}
					}
				}.start();
			}
		},
		tbListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				(new GUITeamBuilder()).buildTeam();		
			}
		},
		chartListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread() {
					public void run() {
						(new Typechart()).display(false);
					}
				}.start();
			}
		};
		/*dexListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ButtonGroup bGroup = new ButtonGroup();	
				JPanel msg = new JPanel(new GridLayout(11,1));
				msg.add(new JLabel("Order by:"));
				for(String s : "HP Atk Def SpA SpD Spe BST Name Type".split(" ")) {
					JRadioButton btn = new JRadioButton(s);
					if(s.equals("Name"))
						btn.setSelected(true);
					bGroup.add(btn);
					msg.add(btn);
				}
				JCheckBox rev = new JCheckBox("Reversed order");
				msg.add(rev);
				int sel = JOptionPane.showOptionDialog(
							null,
							msg,
							"Select dex order",
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE,
							null,
							null,
							null
				);
				if(sel == JOptionPane.CANCEL_OPTION || sel == JOptionPane.CLOSED_OPTION)
					return;
				
				String args = "";
				for(Enumeration<AbstractButton> en = bGroup.getElements(); en.hasMoreElements(); ) {
					AbstractButton b = en.nextElement();
					if(b.isSelected()) {
						args = ((JRadioButton)b).getText();
						break;
					}
				}
				if(rev.isSelected())
					args += " rev";

				consoleHeader("   FAST PONYDEX   ");
				if(System.console() == null) {
					MessageProxy proxy = new MessageProxy(JFrame.DISPOSE_ON_CLOSE);
					MessageManager.setAltOut(proxy.getAltOut());
					MessageManager.setAltErr(proxy.getAltErr());
					proxy.startGUI("Fast ponydex");
				}

				final String[] _args = args.split(" ");
				try {
					FastPonydex.main(_args);	
				} catch(Exception ex) {
					printDebug("Exception while invoking FastPonydex: "+ex);
				}
			}
		};*/
}
