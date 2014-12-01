//: gui/ImageToolTip.java
// UNUSED

package pokepon.gui;

import javax.swing.*;
import javax.swing.plaf.metal.*;
import java.awt.*;

public class ImageToolTip extends JToolTip {
	public ImageToolTip(String img) {
		setUI(new ImageToolTipUI(img));
	}
}

class ImageToolTipUI extends MetalToolTipUI {
	
	private String imgSrc;
	private Image img;

	public ImageToolTipUI(String img) {
		super();
		imgSrc = img;
		this.img = new ImageIcon(imgSrc).getImage();
	}

	public void paint(Graphics g, JComponent c) {
		FontMetrics metrics = c.getFontMetrics(g.getFont());
		g.setColor(c.getForeground());
		g.drawString(((JToolTip) c).getTipText(), 1, 1);
		g.drawImage(img, 1, metrics.getHeight(), c);
	}

	public Dimension getPreferredSize(JComponent c) {
		FontMetrics metrics = c.getFontMetrics(c.getFont());
		String tipText = ((JToolTip) c).getTipText();
		if (tipText == null) 
			tipText = "";
		
		int width = SwingUtilities.computeStringWidth(metrics, tipText);
		int height = metrics.getHeight() + img.getHeight(c);

		if (width < img.getWidth(c)) 
			width = img.getWidth(c);

		return new Dimension(width, height);
	}
}
