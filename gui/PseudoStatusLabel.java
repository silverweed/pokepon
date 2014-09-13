//: gui/PseudoStatusLabel.java

package pokepon.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/** This class is used to create "pseudo-status" labels, which hold record
 * of 3 properties: name, gridwidth and "goodness"; 'good' pseudo-status labels
 * are green, while 'bad' ones are red (in a Pok&#233mon Showdown-like fashion).
 *
 * @author silverweed
 */
class PseudoStatusLabel extends GridLabel {
	
	private static final int MAX_NAME_LENGTH = 15;
	private String name;
	private boolean good;

	public PseudoStatusLabel(String name,int gridWidth,boolean good,int gridx,int gridy) {
		super(gridx,gridy,gridWidth,1);
		this.name = (name.length() > MAX_NAME_LENGTH ? name.substring(MAX_NAME_LENGTH) : name);
		this.good = good;

		setOpaque(true);
		setText(name);
		setHorizontalAlignment(JLabel.CENTER);
		setBackground(good ? new Color(0xCCF5CC) : new Color(0xFFCCCC));
		setForeground(good ? new Color(0x00B800) : new Color(0xE60000));
		setBorder(new LineBorder(good ? new Color(0x00B800) : new Color(0xE60000)));
	}

	public PseudoStatusLabel(String name,int gridWidth,boolean good) {
		this(name,gridWidth,good,0,0);
	}

	public String getName() { return name; }
	public boolean isGood() { return good; }

	public void setGood(boolean good) {
		this.good = good;
		setBackground(good ? new Color(0xCCF5CC) : new Color(0xFFCCCC));
		setForeground(good ? new Color(0x00B800) : new Color(0xE60000));
		setBorder(new LineBorder(good ? new Color(0x00B800) : new Color(0xE60000)));
	}

	public String toString() {
		return "PseudoStatus {name="+name+",gridwidth="+gridwidth+",good="+good+",gridx="+gridx+",gridy="+gridy+"}";
	}
}
