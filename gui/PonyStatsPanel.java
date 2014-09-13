//: gui/PonyStatsPanel.java

package pokepon.gui;

import pokepon.pony.*;
import javax.swing.*;
import java.awt.*;

/** Auxiliary component that displays a pony's base stats.
 *
 * @author silverweed
 */
class PonyStatsPanel extends StatsPanel {

	private Pony pony;
	private JLabel[] statsLabel = new JLabel[7];
	private JTextField[] statsValue = new JTextField[7];
	private String[] statsLabelName = { "HP", "Atk", "Def", "SpA", "SpD", "Spe", "BST" };


	public PonyStatsPanel() {
		setLayout(new GridLayout(7,2));
		for(int i = 0; i < 7; ++i) {
			statsLabel[i] = new JLabel();
			statsValue[i] = new JTextField(4);
			statsLabel[i].setText(statsLabelName[i]);
			add(statsLabel[i]);
			add(statsValue[i]);
			statsValue[i].setEditable(false);
		}
	}

	public PonyStatsPanel(Pony p) {
		this();
		setPony(p);
	}

	public Pony getPony() {
		return pony;
	}

	public void setPony(Pony p) {
		pony = p;
		statsValue[0].setText(""+p.getBaseHp());
		statsValue[1].setText(""+p.getBaseAtk());
		statsValue[2].setText(""+p.getBaseDef());
		statsValue[3].setText(""+p.getBaseSpatk());
		statsValue[4].setText(""+p.getBaseSpdef());
		statsValue[5].setText(""+p.getBaseSpeed());
		statsValue[6].setText(""+p.bst());
	}
}
