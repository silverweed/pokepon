//: gui/GradientButton.java

package pokepon.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import pokepon.util.*;
import static pokepon.util.MessageManager.*;

/** A JButton with a gradient background; code inspired by cyclops (stackoverflow.com)
 *
 * @author silverweed
 */
public class GradientButton extends JToggleButton {

	private Color bgColor;
	private Color bgColor2 = Color.WHITE;
	private boolean toggleable = true;

	public GradientButton() {
		bgColor = Color.GRAY;
		setContentAreaFilled(false);
		setFocusPainted(false);
	}

	public GradientButton(Color bgColor) {
		this();
		this.bgColor = bgColor;
	}

	public GradientButton(final Color bgColor, final String text) {
		super(text);
		this.bgColor = bgColor;
		setContentAreaFilled(false);
		setFocusPainted(false); 
	}

	public GradientButton(final Color bgColor, final Color bgColor2, final String text) {
		this(bgColor,text);
		this.bgColor2 = bgColor2;
	}
	
	/** Sets whether this button should stay clicked (like a toggle button,
	 * default) or not.
	 */
	public void setToggleable(boolean bool) {
		toggleable = bool;
	}

	public void setBackground(Color bgColor) {
		this.bgColor = bgColor;
	}

	public Color getBackground() {
		return bgColor;
	}

	@Override
	protected void paintComponent(Graphics g) {
		if(Debug.pedantic) printDebug("Called paintComponent on GradientButton "+
					getClass().getSimpleName()+"; selected="+isSelected());
		Graphics2D g2 = (Graphics2D)g.create();

		if(toggleable && isSelected()) {
			g2.setPaint(new GradientPaint(
				new Point(0,0), 
				bgColor2.darker(), 
				new Point(0,getHeight()), 
				bgColor.darker()));
		} else {
			g2.setPaint(new GradientPaint(
				new Point(0,0), 
				bgColor2, 
				new Point(0,getHeight()), 
				bgColor));
		}
		g2.fillRect(0,0,getWidth(),getHeight());
		g2.dispose();

		super.paintComponent(g);
	}
}
