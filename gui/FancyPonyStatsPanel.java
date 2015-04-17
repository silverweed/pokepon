//: gui/FancyPonyStatsPanel.jav

package pokepon.gui;

import pokepon.pony.*;
import pokepon.util.*;
import static pokepon.util.MessageManager.*;
import static pokepon.pony.Pony.Nature;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import javax.swing.event.*;
import java.util.regex.*;
import java.awt.event.*;
import java.util.*;

/** A component that displays graphically a pony's stats and allows to change them
 * with sliders and textfields (totally Zarel-style); a good deal of teambuilding
 * is done here;
 * since this is one of the most complex GUI classes, handle with care or something
 * may break.
 *
 * @author silverweed
 */
class FancyPonyStatsPanel extends StatsPanel {

	private static final int BAR_MAX_LENGTH = 420;

	private int[] ev = new int[6];
	private int[] iv = new int[6];
	private int lastUpdatedEV = -1;
	private String nature = "neutral";
	private JLabel remaining = new JLabel();
	private JSlider[] slider = new JSlider[6];
	private JLabel[] baseStats = new JLabel[6];
	private JLabel[] totStats = new JLabel[6];
	private JLabel bstLabel = new JLabel();
	private JTextField[] evField = new RoundJTextField[6];
	private JTextField[] ivField = new RoundJTextField[6];
	private Pony pony;
	private ShapeComponent[] bar = new ShapeComponent[6];
	private ShapeComponent placeholderBar = new ShapeComponent(new Rectangle(BAR_MAX_LENGTH,10),Color.WHITE,0f);
	private JComboBox<String> natures = new JComboBox<>();
	/** Used to prevent infinite loops between nature and ev listeners */
	private volatile boolean adjustingEVs;
	private volatile boolean adjustingIVs;
	private volatile boolean adjustingNature;
	private volatile boolean adjustingSlider;
	private volatile Boolean updateBars;
	private int negativeStatNum = -1;
	private int positiveStatNum = -1;
	private java.util.List<FancyStatsPreviewPanel> previewPanels = new LinkedList<FancyStatsPreviewPanel>();

	// TODO: add weightx/y to components
	public FancyPonyStatsPanel() {
		setLayout(new GridBagLayout()); 
		GridBagConstraints c = new GridBagConstraints();

		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_START;
		c.insets = new Insets(2,3,2,3);

		for(int i = 0; i < 6; ++i) {
			baseStats[i] = new JLabel();
			totStats[i] = new JLabel();
			evField[i] = new RoundJTextField(4);
			slider[i] = new JSlider(0,Pony.MAX_EV,0);
			bar[i] = new ShapeComponent(new Rectangle(100,10));
			ivField[i] = new RoundJTextField(2);

			slider[i].addChangeListener(new SliderChangeListener(i));
			evField[i].getDocument().addDocumentListener(new EVChangeListener(i));
			ivField[i].getDocument().addDocumentListener(new IVChangeListener(i));

			c.gridx = 0;
			c.gridwidth = 1;
			add(new JLabel(Pony.STAT_NAMES[i].substring(0,Math.min(Pony.STAT_NAMES[i].length(),3))),c);

			c.gridx = 1;
			c.anchor = GridBagConstraints.EAST;
			add(baseStats[i],c);
		
			// this is to set a fixed width to the bars column.
			if(i == 0) {
				c.gridx = 2;
				c.gridwidth = 3;
				c.anchor = GridBagConstraints.WEST;
				add(placeholderBar,c);
			}
			c.gridx = 2;
			c.gridwidth = 3;
			c.anchor = GridBagConstraints.WEST;
			add(bar[i],c);

			c.gridx = 5;
			c.gridwidth = 1;
			evField[i].setMinimumSize(evField[i].getPreferredSize());
			add(evField[i],c);

			c.gridx = 6;
			c.gridwidth = 3;
			slider[i].setMinimumSize(slider[i].getPreferredSize());
			add(slider[i],c);

			c.gridx = 9;
			c.gridwidth = 1;
			ivField[i].setMinimumSize(ivField[i].getPreferredSize());
			add(ivField[i],c);

			c.gridx = 10;
			add(totStats[i],c);
			c.gridy++;
		}

		for(Nature n : Nature.values()) {
			StringBuilder desc = new StringBuilder("(");
			if(n.increasedStat() == null) {
				desc.append("neutral)");
			} else { 
				desc.append("+"+n.increasedStat()+" -"+n.decreasedStat()+")");
			}
			natures.addItem(n+" "+desc);
		}

		c.gridx = 1;
		add(new JLabel("Nature"),c);
		c.gridx = 2;
		c.gridwidth = 3;
		natures.addActionListener(new NatureListener());
		natures.setBackground(ivField[0].getBackground());
		add(natures,c);

		c.gridx = 5;
		c.gridwidth = 1;
		add(new JLabel("Remaining EVs:"),c);
		c.gridx = 6;
		add(remaining,c);

		c.gridx = 9;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.LINE_END;
		add(bstLabel,c);
	}

	public FancyPonyStatsPanel(Pony p) {
		this();
		setPony(p);
	}

	public Pony getPony() {
		return pony;
	}

	/** Changes the currently selected pony, refreshing the GUI accordingly. */
	public synchronized void setPony(final Pony p) {
		pony = p;
		Pony.Stat[] stats = Pony.Stat.core();
		for(int i = 0; i < 6; ++i) {
			slider[i].setValue(0);
			baseStats[i].setText(""+pony.getBaseStat(stats[i]));
			ev[i] = p.getEV(stats[i]);
			iv[i] = p.getIV(stats[i]);
			bstLabel.setText("BST: "+p.bst());
			remaining.setText(""+p.remainingEVs());
		}
		if(pony.getNature().increasedStat() != null) {
			positiveStatNum = Arrays.asList(Pony.Stat.core()).indexOf(pony.getNature().increasedStat());
			negativeStatNum = Arrays.asList(Pony.Stat.core()).indexOf(pony.getNature().decreasedStat());
		}
		refresh();
	}

	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.add(new FancyPonyStatsPanel(new PrincessLuna(100)));
		SwingConsole.run(f);
	}


	/////////////////////////// START LISTENERS //////////////////////////////////////

	private abstract class MyChangeListener	{	
		protected final int num;
		protected String stat;
	
		public MyChangeListener(final int num) {
			this.num = num;
			switch(num) {
				case 0: 
					stat = "hp";
					break;
				case 1: 
					stat = "atk";
					break;
				case 2: 
					stat = "def";
					break;
				case 3: 
					stat = "spatk";
					break;
				case 4: 
					stat = "spdef";
					break;
				case 5: 
					stat = "speed";
					break;
			}
		}
	}

	/** Catches changes in the sliders and modifies the pony's EV accordingly. */
	private class SliderChangeListener extends MyChangeListener implements ChangeListener {

		public SliderChangeListener(final int num) {
			super(num);
		}

		public synchronized void stateChanged(ChangeEvent e) {	
			if(stat == null || adjustingSlider) return;
			ev[num] = ((JSlider)e.getSource()).getValue();
			if(Debug.pedantic) printDebug("[Slider]: set ev["+num+"] to "+ev[num]);
			lastUpdatedEV = num;
			refresh();
		}
	}

	/** When the EV fields are edited, it reads the new value and sets the pony's EV accordingly; also 
	 * parses the nature modifiers '+' and '-'.
	 */
	private class EVChangeListener extends MyChangeListener implements DocumentListener {

		private Pattern pattern = Pattern.compile("^\\s*(?<value>[0-9]*)\\s*(?<mod>[\\+\\-])?");

		public EVChangeListener(final int num) {
			super(num);
		}

		public void changedUpdate(DocumentEvent e) {
			if(Debug.pedantic) printDebug("EVs: changedUpdate");
			updateEV();
		}

		public void removeUpdate(DocumentEvent e) {
			if(Debug.pedantic) printDebug("EVs: removeUpdate");
			updateEV();
		}

		public void insertUpdate(DocumentEvent e) {
			if(Debug.pedantic) printDebug("EVs: insertUpdate");
			updateEV();
		}

		private synchronized void updateEV() {
			if(stat == null || !evField[num].isFocusOwner() || adjustingEVs) {
				if(Debug.pedantic) printDebug("[EVCh.List.] returning");
				return;
			}

			String text = evField[num].getText();
			Matcher matcher = pattern.matcher(text);

			if(Debug.pedantic) printDebug(">>> [EVCh-List.] text="+text+",num="+num);

			// if text is invalid, set EV to 0 and, if necessary, nature to neutral.
			if(!matcher.matches()) {
				ev[num] = 0;
				if(	pony.getNature().increasedStat() != null && 
					(	pony.getNature().increasedStat().equals(stat) || 
						pony.getNature().decreasedStat().equals(stat)
					)
				)
					nature = "neutral";
				refresh();
				return;
			}
			
			if(matcher.group("value") != null && !matcher.group("value").isEmpty()) {
				lastUpdatedEV = num;
				if(Debug.pedantic) printDebug("[EVCh.List. group value = "+matcher.group("value"));
				try {
					ev[num] = Integer.parseInt(matcher.group("value"));
				} catch(IllegalArgumentException ee) {
					printDebug("Caught exception in EVChangeListener <pony.setEV>: "+ee);
				}
			} else {
				ev[num] = 0;
			}

			if(matcher.group("mod") != null && !matcher.group("mod").isEmpty()) {
				if(Debug.pedantic) printDebug("[EVCh.List.] group mod = "+matcher.group("mod"));
				if(matcher.group("mod").equals("+")) {
					positiveStatNum = num;
				} else if(matcher.group("mod").equals("-")) {
					negativeStatNum = num;
				} else {
					printDebug("What?? group(mod) is neither `+' nor `-', but "+matcher.group("mod")+"!");
				}							
			} else {
				if(positiveStatNum == num)
					positiveStatNum = -1;
				if(negativeStatNum == num)
					negativeStatNum = -1;
			}
			refresh();
		}

	}

	/** When the IV fields are edited, it reads the new value and sets the pony's IV accordingly. */
	private class IVChangeListener extends MyChangeListener implements DocumentListener {

		public IVChangeListener(final int num) {
			super(num);
		}
		
		public void changedUpdate(DocumentEvent e) {
			updateIV();
		}

		public void removeUpdate(DocumentEvent e) {
			updateIV();
		}

		public void insertUpdate(DocumentEvent e) {
			updateIV();
		}

		private void updateIV() {
			if(stat == null || !ivField[num].isFocusOwner() || adjustingIVs) return;
			String text = ivField[num].getText();

			if(text.matches("^\\s*[0-9]+\\s*$")) {
				try {
					iv[num] = Integer.parseInt(text);
				} catch(IllegalArgumentException ee) {
					printDebug("Caught exception in IVChangeListener <pony.setIV>: "+ee);
				}
			} else {
				iv[num] = 0;
			}

			refresh();
		}
	}

	private class NatureListener implements ActionListener {

		public synchronized void actionPerformed(ActionEvent e) {
			if(adjustingNature) return;
			String text = ((String)((JComboBox)e.getSource()).getSelectedItem()).split(" ")[0];
			nature = (Nature.forName(text) == null ? "neutral" : Nature.forName(text).toString());
			if(nature.equals("neutral")) {
				positiveStatNum = negativeStatNum = -1;
			} else {
				positiveStatNum = Arrays.asList(Pony.STAT_NAMES).indexOf(Nature.forName(text).increasedStat());
				negativeStatNum = Arrays.asList(Pony.STAT_NAMES).indexOf(Nature.forName(text).decreasedStat());
			}
			refresh();
			return;
		}
	}

	//////////////////////////////////////////////// END LISTENERS

	synchronized void refresh() {
		if(Debug.pedantic) printDebug("CALLED REFRESH()");
		SwingUtilities.invokeLater(new Runnable() {
			public synchronized void run() {
				adjustingEVs = true;
				adjustingIVs = true;
				adjustingNature = true;
				adjustingSlider = true;
				if(Debug.pedantic) {
					printDebug("positiveStatNum = "+positiveStatNum);
					printDebug("negativeStatNum = "+negativeStatNum);
					printDebug("nature = "+nature);
				}
				/* Set nature */
				if(positiveStatNum < 1 || negativeStatNum < 1) {
					for(Nature n : Nature.values())
						if(n.increasedStat() == null) {
							pony.setNature(n);
							if(Debug.pedantic) printDebug("Set nature to "+n);
							break;
						}
				} else {
					for(Nature n : Nature.values()) {
						int incInd = Arrays.asList(Pony.STAT_NAMES).indexOf(n.increasedStat());
						int decInd = Arrays.asList(Pony.STAT_NAMES).indexOf(n.decreasedStat());
						if(incInd == positiveStatNum && decInd == negativeStatNum) {
							pony.setNature(n);
							if(Debug.pedantic) printDebug("Set nature to "+n);
							break;
						}
					}
				}
				for(int i = 0; i < natures.getItemCount(); ++i)
					if(natures.getItemAt(i).split(" ")[0].equals(pony.getNature().toString())) {
						natures.setSelectedIndex(i);
						break;
					}
				/* Check EVs are less or equal than TOT_EV */
				int totEVs = 0;
				for(int i = 0; i < 6; ++i)
					totEVs += ev[i];
				if(totEVs > Pony.TOT_EV) {
					if(Debug.pedantic) printDebug("EVs > "+Pony.TOT_EV+" ("+totEVs+")");
					ev[lastUpdatedEV] -= Math.min(ev[lastUpdatedEV],totEVs - Pony.TOT_EV);
				}

				/* Set EVs and IVs (and sliders) */
				final Pony.Stat[] stats = Pony.Stat.core();
				for(int i = 0; i < 6; ++i) {
					pony.setEV(stats[i],ev[i]);
					evField[i].setText(ev[i]+(positiveStatNum == i ? "+" : (negativeStatNum == i ? "-" : "")));
					pony.setIV(stats[i],iv[i]);
					ivField[i].setText(iv[i]+"");
					slider[i].setValue(pony.getEV(stats[i]));
					totStats[i].setText(pony.getStat(stats[i])+"");
				}	
				remaining.setText(""+pony.remainingEVs());

				adjustingEVs = false;
				adjustingIVs = false;
				adjustingSlider = false;
				adjustingNature = false;

				for(int i = 0; i < 6; ++i) {
					/* Set bars length and color */
					int stat = pony.getStat(stats[i]);
					if(i == 0) {
						((Rectangle)bar[i].getShape()).setSize(Math.min(BAR_MAX_LENGTH,stat*90/140),10);
						bar[i].setColor(new Color(
							Math.max(0,Math.min(1,1-(stat-pony.getLevel())/400f)),
							Math.max(0,Math.min(1,(stat-pony.getLevel())/400f)),
							(stat-pony.getLevel() > 300 
							 	? Math.max(0,Math.min(1,(stat-pony.getLevel()-300)/200f)) 
								: 0)
						));
					} else {
						((Rectangle)bar[i].getShape()).setSize(Math.min(BAR_MAX_LENGTH,stat*90/100),10);
						bar[i].setColor(new Color(
							Math.max(0,1-stat/400f),
							Math.min(1,stat/400f),
							(stat > 300 ? Math.min(1,(stat-300)/200f) : 0)
						));
					}
				}
				/* Update preview panels, if any */
				for(FancyStatsPreviewPanel pp : previewPanels)
					updatePreviewPanel(pp);

				if(Debug.pedantic) printDebug("ENDED REFRESH()");
			}
		});
	}

	public class FancyStatsPreviewPanel extends JPanel {
		
		private ShapeComponent[] prevBar;
		private JLabel[] prevEV;

		public FancyStatsPreviewPanel(ShapeComponent[] sc,JLabel[] ev) {
			super(true);
			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.insets = new Insets(5,2,5,2);
			c.fill = GridBagConstraints.BOTH;
			c.gridwidth = 4;
			add(new JLabel("EV Spread"),c);
			c.gridy = 1;
			c.gridx = 0;
			c.anchor = GridBagConstraints.LINE_START;
			//add(new ShapeComponent(new Rectangle(60,5),Color.WHITE,0f),c);
			prevBar = sc;
			prevEV = ev;
			
			for(int i = 0; i < 6; ++i) {
				c.gridwidth = 4;
				c.anchor = GridBagConstraints.LINE_START;
				c.insets = new Insets(5,2,5,2);
				add(sc[i],c);
				c.gridwidth = 1;
				c.gridx += 4;
				c.anchor = GridBagConstraints.LINE_END;
				c.ipady = 0;
				c.insets = new Insets(0,5,0,5);
				add(ev[i],c);
				c.gridy++;
				c.gridx = 0;
			}				

			setBorder(new CompoundBorder(BorderFactory.createRaisedBevelBorder(),new EmptyBorder(4,4,4,4)));
		}

		public ShapeComponent[] getPrevBars() { return prevBar; }
		public JLabel[] getPrevEV() { return prevEV; }
	}

	public FancyStatsPreviewPanel getStatsPreviewPanel() {
		ShapeComponent[] prevBar = new ShapeComponent[6];
		JLabel[] prevEV = new JLabel[6];

		final Pony.Stat[] stats = Pony.Stat.core();
		for(int i = 0; i < 6; ++i) {
			prevBar[i] = new ShapeComponent(new Rectangle(0,5));
			prevBar[i].setColor(bar[i].getColor());
			((Rectangle)prevBar[i].getShape()).setSize((int)(((Rectangle)bar[i].getShape()).getWidth() / 2f),5);
			if(pony != null) 
				prevEV[i] = new JLabel("<html><small>" + 
							(pony.getEV(stats[i]) != 0 
								? pony.getEV(stats[i]) + natureModString(i)
								: natureModString(i))
							+ "</small></html>"
						);
			else
				prevEV[i] = new JLabel("");
		}

		FancyStatsPreviewPanel prevPanel = new FancyStatsPreviewPanel(prevBar,prevEV);
		previewPanels.add(prevPanel);
		return prevPanel;
	}

	public void updatePreviewPanel(FancyStatsPreviewPanel prevPanel) {
		ShapeComponent[] prevBar = prevPanel.getPrevBars();
		JLabel[] prevEV = prevPanel.getPrevEV();
		final Pony.Stat[] stats = Pony.Stat.core();
		for(int i = 0; i < 6; ++i) {
			prevBar[i].setColor(bar[i].getColor());
			((Rectangle)prevBar[i].getShape()).setSize((int)(((Rectangle)bar[i].getShape()).getWidth() / 2f),5);
			if(pony != null) 
				prevEV[i].setText("<html><small>" + 
						(pony.getEV(stats[i]) != 0 
								? pony.getEV(stats[i]) + natureModString(i)
								: natureModString(i))
							+ "</small></html>"
						);
			else
				prevEV[i].setText("");
		}
	}

	private String natureModString(int statnum) {
		if(pony.getNature().increasedStat() == null) return "";
		return pony.getNature().increasedStat().equals(Pony.Stat.core()[statnum])
			? "+"
			: pony.getNature().decreasedStat().equals(Pony.Stat.core()[statnum])
				? "-"
				: "";
	}
}
