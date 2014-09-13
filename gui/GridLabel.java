//: gui/GridLabel.java

package pokepon.gui;

import javax.swing.*;
import java.awt.Color;
import javax.swing.border.*;

class GridLabel extends JLabel {

	public int gridx, gridy;
	public int gridwidth, gridheight;

	public GridLabel(int x,int y,int width,int height) {
		gridx = x;
		gridy = y;
		gridwidth = width;
		gridheight = height;
	}

	/** @return Grid scalar coordinate of this label */
	public int pos() {
		return HPBar.HPBAR_GRIDWIDTH*gridy+gridx;
	}
}
