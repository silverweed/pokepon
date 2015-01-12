//: gui/TeamPanel.java

package pokepon.gui;

import pokepon.pony.*;
import pokepon.move.*;
import pokepon.util.*;
import pokepon.player.Team;
import static pokepon.util.MessageManager.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.imageio.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

/** This class handles the bottom team panel used in BattlePanel and in GUITeamBuilder. 
 *
 * @author Giacomo Parolini
 */
public class TeamPanel extends JPanel {

	public static enum TokenStyle { TEXT_AND_IMAGE, NAME_AND_IMAGE, LEVEL_AND_IMAGE, ONLY_IMAGE, ONLY_TEXT, ONLY_NAME };

	private TokenStyle tokenStyle = TokenStyle.LEVEL_AND_IMAGE;
	private URL emptyTokenURL;
	private ButtonGroup bGroup = new ButtonGroup();
	private JToggleButton[] token = new JToggleButton[Team.MAX_TEAM_SIZE];
	private ImageIcon[] icon = new ImageIcon[Team.MAX_TEAM_SIZE];
	private Pony[] pony = new Pony[Team.MAX_TEAM_SIZE];
	private int selectedIndex = 0;

	public TeamPanel() {
		this(false);
	}

	public TeamPanel(boolean setToolTips) {
		setLayout(new GridLayout(1,Team.MAX_TEAM_SIZE,10,1));
		setBorder(new CompoundBorder(new TitledBorder("Team"),new EmptyBorder(3,3,3,3)));

		emptyTokenURL = getClass().getResource("/"+Meta.complete(Meta.TOKEN_DIR)+"/empty_token_icon_left_small.png");

		for(int i = 0; i < Team.MAX_TEAM_SIZE; ++i) {
			token[i] = new JToggleButton();
			token[i].addMouseListener(new HoverListener(token[i]));
			bGroup.add(token[i]);
			try {
				if(Debug.on) printDebug("empty token image url="+emptyTokenURL);
				Image img = ImageIO.read(emptyTokenURL);
				token[i].setIcon(new ImageIcon(img));
			} catch(IOException e) {
				printDebug("[TeamPanel] Caught IOException while loading empty token image: "+e);
				token[i].setText("Empty");
			}
			if(setToolTips)
				token[i].addMouseListener(new TeamMouseListener(i));
			add(token[i]);
		}
		token[0].setSelected(true);
		setVisible(true);
	}

	public Pony getPony(int i) {
		return pony[i];
	}

	public void setPony(int num,Pony _pony) {
		if(Debug.on) printDebug("Called TeamPanel.setPony("+num+","+(_pony == null ? "null" : _pony.getName())+")");
		pony[num] = _pony;
		if(_pony != null) {
			if(/*_pony.getToken() != null && */tokenStyle != TokenStyle.ONLY_NAME && tokenStyle != TokenStyle.ONLY_TEXT) {
				try {
					Image img = ImageIO.read(_pony.getFrontSprite());
					Image newimg = img.getScaledInstance(35,-1,Image.SCALE_SMOOTH);
					token[num].setIcon(new ImageIcon(newimg));
					switch(tokenStyle) {
						case ONLY_IMAGE:
							token[num].setText("");
							break;
						case TEXT_AND_IMAGE:
							token[num].setText(""+_pony);
							break;
						case NAME_AND_IMAGE:
							token[num].setText(""+_pony.getName());
							break;
						case LEVEL_AND_IMAGE:
							token[num].setText(""+_pony.getLevel());
							break;
					}

				} catch(Exception e) {
					printDebug("Caught Exception while loading "+_pony.getName()+"'s token: "+e);
					token[num].setIcon(null);
					switch(tokenStyle) {
						case ONLY_IMAGE:
							token[num].setText(_pony.getName().substring(0,3));
							break;
						case TEXT_AND_IMAGE:
							token[num].setText(_pony+"");
							break;
						case NAME_AND_IMAGE:
							token[num].setText(_pony.getName());
							break;
						case LEVEL_AND_IMAGE:
							token[num].setText(_pony.getName().substring(0,3)+" "+_pony.getLevel());
							break;
					}
				}
			} else {
				token[num].setIcon(null);
				switch(tokenStyle) {
					case ONLY_TEXT:
						token[num].setText(_pony.toString());
						break;
					case ONLY_NAME:
						token[num].setText(_pony.getName());
						break;
					default:
						token[num].setText("");
						break;
				}
			}
		} else {
			if(emptyTokenURL != null && tokenStyle != TokenStyle.ONLY_NAME && tokenStyle != TokenStyle.ONLY_TEXT) {
				try {
					Image img = ImageIO.read(emptyTokenURL);
					token[num].setIcon(new ImageIcon(img));
					token[num].setText("");
				} catch(Exception e) {
					printDebug("[TeamPanel] Caught Exception while loading empty token image: "+e);
					token[num].setIcon(null);
					token[num].setText("Empty");
				}
			} else {
				token[num].setIcon(null);
				token[num].setText("Empty");
			}
		}
		repaint();
	}

	public void addPony(Pony _pony) {
		for(int i = 0; i < Team.MAX_TEAM_SIZE; ++i) {
			if(pony[i] == null) {
				setPony(i,_pony);
				return;
			}
		}
	}

	public int getSelectedIndex() {
		return selectedIndex;
	}
	
	public void clear() {
		for(int i = 0; i < Team.MAX_TEAM_SIZE; ++i)
			setPony(i,null);
	}

	public JToggleButton[] getTokens() {
		return token;
	}

	public JToggleButton getToken(int num) {
		return token[num];
	}

	public void setPreferredTokenSize(Dimension d) {
		for(int i = 0; i < Team.MAX_TEAM_SIZE; ++i) 
			token[i].setPreferredSize(d);
	}

	public void setTokenStyle(TeamPanel.TokenStyle style) {
		if(Debug.on) printDebug("TokenStyle set to "+style);
		tokenStyle = style;
	}

	public void setSelectedIndex(final int index) {
		selectedIndex = index;
		token[index].setSelected(true);
	}

	/** Method to be called after a setTokenStyle() to make the changes
	 * effective.
	 */
	public void restyle() {
		for(int i = 0; i < Team.MAX_TEAM_SIZE; ++i)
			setPony(i,pony[i]);
	}

	private class TeamMouseListener extends CustomToolTipListener {
		private final int num;
		
		public TeamMouseListener(final int num) {
			super(TeamPanel.this);
			this.num = num;
		}

		@Override
		protected void setText() {
			if(pony[num] == null) return;
			String theMoves = "<ul type='disc'>";
			for(Move m : pony[num].getMoves()) 
				theMoves += "<li>" + m.getName() + (m.getName().equals("Hidden Talent")
					? " " + m.getType()
					: ""
				) + " <small>("+m.getPP()+"/"+m.getMaxPP()+")</small></li>";
			theMoves += "</ul>";
			text = "<html><b>"+pony[num]+"</b><br>"+
				pony[num].getTypingHTMLTokens()+"<br>"+
				"HP: "+pony[num].getHp()+"<br>"+
				(pony[num].getAbility() != null ? "Ability: "+pony[num].getAbility()+"<br>" : "")+
				(pony[num].getItem() != null ? "Item: "+pony[num].getItem()+"<br>" : "")+
				"Status: "+ 
				(pony[num].isKO() ? "KO" :
				pony[num].isBurned() ? "brn" :
				pony[num].isAsleep() ? "slp" :
				pony[num].isParalyzed() ? "par" :
				pony[num].isIntoxicated() ? "tox" :
				pony[num].isPoisoned() ? "psn" :
				pony[num].isPetrified() ? "ptr" : "ok")+
				theMoves+
				"</html>";
		}

		@Override
		protected boolean showCondition() {
			return token[num] != null;
		}

		// Show tooltip in fixed position, not where the cursor enters the button 
		@Override
		protected void showToolTip(MouseEvent e) {
			setText();
			toolTip.setTipText(text);
			int linecount = (text.length() - text.replaceAll("<br>", "").length()) / 4;
			int x = (int)token[num].getLocationOnScreen().getX();
			int y = (int)token[num].getLocationOnScreen().getY() - 170; //- linecount*40;
			popup = popupFactory.getPopup(component,toolTip,x,y);
			popup.show();
		}
	}
}
