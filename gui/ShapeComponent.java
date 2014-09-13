package pokepon.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.AlphaComposite;
import javax.swing.JComponent;

/**
 *  A component that will paint a Shape object. Click detection will be
 *  determined by the Shape itself, not the bounding Rectangle of the Shape.
 *
 *  Shape objects can be created with an X/Y offset. These offsets will
 *  be ignored and the Shape will always be painted at (0, 0) so the Shape is
 *  fully contained within the component.
 *
 *  The foreground color will be used to "fill" the Shape.
 *
 * @author camickr (StackOverflow) [with some additions by Giacomo Parolini]
 */
public class ShapeComponent extends JComponent
{
    private Shape shape;
    private boolean antiAliasing = true;
    private float opacity = 1f;

    /**
     *  Create a ShapeComponent that is painted black.
     *
     *  @param shape the Shape to be painted
     */
    public ShapeComponent(Shape shape)
    {
        this(shape, Color.BLACK);
    }

    /**
     *  Create a ShapeComponent that is painted filled and outlined.
     *
     *  @param shape the Shape to be painted
     *  @param color the color of the Shape
     */
    public ShapeComponent(Shape shape, Color color)
    {
        setShape( shape );
        setForeground( color );

        setOpaque( false );
    }

    public ShapeComponent(Shape shape,Color color,float opacity) {
    	this(shape,color);
	this.opacity = opacity;
   }

    public void setColor(Color color) {
	setForeground(color);
    }

    public Color getColor() {
    	return getForeground();
    }

    /**
     *  Get the Shape of the component
     *
     *  @return the the Shape of the compnent
     */
    public Shape getShape()
    {
        return shape;
    }

    /**
     *  Set the Shape for this component
     *
     *  @param shape the Shape of the component
     */
    public void setShape(Shape shape)
    {
        this.shape = shape;
        revalidate();
        repaint();
    }

    /**
     *  Use AntiAliasing when painting the shape
     *
     *  @return true for AntiAliasing false otherwise
     */
    public boolean isAntiAliasing()
    {
        return antiAliasing;
    }

    /**
     *  Set AntiAliasing property for painting the Shape
     *
     *  @param antiAliasing true for AntiAliasing, false otherwise
     */
    public void setAntiAliasing(boolean antiAliasing)
    {
        this.antiAliasing = antiAliasing;
        revalidate();
        repaint();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getPreferredSize()
    {
        //  Include Border insets and Shape bounds

        Insets insets = getInsets();
        Rectangle bounds = shape.getBounds();

        //  Determine the preferred size

        int width = insets.left + insets.right + bounds.width;
        int height = insets.top + insets.bottom + bounds.height;

        return new Dimension(width, height);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getMinimumSize()
    {
        return getPreferredSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getMaximumSize()
    {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        //  Graphics2D is required for antialiasing and painting Shapes

        Graphics2D g2d = (Graphics2D)g.create();

        if (isAntiAliasing())
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //  Shape translation (ie. non-zero X/Y position in bounding rectangle)
        //  and Border insets.

        Rectangle bounds = shape.getBounds();
        Insets insets = getInsets();

        //  Do all translations at once

        g2d.translate(insets.left - bounds.x, insets.top - bounds.y);

	// If opacity < 1, set composite to draw transparent shape
	if(opacity < 1f) 
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,opacity));

        //  Fill the Shape
        g2d.fill( shape );

        g2d.dispose();
    }

    /**
     *  Determine if the point is in the bounds of the Shape
     *
     * {@inheritDoc}
     */
    @Override
    public boolean contains(int x, int y)
    {
        Rectangle bounds = shape.getBounds();
        Insets insets = getInsets();

        //  Check to see if the Shape contains the point. Take into account
        //  the Shape X/Y coordinates, Border insets and Shape translation.

        int translateX = x + bounds.x - insets.left;
        int translateY = y + bounds.y - insets.top;

        return shape.contains(translateX, translateY);
    }
}
