//: gui/PonyPanel.java

package pokepon.gui;

import pokepon.pony.*;
import pokepon.enums.*;
import pokepon.util.*;
import static pokepon.util.MessageManager.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.imageio.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;

/** Graphical component that, given a Pony, displays its name, sprite,
 * nickname, typing and stats.
 *
 * @author silverweed
 */
public class PonyPanel extends JPanel {

	protected Pony pony;
	protected JTextField nickname = new RoundJTextField(20);
	//protected JTextField name = new JTextField(20);
	protected JLabel name = new JLabel();
	//protected JTextField typing = new JTextField(20);
	protected JLabel typing = new JLabel();
	protected JTextField level = new RoundJTextField(3);
	protected JTextField happiness = new RoundJTextField(3);
	protected JLabel canon = new JLabel();
	protected JLabel species = new JLabel();
	protected JLabel phrase = new JLabel(); 
	protected StatsPanel statsPanel = new PonyStatsPanel();
	protected JLabel sprite = new JLabel();
	//protected JComboBox<String> ability = new JComboBox<String>();
	//protected JTextField ability = new JTextField(20);

	public PonyPanel() {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.insets = new Insets(0,4,0,4);
		add(new JLabel("Nick:"),c);
		c.gridx = 1;
		c.gridwidth = 4;
		nickname.getDocument().addDocumentListener(new NicknameListener());
		nickname.setMinimumSize(nickname.getPreferredSize());
		add(nickname,c);
		
		c.gridy = 1;
		c.gridheight = 6;
		c.insets = new Insets(3,4,3,4);
		add(Box.createRigidArea(new Dimension(170,150)),c);
		add(sprite,c);
		
		c.gridy = 7;
		c.gridheight = 1;
		c.insets = new Insets(0,4,0,4);
		//name.setEditable(false);
		add(name,c);

		c.gridy = 8;
		//typing.setEditable(false);
		add(typing,c);
		
		c.gridx = 6;
		c.gridy = 0;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		add(new JLabel("Level"),c);

		c.gridx = 7;
		level.getDocument().addDocumentListener(new LevelListener());
		level.setMinimumSize(level.getPreferredSize());
		add(level,c);

		c.gridx = 6;
		c.gridy = 1;
		add(new JLabel("Happiness"),c);
	
		c.gridx = 7;
		happiness.getDocument().addDocumentListener(new HappinessListener());
		happiness.setMinimumSize(happiness.getPreferredSize());
		add(happiness,c);

		c.gridx = 6;
		c.gridy = 2;
		c.insets = new Insets(4,4,4,4);
		add(new JLabel("Canon"),c);

		c.gridx = 7;
		add(canon,c);

		c.gridx = 6;
		c.gridy = 3;
		add(new JLabel("Species"),c);

		c.gridx = 7;
		add(species,c);
		
		c.gridx = 6;
		c.gridy = 4;
		c.gridwidth = 2;
		//phrase.setEditable(false);
		phrase.setMinimumSize(phrase.getPreferredSize());
		phrase.setFont(phrase.getFont().deriveFont(Font.PLAIN));
		add(phrase,c);

		/*c.gridx = 6;
		c.gridy = 5;
		add(new JLabel("Ability"),c);

		c.gridy = 6;
		add(ability,c);*/

		c.gridx = 0;
		c.gridy = 10;
		c.gridheight = 1;
		c.gridwidth = 7;
		c.anchor = GridBagConstraints.LINE_START;
		add(statsPanel,c);
	}

	public PonyPanel(Pony p) {
		this();
		setPony(p);
	}

	public Pony getPony() {
		return pony;
	}

	public int getHappiness() {
		try {
			int h = Integer.parseInt(happiness.getText());
			if(h < 0) h = 0;
			else if(h > Pony.MAX_HAPPINESS) h = Pony.MAX_HAPPINESS;
			return h;
		} catch(IllegalArgumentException e) {
			return 0;
		}
	}

	public String getNickname() {
		return nickname.getText();
	}
	
	public StatsPanel getStatsPanel() {
		return statsPanel;
	}

	public void setStatsPanel(StatsPanel s) {
		statsPanel = s;
	}

	public void setAndDrawStatsPanel(StatsPanel s) {
		remove(statsPanel);
		statsPanel = s;
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.gridheight = 8;
		add(statsPanel,c);
		statsPanel.repaint();
		validate();
	}

	public void setAndDrawStatsPanel(StatsPanel s,GridBagConstraints c) {
		remove(statsPanel);
		statsPanel = s;
		add(statsPanel,c);
		statsPanel.repaint();
		validate();
	}

	public void setPony(final Pony p) {
		if(p == null) return;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				pony = p;
				name.setText(p.getName());
				nickname.setText(p.getNickname());
				typing.setText("<html>"+p.getTypingHTMLTokens()+"</html>");
				try {
					sprite.setIcon(new ImageIcon(p.getFrontSprite()));
				} catch(Exception e) {
					printDebug("Caught exception while constructing PonyPanel("+p.getName()+"):");
					e.printStackTrace();
					try {
						URL emptyTokenURL = null;
						emptyTokenURL = getClass().getResource(Meta.complete2(Meta.TOKEN_DIR)+"/empty_token_icon.png");
						if(emptyTokenURL != null) {
							Image img = ImageIO.read(emptyTokenURL);
							img = img.getScaledInstance(110,-1,Image.SCALE_SMOOTH);
							sprite.setIcon(new ImageIcon(img));
						}
					} catch(java.io.IOException ee) {
						printDebug("Caught exception while setting empty token in PonyPanel.setPony(): "+ee);
					}
				}
				if(sprite.getIcon() == null || sprite.getIcon().getIconWidth() < 0) {
					try {
						URL emptyTokenURL = null;
						emptyTokenURL = getClass().getResource(Meta.complete2(Meta.TOKEN_DIR)+"/empty_token_icon.png");
						if(emptyTokenURL != null) {
							Image img = ImageIO.read(emptyTokenURL);
							img = img.getScaledInstance(110,-1,Image.SCALE_SMOOTH);
							sprite.setIcon(new ImageIcon(img));
						}
					} catch(java.io.IOException e) {
						printDebug("Caught exception while setting empty token in PonyPanel.setPony(): "+e);
					}
				}
				if(Debug.on) printDebug("icon size: "+sprite.getSize());
				level.setText(""+p.getLevel());
				happiness.setText(""+p.getHappiness());
				canon.setText((p.isCanon() ? "<html><font color=green>yes</font></html>" : "<html><font color=red>no</font></html>"));
				species.setText(p.getRace().toString());
				phrase.setText("<html><em>"+p.getIVMsg()+"</em></html>");
				statsPanel.setPony(p);

				/*ability.removeActionListener(abilityListener);
				ability.removeAllItems();
				validate();
				repaint();
				ability.addItem("");
				for(String mv : pony.getPossibleAbilities()) {
					ability.addItem(mv);
				}
				
				if(pony.getAbility() != null)
					ability.setSelectedItem(pony.getAbility().getName());
				else
					ability.setSelectedItem("");

				ability.addActionListener(abilityListener);
				*/
			}
		});
	}

	public void updatePhrase(final Pony p) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				phrase.setText("<html><em>"+p.getIVMsg().toString()+"</em></html>");
			}
		});
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		Pony pony = new PrincessLuna(1);
		frame.add(new PonyPanel(pony));
		SwingConsole.run(frame);
	}

	/*private ActionListener abilityListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			String selected = (String)ability.getSelectedItem();
			if(Debug.on) printDebug("[PonyPanel] selected ability: "+selected);
			if(selected == null || selected.equals("")) 
				pony.setAbility(null);
			else {
				try {
					pony.setAbility(AbilityCreator.create(selected));
					if(Debug.on) printDebug("[PonyPanel] set pony's ability to "+pony.getAbility());
				} catch(ReflectiveOperationException ee) {
					printDebug("[PonyPanel] Failed to create ability: "+selected);
				}
			}
		}
	};*/

	private class LevelListener implements DocumentListener {
		
		public void changedUpdate(DocumentEvent e) {
			if(Debug.pedantic) printDebug("Level: changedUpdate");
			updateLevel();
		}

		public void removeUpdate(DocumentEvent e) {
			if(Debug.pedantic) printDebug("Level: removeUpdate");
			updateLevel();
		}

		public void insertUpdate(DocumentEvent e) {
			if(Debug.pedantic) printDebug("Level: insertUpdate");
			updateLevel();
		}

		public void updateLevel() {
			String lev = level.getText();

			if(lev == null || lev.length() == 0 || !lev.matches("^[0-9]*$")) {
				pony.setLevel(1);
				return;
			}

			try {
				pony.setLevel(Math.min(Pony.MAX_LEVEL,Integer.parseInt(lev)));
				if(Debug.on) printDebug("Setting pony level to "+pony.getLevel());
			} catch(IllegalArgumentException e) {
				if(Debug.on) printDebug("Error in parsing level: setting level to 1.");
				pony.setLevel(1);
			}
		}
	}
				
	private class HappinessListener implements DocumentListener {
		
		public void changedUpdate(DocumentEvent e) {
			if(Debug.pedantic) printDebug("Happiness: changedUpdate");
			updateHappiness();
		}

		public void removeUpdate(DocumentEvent e) {
			if(Debug.pedantic) printDebug("Happiness: removeUpdate");
			updateHappiness();
		}

		public void insertUpdate(DocumentEvent e) {
			if(Debug.pedantic) printDebug("Happiness: insertUpdate");
			updateHappiness();
		}

		public void updateHappiness() {
			String happ = happiness.getText(); 

			if(happ == null || happ.length() == 0 || !happ.matches("^[0-9]*$")) {
				pony.setHappiness(0);
				return;
			}

			try {
				pony.setHappiness(Math.min(Pony.MAX_HAPPINESS,Integer.parseInt(happ)));
				if(Debug.on) printDebug("Setting pony happiness to "+pony.getHappiness());
			} catch(IllegalArgumentException e) {
				if(Debug.on) printDebug("Error in parsing happiness: setting happiness to 0.");
				pony.setHappiness(0);
			}
		}
	}

	private class NicknameListener implements DocumentListener {
		
		public void changedUpdate(DocumentEvent e) {
			updateNick();
		}

		public void removeUpdate(DocumentEvent e) {
			updateNick();
		}

		public void insertUpdate(DocumentEvent e) {
			updateNick();
		}

		public void updateNick() {
			String nick = nickname.getText(); 

			if(nick == null || nick.length() == 0) {
				return;
			}

			pony.setNickname(nick);
			if(Debug.on) printDebug("Setting pony nickname to "+pony.getNickname());
		}
	}

}

