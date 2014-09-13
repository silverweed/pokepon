//: gui/StatsPanel.java

package pokepon.gui;

import pokepon.pony.Pony;
import javax.swing.JPanel;

public abstract class StatsPanel extends JPanel {

	protected Pony pony;

	public Pony getPony() {
		return pony;
	}

	public void setPony(Pony p) {
		pony = p;
	}
}
