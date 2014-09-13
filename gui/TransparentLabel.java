//: gui/TransparentLabel.java

package pokepon.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.AlphaComposite;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import static pokepon.util.MessageManager.*;

public class TransparentLabel extends JLabel {
	protected boolean antiAliasing = true;
	protected float opacity = 1f;

	public TransparentLabel() {
		this(1f);
	}

	public TransparentLabel(JLabel lab, float opacity) {
		this(opacity);
		setIcon(lab.getIcon());
		setText(lab.getText());
	}

	public TransparentLabel(JLabel lab) {
		this(lab,1f);
	}

	public TransparentLabel(float opacity) {
		setOpaque(false);
		this.opacity = opacity;
	}

	/**
	*  Use AntiAliasing when painting the shape
	*
	*  @return true for AntiAliasing false otherwise
	*/
	public boolean isAntiAliasing() {
		return antiAliasing;
	}

	/**
	*  Set AntiAliasing property for painting the Shape
	*
	*  @param antiAliasing true for AntiAliasing, false otherwise
	*/
	public void setAntiAliasing(boolean antiAliasing) {
		this.antiAliasing = antiAliasing;
		revalidate();
		repaint();
	}
	
	public float getOpacity() {
		return opacity;
	}

	public void setOpacity(float op) {
		opacity = op;
	}

	@Override
	protected void paintComponent(Graphics g) {
		//  Graphics2D is required for antialiasing and painting Shapes
		Graphics2D g2d = (Graphics2D)g.create();

		if (isAntiAliasing())
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// If opacity < 1, set composite to draw transparent shape
		if(opacity < 1f) 
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,opacity));

		if(getIcon() != null) {
			g2d.drawImage(((ImageIcon)getIcon()).getImage(),0,0,getWidth(),getHeight(),this);
		}

		//g2d.dispose();
		super.paintComponent(g2d);
	}
}
