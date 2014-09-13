//: gui/TeamMenuPanel.java
package pokepon.gui;

import pokepon.pony.*;
import pokepon.util.*;
import pokepon.player.*;
import static pokepon.util.MessageManager.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;
import javax.imageio.*;
import java.io.*;
import javax.swing.border.*;

/** Class handling the little team displayed on the side bars of the BattlePanel
 *
 * @author Giacomo Parolini
 */
class TeamMenuPanel extends JPanel {

	private static URL EMPTY_TOKEN_URL = TeamMenuPanel.class.getResource(Meta.complete2(Meta.TOKEN_DIR)+"/empty_token_icon_small.png");
	private static URL UNKNOWN_PONY_URL = TeamMenuPanel.class.getResource(Meta.complete2(Meta.TOKEN_DIR)+"/empty_token_icon_small.png");

	private Player player;
	private PonyToken[] token = new PonyToken[Team.MAX_TEAM_SIZE];

	class PonyToken extends JLabel {
		private Pony pony;
		private boolean unknown;
		private boolean fainted;

		public PonyToken(Pony pony) {
			this(pony, false, false);
		}
		public PonyToken(Pony pony, boolean unknown) {
			this(pony, unknown, false);
		}
		public PonyToken(Pony pony, boolean unknown, boolean fainted) {
			set(pony, unknown, fainted);
			addMouseListener(teamMenuPonyListener);
			if(Debug.on) printDebug("Constructed PonyToken("+pony+", "+unknown+", "+fainted+")");
		}

		public void set(Pony pony, boolean unknown, boolean fainted) {
			this.pony = pony;
			this.unknown = unknown;
			this.fainted = fainted;
			updateIcon();
		}
			
		private void updateIcon() {
			if(Debug.on) printDebug("[TMP] Called updateIcon("+pony+") unknown: "+unknown+", fainted: "+fainted);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					Image img = null;
					try {
						if(unknown) {
							img = ImageIO.read(UNKNOWN_PONY_URL)
									.getScaledInstance(BattlePanel.TEAM_SPRITE_SIZE, -1, Image.SCALE_SMOOTH);
							if(Debug.on) printDebug("[TMP] Loaded unknown_pony icon");
						} else if(pony == null || pony.getFrontSprite() == null) {
							img = ImageIO.read(EMPTY_TOKEN_URL)
									.getScaledInstance(BattlePanel.TEAM_SPRITE_SIZE, -1, Image.SCALE_SMOOTH);
							if(Debug.on) printDebug("[TMP] Loaded empty_token icon");
						} else {
							img = ImageIO.read(pony.getFrontSprite())
								.getScaledInstance(BattlePanel.TEAM_SPRITE_SIZE, -1, Image.SCALE_SMOOTH);
							if(Debug.on) printDebug("[TMP] Loaded regular pony icon");
							if(fainted)
								img = GrayFilter.createDisabledImage(img);
						}
						if(img == null)
							setIcon(new ImageIcon(ImageIO.read(UNKNOWN_PONY_URL)
								.getScaledInstance(BattlePanel.TEAM_SPRITE_SIZE,-1,Image.SCALE_SMOOTH)));
						else
							setIcon(new ImageIcon(img));
					} catch(IOException e) {
						printDebug("[TeamMenuPanel.PonyToken]: "+e);
						try {
							if(Debug.on) printDebug("[TMP] Reading icon from "+UNKNOWN_PONY_URL+"...");
							setIcon(new ImageIcon(ImageIO.read(UNKNOWN_PONY_URL)
								.getScaledInstance(BattlePanel.TEAM_SPRITE_SIZE,-1,Image.SCALE_SMOOTH)));
						} catch(IOException ee) {
							printDebug("[TeamMenuPanel.PonyToken]: Failed to read placeholder icon:");
							ee.printStackTrace();
						}
					}
					validate();
					repaint();
				}
			});
		}

		public Pony getPony() { return pony; }
		public boolean isFainted() { return fainted; }
		public boolean isUnknown() { return unknown; }

		public void setFainted(boolean fnt) { 
			fainted = fnt;
			updateIcon();
		}

		public void setPony(Pony pny) { 
			pony = pny;
			updateIcon();
		}

		public void setUnknown(boolean unk) { 
			unknown = unk;
			updateIcon();
		}

		private CustomToolTipListener teamMenuPonyListener = new CustomToolTipListener(this) {
			@Override
			protected void setText() {
				if(pony == null) return;
				text = pony.getName();
				if(pony.getStatus().size() > 0 || pony.hp() < pony.maxhp()) {
					text += "("+(int)(pony.hp() * 100 / pony.maxhp())+"%";
					if(pony.isFainted()) text += "|ko";
					else if(pony.isParalyzed()) text += "|par";
					else if(pony.isAsleep()) text += "|slp";
					else if(pony.isPetrified()) text += "|ptr";
					else if(pony.isBurned()) text += "|brn";
					else if(pony.isIntoxicated()) text += "|tox";
					else if(pony.isPoisoned()) text += "|psn";
					text += ")";
				}
			}

			@Override
			protected boolean showCondition() {
				return pony != null && !unknown;
			}
		};
	}

	public TeamMenuPanel() {
		setLayout(new GridLayout(2,3));
		setOpaque(false);
	}

	public TeamMenuPanel(final Player p) {
		this();
		initialize(p);
	}

	public void initialize(final Player p) {
		player = p;
		for(int i = 0; i < 6; ++i) {
			token[i] = new PonyToken(player.getTeam().getPony(i));
			//token[i].addMouseListener(new TeamMenuMouseListener(i));
			if(Debug.on) printDebug("[TeamMenuPanel] pony "+i+": "+player.getTeam().getPony(i));
			add(token[i]);
		}
		setBorder(new TitledBorder(p.getName()));
		if(Debug.on) printDebug("Initialized TeamMenuPanel for player "+player.getName());
	}

	public void addPony(Pony pony) {
		addPony(pony, false, false);
	}

	public void addPony(Pony pony, boolean unknown) {
		addPony(pony, unknown, false);
	}

	public void addPony(Pony pony, boolean unknown, boolean fainted) {
		if(Debug.on) printDebug("[TMP] called addPony("+pony+","+unknown+","+fainted+")");
		for(int i = 0; i < token.length; ++i)
			if(token[i] == null || token[i].getPony() == null) {
				if(Debug.on) printDebug("[TMP] token["+i+"] set.");
				token[i].set(pony, unknown, fainted);
				return;
			}
	}

	public PonyToken[] getTokens() {
		return token;
	}

	public void setFainted(int num, boolean fnt) {
		token[num].setFainted(fnt);
	}

	public void setUnknown(int num, boolean unk) {
		token[num].setUnknown(unk);
	}
}		
