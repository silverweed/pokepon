//: gui/RoundJTextField.java

package pokepon.gui;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

/** A rounded-border JTextField.
 * @author Harry Joy (stackoverflow.com)
 */

public class RoundJTextField extends JTextField {

    private Shape shape;
    private int arcW, arcH;

    public RoundJTextField(int size) {
	this(size, 10, 10);
    }

    public RoundJTextField(int size, int arcWidth, int arcHeight) {
        super(size);
        setOpaque(false); 
	arcW = arcWidth;
	arcH = arcHeight;
    }

    protected void paintComponent(Graphics g) {
         g.setColor(getBackground());
         g.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, arcW, arcH);
         super.paintComponent(g);
    }

    protected void paintBorder(Graphics g) {
         g.setColor(new Color(0x8080FF));
         g.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, arcW, arcH);
    }

    public boolean contains(int x, int y) {
         if (shape == null || !shape.getBounds().equals(getBounds())) {
             shape = new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, arcW, arcH);
         }
         return shape.contains(x, y);
    }
}
