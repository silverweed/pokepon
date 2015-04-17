//: gui/animation/Whirl.java

package pokepon.gui.animation;

import pokepon.gui.animation.*;
import pokepon.util.Debug;
import static pokepon.util.MessageManager.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.awt.Point;

/** An animation that makes the sprite whirl around a given point.
 * 
 * @author , silverweed
 */
public class Whirl extends BasicAnimation {

	/** Determines the formula used to compute velocity */
	private int count = 0;
	private int shakes = 1;
	private float t = 0;
	private int period = 400;
	private int radius = 30;

	/** Whirl constructor
	 * @param opts Opts:
	 * <ul>
	 *   <li>shakes: how many times to whirl around the center (default: 1)</li>
	 *   <li>center: the central point of the circle (default: sprite X/Y, gets parsed if String: see {@link BasicAnimation#parseShift})</li>
	 *   <li>radius: the radius of the circle, in pixels (default: 30)</li>
	 * </ul>
	 */ 
	public Whirl(final JComponent panel,Map<String,Object> opts) {
		super(panel,opts);
		if(delay == -1) delay = 10;
		initialX = sprite.getX();
		initialY = sprite.getY();
		for(Map.Entry<String,Object> entry : opts.entrySet()) {
			if(entry.getKey().equals("shakes")) {
				shakes = (Integer)entry.getValue();
			} else if(entry.getKey().equals("center")) {
				Point pt = new Point();
				if(entry.getValue() instanceof String)
					pt = parseShift((String)entry.getValue());
				else
					pt = (Point)entry.getValue();
				initialX = (int)pt.getX();
				initialY = (int)pt.getY();
			} else if(entry.getKey().equals("radius")) {
				radius = (Integer)entry.getValue();
			}
		}
		sprite.setLocation(initialX-radius, initialY);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		double omega = 2*Math.PI/period;
		int x = sprite.getX(), y = sprite.getY();
	
		// update sprite location
		x -= radius*omega*delay*Math.cos(-omega*t);
		y += radius*omega*delay*Math.sin(-omega*t);
		t += delay;
		sprite.setLocation(x, y);

		if(Debug.pedantic) {
			printDebug("Location: "+sprite.getLocation());
			printDebug("t: "+t);
		}
		panel.repaint();
					
		if(t % period == (int)(period*.25)) 
			sprite.setLocation(initialX-radius,initialY-radius);
		if(t % period == (int)(period*.5)) 
			sprite.setLocation(initialX,initialY-2*radius);
		if(t % period == (int)(period*.75)) 
			sprite.setLocation(initialX+radius,initialY-radius);
		if(t % period == 0) 
			sprite.setLocation(initialX,initialY);
		if(t >= period*shakes) {
			// put the sprite to its original coordinates
			sprite.setLocation(initialX,initialY);
			panel.repaint();
			terminate(e);
		}
	}
}

