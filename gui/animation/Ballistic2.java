//: gui/animation/Ballistic2.java

package pokepon.gui.animation;

import pokepon.gui.animation.*;
import pokepon.util.*;
import static pokepon.util.MessageManager.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.*;

/** Like Ballistic, but return path is a straight line
 *
 * @author silverweed
 */
public class Ballistic2 extends AttackAnimation {
	
	private int topX;
	/** y coordinate of the parabula top, in units of enemy sprite's height */
	private double topY = 5/4.;
	private double aCoeff;
	private boolean bounced;

	/** @param opts Opts: 
	 * <ul>
	 *   <li><b>topY</b>: the parabula top, in units of enemy sprite's height</li>
	 * </ul>
	 */
	public Ballistic2(final JComponent panel,Map<String,Object> opts) {
		super(panel,opts);
		if(delay == -1) delay = 30;
		for(Map.Entry<String,Object> entry : opts.entrySet()) {
			if(entry.getKey().equals("topY"))
				topY = (Double)entry.getValue();
		}
		
		if(oppBounds == null || allyBounds == null) 
			throw new RuntimeException("[BallisticAnim] oppBounds or allyBounds not initialized!");

		forward = usedByAlly;

		// parameters
		initialX = (int)(
				usedByAlly ? allyBounds.getX() :
				oppBounds.getX() 
				);
		finalX = (int)(
				usedByAlly ? oppBounds.getX() + (avoided ? -70 : 0) :
				allyBounds.getX() + (avoided ? 70 : 0)
				);
		topX = (int)(allyBounds.getX() + 3./4. * (oppBounds.getX() - allyBounds.getX()));
		if(avoided && usedByAlly)
			topX -= 50;

		leftLimit = usedByAlly ? initialX : finalX;
		rightLimit = usedByAlly ? finalX : initialX;

		aCoeff = -1./ ((topX - allyBounds.getX())*(topX - allyBounds.getX()));

		if(Debug.on) printDebug("[Ballistic] Constructed. topX = "+topX+", aCoeff = "+aCoeff+",inX = "+initialX+",finX="+finalX);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		int x = sprite.getX();
		double y = sprite.getY();
		int v = (int)(maxV * (1. - (double)Math.abs(x - finalX)/Math.abs(finalX - initialX)) + minV);
		x += (forward ? v : -v);
		if(x > rightLimit) {
			x = rightLimit;
			forward = false;
			bounced = true;
			++count;
		} else if(x < leftLimit) {
			x = leftLimit;
			forward = true;
			bounced = true;
			++count;
		}

		if(bounced) {
			double m = Math.abs((finalX - initialX) / (allyBounds.getY() - oppBounds.getY()));
			double q = allyBounds.getY() - m * allyBounds.getX();
			y = (m * x + q) / (allyBounds.getY() + allyBounds.getHeight() / 2) - 1;
		} else {
			y = aCoeff * (x*x - 2*topX*x - allyBounds.getX()*(allyBounds.getX() - 2*topX));
		}
		if(Debug.pedantic) printDebug("x = "+(x/(double)topX)+", y = "+y+", v = "+v);
		
		sprite.setLocation(x + horizOffset, (int)(allyBounds.getY()*(1-y)) + vertOffset);
		/*	
		java.awt.Shape r = new java.awt.Rectangle(new java.awt.Dimension(x+5,(int)y+5));
		pokepon.gui.ShapeComponent rc = new pokepon.gui.ShapeComponent(r,java.awt.Color.RED);
		rc.setBounds(x,(int)(allyBounds.getY()*(1-y)),5,5);
		((JLayeredPane)panel).add(rc,new Integer(10));
		*/
		panel.repaint();

		if(!bounceBack && count == 1 || bounceBack && count == 2) {
			if(bounceBack) 
				sprite.setLocation((int)originalBounds.getX(),(int)originalBounds.getY());
			terminate(e);
		}

	}
}
