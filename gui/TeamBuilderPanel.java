//: gui/TeamBuilderPanel.java

package pokepon.gui;

import pokepon.pony.*;
import pokepon.ability.*;
import pokepon.move.*;
import pokepon.player.*;
import pokepon.util.*;
import pokepon.item.*;
import pokepon.enums.*;
import static pokepon.util.MessageManager.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.imageio.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;

/** The main component of the GUITeamBuilder; extends Ponydex but changes the StatsPanel
 * to allow editing the pony's stats.
 *
 * @author Giacomo Parolini
 */
public class TeamBuilderPanel extends Ponydex {

	private static final String STATSPANEL_CARD = "STATS_PANEL";
	private static final String ABILITYPREV_CARD = "ABILITY_PREV";
	private static final String ITEMPREV_CARD = "ITEM_PREV";
	private static final String MOVEPREV_CARD = "MOVE_PREV";
	private JPanel bottomPanel = new JPanel(new CardLayout());
	private AbilityPreviewer abilityPrev = new AbilityPreviewer();	
	private ItemPreviewer itemPrev = new ItemPreviewer();	
	private MovePreviewer movePrev = new MovePreviewer();
	private StatsPanel statsPanel = new FancyPonyStatsPanel();
	private TeamPanel teamPanel = new TeamPanel();
	private JPanel movePanel = new JPanel(new GridLayout(Pony.MOVES_PER_PONY,1,0,3));
	private JTextField[] moveField = new JTextField[Pony.MOVES_PER_PONY];
	private JTextField ability = new RoundJTextField(20);
	private JTextField item = new RoundJTextField(20);
	private FancyPonyStatsPanel.FancyStatsPreviewPanel statsPrevPanel;
	private int selectedTeamIndex = 0;
	private Pony pony;

	public TeamBuilderPanel() {
		for(int i = 0; i < Team.MAX_TEAM_SIZE; ++i) {
			final int num = i;
			teamPanel.getTokens()[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(Debug.on) printDebug("Called TeamMemberListener(#"+num+")");
					selectedTeamIndex = num;
					if(teamPanel.getPony(num) == null) return;
					setPony(teamPanel.getPony(num));
				}
			});
		}
		ponies.removeListSelectionListener(poniesSelectionListener);
		ponies.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(e.getValueIsAdjusting()) return;		
				String selected = ponies.getSelectedValue();
				try {
					Pony pony = null;
					if(cachedPonies.get(selected) == null) {
						pony = PonyCreator.create(selected);
						if(Debug.on) printDebug("[TBPanel] Created pony "+pony.getName());
						cachedPonies.put(selected, pony);
					} else {
						pony = cachedPonies.get(selected);
						if(Debug.on) printDebug("[TBPanel] Using cached "+pony.getName());
					}
					pony.setNature(Pony.Nature.FRIENDLY);
					if(Debug.on) printDebug("[TBPanel] Set nature to Friendly.");
					setPony(pony);
					validate();
				} catch(Exception ex) {
					printDebug("Caught exception: ");
					ex.printStackTrace();
				}
			}
		});

		// add FancyPonyStatPanel and previewers to bottomPanel
		bottomPanel.add(statsPanel,STATSPANEL_CARD);
		bottomPanel.add(abilityPrev,ABILITYPREV_CARD);
		bottomPanel.add(itemPrev,ITEMPREV_CARD);
		bottomPanel.add(movePrev,MOVEPREV_CARD);

		// add abItemPanel to PonyPanel
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 6;
		c.gridy = 5;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(1,4,0,4);
		c.gridwidth = 2;
		c.gridheight = 4;
		JPanel abItemPanel = new JPanel(new GridLayout(4,1,0,2));
		ponyPanel.add(abItemPanel,c);

		// add ability field to abItemPanel
		abItemPanel.add(new JLabel("Ability"));

		ability.setMinimumSize(ability.getPreferredSize());
		ability.setBorder(new CompoundBorder(ability.getBorder(),new EmptyBorder(3,3,3,3)));
		ability.addFocusListener(new PreviewerListener(abilityPrev,ABILITYPREV_CARD));
		ability.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				if(abilityPrev != null)
					abilityPrev.showPreview(ability.getText());
			}
			public void changedUpdate(DocumentEvent e) {
				if(abilityPrev != null)
					abilityPrev.showPreview(ability.getText());
			}
			public void removeUpdate(DocumentEvent e) {
				if(abilityPrev != null)
					abilityPrev.showPreview(ability.getText());
			}
		});
		abilityPrev.getPreviewerList().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(e.getValueIsAdjusting()) return;		
				Ability selected = (Ability)abilityPrev.getPreviewerList().getModel()
							.getElementAt(abilityPrev.getPreviewerList().getSelectedIndex());
				if(Debug.on) printDebug("[abilityPrev] selected: "+selected+" (index="+abilityPrev.getPreviewerList().getSelectedIndex()+")");
				printDebug("");
				if(selected == null || selected.getName() == null) {
					return;
				}
				try {
					final Ability ab = AbilityCreator.create(selected.getName());
					if(Debug.on) printDebug("Created ability: "+ab);
					if(abilityPrev.getPony() != null) {
						abilityPrev.getPony().setAbility(ab);
						if(Debug.on) printDebug(abilityPrev.getPony()+"'s ability is now "+abilityPrev.getPony().getAbility());
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								ability.setText(ab.getName());
								KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent();
							}
						});
					}
				} catch(ReflectiveOperationException ex) {
					printDebug("[AbilityPreviewer] Caught exception: "+ex);
				}
				((CardLayout)bottomPanel.getLayout()).show(bottomPanel,STATSPANEL_CARD);
			}
		});
		abItemPanel.add(ability);

		// add item to PonyPanel
		abItemPanel.add(new JLabel("Item"));
		item.setBorder(new CompoundBorder(item.getBorder(),new EmptyBorder(3,3,3,3)));
		item.addFocusListener(new PreviewerListener(itemPrev,ITEMPREV_CARD));
		item.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				if(itemPrev != null)
					itemPrev.showPreview(item.getText());
			}
			public void changedUpdate(DocumentEvent e) {
				if(itemPrev != null)
					itemPrev.showPreview(item.getText());
			}
			public void removeUpdate(DocumentEvent e) {
				if(itemPrev != null)
					itemPrev.showPreview(item.getText());
			}
		});
		itemPrev.getPreviewerList().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(e.getValueIsAdjusting()) return;		
				Item selected = (Item)itemPrev.getPreviewerList().getModel()
						.getElementAt(itemPrev.getPreviewerList().getSelectedIndex());
				if(Debug.on) printDebug("[itemPrev] selected: "+selected+" (index="+itemPrev.getPreviewerList().getSelectedIndex()+")");
				printDebug("");
				if(selected == null || selected.getName() == null) {
					return;
				}
				try {
					final Item it = ItemCreator.create(selected.getName());
					if(Debug.on) printDebug("Created item: "+it);
					if(itemPrev.getPony() != null) {
						itemPrev.getPony().setItem(it);
						if(Debug.on) printDebug(itemPrev.getPony()+"'s item is now "+itemPrev.getPony().getItem());
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								item.setText(it.getName());
								KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent();
							}
						});
					}
				} catch(ReflectiveOperationException ex) {
					printDebug("[ItemPreviewer] Caught exception: "+ex);
				}
				((CardLayout)bottomPanel.getLayout()).show(bottomPanel,STATSPANEL_CARD);
			}
		});
		item.setMinimumSize(item.getPreferredSize());
		abItemPanel.add(item);

		// replace Ponydex's stats panel with a fancier one
		ponyPanel.remove(ponyPanel.getStatsPanel());
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 4;
		c.gridheight = 1;
		c.insets = new Insets(7,14,4,14);
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(bottomPanel,c);
		ponyPanel.setStatsPanel(statsPanel);

		// construct movePanel and add it
		movePanel.setBorder(new CompoundBorder(new TitledBorder("Moves"),new EmptyBorder(3,3,3,3)));
		for(int i = 0; i < Pony.MOVES_PER_PONY; ++i) {
			moveField[i] = new RoundJTextField(20);
			moveField[i].setBorder(new CompoundBorder(moveField[i].getBorder(),new EmptyBorder(4,4,4,4)));
			moveField[i].addFocusListener(new PreviewerListener(movePrev,MOVEPREV_CARD));
			final int j = i;
			moveField[i].getDocument().addDocumentListener(new DocumentListener() {
				public void insertUpdate(DocumentEvent e) {
					if(movePrev != null) {
						movePrev.showPreview(moveField[j].getText());
					}
				}
				public void changedUpdate(DocumentEvent e) {
					if(movePrev != null) {
						movePrev.showPreview(moveField[j].getText());
					}
				}
				public void removeUpdate(DocumentEvent e) {
					if(movePrev != null) {
						movePrev.showPreview(moveField[j].getText());
					}
				}
			});
			movePanel.add(moveField[i]);
		}
		// attach selection listener to movePrev's list.
		movePrev.getPreviewerList().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(e.getValueIsAdjusting()) return;		
				Move selected = (Move)movePrev.getPreviewerList().getModel()
						.getElementAt(movePrev.getPreviewerList().getSelectedIndex());
				if(Debug.pedantic) {
					printDebug("[movePrev] moves:");
					for(int i = 0; i < movePrev.getPreviewerList().getModel().getSize(); ++i)
						printDebugnb(movePrev.getPreviewerList().getModel().getElementAt(i)+",");
				}
				if(Debug.on) printDebug("[movePrev] selected: "+selected+
					" (index="+movePrev.getPreviewerList().getSelectedIndex()+
					" --> "+movePrev.getPreviewerList().getModel().getElementAt(
						movePrev.getPreviewerList().getSelectedIndex())+")");

				if(	selected == null || selected.getName() == null || 
					movePrev.getMoveIndex() < 0 || movePrev.getMoveIndex() >= Pony.MOVES_PER_PONY
				) {
					printDebug("[!] selected = "+selected+", selectedIndex="+movePrev.getMoveIndex());
					return;
				}
				try {
					final Move mv = MoveCreator.create(selected.getName().startsWith("Hidden Talent")
								? "Hidden Talent"
								: selected.getName()
							);
					if(mv.getName().equals("Hidden Talent")) {
						Type type = Type.forName(selected.getName()
								.replaceAll("Hidden Talent \\(","").replaceAll("\\)",""));
						mv.setType(type);
						mv.setName(selected.getName());
						HiddenTalent.adjustIVs(pony, type);
						setPony(pony);
					}
					if(Debug.on) printDebug("Created move: "+mv);
					if(movePrev.getPony() != null) {
						movePrev.getPony().setMove(movePrev.getMoveIndex(),mv);
						if(Debug.on) printDebug(movePrev.getPony()+"'s move #"+movePrev.getMoveIndex()+
							" is now "+movePrev.getPony().getMove(movePrev.getMoveIndex()));
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								moveField[movePrev.getMoveIndex()].setText(mv.getName());
								KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent();
							}
						});
					}
				} catch(ReflectiveOperationException ex) {
					printDebug("[MovePreviewer] Caught exception: "+ex);
				}
				((CardLayout)bottomPanel.getLayout()).show(bottomPanel,STATSPANEL_CARD);
			}
		});

		// add MovePanel and StatsPreviewPanel to sidePanel
		JPanel sidePanel = new JPanel(new GridLayout(2,1,2,2));

		sidePanel.add(movePanel);
		
		statsPrevPanel = ((FancyPonyStatsPanel)statsPanel).getStatsPreviewPanel();
		statsPrevPanel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				((CardLayout)bottomPanel.getLayout()).show(bottomPanel,STATSPANEL_CARD);
			}
		});
		sidePanel.add(statsPrevPanel);

		c.gridx = 3;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.insets = new Insets(14,14,4,14);
		c.anchor = GridBagConstraints.NORTH;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0;
		add(sidePanel,c);
		
		// add a TeamPanel
		c.gridy = 2;
		c.gridx = 0;
		c.gridwidth = 4;
		c.weightx = 1;
		add(teamPanel,c);
		try {
			Pony tmpny = PonyCreator.create(ponies.getSelectedValue());
			tmpny.setNature(Pony.Nature.FRIENDLY);
			setPony(tmpny);
		} catch(ReflectiveOperationException e) {
			printDebug("Caught exception while creating TeamBuilderPanel: "+e);
		}
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.add(new TeamBuilderPanel());
		SwingConsole.run(frame,800,600,"Ponydex");
	}

	public void clear() {
		teamPanel.clear();
		selectedTeamIndex = 0;
	}

	public TeamPanel getTeamPanel() {
		return teamPanel;
	}
	
	public int getSelectedTeamIndex() {
		return selectedTeamIndex;
	}

	public JPanel getMovePanel() {
		return movePanel;
	}

	public void setSelectedTeamIndex(int index) {
		selectedTeamIndex = index;
		teamPanel.setSelectedIndex(index);
	}

	private class PreviewerListener implements FocusListener {
		private final String card;
		private final PokeponPreviewer previewer;

		public PreviewerListener(final PokeponPreviewer previewer,final String card) {
			this.card = card;
			this.previewer = previewer;
		}
		public void focusGained(FocusEvent e) {
			((CardLayout)bottomPanel.getLayout()).show(bottomPanel,card);
			previewer.showPreview(((JTextField)e.getSource()).getText());
			for(int i = 0; i < moveField.length; ++i) 
				if(e.getSource() == moveField[i]) {
					((MovePreviewer)previewer).setMoveIndex(i);
					break;
				}
		}
		
		public void focusLost(FocusEvent e) {
			if(e.isTemporary()) return;
			((CardLayout)bottomPanel.getLayout()).show(bottomPanel,STATSPANEL_CARD);
		}
	};

	public void setPony(Pony pony) {
		this.pony = pony;
		ponyPanel.setPony(pony);
		for(int i = 0; i < Pony.MOVES_PER_PONY; ++i) {
			if(pony != null && pony.getMove(i) != null)
				moveField[i].setText(pony.getMove(i).getName());
			else
				moveField[i].setText("");
		}
		movePrev.setPony(pony);
		abilityPrev.setPony(pony);
		itemPrev.setPony(pony);
		if(pony == null) {
			ability.setText("");
			item.setText("");
		} else {
			if(pony.getAbility() == null) {
				// if possibleAbilities is non-empty, set first possible ability as default.
				if(pony.getPossibleAbilities().size() > 0) {
					try {
						pony.setAbility(AbilityCreator.create(pony.getPossibleAbilities().get(0)));
						ability.setText(pony.getPossibleAbilities().get(0));
					} catch(ReflectiveOperationException e) {
						printDebug("[TBPanel.setPony("+pony+")] Error creating ability:");
						e.printStackTrace();
						ability.setText("");
					}
				} else {
					ability.setText("");
				}
			} else {
				ability.setText(pony.getAbility().getName());
			}
			if(pony.getItem() == null)
				item.setText("");
			else
				item.setText(pony.getItem().getName());
		}
		abilityPrev.showPreview(null);
		itemPrev.showPreview(null);
		movePrev.showPreview(null);
	}
}
