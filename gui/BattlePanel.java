//: gui/BattlePanel.java

package pokepon.gui;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.font.*;
import javax.swing.text.html.*;
import javax.imageio.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.List;
import java.util.regex.*;
import java.lang.reflect.*;
import pokepon.gui.animation.*;
import pokepon.move.*;
import pokepon.move.hazard.*;
import pokepon.pony.*;
import pokepon.util.*;
import pokepon.battle.*;
import pokepon.enums.*;
import pokepon.enums.Type;
import pokepon.player.*;
import pokepon.net.jack.*;
import pokepon.net.jack.client.ChatPanel;
import pokepon.sound.*;
import static pokepon.pony.Pony.Status;
import static pokepon.util.MessageManager.*;
import static pokepon.util.ConcatenateArrays.merge;

/** A Pok&#233mon Showdown-like battle panel; this is one of the most
 * important classes of the game, since it contains almost all the
 * client-side event-handling code; almost all the work is managed and
 * dispatched by the interpret() method.
 *
 * @author Giacomo Parolini
 */
public class BattlePanel extends JPanel implements pokepon.main.TestingClass {
	
	public static URL EMPTY_TOKEN_URL = BattlePanel.class.getResource(Meta.complete2(Meta.TOKEN_DIR)+"/empty_token_icon_left_small.png");
	public static URL UNKNOWN_PONY_URL = BattlePanel.class.getResource(Meta.complete2(Meta.TOKEN_DIR)+"/empty_token_icon_left_small.png");
	public static final String[] BG_IMG_URL = {
						"equestria_background_by_cl0setbr0ny.png",
						"equestria_at_night_by_darcowalways.png"
						};
	// Pelipper ftw
	public static final URL[] PLACEHOLDER_URL = {
						BattlePanel.class.getResource(Meta.complete2(Meta.SPRITE_DIR)+"/Placeholder/stand_left.gif"),
						BattlePanel.class.getResource(Meta.complete2(Meta.SPRITE_DIR)+"/Placeholder/stand_right.gif")
						};

	/** Type of event for the appendEvent method */
	private static enum EventType { 
					CHAT,
					JOIN,
					LEAVE,
					RULE,
					SWITCH,
					MOVE,
					TURN,
					BATTLE,
					BOOST,
					EMPHASIZED,
					ERROR,
					CRITICAL,
					STATUS,
					HTML,
					INFO 
					};
	/** Type of result for the resultAnim method */
	private static enum ResultType { GOOD, BAD, NEUTRAL };

	/// BUNCH OF STATIC DEFINITIONS ///
	/** Horizontal dimension of BattlePanel (actually decided by pack()) */
	private static final int DIM_X = 980;
	/** Vertical dimension of BattlePanel (actually decided by pack()) */
	private static final int DIM_Y = 680;
	private static final int FIELD_WIDTH = 710;
	private static final int FIELD_HEIGHT = 400;
	private static final int INTERPRET_DELAY = 700; //ms
	private static final int RESULT_ANIM_DELAY = 200; //ms
	/** Determines the width of the field side bars (e.g 0.5 means effective field height is half
	 * the width, once subtracted the bars) */
	private static final float FIELD_HW_RATIO = 0.9f;
	/** Coordinate at which the actual field starts (equal to the side bar's width) */
	private static final int FIELD_X = (int)((FIELD_WIDTH-(float)FIELD_HEIGHT/FIELD_HW_RATIO)/2f);

	/** This overrides the one in ChatPanel */
	private static final int MAX_HIST_SIZE = 40;
	private static final int HAZARD_TOKEN_SIZE = 30;
	static final int TEAM_SPRITE_SIZE = 35;
	// field layers
	private static final Integer BG_LAYER = 0;
	private static final Integer SIDEBAR_LAYER = 1;
	private static final Integer HAZARD_LAYER = 2;
	private static final Integer TEAM_LAYER = 2;
	private static final Integer PONY_LAYER = 3;
	private static final Integer MOVE_LAYER = 4;
	private static final Integer HPBAR_LAYER = 5;
	private static final Integer MESSAGE_LAYER = 6;

	/// INSTANCE FIELDS ///
	private Player p1;
	private Player p2;
	/** if true, the TeamMenuPanel won't spoil opponent ponies */
	private boolean isRandomBattle;
	/** this, if set, is used to shortcut the player name in communications client-&gt;server */
	private int playerID;
	/** Reference to the parent client's connection */
	private Connection connection;
	/** Reference to an optional BattleLogger */
	private BattleLogger battleLogger;
	/** ID used by both the client and the server to uniquely identify the battle */
	private String battleID;
	/** The team menu shown in the side bars */
	private TeamMenuPanel teamMenu1;
	private TeamMenuPanel teamMenu2;
	private volatile boolean battleStarted;
	/** Main field pane;
	 * Layer 0 - background image;
	 * Layer 1 - side bars;
	 * Layer 2 - hazards and teams;
	 * Layer 3 - ponies;
	 * Layer 4 - moves;
	 * Layer 5 - hp bars etc;
	 * Layer 6 - messages;
	 */
	private JLayeredPane fieldP = new JLayeredPane();
	/** Panel containing move panel and team choice panel */
	private JPanel bottomP = new JPanel(new GridLayout(2,1,0,20));
	/** CardLayout Panel containing the moves and the alerts panels */
	private JPanel bottomCardP = new JPanel(new CardLayout());
	// Cards of the bottomCardP
	private static final String MOVE_CARD = "Move Panel";
	private static final String ALERT_CARD = "Alert Panel";
	/** Moves panel */
	private JPanel moveP = new JPanel();
	/** Alerts panel */
	private JPanel alertP = new JPanel(new BorderLayout());
	/** Alerts label contained in alertP */
	private JLabel alertLabel = new JLabel();
	/** Team choice panel */
	private TeamPanel teamP = new TeamPanel(true);
	/** Side chat/events panel */
	private ChatPanel eventP = new ChatPanel();
	private JLabel bgImage = new JLabel();
	/** Array of moves buttons */
	private MoveButton[] moveB = new MoveButton[Pony.MOVES_PER_PONY];
	/** TextPane with chat */
	private JTextPane eventA = new JTextPane();
	/** HTMLDocument of eventA */
	private HTMLDocument eventD;
	/** Chat input text field */
	private JTextField inputF = new JTextField(40);
	// sprites
	private volatile TransparentLabel allySprite;
	private volatile TransparentLabel oppSprite;
	/** Used for teampreview */
	private JLabel[] previewSprite1 = new JLabel[6];
	/** Used for teampreview */
	private JLabel[] previewSprite2 = new JLabel[6];
	private volatile TransparentLabel allySubstitute;
	private volatile TransparentLabel oppSubstitute;
	// ponies
	private volatile Pony allyPony;
	private volatile Pony oppPony;
	// hpbars
	private HPBar allyHPBar;
	private HPBar oppHPBar;
	/** Effective stats (except HP) of ally pony (used in PonySpriteListener) */
	private int[] ponyEffStats = { 0, 0, 0, 0, 0 };
	/** This color depends on the background image (for dark backgrounds we set this to
	 * white).
	 */
	private Color hpBarTxtColor = Color.BLACK;
	// hazards
	/** hazards[0]: on ally side; [1]: on opponent side */
	private List<Map<String,Integer>> hazards = new ArrayList<Map<String,Integer>>();
	/** map of (lists of) hazard tokens; e.g SharpNails may have a list of up to 3 tokens. */
	private List<Map<String,List<JLabel>>> hazardTokens = new ArrayList<Map<String,List<JLabel>>>();
	// Volume & sound
	/** Looper handling the BGM */
	private Looper looper;
	private VolumeBar volumeBar;
	// resultAnim delayers
	ExecutorService resultAnimExec = Executors.newSingleThreadExecutor();
	private volatile boolean resultAnimWait;
	/** The monitor Object */
	private final Object resultAnimSemaphore = new Object();
	// persistent effects
	/** array (0: ally, 1: opp) of maps [peName, peSprite] */
	private List<Map<String,JLabel>> persEffectsSprite = new ArrayList<>();
	/** The format of this battle */
	private String format;

	/** This gets called by all the constructors to initialize some objects which cannot be constructed inline */
	private void constructStuff() {
		hazards.add(new HashMap<String,Integer>());
		hazards.add(new HashMap<String,Integer>());
		hazardTokens.add(new HashMap<String,List<JLabel>>());
		hazardTokens.add(new HashMap<String,List<JLabel>>());
		persEffectsSprite.add(new HashMap<String,JLabel>());
		persEffectsSprite.add(new HashMap<String,JLabel>());
	}

	public BattlePanel(Connection conn, String btlID, Player p1, Player p2) {
		this(p1,p2);
		connection = conn;
		battleID = btlID;
		constructStuff();
	}

	/** This method is to be used only for testing, since it doesn't set the connection
	 * with the server.
	 */
	public BattlePanel(Player p1, Player p2) {
		super(true);	//enables double buffering
		this.p1 = p1;
		this.p2 = p2;
		constructStuff();
	}

	public void initialize() {
		initialize("");
	}

	public void initialize(String format) {
		initialize(format, (new Random()).nextInt(BG_IMG_URL.length));
	}

	public void initialize(String format, int bgNum) {
		initialize(format, bgNum, (new Random()).nextInt(PresetBGM.getPresets().size()));
	}

	/** This function sets up the Battle Panel;
	 * the setup happens this way:
	 * first, the battle field is populated with the background image and the side bars;
	 * then, the team menus are created and populated with the players' ponies;
	 * the move buttons' panel and the team chooser panel are then added to the bottom panel
	 * the event panel is initialized
	 * all the components are finally added to the BattlePanel
	 *
	 * @param format Format of the battle (optional)
	 * @param bgNum ID of the background to use (optional)
	 * @param bgmNum ID of the background music to use (optional)
	 */
	public void initialize(String format, int bgNum, int bgmNum) {
		this.format = format;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		isRandomBattle = format.equals("Random_Battle");

		// SETUP FIELD //
		if(FIELD_WIDTH < screenSize.width && FIELD_HEIGHT < screenSize.height)
			fieldP.setPreferredSize(new Dimension(FIELD_WIDTH,FIELD_HEIGHT));
		else {
			if(FIELD_WIDTH >= screenSize.width)
				fieldP.setPreferredSize(new Dimension(
						(int)(screenSize.width*0.75),
						(int)(screenSize.width*0.75*(double)FIELD_HEIGHT/FIELD_WIDTH)));
			else 
				fieldP.setPreferredSize(new Dimension(
						(int)(screenSize.height*0.75*(double)FIELD_WIDTH/FIELD_HEIGHT),
						(int)(screenSize.height*0.75)));
		}
		try {
			if(bgNum < 0) {
				bgNum = 0;
				printDebug("[BP.initialize()] bgNum is negative! Changing it to 0.");
			} else if(bgNum > BG_IMG_URL.length-1) {
				bgNum = BG_IMG_URL.length-1;
				printDebug("[BP.initialize()] bgNum is too big! Changing it to "+bgNum);
			}
			if(bgNum == 1) hpBarTxtColor = Color.WHITE;
			bgImage.setIcon(new ImageIcon(completeBGPath(BG_IMG_URL[bgNum])));
		} catch(Exception e) {
			e.printStackTrace();
		}
		// add background image -- REMEMBER TO SET BOUNDS TO MAKE THE COMPONENT VISIBLE!!!
		bgImage.setBounds(0,0,bgImage.getIcon().getIconWidth(),bgImage.getIcon().getIconHeight());
		fieldP.add(bgImage,BG_LAYER);
		// add lateral bars
		int tmp = FIELD_X; 
		ShapeComponent bar1 = new ShapeComponent(new Rectangle(0,0,tmp,FIELD_HEIGHT),Color.WHITE,0.5f);
		ShapeComponent bar2 = new ShapeComponent(new Rectangle(FIELD_WIDTH - tmp,0,tmp,FIELD_HEIGHT),Color.WHITE,0.5f);
		bar1.setBounds(0,0,tmp,FIELD_HEIGHT);
		bar2.setBounds(FIELD_WIDTH - tmp,0,tmp,FIELD_HEIGHT);
		fieldP.add(bar1,SIDEBAR_LAYER);
		fieldP.add(bar2,SIDEBAR_LAYER);

		// add team menus
		teamMenu1 = new TeamMenuPanel(p1);
		teamMenu1.setBounds(
				bar1.getX() + (bar1.getWidth() / 10), 
				bar1.getY() + (int)(FIELD_HEIGHT * 0.75f), 
				bar1.getWidth()*8/10,
				(int)(bar1.getHeight()*0.2f)
				);
		fieldP.add(teamMenu1,TEAM_LAYER);
		teamMenu2 = new TeamMenuPanel(p2);
		teamMenu2.setBounds(
				bar2.getX() + (bar2.getWidth() / 10),
				bar2.getY() + (int)(FIELD_HEIGHT * 0.1f), 
				bar2.getWidth()*8/10,
				(int)(bar2.getHeight()*0.2f)
				);
		fieldP.add(teamMenu2,TEAM_LAYER);

		// SETUP TEAM PANEL //
		teamP.setPreferredTokenSize(new Dimension(100,50));
		//teamP.setTokenStyle(TeamPanel.TokenStyle.ONLY_IMAGE);
		for(int i = 0; i < 6; ++i) {
			//teamP.setPony(i,p1.getTeam().getPony(i));
			teamP.getToken(i).addActionListener(new SwitchListener(i));	
		}

		// SETUP MOVES BUTTON PANEL//
		moveP.setLayout(new GridLayout(1,4,15,0));
		for(int i = 0; i < moveB.length; ++i) {
			moveB[i] = new MoveButton(null);
			moveB[i].setPreferredSize(new Dimension(140,50));
			moveB[i].setForeground(Color.WHITE);
			moveB[i].addActionListener(new MoveActionListener(i));
			moveB[i].addMouseListener(new HoverListener(moveB[i]));
			moveB[i].setEnabled(false);
			moveP.add(moveB[i]);
		}

		// SETUP ALERT PANEL
		alertLabel.setHorizontalAlignment(JLabel.CENTER);
		alertP.add(alertLabel);

		// Add moves and alerts to the bottomCardPanel
		bottomCardP.add(moveP,MOVE_CARD);
		bottomCardP.add(alertP,ALERT_CARD);
		((CardLayout)bottomCardP.getLayout()).show(bottomCardP,MOVE_CARD);
		// Add the bottomCardP and the team panel to the bottom panel
		bottomP.add(bottomCardP);
		bottomP.add(teamP);

		// SETUP EVENT PANEL //
		eventP.setPreferredSize(new Dimension(FIELD_WIDTH/2,(int)(DIM_Y*7./8.)));
		inputF = eventP.getInputField();
		eventP.setKeyListener(outKeyListener);
		if(connection != null)
			eventP.initialize(connection.getSocket());

		/// SETUP BATTLE PANEL ///
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.gridheight = 2;
		c.insets = new Insets(3,3,3,3);
		add(fieldP,c);

		c.gridy = 2;
		c.gridheight = 1;
		c.insets = new Insets(10,3,10,3);
		add(bottomP,c);

		c.gridx = 2;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 3;
		c.insets = new Insets(3,3,3,3);
		add(eventP,c);

		// Start the BGM
		if(GUIGlobals.soundOn) {
			looper = PresetBGM.getLooper("xy-rival.wav");
			// TODO
			//volumeBar = new VolumeBar(looper);
			//volumeBar.setBounds(inputF.getBounds().x + inputF.getBounds().width - 30, inputF.getBounds().y + inputF.getBounds().height - 100, 30, 100);
			//volumeBar.setVisible(true);
			//add(volumeBar);
			if(looper != null)
				new Thread(looper).start();
		}
	}

	/** @return A Point with the coordinates of the ally sprite */
	private Point allyLocation() {
		if(allySprite != null)
			return new Point(FIELD_X + 30, FIELD_HEIGHT - allySprite.getHeight() - 30);
		else
			return new Point(FIELD_X + 30, FIELD_HEIGHT - 130);
	}

	/** @return A Point with the coordinates of the opponent sprite */
	private Point oppLocation() {
		if(oppSprite != null)
			return new Point(FIELD_WIDTH - FIELD_X - oppSprite.getWidth() - 30, 30);
		else
			return new Point(FIELD_WIDTH - FIELD_X - 130, 30);
	}

	private Point allyLocation(final JLabel sprite) {
		if(sprite != null && sprite.getIcon() != null)
			return new Point(FIELD_X + 30, FIELD_HEIGHT - sprite.getIcon().getIconHeight() - 30);
		else
			return new Point(FIELD_X + 30, FIELD_HEIGHT - 130);
	}
	private Point oppLocation(final JLabel sprite) {
		if(sprite != null && sprite.getIcon() != null)
			return new Point(FIELD_WIDTH - FIELD_X - sprite.getIcon().getIconWidth() - 30, 30);
		else
			return new Point(FIELD_WIDTH - FIELD_X - 130, 30);
	}

	private void setOpponentBounds(final JLabel sprite) {
		sprite.setBounds(
			(int)oppLocation(sprite).getX(),
			(int)oppLocation(sprite).getY(),
			sprite.getIcon().getIconWidth(),
			sprite.getIcon().getIconHeight()
		);
	}

	/** Spawn a JLabel on opponent side on the PONY_LAYER */
	private void setOnOpponentSide(final JLabel sprite) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setOpponentBounds(sprite);
				if(Debug.pedantic) printDebug("oppSide: "+sprite.getBounds());
				fieldP.add(sprite,PONY_LAYER);
				oppSprite = new TransparentLabel(sprite);
				oppSprite.addMouseListener(new PonySpriteListener(false));
			}
		});
	}

	private void setOnOpponentSide(URL url) {
		if(url == null) {
			JLabel sprite = new JLabel(new ImageIcon(PLACEHOLDER_URL[0]));
			setOnOpponentSide(sprite);
		} else {
			JLabel sprite = new JLabel(new ImageIcon(url));
			if(sprite.getIcon() == null || sprite.getIcon().getIconWidth() < 0) {
				if(Debug.on) printDebug("[setOnOpponentSide] sprite is null: using placeholder...");
				sprite.setIcon(new ImageIcon(PLACEHOLDER_URL[0]));
			}
			setOnOpponentSide(sprite);
		}
	}

	private void setAllyBounds(final JLabel sprite) {
		sprite.setBounds(
				(int)allyLocation(sprite).getX(),
				(int)allyLocation(sprite).getY(),
				sprite.getIcon().getIconWidth(),
				sprite.getIcon().getIconHeight()
		);
	}

	/** Spawn a JLabel on ally side on the PONY_LAYER */
	private void setOnAllySide(final JLabel sprite) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setAllyBounds(sprite);
				if(Debug.pedantic) printDebug("allySide: "+sprite.getBounds());
				fieldP.add(sprite,PONY_LAYER);
				allySprite = new TransparentLabel(sprite);
				allySprite.addMouseListener(new PonySpriteListener(true));
			}
		});
	}

	private void setOnAllySide(URL url) {
		if(url == null) {
			JLabel sprite = new JLabel(new ImageIcon(PLACEHOLDER_URL[1]));
			setOnAllySide(sprite);
		} else {
			JLabel sprite = new JLabel(new ImageIcon(url));
			if(sprite.getIcon() == null || sprite.getIcon().getIconWidth() < 0) {
				if(Debug.on) printDebug("[setOnAllySide] sprite is null: using placeholder...");
				sprite.setIcon(new ImageIcon(PLACEHOLDER_URL[1]));
			}
			setOnAllySide(sprite);
		}
	}
	
	public Player getPlayer(int num) {
		if(num == 1) return p1;
		else if(num == 2) return p2;
		else throw new IllegalArgumentException("Player id must be 1 or 2!");
	}

	public String getFormat() {
		return format != null && format.length() > 0 ? format : "Unknown Format";
	}

	public final String getBattleID() { return battleID; }

	/** Searches for pony named 'name' in p[num]'s team and returns it. */
	public Pony findInTeam(int num,String name) {
		Player pl = (num == 1 ? p1 : (num == 2 ? p2 : null));
		if(pl == null) throw new RuntimeException("findInTeam: num is "+num);
		for(Pony p : pl.getTeam()) {
			if(p.getNickname().equals(name)) return p;
		}
		return null;
	}

	/** Searches for pony (nick)named 'name' in p[num]'s team and returns its index in team */
	public int findIndexOf(int num,String name) {
		Player pl = (num == 1 ? p1 : (num == 2 ? p2 : null));
		if(pl == null) throw new RuntimeException("findIndexOf: num is "+num);
		for(int i = 0; i < pl.getTeam().members(); ++i) {
			if(pl.getTeam().getPony(i).getNickname().equals(name)) return i;
		}
		return -1;
	}

	public int findIndexOf(int num,Pony pony) {
		Player pl = (num == 1 ? p1 : (num == 2 ? p2 : null));
		if(pl == null) throw new RuntimeException("findIndexOf: num is "+num);
		for(int i = 0; i < pl.getTeam().members(); ++i) {
			if(pl.getTeam().getPony(i) == pony) return i;
		}
		return -1;
	}		

	public void setBattleLogger(final BattleLogger logger) {
		battleLogger = logger;
	}

	/** This is the main method handling the battle: 
	 * a message is passed via a String, and it gets interpreted by the
	 * BattlePanel and executed; we follow a Pok&#233mon Showdown-like
	 * protocol: a message has the form 
	 * |TYPE|DATA
	 * where TYPE may be something like `switch`, `move`, etc and DATA are the
	 * arguments of that message; for example, the BattlePanel can be instructed
	 * to execute a move animation with a command like:
	 * |move|ally|MoveName (for a Move used by ally pony)
	 */
	@SuppressWarnings("unchecked")
	public void interpret(String line) {
		if(line == null || !line.startsWith("|") || line.length() < 2) {
			if(Debug.on) printDebug("[BattlePanel]: Ignoring malformed line: "+line);
			return;
		}

		if(Debug.on) printDebug("[BattlePanel]: received line: "+line);
		
		final String[] token = line.substring(1).split("\\|");

		if(token.length < 1) throw new RuntimeException("[BattlePanel.interpret()]: token length < 1!");

		if(token[0].equals("join") && token.length > 2) {
			/* |join|(ally[:p1/p2]/opp)|Name Of Player */
			appendEvent(EventType.JOIN,merge(token,2));
			if(token[1].startsWith("ally")) {
				String[] tk = token[1].split(":");
				if(tk.length > 1) {
					p1.setName(token[2]);
					if(Debug.on) printDebug("Set player name to "+p1.getName());
					if(tk[1].equals("p1")) playerID = 1;
					else if(tk[1].equals("p2")) playerID = 2;
				} else {
					p1.setName(token[2]);
				}
				if(Debug.on) printDebug("playerID is "+playerID+(playerID == 0 ? " (unset)" : ""));
				((TitledBorder)teamMenu1.getBorder()).setTitle(p1.getName());
			} else if(token[1].startsWith("opp")) {
				p2.setName(merge(token,2));
				((TitledBorder)teamMenu2.getBorder()).setTitle(p2.getName());
				try {
					((JFrame)SwingUtilities.getWindowAncestor(this)).setTitle("Battle vs "+p2.getName());
				} catch(ClassCastException ignore) {}
			} //TODO: else guest

		} else if(token[0].equals("leave") && token.length > 1) {
			/* |leave|Name Of Player */
			appendEvent(EventType.LEAVE,token[1]);
		
		} else if(token[0].equals("forfeit") && token.length > 1) {
			/* |forfeit|Name Of Player */
			appendEvent(EventType.LEAVE,token[1],"forfeited");

		} else if(token[0].equals("pony") && token.length > 3) {
			/* |pony|(ally/opp)|Name Of Pony|Level|[|Nickname|Ability|Item] */
			try {
				Pony newpony = PonyCreator.create(token[2]);
				try {
					newpony.setLevel(Integer.parseInt(token[3]));
				} catch(IllegalArgumentException e) {
					printDebug("Illegal level: "+token[3]+"; setting lv to 1.");
					newpony.setLevel(1);
				}
				if(token.length > 4 && token[4].length() > 0)
					newpony.setNickname(token[4]);
				if(token.length > 5 && token[5].length() > 0) {
					try {
						newpony.setAbility(AbilityCreator.create(token[5]));
						if(Debug.on) printDebug("[BP.interpret(pony)] Set "+newpony.getNickname()+"'s ability to "+token[5]);
					} catch(ReflectiveOperationException e) {
						printDebug("[BP.interpret(pony)] Failed to create ability: "+token[5]);
					}
				}
				if(token.length > 6 && token[6].length() > 0) {
					try {
						newpony.setItem(ItemCreator.create(token[6]));
						if(Debug.on) printDebug("[BP.interpret(pony)] Set "+newpony.getNickname()+"'s item to "+token[6]);
					} catch(ReflectiveOperationException e) {
						printDebug("[BP.interpret(pony)] Failed to create item: "+token[6]);
					}
				}
				newpony.setHp(newpony.maxhp());
				if(Debug.on) printDebug("[BP] set "+newpony.getNickname()+"'s hp to "+newpony.getHp());

				if(token[1].equals("ally")) {
					p1.getTeam().add(newpony);
					teamMenu1.addPony(newpony, isRandomBattle);
					teamP.addPony(newpony);
				} else if(token[1].equals("opp")) {
					p2.getTeam().add(newpony);
					teamMenu2.addPony(newpony, isRandomBattle);
				}
			} catch(ReflectiveOperationException e) {
				printDebug("[BattlePanel.interpret(pony)]: while creating pony: "+e);
			}

		} else if(token[0].equals("rule") && token.length > 1) {
			/* |rule|Rule Name: and description */
			appendEvent(EventType.RULE,merge(token,1));

		} else if(token[0].equals("teampreview")) {
			if(battleStarted) {
				printDebug("Error: received teampreview during the battle!");
				return;
			}
			showBottomAlert("Choose a pony to switch in");
			StringBuilder sb = new StringBuilder("<font color=#40576A size=3><b>"+p1.getName()+
				"'s team:</b><br><span style=\"color:#445566;display:block;\">");
			for(Pony p : p1.getTeam().getAllPonies())
				sb.append(p.getName()+" / ");
			sb.delete(sb.length()-2,sb.length());
			sb.append("</span><br><b>"+p2.getName()+"'s team:</b><br><span style=\"color:#445566;display:block;\">");
			for(Pony p : p2.getTeam().getAllPonies())
				sb.append(p.getName()+" / ");
			sb.delete(sb.length()-2,sb.length());
			sb.append("</span></font>");
			appendEvent(EventType.HTML,sb.toString());
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					moveP.setVisible(false);
					int offsetx1 = FIELD_X-15;
					int offsety1 = FIELD_HEIGHT/2-50;
					int offsetx2 = FIELD_X+150;
					int offsety2 = 20;
					int layer = 3;
					if(allySprite != null) {
						fieldP.remove(allySprite);
						allySprite = null;
					}
					if(oppSprite != null) {
						fieldP.remove(oppSprite);
						oppSprite = null;
					}
					if(allyHPBar != null) {
						fieldP.remove(allyHPBar);
						allyHPBar = null;
					}
					if(oppHPBar != null) {
						fieldP.remove(oppHPBar);
						oppHPBar = null;
					}
					validate();
					repaint();

					for(int i = 0; i < 6; ++i) {
						if(p1.getTeam().getPony(i) != null) {
							if(p1.getTeam().getPony(i).getBackSprite() == null)
								previewSprite1[i] = new JLabel(new ImageIcon(PLACEHOLDER_URL[1]));
							else
								previewSprite1[i] = new JLabel(new ImageIcon(
											p1.getTeam().getPony(i).getBackSprite()));
							previewSprite1[i].setBounds(offsetx1,offsety1,
										previewSprite1[i].getIcon().getIconWidth(),
										previewSprite1[i].getIcon().getIconHeight());
							if(Debug.on) printDebug("Sprite: "+previewSprite1[i].getIcon());
							fieldP.add(previewSprite1[i],new Integer(layer));
						}
						if(p2.getTeam().getPony(i) != null) {
							if(p2.getTeam().getPony(i).getFrontSprite() == null)
								previewSprite2[i] = new JLabel(new ImageIcon(PLACEHOLDER_URL[0]));
							else
								previewSprite2[i] = new JLabel(new ImageIcon(
											p2.getTeam().getPony(i).getFrontSprite()));
							previewSprite2[i].setBounds(offsetx2,offsety2,
										previewSprite2[i].getIcon().getIconWidth(),
										previewSprite2[i].getIcon().getIconHeight());
							fieldP.add(previewSprite2[i],new Integer(layer));
						}
						++layer;
						offsetx1 += 55;
						offsetx2 += 55;
						offsety1 += 25;
						offsety2 += 25;
					}
				}
			});

		} else if(token[0].equals("start")) {
			if(battleStarted) {
				appendEvent(EventType.CRITICAL,"Error: battle started again!?");
				printDebug("Error: battle started more than once!");
				return;
			}
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					for(int i = 0; i < 6; ++i) {
						if(previewSprite1[i] != null) {
							fieldP.remove(previewSprite1[i]);
							previewSprite1[i] = null;
						}
						if(previewSprite2[i] != null) {
							fieldP.remove(previewSprite2[i]);
							previewSprite2[i] = null;
						}
					}
					showBottomMoves();
					moveP.setVisible(true);
					validate();
					repaint();
					battleStarted = true;
				}
			});

		} else if(token[0].equals("switch") && token.length > 3) {
			/* |switch|(ally/opp)|Number of Pony in team|hp[|maxhp] */
			try {
				/* here we use invokeAndWait because we must be sure both the attacker and the defender
				 * are set correctly. Doing this asynchronously messes up the order of the actions, e.g.
				 * a sequence of messages like:
				 * |switch|ally|Trixie
				 * |damage|ally|100
				 * often ends up dealing the damage to the current allyPony _before_ it's switched out.
				 */
				if(Debug.on) printDebug("switch: tokens = "+Arrays.asList(token));
				int index = -1;
				try {
					index = Integer.parseInt(token[2]);
				} catch(IllegalArgumentException e) {
					throw new RuntimeException("Error parsing pony index: "+e);
				}
				if(token[1].equals("ally")) {
					
					if(allyPony != null && allyPony.isTransformed()) allyPony.transformBack();

					// If activePony is non-null, switch-out animation
					if(allyPony != null && allySprite != null) {
						switchOutAnim(allySprite,true);
					}
					if(Debug.on) printDebug("Setting active ally: #"+token[2]);

					final Pony newActive = p1.getTeam().getPony(index);
					if(newActive == null) {
						printDebug("Error: ally pony #"+token[2]+" does not exist.");
						return;
					}
					newActive.setActive(true);
					allyPony = newActive;
					if(teamMenu1.getTokens()[index].isUnknown())
						teamMenu1.setUnknown(index, false);

					// switch-in animation
					if(allyPony.getBackSprite() == null)
						allySprite = new TransparentLabel(new JLabel(new ImageIcon(PLACEHOLDER_URL[1])),0f);
					else {
						allySprite = new TransparentLabel(new JLabel(new ImageIcon(allyPony.getBackSprite())),0f);
						if(allySprite.getIcon().getIconWidth() < 0)
							allySprite.setIcon(new ImageIcon(PLACEHOLDER_URL[1]));
					}
					switchInAnim(allySprite,true);

					SwingUtilities.invokeAndWait(new Runnable() {
						public void run() {
							try {
								if(token.length > 4) 
									newActive.setMaxHp(Integer.parseInt(token[4]));
								else
									newActive.setMaxHp(Integer.parseInt(token[3]));
								newActive.setHp(Integer.parseInt(token[3]));
							} catch(IllegalArgumentException ee) {
								throw new RuntimeException("Error while parsing hp:"+ee);
							}
							if(Debug.on) printDebug("Ally pony set to "+allyPony);
							// create new HP bar
							if(allyHPBar != null) {
								synchronized(allyHPBar) {
									allyHPBar.setVisible(false);
								}
								fieldP.remove(allyHPBar);
							}
							allyHPBar = new HPBar(allyPony,hpBarTxtColor);
							allyHPBar.setBounds(320,250,240,100);
							allyHPBar.setVisible(true);
							fieldP.add(allyHPBar,HPBAR_LAYER);
							// set moves
							if(allyPony.finishedPP()) {
								moveB[0].setMove(new Struggle());
								for(int i = 1; i < Pony.MOVES_PER_PONY; ++i)
									moveB[i].setMove(null);
							} else {
								for(int i = 0; i < Pony.MOVES_PER_PONY; ++i)
									moveB[i].setMove(allyPony.getMove(i));
							}
							moveP.setVisible(true);
							validate();
							repaint();
							appendEvent(EventType.SWITCH, allyPony.getNickname()+
									(allyPony.hasNickname() ? "|"+allyPony.getName() : ""),
									"ally");
						}
					});
				} else if(token[1].equals("opp")) {

					if(oppPony != null && oppPony.isTransformed()) oppPony.transformBack();

					// If activePony is non-null, switch-out animation
					if(oppPony != null && oppSprite != null) {
						switchOutAnim(oppSprite,false);
					}
					if(Debug.on) printDebug("Setting active opponent: #"+token[2]);

					final Pony newActive = p2.getTeam().getPony(index);
					if(newActive == null) {
						printDebug("Error: opponent pony #"+token[2]+" does not exist.");
						return;
					}
					newActive.setActive(true);
					oppPony = newActive;
					if(teamMenu2.getTokens()[index].isUnknown())
						teamMenu2.setUnknown(index, false);

					// switch-in animation
					if(oppPony.getFrontSprite() == null)
						oppSprite = new TransparentLabel(new JLabel(new ImageIcon(PLACEHOLDER_URL[0])),0f);
					else {
						oppSprite = new TransparentLabel(new JLabel(new ImageIcon(oppPony.getFrontSprite())),0f);
						if(oppSprite.getIcon().getIconWidth() < 0)
							oppSprite.setIcon(new ImageIcon(PLACEHOLDER_URL[0]));
					}
					switchInAnim(oppSprite,false);				

					SwingUtilities.invokeAndWait(new Runnable() {
						public void run() {
							try {
								if(token.length > 4) 
									newActive.setMaxHp(Integer.parseInt(token[4]));
								else
									newActive.setMaxHp(Integer.parseInt(token[3]));
								newActive.setHp(Integer.parseInt(token[3]));
							} catch(IllegalArgumentException ee) {
								throw new RuntimeException("Error while parsing hp:"+ee);
							}
							if(Debug.on) printDebug("Opponent pony set to "+oppPony);
							// create new HP bar
							if(oppHPBar != null) {
								synchronized(oppHPBar) {
									oppHPBar.setVisible(false);
								}
								fieldP.remove(oppHPBar);
							}
							oppHPBar = new HPBar(oppPony,hpBarTxtColor);
							oppHPBar.setBounds(FIELD_X+20,5,240,100);
							oppHPBar.setVisible(true);
							fieldP.add(oppHPBar,HPBAR_LAYER);
							validate();
							repaint();
							appendEvent(EventType.SWITCH, oppPony.getNickname()+
									(oppPony.hasNickname() ? "|"+oppPony.getName() : ""),
									"opp");
						}
					});
				}

			} catch(InterruptedException e) {
				printDebug("[BP.interpret(switch)] interrupted!");
				return;
			} catch(InvocationTargetException e) {
				e.printStackTrace();
				printDebug("Caused by: "+e.getCause());
				return;
			}
		
		} else if(token[0].equals("stats") && token.length > 5) {
			/* |stats|atk|def|spatk|spdef|speed */
			String[] stats = "Atk Def Spa SpD Spe".split(" ");
			for(int i = 1; i < 6; ++i) {
				try {
					int s = Integer.parseInt(token[i]);
					if(s < 1) {
						printDebug("[BP.interpret(stats)] Invalid stat received: "+s);
						continue;
					}
					ponyEffStats[i-1] = s;
				} catch(IllegalArgumentException ee) {
					printDebug("[BP.interpret(stats)] Exception while parsing token["+i+"]: "+ee);
					continue;
				}
			}
		
		} else if(token[0].equals("setmv") && token.length > 3) {
			/* |setmv|ponynum|movenum|(Move Name/none)[|pp] */
			try {
				final int ponynum = Integer.parseInt(token[1]);
				final int movenum = Integer.parseInt(token[2]);
				if(ponynum < 0 || ponynum >= Team.MAX_TEAM_SIZE || movenum < 0 || movenum >= Pony.MOVES_PER_PONY) {
					printDebug("[BattlePanel.interpret(setmv)]: ERROR - received setmv ponynum: "+ponynum+", movenum: "+movenum);
					return;
				}
				if(token[3].equals("none")) {
					if(Debug.on) printDebug("Set move["+movenum+"] of "+p1.getTeam().getPony(ponynum)+" to none");
					p1.getTeam().getPony(ponynum).setMove(movenum, null);
				} else {
					try { 
						final Move move = MoveCreator.create(token[3]);
						if(move == null) {
							throw new NullPointerException("Move is null in BP.interpret(setmv)!");
						}
						if(Debug.on) 
							printDebug("Set move["+movenum+"] of "+p1.getTeam().getPony(ponynum)+
									" to "+move.getName()+" (type: "+move.getType()+
									",color: "+move.getType().getBGColor()+", "+move.getType().getFGColor()+")");
						if(token.length > 4) {
							try {
								move.setPP(Integer.parseInt(token[4]));
								if(Debug.on) printDebug("[BP.interpret(setmv)] set "+move+" PP to "+move.getPP());
							} catch(IllegalArgumentException ee) {
								printDebug("[BP.interpret(setmv)] illegal PP number: "+token[4]);
							}
						}
						p1.getTeam().getPony(ponynum).setMove(movenum, move);
						if(battleStarted)
							moveB[movenum].setMove(move);
						
					} catch(ReflectiveOperationException e) {
						printDebug("[BP.interpret(setmv)]: "+e);
						return;
					}
				}
			} catch(IllegalArgumentException e) {
				printDebug("[BP.interpret(setmv)] Illegal argument: "+e);
			}

		} else if(token[0].equals("setmvtype")) {
			/* |setmvtype|ponynum|movenum|Type */
			try {
				final int ponynum = Integer.parseInt(token[1]);
				final int movenum = Integer.parseInt(token[2]);
				if(ponynum < 0 || ponynum >= Team.MAX_TEAM_SIZE || movenum < 0 || movenum >= Pony.MOVES_PER_PONY) {
					printDebug("[BattlePanel.interpret(setmvtype)]: ERROR - received setmvtype ponynum: "+ponynum+", movenum: "+movenum);
					return;
				}
				Type type = Type.forName(token[3]);
				if(type != null) 
					p1.getTeam().getPony(ponynum).getMove(movenum).setType(type);
				else
					printDebug("[BattlePanel.interpret(setmvtype)] ERROR: received unknown type "+token[3]);

			} catch(IllegalArgumentException e) {
				printDebug("[BP.interpret(setmvtype)] Illegal argument: "+e);
			}
		
		} else if(token[0].equals("mustswitch")) {
			/* |mustswitch */
			if(Debug.on) printDebug("[BP] invoking mustswitch...");
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						//moveP.setVisible(false);
						showBottomAlert("Choose a pony to switch in");
						validate();
						repaint();
					}
				});
				appendEvent(EventType.BATTLE,"Choose a pony to switch in.");
			} catch(InterruptedException e) {
				printDebug("[BP.interpret(mustswitch)]: interrupted.");
				return;
			} catch(InvocationTargetException e) {
				printDebug("[BP.interpret(mustswitch)]: "+e);
				printDebug("Caused by: "+e.getCause());
				return;
			}
		
		} else if(token[0].equals("move") && token.length > 2) {
			/* |move|(ally/opp)|Move Name[|avoid] */
			
			BasicAnimation anim = null;
			boolean avoid = token.length > 3 && token[3].equals("avoid");

			try {
				Map<String,Object> opts = MoveCreator.create(token[2]).getAnimation();

				if(opts == null || opts.get("name") == null) 
					return;

				List<String> anims = null;
				List<Map<String,Object>> animsopts = null;
				
				/* A Compound animation is a chain of several animations to be reproduced
				 * in series. To specify a compound animation, the options must look like:
				 *   animation.put("name", "Compound");
				 *   animation.put("anims", Arrays.asList("Animation1", "Animation2", ...));
				 */
				if(opts.get("name").equals("Compound")) {
					anims = (List<String>)opts.get("anims");
					animsopts = new ArrayList<Map<String,Object>>(anims.size());
					for(int i = 0; i < anims.size(); ++i)
						animsopts.add(new HashMap<String,Object>());

					/* opts whose keys start with [0-9]: are specific to a single
					 * animation in the chain; the others are global to all
					 * animations. e.g. 1:sprite=wisp.png will apply only to the
					 * FIRST animation in the chain.
					 */
					for(String key : opts.keySet()) {
						if(key.matches("^[0-9]:.*")) {
							try {
								animsopts.get(Integer.parseInt(key.substring(0,1))-1)
									.put(key.substring(2), opts.get(key));
							} catch(IndexOutOfBoundsException e) {
								printDebug("[BP.interpret(move)] Invalid key: "+key);
							}
						} else if(!key.equals("name")) {
							for(int i = 0; i < anims.size(); ++i)
								animsopts.get(i).put(key, opts.get(key));
						}
					}
				} else {
					anims = Arrays.asList((String)opts.get("name"));
					animsopts = Arrays.asList(opts);
				}

				for(int i = 0; i < anims.size(); ++i) {
					animsopts.get(i).put("name", anims.get(i));
					if(Debug.on) printDebug("opts = "+animsopts.get(i));

					anim = createAnimation(animsopts.get(i), token[1], avoid);

					if(anim == null) {
						printDebug("[BP.interpret(move)] no animation found for move "+token[2]);
						return;
					}
					anim.start();
					synchronized(anim) {
						try {
							anim.wait();
						} catch(InterruptedException e) {
							printDebug("Animation interrupted.");
						}
					}
					if(anim != null && anim.getSprite() != allySprite && anim.getSprite() != oppSprite && !anim.isPersistent()) {
						fieldP.remove(anim.getSprite());
					}
					if(animsopts.get(i).get("postWait") != null) {
						try {
							Thread.sleep((int)animsopts.get(i).get("postWait"));
						} catch(IllegalArgumentException|ClassCastException ee) {
							printDebug("[BP.interpret(move)] illegal argument: "+animsopts.get(i).get("postWait"));
						} catch(InterruptedException ignore2) {}
					}
				}
				if(avoid) {
					if(token[1].equals("ally")) {
						appendEvent(EventType.BATTLE,oppPony.getName() + " avoids the attack!");
						resultAnim(oppLocation(),"Avoided!");
					} else if(token[1].equals("opp")) {
						appendEvent(EventType.BATTLE,allyPony.getName() + " avoids the attack!");
						resultAnim(allyLocation(),"Avoided!");
					}
					try {
						Thread.sleep(INTERPRET_DELAY);
					} catch(InterruptedException ignore) {}
				}
				if(Debug.on) printDebug("Ended animation.");

			} catch(ReflectiveOperationException e) {
				printDebug("[BP.interpret(move)] failed to create move "+token[2]);
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				if(allySprite != null)
					fieldP.setLayer(allySprite,PONY_LAYER);
				if(oppSprite != null)
					fieldP.setLayer(oppSprite,PONY_LAYER);
				if(anim != null && anim.getSprite() != allySprite && anim.getSprite() != oppSprite && !anim.isPersistent()) {
					fieldP.remove(anim.getSprite());
				}
			}
						
		} else if(token[0].equals("avoid") && token.length > 1) {
			/* |avoid|ally/opp */
			if(token[1].equals("ally")) {
				appendEvent(EventType.BATTLE,allyPony.getName() + " avoids the attack!");
				resultAnim(allyLocation(),"Avoided!");
			} else if(token[1].equals("opp")) {
				appendEvent(EventType.BATTLE,oppPony.getName() + " avoids the attack!");
				resultAnim(oppLocation(),"Avoided!");
			}
			try {
				Thread.sleep(INTERPRET_DELAY);
			} catch(InterruptedException ignore) {}

		} else if(token[0].equals("anim") && token.length > 2) {
			/* |anim|(opp/ally)|key1=value1|key2=value2|... */
			if(!token[1].equals("opp") && !token[1].equals("ally")) {
				printDebug("[BP.interpret(anim)] Error: side is "+token[1]+"!");
				return;
			}
			Map<String,Object> opts = new HashMap<>();
			for(int i = 2; i < token.length; ++i) {
				String[] pair = token[i].split("=", 2);
				if(pair.length != 2) {
					printDebug("[BP.interpret(anim)] Bad pair received: "+token[i]+"; ignoring.");
					continue;
				}
				Object value = null;
				/* Parse optional argument typecast. Since all we receive are strings,
				 * one can specify a 'typecast pattern' in the value field, like:
				 * delay=(i)40  // will convert "40" to an Integer
				 * We don't use implicit casting based on the key because it's a less
				 * flexible and explicit approach. It's the caller's responsibility to correctly
				 * cast values types according to their key.
				 */
				Matcher matcher = Pattern.compile("^\\(([a-z])\\)(.+)$").matcher(pair[1]);
				if(matcher.matches()) {
					if(Debug.on) printDebug("Converting type of "+pair[1]);
					try {
						switch(matcher.group(1).charAt(0)) {
							case 'b':
								value = Boolean.parseBoolean(matcher.group(2));
								break;
							case 'i':
								value = Integer.parseInt(matcher.group(2));
								break;
							case 'f':
								value = Float.parseFloat(matcher.group(2));
								break;
							case 'd':
								value = Double.parseDouble(matcher.group(2));
								break;
							default:
								value = pair[1];
						}
					} catch(IllegalArgumentException e) {
						printDebug("[BP.interpret(anim)] Cannot convert '"+matcher.group(2)+"' to "+matcher.group(1));
						continue;
					}
				} else {
					value = pair[1];
				}
				opts.put(pair[0], value);
			}
			Animation anim = createAnimation(opts, token[1], false);
			if(anim == null) {
				printDebug("[BP.interpret(anim)] Error: resulting animation is null!");
				return;
			}
			anim.start();
			synchronized(anim) {
				try {
					anim.wait();
				} catch(InterruptedException e) {
					printDebug("Animation interrupted.");
				}
			}
			if(!opts.containsKey("nodelay")) {
				try {
					Thread.sleep(INTERPRET_DELAY);
				} catch(InterruptedException ignore) {}
			}

		} else if(token[0].equals("lockon") && token.length > 1) {
			/* |lockon|Move Name */
			for(Move m : allyPony.getMoves()) 
				if(m.getName().equals(token[1])) {
					moveB[0].setMove(m);
					for(int i = 1; i < Pony.MOVES_PER_PONY; ++i)
						moveB[i].setMove(null);
					allyPony.setLockedOnMove(true);
					return;
				}
			try {
				moveB[0].setMove(MoveCreator.create(token[1], allyPony));
				for(int i = 1; i < Pony.MOVES_PER_PONY; ++i)
					moveB[i].setMove(null);
				allyPony.setLockedOnMove(true);
			} catch(ReflectiveOperationException e) {
				printDebug("[BP.interpret(lockon)] couldn't create move "+token[1]);
			}

		} else if(token[0].equals("unlock")) {
			/* |unlock */
			if(allyPony == null || !allyPony.isLockedOnMove()) {
				appendEvent(EventType.ERROR,"Received 'unlock' but ally is not locked on move!");
				printDebug("[BP.interpret(unlock)] allyPony is not locked on move!");
				return;
			}
			for(int i = 0; i < Pony.MOVES_PER_PONY; ++i)
				moveB[i].setMove(allyPony.getMove(i));
			allyPony.setLockedOnMove(false);

		} else if(token[0].equals("protected") && token.length > 1) {
			/* |protected|ally/opp */
			if(token[1].equals("ally")) {
				appendEvent(EventType.BATTLE,allyPony.getName() + " is protected!");
				resultAnim(allyLocation(),"Protected!");
			} else if(token[1].equals("opp")) {
				appendEvent(EventType.BATTLE,oppPony.getName() + " is protected!");
				resultAnim(oppLocation(),"Protected!");
			}
		
		} else if(token[0].equals("chat") && token.length > 2) {
			/* |chat|Name|msg */
			appendEvent(EventType.CHAT,token[1],merge(token,2));

		} else if(token[0].equals("html") && token.length > 1) {
			/* |html|Message */
			appendEvent(EventType.HTML,token[1]);

		} else if(token[0].equals("htmlconv") && token.length > 1) {
			/* |htmlconv|Message with tags to convert */
			String replaced = Meta.toLocalURL(merge(token,1));
			appendEvent(EventType.HTML,replaced);

		} else if(token[0].equals("error") && token.length > 1) {
			/* |error|Message */
			appendEvent(EventType.ERROR,token[1]);
			
		} else if(token[0].equals("damage") && token.length > 2) {
			/* |damage|(ally/opp)|amount[|phrase] */

			if(token[1].equals("ally")) {
				try {
					int prevhp = allyPony.hp();
					int dam = (int)(Float.parseFloat(token[2]));
					synchronized(allyPony) {
						allyPony.damage(dam);
					}
					if(allyHPBar != null)
						allyHPBar.update();
					if(!(token.length > 3 && token[3].equalsIgnoreCase("quiet")))
						appendEvent(EventType.BATTLE,parseDamageEvent(
									allyPony,
									dam,
									(token.length > 3 ? token[3] : null))
						);
					if(dam >= 0)
						resultAnim(allyLocation(),"-" + 
							(int)(Math.min(prevhp, dam)*100 / allyPony.maxhp()) + "%!",
							ResultType.BAD);
					else
						resultAnim(allyLocation(),"+" + 
							(int)(Math.min(prevhp, -dam)*100 / allyPony.maxhp()) + "%!",
							ResultType.GOOD);
				} catch(IllegalArgumentException e) {
					printDebug("[BattlePanel.interpret(damage)]: Illegal argument: "+e);
				}
			} else if(token[1].equals("opp")) {
				try {
					int prevhp = oppPony.hp();
					int dam = (int)(Float.parseFloat(token[2]));
					synchronized(oppPony) {
						oppPony.damage(dam);
					}
					if(oppHPBar != null)
						oppHPBar.update();
					appendEvent(EventType.BATTLE,parseDamageEvent(oppPony,
									dam,
									(token.length > 3 ? token[3] : null)));
					if(dam > 0)
						resultAnim(oppLocation(),"-" +
							(int)(Math.min(prevhp, dam)*100 / oppPony.maxhp()) + "%!",
							ResultType.BAD);
					else
						resultAnim(oppLocation(),"+" + 
							(int)(Math.min(prevhp, -dam)*100 / oppPony.maxhp()) + "%!",
							ResultType.GOOD);
				} catch(IllegalArgumentException e) {
					printDebug("[BattlePanel.interpret(damage)]: Illegal argument: "+e);
				}
			}

			try {
				Thread.sleep(INTERPRET_DELAY);
			} catch(InterruptedException ignore) {}

		} else if(token[0].equals("rated")) {
			appendEvent(EventType.RULE,"Rated battle");

		} else if(token[0].equals("battle") && token.length > 1) {
			/* |battle|message[|(emph/html/move)] */
			if(token.length > 2) {
				if(token[2].equals("emph"))
					appendEvent(EventType.EMPHASIZED,token[1]);
				else if(token[2].equals("html"))
					appendEvent(EventType.HTML,token[1]);
				else if(token[2].equals("move") && token.length > 3)
					appendEvent(EventType.MOVE,token[1],token[3]);
			} else {
				appendEvent(EventType.BATTLE,token[1]);
			}

		} else if(token[0].equals("turn") && token.length > 1) {
			/* |turn|turnCount */
			if(Debug.on) printDebug("--- TURN "+token[1]+" ---");
			appendEvent(EventType.TURN,token[1]);

		} else if(token[0].equals("boost") && token.length > 3) {
			/* |boost|(ally/opp)|stat|amount[|Phrase] */
			String name = "";
			StringBuilder sb = new StringBuilder("");
			if(token[1].equals("opp")) {
				if(token.length < 5)
					sb.append("Enemy ");
			} else if(!token[1].equals("ally")) {
				printDebug("[BattlePanel.interpret(boost)]: error: token[1] is neither ally nor opp.");
				return;
			}

			try {
				int value = Integer.parseInt(token[3]);
				if(token.length > 4) {
					// custom phrase
					if(!token[4].equalsIgnoreCase("quiet"))
						sb.append(token[4]);
				} else {
					sb.append((token[1].equals("ally") ?
							allyPony.getNickname() :
							oppPony.getNickname())+"'s ");
					sb.append(token[2]);
					
					if(value > 2) {
						sb.append(" rose drastically!");
					} else if(value > 1) {
						sb.append(" sharply rose!");
					} else if (value > 0) {
						sb.append(" rose!");
					} else if (value == 0) {
						printDebug("[BattlePanel.interpret(boost)]: received boost 0!");
						return;
					} else if(value > -2) {
						sb.append(" fell!");
					} else if(value > -1) {
						sb.append(" harshly fell!");
					} else {
						sb.append(" fell drastically!");
					}
				}

				if(token[1].equals("ally")) {
					allyPony.boost(token[2], value);
					if(value > 0)
						resultAnim(allyLocation(),"+"+value+" "+Pony.toBriefStat(token[2])+"!",ResultType.GOOD);
					else
						resultAnim(allyLocation(),value+" "+Pony.toBriefStat(token[2])+"!",ResultType.BAD);
					if(allyHPBar != null) 
						allyHPBar.update();
				} else if(token[1].equals("opp")) {
					oppPony.boost(token[2],value);
					if(value > 0)
						resultAnim(oppLocation(),"+"+value+" "+Pony.toBriefStat(token[2])+"!",ResultType.GOOD);
					else
						resultAnim(oppLocation(),value+" "+Pony.toBriefStat(token[2])+"!",ResultType.BAD);
					if(oppHPBar != null) 
						oppHPBar.update();
				}
				if(sb.length() > 0)
					appendEvent(EventType.BOOST,sb.toString());
				Thread.sleep(INTERPRET_DELAY);
			} catch(IllegalArgumentException e) {
				printDebug("[BattlePanel.interpret(boost)]: illegal argument "+e);
				return;
			} catch(InterruptedException ignore) {}

		} else if(token[0].equals("recoil") && token.length > 2) {
			/* |recoil|(ally/opp)|recoilDamage */
			try {
				if(token[1].equals("ally")) {
					if(allyPony != null && !allyPony.isFainted()) {
						synchronized(allyPony) {
							allyPony.damage(Integer.parseInt(token[2]));
						}
						allyHPBar.update();
						appendEvent(EventType.BATTLE,allyPony.getNickname() + " lost "+ 
							(Battle.SHOW_HP_PERC ?
							Math.min(allyPony.hp()*100 / allyPony.maxhp(),
								allyPony.calculateDamagePerc(Integer.parseInt(token[2])))
							+ "% of its HP" :
							Integer.parseInt(token[2]) + " HP") + " due to recoil!");
						resultAnim(allyLocation(),"Recoil",ResultType.BAD);
					}
				} else if(token[1].equals("opp")) {
					if(oppPony != null && !oppPony.isFainted()) {
						synchronized(oppPony) {
							oppPony.damage(Integer.parseInt(token[2]));
						}
						oppHPBar.update();
						appendEvent(EventType.BATTLE,"Enemy "+oppPony.getNickname() + " lost "+ 
							(Battle.SHOW_HP_PERC ?
							Math.min(oppPony.hp()*100 / oppPony.maxhp(),
								oppPony.calculateDamagePerc(Integer.parseInt(token[2])))
							+ "% of its HP" :
							Integer.parseInt(token[2]) + " HP") + " due to recoil!");
						resultAnim(oppLocation(),"Recoil",ResultType.BAD);
					}
				}
			} catch(IllegalArgumentException e) {
				printDebug("[BattlePanel.interpret(recoil): illegal argument "+e);
			}

		} else if(token[0].equals("transform") && token.length > 2) {
			/* |transform|(ally/opp)|Pony Name */
			if(token[1].equals("ally")) {
				try {
					allyPony.transformInto(PonyCreator.create(token[2]));
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							if(allySprite == null)
								allySprite = new TransparentLabel();
							if(allyPony.getBackSprite() == null)
								allySprite.setIcon(new ImageIcon(PLACEHOLDER_URL[1]));
							else
								allySprite.setIcon(new ImageIcon(allyPony.getBackSprite()));
							setAllyBounds(allySprite);
							validate();
							repaint();
						}
					});
					allyHPBar.update();
					resultAnim(allyLocation(),"Transformed!",ResultType.NEUTRAL);
				} catch(ReflectiveOperationException e) {
					printDebug("[BP.interpret(transform)]: error creating "+token[2]+": "+e);
					return;
				}

			} else if(token[1].equals("opp")) {
				try {
					oppPony.transformInto(PonyCreator.create(token[2]));
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							if(oppSprite == null)
								oppSprite = new TransparentLabel();
							if(oppPony.getFrontSprite() == null)
								oppSprite.setIcon(new ImageIcon(PLACEHOLDER_URL[0]));
							else
								oppSprite.setIcon(new ImageIcon(oppPony.getFrontSprite()));
							setOpponentBounds(oppSprite);
							validate();
							repaint();
						}
					});
					oppHPBar.update();
					resultAnim(oppLocation(),"Transformed!",ResultType.NEUTRAL);
				} catch(ReflectiveOperationException e) {
					printDebug("[BP.interpret(transform)]: error creating "+token[2]+": "+e);
					return;
				}
			} else {
				printDebug("[BP.interpret(transform)] Error: side is "+token[1]);
				return;
			}

		} else if(token[0].equals("substitute") && token.length > 1) {
			/* |substitute|(ally/opp) */
			if(!(token[1].equals("ally") || token[1].equals("opp"))) {
				printDebug("[BP.interpret(substitute)] Error: side is "+token[1]);
				return;
			}
			setSubstitute(token[1].equals("ally"));
			try {
				Thread.sleep(INTERPRET_DELAY);
			} catch(InterruptedException ignore){}

		} else if(token[0].equals("rmsubstitute") && token.length > 1) {
			/* |rmsubstitute|(ally/opp)[|noanim] */
			if(!(token[1].equals("ally") || token[1].equals("opp"))) {
				printDebug("[BP.interpret(rmsubstitute)] Error: side is "+token[1]);
				return;
			}
			removeSubstitute(token[1].equals("ally"), token.length > 2 && token[2].equals("noanim"));
		
		} else if(token[0].equals("persistent") && token.length > 2) {
			/* |persistent|(ally/opp)|Name */
			if(!(token[1].equals("ally") || token[1].equals("opp"))) {
				printDebug("[BP.interpret(persistent)] Error: side is "+token[1]);
				return;
			}
			setPersistentEffect(token[1].equals("ally"), token[2]);
			
		} else if(token[0].equals("rmpersistent") && token.length > 2) {
			/* |rmpersistent|(ally/opp)|Name */
			if(!(token[1].equals("ally") || token[1].equals("opp"))) {
				printDebug("[BP.interpret(rmpersistent)] Error: side is "+token[1]);
				return;
			}
			removePersistentEffect(token[1].equals("ally"), token[2]);

		} else if(token[0].equals("fail") && token.length > 1) {
			/* |fail|(ally/opp) */	
			appendEvent(EventType.BATTLE, "But it failed...");
			if(token[1].equals("ally")) 
				resultAnim(allyLocation(), "Failed");
			else if(token[1].equals("opp"))
				resultAnim(oppLocation(), "Failed");
			else
				printDebug("[BP.interpret(fail)] Error: side is "+token[1]+"!");
			try {
				Thread.sleep(INTERPRET_DELAY);
			} catch(InterruptedException ignore) {}

		} else if(token[0].equals("resultanim") && token.length > 3) {
			/* |resultanim|(ally/opp)|(good/bad/neutral/color)|Message */
			if(!(token[1].equals("ally") || token[1].equals("opp"))) {
				printDebug("[BP.interpret(resultanim)] Error: side is "+token[1]);
				return;
			}
			Color color = null;
			ResultType resType = token[2].equals("good") ? ResultType.GOOD :
						token[2].equals("bad") ? ResultType.BAD :
						token[2].equals("neutral") ? ResultType.NEUTRAL :
						null;
			if(resType == null) {
				try {
					color = new Color(Integer.parseInt(token[2]));
				} catch(IllegalArgumentException e) {
					printDebug("[BP.interpret(resultanim)] Error parsing token[2]: using ResultType.NEUTRAL");
					color = null;
					resType = ResultType.NEUTRAL;
				}
			}
			if(color == null) 
				resultAnim(token[1].equals("ally") ? allyLocation() : oppLocation(),token[3],resType);
			else
				resultAnim(token[1].equals("ally") ? allyLocation() : oppLocation(),token[3],color);
			
		} else if(token[0].equals("win") && token.length > 1) {
			/* |win|(ally/opp) */
			runWinEvent(token[1]);

		} else if(token[0].equals("flinch") && token.length > 1) {
			/* |flinch|(ally/opp) */
			if(token[1].equals("ally")) {
				appendEvent(EventType.EMPHASIZED,allyPony.getNickname() + " flinched and couldn't move!");
				resultAnim(allyLocation(),"Flinched!");
			} else if(token[1].equals("opp")) {
				appendEvent(EventType.EMPHASIZED,"Enemy " + oppPony.getNickname() + " flinched and couldn't move!");
				resultAnim(oppLocation(),"Flinched!");
			}
			try {
				Thread.sleep(INTERPRET_DELAY);
			} catch(InterruptedException ignore) {}

		} else if(token[0].equals("par") && token.length > 1) {
			/* |par|(ally/opp) */
			if(token[1].equals("ally")) {
				appendEvent(EventType.EMPHASIZED,allyPony.getNickname() + " is fully paralyzed!");
				resultAnim(allyLocation(),"Paralyzed!",new Color(0xCFA600));
			} else if(token[1].equals("opp")) {
				appendEvent(EventType.EMPHASIZED,"Enemy " + oppPony.getNickname() + " is fully paralyzed!");
				resultAnim(oppLocation(),"Paralyzed!",new Color(0xCFA600));
			}
			try {
				Thread.sleep(INTERPRET_DELAY);
			} catch(InterruptedException ignore) {}

		} else if(token[0].equals("ptr") && token.length > 1) {
			/* |ptr|(ally/opp) */
			if(token[1].equals("ally")) {
				appendEvent(EventType.EMPHASIZED,allyPony.getNickname() + " is petrified and cannot move!");
				resultAnim(allyLocation(),"Petrified!",new Color(0x666699));
			} else if(token[1].equals("opp")) {
				appendEvent(EventType.EMPHASIZED,"Enemy " + oppPony.getNickname() + " is petrified and cannot move!");
				resultAnim(oppLocation(),"Petrified!",new Color(0x666699));
			}
			try {
				Thread.sleep(INTERPRET_DELAY);
			} catch(InterruptedException ignore) {}

		} else if(token[0].equals("slp") && token.length > 1) {
			/* |slp|(ally/opp) */
			if(token[1].equals("ally")) {
				appendEvent(EventType.EMPHASIZED,allyPony.getNickname() + " is fast asleep!");
				resultAnim(allyLocation(),"Asleep!",new Color(0xA3A3C2));
			} else if(token[1].equals("opp")) {
				appendEvent(EventType.EMPHASIZED,"Enemy " + oppPony.getNickname() + " is fast asleep!");
				resultAnim(oppLocation(),"Asleep!",new Color(0xA3A3C2));
			}
			try {
				Thread.sleep(INTERPRET_DELAY);
			} catch(InterruptedException ignore) {}

		} else if(token[0].equals("brn") && token.length > 1) {
			/* |brn|(ally/opp) */
			if(token[1].equals("ally")) {
				synchronized(allyPony) {
					allyPony.damagePerc(Battle.BURN_DAMAGE*100f);
				}
				if(allyHPBar != null)
					allyHPBar.update();
				appendEvent(EventType.EMPHASIZED,allyPony.getNickname() + " is hurt by its burn!");
				resultAnim(allyLocation(),"-"+(int)(Battle.BURN_DAMAGE*100)+"%!",ResultType.BAD);
			} else if(token[1].equals("opp")) {
				synchronized(oppPony) {
					oppPony.damagePerc(Battle.BURN_DAMAGE*100f);
				}
				if(oppHPBar != null)
					oppHPBar.update();
				appendEvent(EventType.EMPHASIZED,"Enemy " + oppPony.getNickname() + " is hurt by its burn!");
				resultAnim(oppLocation(),"-"+(int)(Battle.BURN_DAMAGE*100)+"%!",ResultType.BAD);
			}
			try {
				Thread.sleep(INTERPRET_DELAY);
			} catch(InterruptedException ignore) {}

		} else if(token[0].equals("cnf") && token.length > 1) {
			/* |cnf|(ally/opp) */
			if(token[1].equals("ally")) {
				appendEvent(EventType.EMPHASIZED,allyPony.getNickname() + " is confused!");
				resultAnim(allyLocation(),"Confused!");
			} else if(token[1].equals("opp")) {
				appendEvent(EventType.EMPHASIZED,"Enemy " + oppPony.getNickname() + " is confused!");
				resultAnim(oppLocation(),"Confused!");
			}
			try {
				Thread.sleep(INTERPRET_DELAY);
			} catch(InterruptedException ignore) {}

		} else if(token[0].equals("psn") && token.length > 1) {
			/* |psn|(ally/opp) */
			if(token[1].equals("ally")) {
				synchronized(allyPony) {
					allyPony.damagePerc(Battle.POISON_DAMAGE*100f);
				}
				if(allyHPBar != null)
					allyHPBar.update();
				appendEvent(EventType.EMPHASIZED,allyPony.getNickname() + " is hurt by poison!");
				resultAnim(allyLocation(),"-"+(int)(Battle.POISON_DAMAGE*100)+"%!",ResultType.BAD);
			} else if(token[1].equals("opp")) {
				synchronized(oppPony) {
					oppPony.damagePerc(Battle.POISON_DAMAGE*100f);
				}
				if(oppHPBar != null)
					oppHPBar.update();
				appendEvent(EventType.EMPHASIZED,"Enemy " + oppPony.getNickname() + " is hurt by poison!");
				resultAnim(oppLocation(),"-"+(int)(Battle.POISON_DAMAGE*100)+"%!",ResultType.BAD);
			}
			try {
				Thread.sleep(INTERPRET_DELAY);
			} catch(InterruptedException ignore) {}

		} else if(token[0].equals("tox") && token.length > 2) {
			/* |tox|(ally/opp)|counter */
			try {
				if(token[1].equals("ally")) {
					synchronized(allyPony) {
						allyPony.damagePerc(Battle.BAD_POISON_DAMAGE*100f*Integer.parseInt(token[2]));
					}
					if(allyHPBar != null)
						allyHPBar.update();
					appendEvent(EventType.EMPHASIZED,allyPony.getNickname() + " is hurt by poison!");
					resultAnim(allyLocation(),"-"+(int)(Battle.BAD_POISON_DAMAGE*100*Integer.parseInt(token[2]))+"%!",ResultType.BAD);
				} else if(token[1].equals("opp")) {
					synchronized(oppPony) {
						oppPony.damagePerc(Battle.BAD_POISON_DAMAGE*100f*Integer.parseInt(token[2]));
					}
					if(oppHPBar != null)
						oppHPBar.update();
					appendEvent(EventType.EMPHASIZED,"Enemy " + oppPony.getNickname() + " is hurt by poison!");
					resultAnim(oppLocation(),"-"+(int)(Battle.BAD_POISON_DAMAGE*100*Integer.parseInt(token[2]))+"%!",ResultType.BAD);
				}
				try {
					Thread.sleep(INTERPRET_DELAY);
				} catch(InterruptedException ignore) {}

			} catch(IllegalArgumentException e) {
				printDebug("[BattlePanel.interpret()]: illegal argument: "+e);
			}

		} else if(token[0].equals("addstatus") && token.length > 2) {
			/* |addstatus|(ally/opp)|status[|phrase] */
			Status status = null;
			if(token[2].equals("par")) status = Status.PARALYZED;
			else if(token[2].equals("slp")) status = Status.ASLEEP;
			else if(token[2].equals("ptr")) status = Status.PETRIFIED;
			else if(token[2].equals("psn")) status = Status.POISONED;
			else if(token[2].equals("tox")) status = Status.INTOXICATED;
			else if(token[2].equals("brn")) status = Status.BURNED;
			else if(token[2].equals("cnf")) status = Status.CONFUSED;
			else {
				printDebug("[BP.interpret(addstatus)] Unknown status: "+token[2]);
				return;
			}

			if(token[1].equals("ally")) {
				allyPony.addStatus(status);
				/*if(status != Status.CONFUSED)
					allyHPBar.addStatus(status);
				else
					allyHPBar.addPseudoStatus(Status.CONFUSED.toString(),false);
				*/
				allyHPBar.update();
				if(token.length > 3)
					appendEvent(EventType.STATUS,token[2],"ally|"+token[3]);
				else
					appendEvent(EventType.STATUS,token[2],"ally");

				switch(status) {
					case PARALYZED:
						resultAnim(allyLocation(),status+"!", new Color(0xCFA600));
						break;
					case POISONED:
					case INTOXICATED:
						resultAnim(allyLocation(),status+"!", new Color(0x9900CC));
						break;
					case BURNED:
						resultAnim(allyLocation(),status+"!", new Color(0xCC0000));
						break;
					case PETRIFIED:
						resultAnim(allyLocation(),status+"!", new Color(0x666699));
						break;
					case ASLEEP:
						resultAnim(allyLocation(),status+"!", new Color(0xA3A3C2));
						break;
					default:
						resultAnim(allyLocation(),status+"!");
				}

			} else if(token[1].equals("opp")) {
				oppPony.addStatus(status);
				/*if(status != Status.CONFUSED)
					oppHPBar.addStatus(status);
				else
					oppHPBar.addPseudoStatus(Status.CONFUSED.toString(),false);
				*/
				oppHPBar.update();
				if(token.length > 3)
					appendEvent(EventType.STATUS,token[2],"opp|"+token[3]);
				else
					appendEvent(EventType.STATUS,token[2],"opp");

				switch(status) {
					case PARALYZED:
						resultAnim(oppLocation(),status+"!",new Color(0xCFA600));
						break;
					case POISONED:
					case INTOXICATED:
						resultAnim(oppLocation(),status+"!",new Color(0x9900CC));
						break;
					case BURNED:
						resultAnim(oppLocation(),status+"!",new Color(0xCC0000));
						break;
					case PETRIFIED:
						resultAnim(oppLocation(),status+"!",new Color(0x666699));
						break;
					case ASLEEP:
						resultAnim(oppLocation(),status+"!",new Color(0xA3A3C2));
						break;
					default:
						resultAnim(oppLocation(),status+"!");
				}
			}

			try {
				Thread.sleep(INTERPRET_DELAY);
			} catch(InterruptedException ignore) {}

		} else if(token[0].equals("rmstatus") && token.length > 1) {
			/* |rmstatus|(ally/opp)[|status|phrase] */
			Status status = null;
			boolean quiet = token.length > 3 && token[3].equalsIgnoreCase("quiet");

			if(token.length > 2) {
				if(token[2].equals("par")) status = Status.PARALYZED;
				else if(token[2].equals("slp")) status = Status.ASLEEP;
				else if(token[2].equals("ptr")) status = Status.PETRIFIED;
				else if(token[2].equals("psn")) status = Status.POISONED;
				else if(token[2].equals("tox")) status = Status.INTOXICATED;
				else if(token[2].equals("brn")) status = Status.BURNED;
				else if(token[2].equals("cnf")) status = Status.CONFUSED;
			}

			if(token[1].equals("ally")) {
				if(status == null) {	// heal all statuses
					if(allyPony != null)
						allyPony.healStatus();
					if(allyHPBar != null) {
						allyHPBar.clearStatuses();
						allyHPBar.clearPseudoStatus(Status.CONFUSED.toString());
					}
					if(!quiet) {
						appendEvent(EventType.EMPHASIZED,allyPony.getNickname()+" healed!");
						resultAnim(allyLocation(),"Healed!",ResultType.GOOD);
					}

				} else { 
					if(allyPony != null)
						allyPony.healStatus(status);
					if(allyHPBar != null) {
						if(status != Status.CONFUSED)
							allyHPBar.clearStatus(status);
						else
							allyHPBar.clearPseudoStatus(Status.CONFUSED.toString());
					}
					if(!quiet) {
						if(token.length > 3) 
							appendEvent(EventType.STATUS,token[2],"ally|"+token[3]);
						else
							appendEvent(EventType.STATUS,token[2],"ally|cure");
					
						switch(status) {
							case PARALYZED:
								resultAnim(allyLocation(),"Paralysis cured",ResultType.GOOD);
								break;
							case POISONED:
							case INTOXICATED:
								resultAnim(allyLocation(),"Poison cured",ResultType.GOOD);
								break;
							case ASLEEP:
								resultAnim(allyLocation(),"Woke up",ResultType.GOOD);
								break;
							case PETRIFIED:
								resultAnim(allyLocation(),"Thawed",ResultType.GOOD);
								break;
							case BURNED:
								resultAnim(allyLocation(),"Burn cured",ResultType.GOOD);
								break;
							case CONFUSED:
								resultAnim(allyLocation(),"Cured",ResultType.GOOD);
								break;
						}
					}
				}
			} else if(token[1].equals("opp")) {
				if(status == null) {
					if(oppPony != null)
						oppPony.healStatus();
					if(oppHPBar != null) {
						oppHPBar.clearStatuses();
						oppHPBar.clearPseudoStatus(Status.CONFUSED.toString());
					}
					if(!quiet) {
						appendEvent(EventType.EMPHASIZED,"Enemy "+oppPony.getNickname()+" healed!");
						resultAnim(oppLocation(),"Healed!",ResultType.GOOD);
					}
				} else {
					if(oppPony != null)
						oppPony.healStatus(status);
					if(oppHPBar != null) {
						if(status != Status.CONFUSED)
							oppHPBar.clearStatus(status);
						else
							oppHPBar.clearPseudoStatus(Status.CONFUSED.toString());
					}
					if(!quiet) {
						if(token.length > 3)
							appendEvent(EventType.STATUS,token[2],"opp|"+token[3]);
						else
							appendEvent(EventType.STATUS,token[2],"opp|cure");

						switch(status) {
							case PARALYZED:
								resultAnim(oppLocation(),"Paralysis cured",ResultType.GOOD);
								break;
							case POISONED:
							case INTOXICATED:
								resultAnim(oppLocation(),"Poison cured",ResultType.GOOD);
								break;
							case ASLEEP:
								resultAnim(oppLocation(),"Woke up",ResultType.GOOD);
								break;
							case PETRIFIED:
								resultAnim(oppLocation(),"Thawed",ResultType.GOOD);
								break;
							case BURNED:
								resultAnim(oppLocation(),"Burn cured",ResultType.GOOD);
								break;
							case CONFUSED:
								resultAnim(oppLocation(),"Cured",ResultType.GOOD);
								break;
						}
					}
				}
			}

			try {
				Thread.sleep(INTERPRET_DELAY);
			} catch(InterruptedException ignore) {}

		} else if(token[0].equals("healteam") && token.length > 1) {
			/* |healteam|(ally/opp) */
			if(token[1].equals("ally")) {
				if(allyPony != null)
					allyPony.healStatus();
				if(allyHPBar != null) {
					allyHPBar.clearStatuses();
					allyHPBar.clearPseudoStatus(Status.CONFUSED.toString());
				}
				p1.getTeam().healTeamStatus();	
				appendEvent(EventType.EMPHASIZED, "Team cured!");
				resultAnim(allyLocation(),"Team cured!",ResultType.GOOD);
			} else if(token[1].equals("opp")) {
				if(oppPony != null)
					oppPony.healStatus();
				if(oppHPBar != null) {
					oppHPBar.clearStatuses();
					allyHPBar.clearPseudoStatus(Status.CONFUSED.toString());
				}
				p2.getTeam().healTeamStatus();
				appendEvent(EventType.EMPHASIZED, "Team cured!");
				resultAnim(oppLocation(),"Team cured!",ResultType.GOOD);
			} else {
				printDebug("[BP.interpret(healteam)] error: side is "+token[1]+"!");
			}

			try {
				Thread.sleep(INTERPRET_DELAY);
			} catch(InterruptedException ignore) {}

		} else if(token[0].equals("addpseudo") && token.length > 3) {
			/* |addpseudo|(ally/opp)|(good/bad)|pseudostatus */
			boolean good = false;
			if(token[2].equals("good")) {
				good = true;
			} else if(!token[2].equals("bad")) {
				printDebug("[BP.interpret()] Error: expected good or bad but found "+token[2]);
				return;
			}
			if(token[1].equals("ally")) { 
				allyHPBar.addPseudoStatus(token[3],good);
				resultAnim(allyLocation(),token[3],good ? ResultType.GOOD : ResultType.BAD);
				allyHPBar.update();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						validate();
						repaint();
					}
				});
			} else if(token[1].equals("opp")) {
				oppHPBar.addPseudoStatus(token[3],good);
				resultAnim(oppLocation(),token[3],good ? ResultType.GOOD : ResultType.BAD);
				oppHPBar.update();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						validate();
						repaint();
					}
				});
			}
			try {
				Thread.sleep(INTERPRET_DELAY);
			} catch(InterruptedException ignore) {}

		} else if(token[0].equals("rmpseudo") && token.length > 2) {
			/* |rmpseudo|(ally/opp)|pseudostatus[|result phrase] */
			if(token[1].equals("ally")) {
				allyHPBar.clearPseudoStatus(token[2]);
				if(token.length > 3)
					resultAnim(allyLocation(),token[3]);
				allyHPBar.update();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						validate();
						repaint();
					}
				});
			} else if(token[1].equals("opp")) {
				oppHPBar.clearPseudoStatus(token[2]);
				if(token.length > 3)
					resultAnim(oppLocation(),token[3]);
				oppHPBar.update();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						validate();
						repaint();
					}
				});
			}
			try {
				Thread.sleep(INTERPRET_DELAY);
			} catch(InterruptedException ignore) {}

		} else if(token[0].equals("taunt") && token.length > 1) {
			/* |taunt|(ally/opp) */
			if(token[1].equals("ally")) {
				int cnt = Pony.MOVES_PER_PONY;
				for(int i = 0; i < Pony.MOVES_PER_PONY; ++i) 
					if(moveB[i].getMove() != null && moveB[i].getMove().getMoveType() == Move.MoveType.STATUS) {
						moveB[i].setEnabled(false);
						--cnt;
				}
				if(cnt == 0) {
					try {
						moveB[0].setMove(MoveCreator.create("Struggle"));
						for(int i = 1; i < Pony.MOVES_PER_PONY; ++i)
							moveB[i].setMove(null);
					} catch(ReflectiveOperationException e) {
						printDebug("[BP.interpret(taunt)] Couldn't create move struggle:");
						e.printStackTrace();
					}
				}
				allyHPBar.addPseudoStatus("Taunt", false);
				resultAnim(allyLocation(),"Taunted!");
				allyHPBar.update();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						validate();
						repaint();
					}
				});
				appendEvent(EventType.BATTLE,allyPony.getNickname()+" fell for the taunt!");
				allyPony.setTaunted(true);

			} else if(token[1].equals("opp")) {
				oppHPBar.addPseudoStatus("Taunt", false);
				resultAnim(oppLocation(),"Taunted!");
				oppHPBar.update();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						validate();
						repaint();
					}
				});
				appendEvent(EventType.BATTLE,"The enemy "+oppPony.getNickname()+" fell for the taunt!");
			}

		} else if(token[0].equals("rmtaunt") && token.length > 1) {
			/* |rmtaunt|(ally/opp) */
			if(token[1].equals("ally")) {
				if(allyPony == null) return;
				if(!allyPony.isTaunted()) {
					printDebug("[BP.interpret(taunt)] allyPony is not taunted!");
					return;
				}
				allyPony.setTaunted(false);
				for(int i = 0; i < Pony.MOVES_PER_PONY; ++i) {
					moveB[i].setMove(allyPony.getMove(i));
					moveB[i].setEnabled(true);
				}
				if(allyHPBar != null) {
					allyHPBar.clearPseudoStatus("Taunt");
					allyHPBar.update();
				}
				resultAnim(allyLocation(),"Taunt ended!", ResultType.GOOD);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						validate();
						repaint();
					}
				});
				appendEvent(EventType.BATTLE,allyPony.getNickname()+"'s taunt ended!");

			} else if(token[1].equals("opp")) {
				if(oppPony == null) return;
				if(oppHPBar != null) {
					oppHPBar.clearPseudoStatus("Taunt");
					oppHPBar.update();
				}
				resultAnim(oppLocation(),"Taunt ended!", ResultType.GOOD);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						validate();
						repaint();
					}
				});
				appendEvent(EventType.BATTLE,oppPony.getNickname()+"'s taunt ended!");
			}

		} else if(token[0].equals("fainted") && token.length > 1) {
			/* |fainted|(ally/opp) */
			if(token[1].equals("ally")) {
				faintAnim(allySprite);
				appendEvent(EventType.EMPHASIZED,allyPony.getNickname()+" fainted!");
				final int ponyIndex = findIndexOf(1,allyPony.getNickname());
				teamMenu1.setFainted(ponyIndex,true);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						teamP.getToken(ponyIndex).setEnabled(false);	
						if(allyHPBar != null) {
							synchronized(allyHPBar) {
								allyHPBar.setVisible(false);
								fieldP.remove(allyHPBar);
							}
							allyHPBar = null;
						}
						if(allySprite != null) {
							allySprite.setVisible(false);
							allySprite = null;
						}
						if(allyPony != null) 
							synchronized(allyPony) {
								allyPony.setFainted();
							}
						moveP.setVisible(false);
						for(int i = 0; i < Pony.MOVES_PER_PONY; ++i)
							moveB[i].setMove(null);
					}
				});
			} else if(token[1].equals("opp")) {
				faintAnim(oppSprite);
				appendEvent(EventType.EMPHASIZED,"Enemy "+oppPony.getNickname()+" fainted!");
				final int ponyIndex = findIndexOf(2,oppPony.getNickname());
				teamMenu2.setFainted(ponyIndex,true);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if(oppHPBar != null) {
							synchronized(oppHPBar) {
								oppHPBar.setVisible(false);
								fieldP.remove(oppHPBar);
							}
							oppHPBar = null;
						}
						if(oppSprite != null) {
							oppSprite.setVisible(false);
							oppSprite = null;
						}
						if(oppPony != null) 
							synchronized(oppPony) {
								oppPony.setFainted();
							}
					}
				});
			}
		} else if(token[0].equals("effective") && token.length > 2) {
			/* |effective|ally/opp|multiplier */
			if(!(token[1].equals("ally") || token[1].equals("opp"))) {
				printDebug("[BP.interpret(effective)] Error: side is "+token[1]);
				return;
			}				
			try {
				double multiplier = Double.parseDouble(token[2]);
				if(Debug.on) printDebug("[BP.interpret(effective)]: multiplier is "+multiplier);
				if(multiplier > 2.) {
					appendEvent(EventType.BATTLE,"It's super-duper-effective!");
					resultAnim((token[1].equals("ally") ? allyLocation() : oppLocation()),"Super-effective",ResultType.BAD);
				} else if(multiplier > 1.) {
					appendEvent(EventType.BATTLE,"It's supereffective!");
					resultAnim((token[1].equals("ally") ? allyLocation() : oppLocation()),"Super-effective",ResultType.BAD);
				} else if(multiplier < 1.) {
					appendEvent(EventType.BATTLE,"It's not very effective...");
					resultAnim((token[1].equals("ally") ? allyLocation() : oppLocation()),"Resisted");
				}
			} catch(IllegalArgumentException e) {
				printDebug("[BP.interpret(effective)]: error while parsing multiplier: "+e);
			}

		} else if(token[0].equals("immune") && token.length > 1) {
			/* |immune|(ally/opp) */
			if(token[1].equals("ally")) {
				appendEvent(EventType.BATTLE,"It doesn't affect "+allyPony.getNickname());
				resultAnim(allyLocation(),"Immune!");
			} else if(token[1].equals("opp")) {
				appendEvent(EventType.BATTLE,"It doesn't affect enemy "+oppPony.getNickname());
				resultAnim(oppLocation(),"Immune!");
			}

		} else if(token[0].equals("wait")) {
			/* |wait */
			try {
				if(Debug.on) printDebug("Invoking wait...");
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						moveP.setVisible(false);
						showBottomAlert("Waiting for the opponent...");
						teamP.setVisible(false);
						validate();
						repaint();
						if(Debug.on) printDebug("wait: setVisible(false) completed.");
					}
				});
				appendEvent(EventType.BATTLE,"Waiting for the opponent...");
			} catch(InterruptedException e) {
				printDebug("[BP.interpret(wait)]: interrupted.");
				return;
			} catch(InvocationTargetException e) {
				printDebug("[BP.interpret(wait)]: "+e);
				printDebug("Caused by: "+e.getCause());
				return;
			}

		} else if(token[0].equals("endwait")) {
			/* |endwait */
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					showBottomMoves();
					moveP.setVisible(true);
					teamP.setVisible(true);
					validate();
					repaint();
				}
			});

		} else if(token[0].equals("deductpp") && token.length > 1) {
			/* |deductpp|Move Name */
			if(allyPony == null)
				return;
			for(MoveButton mb : moveB) 
				if(mb.getMove() != null && mb.getMove().getName().equals(token[1])) {
					mb.deductPP();
					if(Debug.on) printDebug("Deducted 1 PP from "+mb.getMove());
				}
	
		} else if(token[0].equals("addhazard") && token.length > 2) {
			/* |addhazard|(ally/opp)|Hazard ClassName */
			if(!(token[1].equals("ally") || token[1].equals("opp"))) {
				printDebug("[BP.interpret(addhazard)]: Error - side is "+token[1]);
				return;
			}
			boolean isAlly = token[1].equals("ally");
			try {
				Hazard hazard = HazardCreator.create(token[2]);	

				// create hazard token
				Image img = ImageIO.read(hazard.getToken());
				if(img == null) {
					printDebug("[BP.interpret(addhazard)]: Error - couldn't load token "+hazard.getToken());
					return;
				}
				JLabel hzToken = new JLabel(new ImageIcon(img));
				// properly set bounds of token: the position depends on 2 factors:
				// 1) on ally side or opponent side?
				// 2) how many layers are allowed?
				int x = isAlly ? 
						FIELD_X+140 : 
						FIELD_WIDTH-FIELD_X-HAZARD_TOKEN_SIZE-140;
				int y = isAlly ? 
						FIELD_HEIGHT-HAZARD_TOKEN_SIZE-30 :
						130;

				if(isAlly) {
					if(hazards.get(0).get(hazard.getName()) != null) {
						x -= (20*hazards.get(0).get(hazard.getName()));
						hazards.get(0).put(hazard.getName(),hazards.get(0).get(hazard.getName())+1);
					} else {
						hazards.get(0).put(hazard.getName(),1);
					}
					if(hazardTokens.get(0).containsKey(hazard.getName()))
						hazardTokens.get(0).get(hazard.getName()).add(hzToken);
					else
						hazardTokens.get(0).put(hazard.getName(),new LinkedList<JLabel>(
							Arrays.asList(new JLabel[] { hzToken })));
				} else {
					if(hazards.get(1).get(hazard.getName()) != null) {
						x += (20*hazards.get(1).get(hazard.getName()));
						hazards.get(1).put(hazard.getName(),hazards.get(1).get(hazard.getName())+1);
					} else {
						hazards.get(1).put(hazard.getName(),1);
					}
					if(hazardTokens.get(1).containsKey(hazard.getName()))
						hazardTokens.get(1).get(hazard.getName()).add(hzToken);
					else
						hazardTokens.get(1).put(hazard.getName(),new LinkedList<JLabel>(
							Arrays.asList(new JLabel[] { hzToken })));
				}
				
				hzToken.setBounds(x,y,hzToken.getIcon().getIconWidth(),hzToken.getIcon().getIconHeight());
				fieldP.add(hzToken,HAZARD_LAYER);

			} catch(ReflectiveOperationException e) {
				printDebug("Failed to create hazard "+token[2]+": "+e);
				return;
			} catch(IOException e) {
				printDebug("IOException while loading "+token[2]+": "+e);
				return;
			}

		
		} else if(token[0].equals("rmhazard") && token.length > 1) {
			/* |rmhazard|(ally/opp)[|Hazard's Move Name|quiet] */
			if(!(token[1].equals("ally") || token[1].equals("opp"))) {
				printDebug("[BP.interpret(rmhazard)]: Error - side is "+token[1]);
				return;
			}
			final boolean quiet = token.length > 3 && token[3].equals("quiet");
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						int side = token[1].equals("ally") ? 0 : 1;
						if(token.length > 2) {
							// only remove the specified hazard
							if(hazardTokens.get(side).get(token[2]) == null) {
								printDebug("[BP.interpret(rmhazard)] Error - no such hazard on side "+side+": "+token[2]);
								return;
							}
							for(JLabel lab : hazardTokens.get(side).get(token[2])) {
								if(lab != null) {
									lab.setVisible(false);
									fieldP.remove(lab);

								} else {
									printDebug("[BP.interpret(rmhazard)] Error - tried to remove non-existing hazard "
										+token[2]);
									return;
								}
							}
							hazards.get(side).remove(token[2]);
							if(!quiet)
								appendEvent(EventType.EMPHASIZED,token[2]+" disappeared from your " +
									(side == 1 ? "opponent's " : "") + "field!");
						} else {
							// remove all hazards
							for(List<JLabel> list : hazardTokens.get(side).values()) {
								for(JLabel lab : list) {
									lab.setVisible(false);
									fieldP.remove(lab);
								}
							}
							hazards.get(side).clear();
							if(!quiet)
								appendEvent(EventType.EMPHASIZED,"Hazards disappeared from your " +
									(side == 1 ? "opponent's " : "") + "field!");
						}
					}
				});
			} catch(InterruptedException e) {
				printDebug("[BP.interpret(rmhazard)] interrupted!");
				return;
			} catch(InvocationTargetException e) {
				e.printStackTrace();
				printDebug("Caused by: "+e.getCause());
				return;
			}
			
		} else if(token[0].equals("disconnect")) {
			/* |disconnect[|message] */
			if(token.length > 1)
				appendEvent(EventType.CRITICAL,token[1]);
			else
				appendEvent(EventType.CRITICAL,"Server disconnected: battle aborted.");
			terminate();
		}
		else {
			printDebug("[BattlePanel]: Unknown command: "+line);
		}

		// FIXME: this may probably be just called on |turn: verify and fix
		if(!(token[0].equals("chat") || token[0].equals("html") || token[0].equals("htmlconv"))) {
			// reset move selections
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if(Debug.pedantic) printDebug("Called unselect()");
					for(MoveButton tb : moveB) {
						tb.setSelected(false);
						if(Debug.pedantic) printDebug("Unselected moveButton: "+tb.getMove());
						tb.repaint();
					}
				}
			});
		}
	}

	/** Class test: initialize a battle start, then accepts commands
	 * from the command line.
	 */
	public static void main(String[] args) throws Exception {
		final JFrame frame = new JFrame();
		final Player p1 = new Player("me");
		final Player p2 = new Player("opponent");
		final BattlePanel bp = new BattlePanel(p1,p2);
		// TODO: replays!
		if(args.length > 0 && args[0].equals("--replay")) {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					bp.initialize();
					frame.add(bp);
					SwingConsole.run(frame,"Pokepon Replay");
				}
			});
			try (BufferedReader bf = new BufferedReader(new InputStreamReader(System.in))) {
				String line = null;
				while((line = bf.readLine()) != null) {
					bp.interpret(line);
				}
			}
			return;
		}
		Team team1 = Team.randomTeam(6);
		p1.setTeam(team1);
		p2.setTeam(Team.randomTeam(6));
		for(Pony p : team1) {
			bp.teamP.addPony(p);
		}

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				bp.initialize();
				//simulate a battle start
				bp.interpret("|join|ally|ALLY");
				bp.interpret("|join|opp|OPPONENT");
				bp.interpret("|teampreview");
				bp.interpret("|start");
			}
		});
		frame.add(bp);
		SwingConsole.run(frame,"Pokepon Battle Panel test");

		Thread.sleep(500);
		bp.interpret("|switch|ally|0|100");
		bp.interpret("|switch|opp|0|100");

		try (BufferedReader bf = new BufferedReader(new InputStreamReader(System.in))) {
			String line = null;
			while((line = bf.readLine()) != null) {
				bp.interpret(line);
			}
		}
	}

	/** Class handling the chat input events */
	private final KeyListener outKeyListener = new KeyListener() {
		private int index;
		private LinkedList<String> history = new LinkedList<String>();

		public void keyPressed(KeyEvent e) {
			//sane history index
			if(index <= -1) index = 0;
			else if(index >= history.size()) index = history.size();

			switch(e.getKeyCode()) {
				case KeyEvent.VK_ENTER:
					if(inputF.getText() != null && inputF.getText().length() > 0) {
						// here we use fullname rather than playerID because guests other than
						// players may use chat as well; this way the server doesn't bother to parse
						// chat messages.
						if(inputF.getText().charAt(0) != CMD_PREFIX) {
							sendB("|chat|"+p1.getName()+"|"+MessageManager.sanitize(inputF.getText())); 
						} else {
							String txt = inputF.getText().trim().substring(1);
							if(txt.equals("export") || txt.equals("save")) {
								if(battleLogger != null) {
									// TODO: add capability to select save location
									battleLogger.processRecord(null);
									if(battleLogger.getFeedbackMsg() != null)
										appendEvent(EventType.INFO,battleLogger.getFeedbackMsg());
								} else {
									appendEvent(EventType.INFO,"You haven't enabled logging for this battle.");
								}
							} else {
								sendB("|cmd|"+(playerID == 0 ? p1.getName() : playerID)+"|"+inputF.getText().substring(1));
							}
						}
						history.add(inputF.getText());
						if(history.size() > MAX_HIST_SIZE) history.removeFirst();
						index = history.size()-1;
						inputF.setText("");
					}
					break;
				case KeyEvent.VK_UP:
					if(history.size() > 0 && index > -1) {
						inputF.setText(history.get(index--));
					}
					break;
				case KeyEvent.VK_DOWN:
					if(history.size() > 0 && index < history.size()-1) {
						inputF.setText(history.get(++index));
					} else {
						if(	inputF.getText() != null && 
							inputF.getText().length() > 0 && 
							!history.get(history.size()-1).equals(inputF.getText())
						) {
							history.add(inputF.getText());
							if(history.size() > MAX_HIST_SIZE) history.removeFirst();
							index = history.size()-1;
						}
						inputF.setText("");
					}
					break;
			}
		}
		public void keyReleased(KeyEvent e) {}
		public void keyTyped(KeyEvent e) {}
	};

	class MoveActionListener implements ActionListener {
		private final int num;

		public MoveActionListener(final int num) {
			this.num = num;
		}

		public void actionPerformed(ActionEvent e) {
			moveB[num].setSelected(true);
			for(MoveButton mb : moveB)
				if(mb != moveB[num])
					mb.setSelected(false);
			if(connection != null) 
				sendB("|move|"+
					(playerID == 0 ? p1.getName() :	playerID) +
					"|" +
					(moveB[num].getMove().getName().equals("Struggle") || allyPony.isLockedOnMove()
						? moveB[num].getMove().getName()
						: num
					)
				);
		}
	}

	class SwitchListener implements ActionListener {
		private final int num;

		public SwitchListener(final int num) {
			this.num = num;
		}

		public void actionPerformed(ActionEvent e) {
			// don't attempt to switch in currently active pony
			if(p1.getTeam().getPony(num) == allyPony) return;

			if(connection != null)
				sendB("|switch|"+(playerID == 0 ? p1.getName() : playerID)+"|"+num);
			else 
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if(p1.getTeam().getPony(num) == null) return;
						interpret("|switch|ally|"+p1.getTeam().getPony(num).getNickname()+
								"|"+p1.getTeam().getPony(num).getName());
					}
				});
		}
	};

	/** MouseListener for the ponies' sprites */
	class PonySpriteListener extends MouseAdapter {
		private final boolean isAlly;
		private Popup popup;
		private PopupFactory popupFactory;
		private JToolTip toolTip; 
		
		public PonySpriteListener(final boolean isAlly) {
			this.isAlly = isAlly;
			popupFactory = PopupFactory.getSharedInstance();
			toolTip = createToolTip();
		}
		private void showToolTip(MouseEvent e) {
			Pony pony = isAlly ? allyPony : oppPony;
			int ponynum = findIndexOf(1, allyPony);
			if(pony == null) return;
			String possibleAbs = "";
			if(!isAlly) 
				for(String ab : pony.getPossibleAbilities()) {
					if(possibleAbs.length() > 0)
						possibleAbs += ", " + ab;
					else
						possibleAbs = ab;
				}
			StringBuilder statsString = new StringBuilder("");
			if(isAlly) {
				// show ally stats
				String[] stat = "Atk Def SpA SpD Spe".split(" ");
				for(int i = 0; i < 5; ++i) {
					if(ponyEffStats[i] != 0)
						statsString.append(ponyEffStats[i] + " " + stat[i] + " / ");
					else
						statsString.append("??? "+stat[i]+" / ");
				}
				statsString.delete(statsString.length() - 2, statsString.length());
			} else {
				// show range of possible enemy speed	
				if(oppPony != null) {
					int baseSpe = oppPony.getBaseSpeed();
					int minSpe = (int)((2 * baseSpe * oppPony.getLevel() / 100 + 5) * 0.9);
					int maxSpe = (int)(((Pony.MAX_IV + 2 * baseSpe + Pony.MAX_EV / 4) * oppPony.getLevel() / 100 + 5) * 1.1);
					statsString.append(minSpe+" to "+maxSpe+" Spe (before items/abilities/modifiers)");
				}
			}
			toolTip.setTipText(
				"<html><body style=\"font-family:"+GUIGlobals.FONT_FAMILY+"\">"+
				"<b>"+pony.getName()+"</b> "+
				(pony.getSex() == Pony.Sex.FEMALE 
					? "<small style=\"color:#C57575\">&#9792;</small>" 
					: "<small style=\"color:#7575C0\">&#9794;</small>"
				)+" <small>L"+pony.getLevel()+"</small><br>"+
				pony.getTypingHTMLTokens()+"<br>"+
				"<p><small>HP: "+(int)(pony.getHpPerc()*100)+"%"+(isAlly ? " ("+pony.getHp()+")" : "")+"</small></p>"+
				"<p>"+(isAlly 
					? "Ability: <b>"+
						(pony.getAbility() == null 
							? "" 
							: pony.getAbility()
						)+"</b>"
					: "Possible abilities:&nbsp;"+possibleAbs.trim()
				)+"</p>"+
				"<p>"+(isAlly 
					? "Item: <b>"+(pony.getItem() == null 
						? "" 
						: pony.getItem()
					)+"</b>" 
					: ""
				)+"</p>"+
				(pony.hasNegativeCondition() ? 
				"<p>Status: "+ 
				(pony.isKO() ? "KO" :
				pony.isBurned() ? "brn" :
				pony.isAsleep() ? "slp" :
				pony.isParalyzed() ? "par" :
				pony.isIntoxicated() ? "tox" :
				pony.isPoisoned() ? "psn" :
				pony.isPetrified() ? "ptr" : "ok") + "</p>" : "")+
				"<p>"+statsString+"</p>"+
				"</body></html>"
			);
			
			// set fixed coordinates for tooltips
			int x = 0, y = 0;
			if(isAlly) {
				x = (int)allySprite.getLocationOnScreen().getX() - 20;
				y = (int)allySprite.getLocationOnScreen().getY() - 120;
			} else {
				x = (int)oppSprite.getLocationOnScreen().getX() - 160;
				y = (int)oppSprite.getLocationOnScreen().getY() - 50;
			}
			popup = popupFactory.getPopup((isAlly ? allySprite : oppSprite),toolTip,x,y);
			popup.show();
		}
		@Override
		public void mouseEntered(MouseEvent e) {
			if((isAlly && allySprite != null) || (!isAlly && oppSprite != null))
				showToolTip(e);
		}
		@Override
		public void mouseExited(MouseEvent e) {
			if(popup != null)
				popup.hide();
		}
	}


	/** Utility method that produces the full path of background images */
	private static URL completeBGPath(String relPath) {
		printDebug(Meta.complete2(Meta.RESOURCE_DIR+Meta.DIRSEP+"background"+Meta.DIRSEP+relPath));
		return BattlePanel.class.getResource(Meta.complete2(Meta.RESOURCE_DIR+Meta.DIRSEP+"background"+Meta.DIRSEP+relPath));
	}

	private void showBottomAlert(String alert) {
		alertLabel.setText(alert);
		((CardLayout)bottomCardP.getLayout()).show(bottomCardP,ALERT_CARD);
	}

	private void showBottomMoves() {
		alertLabel.setText("");
		((CardLayout)bottomCardP.getLayout()).show(bottomCardP,MOVE_CARD);
	}

	public void appendEvent(final EventType type,final String event) {
		appendEvent(type,event,null);
	}

	/** Add an event to the chat pane, formatting it according to the type of the event. */
	public void appendEvent(final EventType type,final String event,final String details) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if(Debug.on) printDebug("Called appendEvent("+type+","+event+","+details+")");
				StringBuilder sb = new StringBuilder("");
				switch(type) {
					case JOIN:
						/* JOIN, Name of Player */
						sb.append("<em><strong><font color=\"blue\">"+event+"</font></strong> joined.</em>");
						break;
					case LEAVE:
						/* LEAVE, Name of Player, [forfeited] */
						sb.append("<em><font color=\"gray\">"+event+" "+(details == null ? "left" : details)+".</em>");
						break;
					case CHAT: 
						/* CHAT, Name of Player, msg */
						if(details == null) {
							printDebug("[appendEvent]: error - chat event has null argument!");
							return;
						}
						if(event.equals(p1.getName()))
							sb.append("<strong><font color=\"green\">"+event+"</font></strong>: "+details);
						else if(event.equals(p2.getName()))
							sb.append("<strong><font color=\"red\">"+event+"</font></strong>: "+details);
						else
							sb.append("<strong><font color=\"blue\">"+event+"</font></strong>: "+details);
						break;
					case RULE: {
						/* RULE, rule */
						if(event.equalsIgnoreCase("canon")) {
							sb.append("<strong>Canon Clause</strong>: <em>"+
								"<font color=\"gray\">only canon ponies are allowed.</font></em>");
							break;
						} else if(event.equalsIgnoreCase("speciesclause")) {
							sb.append("<strong>Species Clause</strong>: <em><font color=\"gray\">only one copy of each pony is allowed.</font></em>");
							break;
						}
						// bold the rule name if format is `Name: description`; else, just italicize it and make it grey.
						String[] token = event.split(":",2);
						if(token.length < 2) {
							sb.append("<em><font color=\"gray\">"+token[0]+"</font></em>");
						} else {
							sb.append("<strong>"+token[0]+"</strong>: <em><font color=\"gray\">"+token[1]+"</font></em>");
						}
						break;
					}
					case BATTLE:
					case BOOST:
						/* BATTLE, event */
						sb.append("<font color=\"gray\" size=3>"+event+"</font>");
						break;
					case SWITCH: {
						/* SWITCH, Name of Pony[|true name], ally/opp */
						if(details == null) {
							printDebug("[appendEvent]: error - switch event has null argument!");
							return;
						}
						String[] nametoken = event.split("\\|",2);
						if(details.equals("opp")) 
							sb.append(p2.getName()+" sent out "+nametoken[0]+
								(nametoken.length > 1 ? " (<small>"+nametoken[1]+"</small>)" : "")+"!");
						else if(details.equals("ally")) 
							sb.append("Go! "+nametoken[0]+
								(nametoken.length > 1 ? " (<small>"+nametoken[1]+"</small>)" : "")+"!");
						else
							printDebug("[appendEvent]: error - switch details is "+details);
						break;
					}
					case MOVE: 
						/* MOVE, Name of Move, ally/opp */
						if(details == null) {
							printDebug("[appendEvent]: error - move event has null argument!");
							return;
						}
						if(details.equals("ally")) 
							sb.append(allyPony.getNickname()+" used <strong>"+event+"</strong>!");
						else if(details.equals("opp"))
							sb.append("The opposing "+oppPony.getNickname()+" used <strong>"+event+"</strong>!");
						else
							printDebug("[appendEvent]: error - move details is "+details);
						break;
					case STATUS: {
						/* STATUS, status, ally/opp */
						if(details == null) {
							printDebug("[appendEvent]: error - status event has null argument!");
							return;
						}
						String[] token = details.split("\\|");
						String pny = "";
						if(token[0].equals("ally")) {
							pny = allyPony.getNickname();
						} else if(token[0].equals("opp")) {
							pny = "Enemy "+oppPony.getNickname();
						} else {
							printDebug("[appendEvent]: error - player is neither ally nor opponent!");
							printDebug("details: "+details+", token="+Arrays.asList(token));
							return;
						}
						// details: ally/opp[|phrase/cure]
						boolean cure = false;
						String phrase = null;
						if(token.length > 1) {
							if(token[1].equalsIgnoreCase("cure")) {
								cure = true;
							} else {
								phrase = token[1];
							}
						}
						Pony.Status status = Pony.Status.forName(event);
						if(status == null) {
							printDebug("[appendEvent]: error - unknown status: "+event);
							return;
						}

						if(phrase == null) {
							switch(status) {
								case PARALYZED:
									if(cure)
										sb.append(pny+" was cured of paralysis.");
									else
										sb.append(pny+" is paralyzed! It may be unable to move!");
									break;
								case BURNED:
									if(cure)
										sb.append(pny+" healed its burn!");
									else
										sb.append(pny+" was burned!");
									break;
								case POISONED:
									if(cure)
										sb.append(pny+" was cured of its poisoning.");
									else
										sb.append(pny+" was poisoned!");
									break;
								case INTOXICATED:
									if(cure)
										sb.append(pny+" was cured of its poisoning.");
									else
										sb.append(pny+" was badly poisoned!");
									break;
								case ASLEEP:
									if(cure)
										sb.append(pny+" woke up!");
									else
										sb.append(pny+" fell asleep!");
									break;
								case PETRIFIED:
									if(cure)
										sb.append(pny+"'s body is back to normal!");
									else
										sb.append(pny+" turned to stone!");
									break;
								case CONFUSED:
									if(cure)
										sb.append(pny+" snapped out of its confusion!");
									else
										sb.append(pny+" became confused!");
									break;
							}
						} else {
							sb.append(phrase);
						}
						break;
					}
					case EMPHASIZED:
						/* EMPHASIZED, event */
						sb.append(event);
						break;
					case TURN:
						/* TURN, turn */
						sb.append("--- <font color=#222222 size=5><b>TURN "+event+"</b></font> ---");
						break;
					case ERROR:
						/* ERROR, msg */
						if(event == null)
							sb.append("<font color=\"gray\">[error] An error has occurred</font>");
						else
							sb.append("<font color=\"gray\">[error] "+event+"</font>");
						break;
					case INFO:
						/* INFO, msg */
						sb.append("<font color=\"gray\">[info] "+event+"</font>");
						break;
					case CRITICAL:
						/* CRITICAL, event */
						sb.append("<font color=#CC2200 size=4><b>"+event+"</b></font>");
						break;
					default:
						sb.append(event);
				}
				
				eventP.append(sb.toString(), false);
			}
		});
	}

	private String parseDamageEvent(Pony pony,int dam,String phrase) {
		if(pony != allyPony && pony != oppPony) throw new RuntimeException("Pony "+pony+" is neither ally nor opponent!");

		if(phrase == null) {
			if(dam < 0) {
				return pony.getNickname() + " regained health!";
			} else {
				return (pony == oppPony ? "Enemy " : "") + pony.getNickname() + " lost "+
					(Battle.SHOW_HP_PERC ? 
						Math.min(100,pony.calculateDamagePerc(dam)) + "% of its" : 
						dam) + " HP!";
			}
		}

		return phrase.replaceAll("\\[pony\\]", pony.getNickname());
		/*
		if(name.startsWith("weather/")) {
			Weather weather = Weather.forName(name.replaceFirst("weather\\/",""));
			if(weather != null) 
				return weather.getPhrase().replaceAll("\\[pony\\]",
						(pony == oppPony ? "Enemy "+pony.getNickname() : pony.getNickname()));
			else throw new RuntimeException("Weather not found: "+name);
		}

		EffectDealer dealer = null;

		try {
			dealer = MoveCreator.create(name);
		} catch(ReflectiveOperationException e) {
			if(Debug.pedantic) printDebug("Exception: "+e);
			if(Debug.on) printDebug(name+" is not a Move; trying Ability.");
			try {
				dealer = AbilityCreator.create(name);
			} catch(ReflectiveOperationException ee) {
				if(Debug.pedantic) printDebug("Exception: "+ee);
				if(Debug.on) printDebug(name+" is not an Ability; trying Item.");
				try {
					dealer = ItemCreator.create(name);
				} catch(ReflectiveOperationException eee) {
					if(Debug.pedantic) printDebug("Exception: "+eee);
					printDebug(name+" is not an Item. Giving up.");
					throw new RuntimeException("dealer not found.");
				}
			}
		}

		if(dealer.getPhrase() != null) 
			return dealer.getPhrase().replaceAll("\\[pony\\]",(pony == oppPony ? "Enemy "+pony.getNickname() : pony.getNickname()));
		else {
			if(pony == allyPony)
				return pony.getNickname() + (dam < 0 ? " healed " : " lost ") + 
						Math.min(100,pony.calculateDamagePerc(dam)) + "% of its HP!";
			else if(pony == oppPony)
				return "Enemy " + pony.getNickname() + (dam < 0 ? " healed " : " lost ") +
						Math.min(100,pony.calculateDamagePerc(dam)) + "% of its HP!";
		}

		// should never happen.
		throw new RuntimeException("[BP.parseDamageEvent()]: pony is neither ally nor opponent!");
		*/
	}

	/** If connection is valid, send messages to the server; else, output on console */
	private void sendB(String msg) {
		if(connection == null) {
			printDebug("[BattlePanel.sendB]: "+msg);
			return;
		}
		if(Debug.on) printDebug("[BattlePanel] sending msg to server: "+msg);
		connection.sendMsg(BTL_PREFIX+battleID+" "+msg);
	}
	
	/** This method ensures a graceful end when a player wins. */
	private void runWinEvent(String str) {
		if(Debug.on) printDebug("[BP] running win event with argument "+str);

		if(str.equals("ally")) {
			appendEvent(EventType.BATTLE,p1.getName() + " won the battle!");
		} else if(str.equals("opp")) {
			appendEvent(EventType.BATTLE,p2.getName() + " won the battle!");
		} else {
			printDebug("[runWinEvent] unknown argument: "+str);
		}
		
		terminate();
	}

	public void sendForfeitMsg() {
		sendB("|forfeit|"+(playerID == 0 ? p1.getName() : playerID));
	}

	public void terminate() {
		if(looper != null) {
			if(Debug.on) printDebug("[BP] Stopping looper...");
			looper.stop();
		}
		if(moveP != null)
			moveP.setVisible(false);
		if(teamP != null)
			teamP.setVisible(false);
		moveP = null;
		teamP = null;
		printMsg("Battle #"+battleID+" terminated.");
	}

	@SuppressWarnings("unchecked")
	private BasicAnimation createAnimation(Map<String,Object> opts,final String side,final boolean avoid) {
		if(!opts.containsKey("name") || !opts.containsKey("sprite")) return null;
		if(!(side.equals("ally") || side.equals("opp"))) {
			printDebug("[createAnimation] error: side is "+side);
			return null;
		}

		String animType = (String)opts.remove("name");
		BasicAnimation anim = null;
		JLabel animSprite = null;

		if(opts.get("sprite").equals("user") && side.equals("ally") || opts.get("sprite").equals("target") && side.equals("opp")) {
			opts.put("sprite",allySprite);
			if(allySprite != null)
				fieldP.setLayer(allySprite,MOVE_LAYER);
		} else if(opts.get("sprite").equals("target") && side.equals("ally") || opts.get("sprite").equals("user") && side.equals("opp")) {
			opts.put("sprite",oppSprite);
			fieldP.setLayer(oppSprite,MOVE_LAYER);
		} else {
			try {
				int width = HAZARD_TOKEN_SIZE;
				int height = -1;
				if(opts.containsKey("width")) {
					try {
						width = (int)opts.get("width");
					} catch(ClassCastException ee) {
						printDebug("[createAnimation] invalid width: "+opts.get("width"));
					} finally {
						if(width < 0) width = HAZARD_TOKEN_SIZE;
						opts.remove("width");
					}
				}
				if(opts.containsKey("height")) {
					try {
						height = (int)opts.get("height");
					} catch(ClassCastException ee) {
						printDebug("[createAnimation] invalid height: "+opts.get("height"));
					} finally {
						opts.remove("height");
					}
				}
				animSprite = new JLabel(
							new ImageIcon(
								ImageIO.read(
									getClass().getResource(Meta.complete2(Meta.TOKEN_DIR)+
									Meta.DIRSEP+"moves"+Meta.DIRSEP+"fx"+
									Meta.DIRSEP+(String)opts.get("sprite"))
								).getScaledInstance(width, height, Image.SCALE_SMOOTH)
							)
						);
				if(opts.get("transparent") != null && (Boolean)opts.get("transparent")) {
					animSprite = new TransparentLabel(animSprite);
				}
				if(animSprite == null) {
					printDebug("[createAnimation] Failed to load animSprite "+(String)opts.get("sprite"));
					return null;
				}
				final JLabel _animSprite = animSprite;
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						_animSprite.setBounds(
							side.equals("ally") ? allySprite.getX() /*+ allySprite.getIcon().getIconWidth()/2*/ : 
								oppSprite.getX() /*+ oppSprite.getIcon().getIconWidth()/2*/,
							side.equals("ally") ? allySprite.getY() /*+ allySprite.getIcon().getIconHeight()/2*/ :
								oppSprite.getY() /*+ oppSprite.getIcon().getIconHeight()/2*/,
							_animSprite.getIcon().getIconWidth(),
							_animSprite.getIcon().getIconHeight()
						);
						//animSprite.setVisible(true);
						fieldP.add(_animSprite,MOVE_LAYER);
						fieldP.validate();
						fieldP.repaint();
						if(Debug.pedantic) printDebug("[createAnimation] created sprite "+_animSprite+
									"\nbounds="+_animSprite.getBounds()+"\nicon="+_animSprite.getIcon());
					}
				});
				opts.put("sprite",animSprite);

			} catch(Exception e) {
				printDebug("[createAnimation("+opts.get("name")+")] error while loading sprite: "+e);
				if(animSprite != null) {
					animSprite.setVisible(false);
					fieldP.remove(animSprite);
				}
				return null;
			}
		}

		if(allySprite != null)
			opts.put("allyBounds",allySprite.getBounds());
		if(oppSprite != null)
			opts.put("oppBounds",oppSprite.getBounds());
		if(side.equals("opp"))
			opts.put("usedByAlly",false);
		if(avoid)
			opts.put("avoided",true);

		if(Debug.pedantic) printDebug("[createAnimation] opts = "+opts);

		try {
			Class<? extends BasicAnimation> animBuilder = (Class<? extends BasicAnimation>)Class.forName(
									(Meta.POKEPON_ROOTDIR+Meta.DIRSEP+Meta.ANIMATION_DIR+Meta.DIRSEP+
									animType.replaceAll(" ","")).replaceAll(""+Meta.DIRSEP,".")
									);
			anim = animBuilder.getConstructor(JComponent.class,Map.class).newInstance(fieldP,opts);
		} catch(ReflectiveOperationException e) {
			printDebug("Exception while creating animation "+animType+": "+e);
			printDebug("Caused by: "+e.getCause());
			if(animSprite != null && animSprite != allySprite && animSprite != oppSprite) {
				animSprite.setVisible(false);
				fieldP.remove(animSprite);
			}
			return null;
		}

		if(Debug.on) printDebug("[createAnimation("+opts.get("name")+")] anim = "+anim);
		return anim;
	}

	private void switchInAnim(TransparentLabel labl,boolean ally) {
		if(ally) {
			setAllyBounds(labl);
			// shift sprite left
			labl.setLocation(labl.getX() - 50,labl.getY());
		} else {
			setOpponentBounds(labl);
			// shift sprite right
			labl.setLocation(labl.getX() + 50,labl.getY());
		}
		if(Debug.on) printDebug("[switchInAnim] labl.location = "+labl.getLocation());
		try {
			fieldP.add(labl,PONY_LAYER);
		} catch(IllegalArgumentException e) {
			printDebug("[switchInAnim] "+e);
			e.printStackTrace();
			printDebug("labl bounds = "+labl.getBounds());
		}
		labl.addMouseListener(new PonySpriteListener(ally));

		if(labl != null && labl.getIcon() != null && labl.getIcon().getIconHeight() > 0) {
			Map<String,Object> opts = new HashMap<>();
			opts.put("sprite",labl);
			opts.put("finalPoint",new Point((ally ? labl.getX() + 50 : labl.getX() - 50), labl.getY()));
			opts.put("fadeOut",false);
			opts.put("persistent",true);
			Animation anim = new Fade(fieldP,opts);
			if(Debug.on) printDebug("Starting switch-in animation");	
			anim.start();
			synchronized(anim) {
				try {
					anim.wait();
				} catch(InterruptedException e) {
					printDebug("Animation interrupted.");
				}
			}
		}
	}

	private void switchOutAnim(TransparentLabel labl,boolean ally) {
		if(labl != null && labl.getIcon() != null && labl.getIcon().getIconHeight() > 0) {
			Map<String,Object> opts = new HashMap<>();
			opts.put("sprite",labl);
			opts.put("finalPoint",new Point((ally ? labl.getX() - 50 : labl.getX() + 50), labl.getY()));
			opts.put("fadeOut",true);
			Animation anim = new Fade(fieldP,opts);
			if(Debug.on) printDebug("Starting switch-out animation");	
			anim.start();
			synchronized(anim) {
				try {
					anim.wait();
				} catch(InterruptedException e) {
					printDebug("Animation interrupted.");
				}
			}
		}
	}
	
	private void faintAnim(TransparentLabel labl) {
		if(labl != null && labl.getIcon() != null && labl.getIcon().getIconHeight() > 0) {
			Map<String,Object> opts = new HashMap<>();
			opts.put("sprite",labl);
			opts.put("finalPoint",new Point(labl.getX(),labl.getY()+50));
			opts.put("fadeOut",true);
			Animation anim = new Fade(fieldP,opts);
			if(Debug.on) printDebug("Starting faint animation");	
			anim.start();
			synchronized(anim) {
				try {
					anim.wait();
				} catch(InterruptedException e) {
					printDebug("Animation interrupted.");
				}
			}
		}
	}

	private void resultAnim(Point location, String message) {
		resultAnim(location, message, ResultType.NEUTRAL);
	}

	private void resultAnim(Point location, String message, ResultType type) {
		switch(type) {
			case GOOD:
				resultAnim(location, message, new Color(0x00CC00));
				break;
			case BAD:
				resultAnim(location, message, new Color(0xCC0000));
				break;
			default:
				resultAnim(location, message, new Color(0x999999));
		}
	}

	/* TODO: consider restructuring this method: this may create the animation and,
	 * instead of starting it, enqueue it in some queue. Then, a separate thread
	 * may read that queue and execute all the animations with a fixed delay between
	 * each task. For now, this works fine if 2 animations overlap, but 3 or more
	 * simultaneous ones would behave badly.
	 */
	private void resultAnim(Point location, String message, Color color) {
		if(resultAnimWait) {
			synchronized(resultAnimSemaphore) {
				try {
					resultAnimSemaphore.wait(RESULT_ANIM_DELAY);
				} catch(InterruptedException ignore) {}
			}
		}
		JLabel label = new TransparentLabel(new JLabel(message),1f);
		label.setOpaque(true);
		label.setHorizontalAlignment(JLabel.CENTER);
		label.setBackground(color);
		label.setForeground(Color.WHITE);
		label.setBounds((int)location.getX(),(int)location.getY(),label.getText().length()*9,20);
		fieldP.add(label,MESSAGE_LAYER);
		Map<String,Object> opts = new HashMap<>();
		opts.put("sprite",label);
		opts.put("fadeOut",true);
		opts.put("accelerated",true);
		opts.put("finalPoint",new Point(label.getX(),label.getY()-30));
		opts.put("delay",65);
		Animation anim = new Fade(fieldP,opts);
		resultAnimWait = true;
		anim.start();
		// prevent more animations to be run before RESULT_ANIM_DELAY ms.
		resultAnimExec.submit(new Runnable() {
			public void run() {
				try {
					Thread.sleep(RESULT_ANIM_DELAY);
				} catch(InterruptedException e) {
					printDebug("[resultAnimExec] interrupted.");
				}
				resultAnimWait = false;
				synchronized(resultAnimSemaphore) {
					resultAnimSemaphore.notifyAll();
				}
			}
		});
		// and don't bother waiting
	}

	private void setSubstitute(boolean ally) {
		Map<String,Object> opts = new HashMap<>();
		if(ally) {
			if(allyPony.hasSubstitute()) {
				appendEvent(EventType.ERROR,"Ally pony already has substitute.");
				return;
			}
			// Fade out the pony sprite
			opts.put("sprite",allySprite);
			if(allySprite != null)
				opts.put("allyBounds",allySprite.getBounds());
			if(oppSprite != null)
				opts.put("oppBounds",oppSprite.getBounds());
			opts.put("finalPoint","ally -30X +15Y");
			opts.put("initialOpacity",1f);
			opts.put("finalOpacity",0.4f);
			opts.put("persistent",true);
			Animation anim = new Fade(fieldP,opts);
			anim.start();
			try {
				synchronized(anim) {
					anim.wait();
				}
			} catch(InterruptedException ignore) {}
			// Fade in the substitute sprite
			try {
				JLabel animSprite = new JLabel(
						new ImageIcon(
							ImageIO.read(
								//new URL(/*Meta.getTokensURL().getProtocol()+*/
									//"file://"+Meta.getTokensURL().getPath()+
									getClass().getResource(Meta.complete2(Meta.TOKEN_DIR)+
									Meta.DIRSEP+"substitute_back.png")
								//)
							)
						)
					);
				allySubstitute = new TransparentLabel(animSprite);
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						allySubstitute.setBounds(
								allySprite.getX(), 
								allySprite.getY(),
								allySubstitute.getIcon().getIconWidth(),
								allySubstitute.getIcon().getIconHeight()
								);
						//animSprite.setVisible(true);
						fieldP.add(allySubstitute,new Integer(PONY_LAYER - 1));
						fieldP.validate();
						fieldP.repaint();
						if(Debug.on) printDebug("[createAnimation] created sprite "+allySubstitute+
									"\nbounds="+allySubstitute.getBounds()+"\nicon="+allySubstitute.getIcon());
					}
				});
				opts.put("sprite",allySubstitute);
			} catch(IOException|InvocationTargetException e) {
				printDebug("[BP.interpret(substitute)] Failed to load sprite:");
				e.printStackTrace();
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			opts.put("fadeOut",false);
			Point fPt = allyLocation();
			fPt.setLocation(fPt.getX() + 40,fPt.getY() - 20);
			opts.put("finalPoint",fPt);
			opts.put("persistent",true);
			anim = new Fade(fieldP,opts);
			anim.start();
			try {
				synchronized(anim) {
					anim.wait();
				}
			} catch(InterruptedException ignore) {}
			allyPony.setSubstitute(true);
		} else {
			if(oppPony.hasSubstitute()) {
				appendEvent(EventType.ERROR,"Opponent pony already has substitute.");
				return;
			}
			// Fade out the pony sprite
			opts.put("sprite",oppSprite);
			if(allySprite != null)
				opts.put("allyBounds",allySprite.getBounds());
			if(oppSprite != null)
				opts.put("oppBounds",oppSprite.getBounds());
			opts.put("finalPoint","opp +30X -15Y");
			opts.put("initialOpacity",1f);
			opts.put("finalOpacity",0.4f);
			opts.put("persistent",true);
			Animation anim = new Fade(fieldP,opts);
			anim.start();
			try {
				synchronized(anim) {
					anim.wait();
				}
			} catch(InterruptedException ignore) {}
			// Fade in the substitute sprite
			try {
				JLabel animSprite = new JLabel(
						new ImageIcon(
							ImageIO.read(
								//new URL(/*Meta.getTokensURL().getProtocol()+*/
								//	"file://"+Meta.getTokensURL().getPath()+
									getClass().getResource(Meta.complete2(Meta.TOKEN_DIR)+
									Meta.DIRSEP+"substitute_front.png")
								//)
							)
						)
					);
				oppSubstitute = new TransparentLabel(animSprite);
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						oppSubstitute.setBounds(
								oppSprite.getX(),
								oppSprite.getY(),
								oppSubstitute.getIcon().getIconWidth(),
								oppSubstitute.getIcon().getIconHeight()
								);
						//animSprite.setVisible(true);
						fieldP.add(oppSubstitute,MOVE_LAYER);
						fieldP.validate();
						fieldP.repaint();
						if(Debug.on) printDebug("[createAnimation] created sprite "+oppSubstitute+
									"\nbounds="+oppSubstitute.getBounds()+"\nicon="+oppSubstitute.getIcon());
					}
				});
				opts.put("sprite",oppSubstitute);
			} catch(IOException|InvocationTargetException e) {
				printDebug("[BP.interpret(substitute)] Failed to load sprite:");
				e.printStackTrace();
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			opts.put("fadeOut",false);
			Point fPt = oppLocation();
			fPt.setLocation(fPt.getX() - 40, fPt.getY() + 20);
			opts.put("finalPoint",fPt);
			opts.put("persistent",true);
			anim = new Fade(fieldP,opts);
			anim.start();
			try {
				synchronized(anim) {
					anim.wait();
				}
			} catch(InterruptedException ignore) {}
			oppPony.setSubstitute(true);
		}
	}

	/** @param noanim If true, the substitute will be simply deleted, without any animation (this
	 * means that the pony behind the Substitute will stay in that position); only used immediately 
	 * before a |fainted|PonyName to ensure the Substitute sprite gets removed.
	 */
	private void removeSubstitute(boolean ally, boolean noanim) {
		if(ally) {
			if(allyPony == null || !allyPony.hasSubstitute() || allySubstitute == null) {
				printDebug("[BP.interpret(rmsubstitute)] Ally pony is not under substitute!");
				return;
			}
			// no animation
			if(noanim) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						allySubstitute.setVisible(false);
						fieldP.remove(allySubstitute);
						allyPony.setSubstitute(false);
						allySubstitute = null;
					}
				});
				return;
			}
			Map<String,Object> opts = new HashMap<>();
			// Fade out substitute
			opts.put("sprite",allySubstitute);
			if(allySprite != null)
				opts.put("allyBounds",allySprite.getBounds());
			if(oppSprite != null)
				opts.put("oppBounds",oppSprite.getBounds());
			opts.put("finalPoint","ally");
			opts.put("fadeOut",true);
			Animation anim = new Fade(fieldP,opts);
			anim.start();
			resultAnim(allyLocation(),"Faded!");
			try {
				synchronized(anim) {
					anim.wait();
				}
			} catch(InterruptedException ignore) {}
			// Fade in ally sprite
			opts.remove("fadeOut");
			opts.put("sprite",allySprite);
			opts.put("finalPoint",allyLocation());
			opts.put("initialOpacity",0.4f);
			opts.put("finalOpacity",1f);
			opts.put("persistent",true);
			anim = new Fade(fieldP,opts);
			anim.start();
			try {
				synchronized(anim) {
					anim.wait();
				}
			} catch(InterruptedException ignore) {}
			allyPony.setSubstitute(false);
			allySubstitute = null;
		} else {
			if(oppPony == null || !oppPony.hasSubstitute() || oppSubstitute == null) {
				printDebug("[BP.interpret(rmsubstitute)] Opponent pony is not under substitute!");
				return;
			}
			if(noanim) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						oppSubstitute.setVisible(false);
						fieldP.remove(oppSubstitute);
						oppPony.setSubstitute(false);
						oppSubstitute = null;
					}
				});
				return;
			}
			Map<String,Object> opts = new HashMap<>();
			// Fade out substitute
			opts.put("sprite",oppSubstitute);
			if(allySprite != null)
				opts.put("allyBounds",allySprite.getBounds());
			if(oppSprite != null)
				opts.put("oppBounds",oppSprite.getBounds());
			opts.put("finalPoint","opp");
			opts.put("fadeOut",true);
			Animation anim = new Fade(fieldP,opts);
			anim.start();
			resultAnim(oppLocation(),"Faded!");
			try {
				synchronized(anim) {
					anim.wait();
				}
			} catch(InterruptedException ignore) {}
			// Fade in ally sprite
			opts.remove("fadeOut");
			opts.put("sprite",oppSprite);
			opts.put("finalPoint",oppLocation());
			opts.put("initialOpacity",0.4f);
			opts.put("finalOpacity",1f);
			opts.put("persistent",true);
			anim = new Fade(fieldP,opts);
			anim.start();
			try {
				synchronized(anim) {
					anim.wait();
				}
			} catch(InterruptedException ignore) {}
			oppPony.setSubstitute(false);
			oppSubstitute = null;
		}
	}

	/** This method adds a persistent effect on a side of the field: this means both
	 * adding the [name, sprite] entry in the map persEffectsSprite and creating and
	 * dispatching the animation for the persistent effect.
	 */
	// FIXME
	private void setPersistentEffect(final boolean ally, final String persName) {
		// disallow duplicate persistent effects on the same side
		if(persEffectsSprite.get(ally? 0 : 1).containsKey(persName)) {
			printDebug("[setPersistentEffect] WARNING: tried to add "+persName+" twice!");
			return;
		}
		String root = Meta.getTokensURL().getPath() + Meta.DIRSEP + "persistent" + Meta.DIRSEP;
		Map<String,Object> opts = new HashMap<>();

		// set animation options
		opts.put("transparent",true);
		opts.put("persistent",true);
		opts.put("initialOpacity", 0f);
		opts.put("finalOpacity", 0.55f);

		if(allySprite != null)
			opts.put("allyBounds",allySprite.getBounds());
		if(oppSprite != null)
			opts.put("oppBounds",oppSprite.getBounds());

		if(	persName.equals("Entrench") ||
			persName.equals("Sonic Barrier")
		) {
			if(ally) {
				int offsetx = 100 + persEffectsSprite.get(0).size() * 15;
				int offsety = 20 + persEffectsSprite.get(0).size() * 10;
				opts.put("initialPoint","ally +" + offsetx + "X +40Y");
				opts.put("finalPoint","ally +" + offsetx + "X -" + offsety + "Y");
			} else {
				int offsetx = 100 + persEffectsSprite.get(1).size() * 15;
				int offsety = 40 + persEffectsSprite.get(1).size() * 10;
				opts.put("initialPoint","opp -" + offsetx + "X +80Y");
				opts.put("finalPoint","opp -" + offsetx + "X +" + offsety + "Y");
			}
		}

		try {
			String path = root+persName.toLowerCase().replaceAll(" ","_")+"_"+(ally ? "ally" : "opp")+".png";
			// FIXME: is there a less ugly way to do this?
			if(!new File(path).isFile()) {
				path = root+persName.toLowerCase().replaceAll(" ","_")+".png";
				if(!new File(path).isFile()) {
					path = root+persName.toLowerCase().replaceAll(" ","_")+"_"+(ally ? "ally" : "opp")+".gif";
					if(!new File(path).isFile()) 
						path = root+persName.toLowerCase().replaceAll(" ","_")+".gif";
				}
			}
					
			if(Debug.on) printDebug("[setPersistentEffect] loading "+path+"...");
			// we suppose the persistent sprites are already the correct size, because resizing them would
			// un-animate the gifs.
			final TransparentLabel sprite = new TransparentLabel(new JLabel(new ImageIcon(new URL("file://"+path))));
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						sprite.setBounds(
								sprite.getX(),
								sprite.getY(),
								sprite.getIcon().getIconWidth(),
								sprite.getIcon().getIconHeight()
								);
						fieldP.add(sprite,MOVE_LAYER);
						fieldP.validate();
						fieldP.repaint();
					}
				});
			} catch(InterruptedException|InvocationTargetException ee) {
				ee.printStackTrace();
			}
			persEffectsSprite.get(ally ? 0 : 1).put(persName,sprite);
			opts.put("sprite",sprite);
			Animation anim = new Fade(fieldP,opts);	
			anim.start();
			try {
				synchronized(anim) {
					anim.wait();
				}
			} catch(InterruptedException ignore) {}
		} catch(IOException e) {
			printDebug("[setPersistentEffect] failed to create sprite:" +e);
		}
	}

	private boolean removePersistentEffect(final boolean ally, final String persName) {
		if(!persEffectsSprite.get(ally ? 0 : 1).containsKey(persName)) {
			printDebug("[removePersistentEffect] Warning: tried to remove non-existent PE: "+persName);
			return false;
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JLabel sprite = persEffectsSprite.get(ally ? 0 : 1).remove(persName);
				sprite.setVisible(false);
				fieldP.remove(sprite);
			}
		});
		return true;
	}
}
