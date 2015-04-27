//: gui/animation/Direct.java

package pokepon.gui.animation;

import pokepon.gui.animation.*;
import pokepon.util.Debug;
import static pokepon.util.MessageManager.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.*;

public class Direct extends AttackAnimation {
	
	private boolean accelerated;
	private int initialY;
	private int finalY;
	private boolean passThrough;
	private double accelerationRate = 1;

	/** @param opts Opts: 
	 * <ul>
	 *   <li>accelerated: if true, the motion is accelerated, else it's uniform (default)</li>
	 *   <li>accelerationRate: if accelerated == true, determines how fast the acceleration is;
	         speed is computed as <code>v = maxV * |x - initialX|^accelerationRate / |initialX - finalX|^accelerationRate</code>,
		 so this value should be near 1 (e.g. 1.1 or 1.5)</li>
	 *   <li>passThrough: if true, the sprite's motion will pass through the opponent's bounds. 
	         Cannot be used along with bounceBack.</li>
	 * </ul>
	 */
	public Direct(final JComponent panel,Map<String,Object> opts) {
		super(panel,opts);
		if(delay == -1)
			delay = 30;
		initialX = (int)(usedByAlly ? allyBounds.getX() + allyBounds.getWidth()/2 : oppBounds.getX() + oppBounds.getWidth()/2);
		initialY = (int)(usedByAlly ? allyBounds.getY() - allyBounds.getHeight()/2 : oppBounds.getY() + oppBounds.getHeight()/2);
		finalX = (int)(	usedByAlly ?
				oppBounds.getX() + oppBounds.getWidth()/2 + (avoided ? -130 : 0) :
				allyBounds.getX() + allyBounds.getWidth()/2 + (avoided ? 90 : 0)
				);
		finalY = (int)((!usedByAlly) ? allyBounds.getY() - allyBounds.getHeight()/2 : oppBounds.getY() + oppBounds.getHeight()/2);
		forward = usedByAlly;
		rightLimit = usedByAlly ? finalX : initialX;
		leftLimit = usedByAlly ? initialX : finalX;
		for(Map.Entry<String,Object> entry : opts.entrySet()) {
			if(entry.getKey().equals("accelerated")) 
				accelerated = (Boolean)entry.getValue();

			else if(entry.getKey().equals("accelerationRate"))
				accelerationRate = (Double)entry.getValue();

			else if(entry.getKey().equals("passThrough"))
				passThrough = true;
		}
		if(passThrough) {
			bounceBack = false;
			accelerated = true;
			minV = 25;
			accelerationRate = 2;
			rightLimit = 2*panel.getWidth();
			leftLimit = -panel.getWidth();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int x = sprite.getX();
		int v = (int)(accelerated
				? maxV * Math.pow(Math.abs(x - initialX),accelerationRate)/
					Math.pow(Math.abs(initialX - finalX),accelerationRate)
				: maxV - minV
			) + minV;

		x += (forward ? 1 : -1) * v;
		int y = (int)(allyBounds.getY() + (x - allyBounds.getX()) *
				(oppBounds.getY() - allyBounds.getY()) / (oppBounds.getX() - allyBounds.getX()));

		if(Debug.pedantic) 
			printDebug("x = "+x+", y = "+y+", initialX = "+initialX+", finalX = "+
				finalX+", v = "+v+", rightLimit="+rightLimit+",leftLimit="+leftLimit);
		sprite.setLocation(x + horizOffset, /*(int)(allyBounds.getY()*(1-y))*/ y + vertOffset);
		panel.repaint();

		if(!passThrough) {
			if(forward && x > rightLimit || !forward && x < leftLimit) {
				x = finalX;
				forward = !forward;
				++count;
				if(!bounceBack) ++count;
				if(Debug.pedantic) printDebug("Bounced. count = "+count);
			}
		} else {
			if(x > rightLimit || x < leftLimit) {
				if(Debug.on) printDebug("(passThrough) x= "+x+", width="+panel.getWidth());
				count = 2;
			}
		}

		if(count > 1) {
			if(bounceBack || passThrough) {
				sprite.setLocation((int)originalBounds.getX(),(int)originalBounds.getY());
				panel.repaint();
			}
			terminate(e);
		}
	}
}
