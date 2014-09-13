//: gui/CustomToolTipListener.java

package pokepon.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/** A base class for showing tooltips; differently from the default
 * Swing tooltips, they're shown as soon as the mouse enters the
 * component.
 *
 * @author silverweed
 */
public abstract class CustomToolTipListener extends MouseAdapter {
	protected Popup popup;
	protected PopupFactory popupFactory;
	protected JToolTip toolTip; 
	protected String text = "";
	protected JComponent component;
	
	public CustomToolTipListener(final JComponent component) {
		this.component = component;
		popupFactory = PopupFactory.getSharedInstance();
		toolTip = component.createToolTip();
		//toolTip.setAlwaysOnTop(true);
	}

	protected abstract void setText();
	protected abstract boolean showCondition();

	protected void showToolTip(MouseEvent e) {
		setText();
		toolTip.setTipText(text);
		
		int x = e.getXOnScreen();
		int y = e.getYOnScreen();
		popup = popupFactory.getPopup(component,toolTip,x,y);
		popup.show();
	}

	public void mouseEntered(MouseEvent e) {
		if(showCondition())
			showToolTip(e);
	}
	public void mouseExited(MouseEvent e) {
		if(popup != null)
			popup.hide();
	}
}
