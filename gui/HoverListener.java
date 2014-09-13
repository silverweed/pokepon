//: gui/HoverListener.java

package pokepon.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/** A MouseListener providing color-change effect on mouse hover 
 * for JButtons.
 *
 * @author Giacomo Parolini
 */
public class HoverListener extends MouseAdapter {
	
	private Color origBgColor;
	private AbstractButton button;
	private boolean hoverDarker;

	public HoverListener(final AbstractButton btn) {
		button = btn;
		origBgColor = button.getBackground();
	}

	/** Determines if the color should get brighter (default) or darker
	 * on hover.
	 */
	public void setHoverDarker(boolean bool) {
		hoverDarker = bool;
	}

	public void mouseEntered(MouseEvent e) {
		origBgColor = button.getBackground();
		if(hoverDarker)
			button.setBackground(origBgColor.darker());
		else
			button.setBackground(origBgColor.brighter().brighter());
		button.repaint();
	}
	public void mouseExited(MouseEvent e) {
		button.setBackground(origBgColor);
		button.repaint();
	}
}
