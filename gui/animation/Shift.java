//: gui/animation/Shift.java

package pokepon.gui.animation;

import pokepon.gui.animation.*;
import pokepon.util.Debug;
import pokepon.gui.*;
import static pokepon.util.MessageManager.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.Point;
import java.util.*;

/** This animation is like {link pokepon.gui.animation.Fade}, but only
 * moves the sprite without changing its opacity; as such, it doesn't
 * require the sprite to be a TransparentLabel.
 * @author silverweed
 */
public class Shift extends BasicAnimation {

	protected boolean forward;
	protected Point initialPoint, finalPoint;
	protected int vx, vy;
	protected float initialDistance;
	protected boolean accelerated;
	protected float perc, prevPerc;

	/** @param opts Opts: 
	 * <ul>
	 *   <li>initialPoint: the point where the animation starts (gets parsed if String, see {@link pokepon.gui.animation.BasicAnimation#parseShift})</li>
	 *   <li>finalPoint: the point where the animation ends (gets parsed if String)</li>
	 *   <li>rewind: if true, the sprite is put back at its original position/opacity after the animation ends.</li>
	 *   <li>rewindTo: if non-null, the sprite is put at the specified Point (with its initialOpacity) at the end of the animation.</li>
	 *   <li>accelerated: if true, the motion is uniformly accelerated, else it's uniform (default)</li>
	 * </ul>
	 */
	public Shift(final JComponent panel,Map<String,Object> opts) {
		super(panel, opts);
		if(delay == -1) delay = 25;
		Iterator<Map.Entry<String,Object>> it = opts.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry<String,Object> entry = it.next();
			if(entry.getKey().equals("accelerated")) {
				accelerated = (Boolean)entry.getValue();

			} else if(entry.getKey().equals("initialPoint")) {
				if(entry.getValue() instanceof String) 
					initialPoint = parseShift((String)entry.getValue());
				else 
					initialPoint = (Point)entry.getValue();
				
			} else if(entry.getKey().equals("finalPoint")) {
				if(entry.getValue() instanceof String) 
					finalPoint = parseShift((String)entry.getValue());
				else 
					finalPoint = (Point)entry.getValue();

			} else {
				continue;
			}
			it.remove();
		}
		
		if(initialPoint == null)
			initialPoint = sprite.getLocation();
			
		if(initialPoint == null || finalPoint == null)
			throw new RuntimeException("[Shift] initialPoint or finalPoint not set!");

		if(Debug.on) printDebug("initialPoint = "+initialPoint+"\nfinalPoint = "+finalPoint);
		sprite.setLocation((int)initialPoint.getX(),(int)initialPoint.getY());

		vx = (int)((float)(finalPoint.getX() - initialPoint.getX())/numIterations);
		vy = (int)((float)(finalPoint.getY() - initialPoint.getY())/numIterations);
		initialDistance = (float)distance((int)initialPoint.getX(),(int)initialPoint.getY(),finalPoint);
		perc = 1f;
	}

	protected double distance(int x,int y,Point p) {
		return Math.sqrt(Math.pow(x - p.getX(),2) + Math.pow(y - p.getY(),2)); 
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int x = sprite.getX();
		int y = sprite.getY();
		prevPerc = perc;
		perc = (float)distance(x,y,finalPoint) / initialDistance;
		if(accelerated) {
			vx *= 1.4;
			vy *= 1.4;
		}
		
		x += vx;
		y += vy;
		// update sprite location
		sprite.setLocation(x, y); 
		panel.validate();
		panel.repaint();

		if(prevPerc < perc) {
			if(!persistent) {
				sprite.setLocation(finalPoint);
			} else {
				if(rewind) {
					if(Debug.on) printDebug("[Shift] rewinding.");
					sprite.setLocation(initialPoint);
				} else if(rewindTo != null) {
					if(Debug.on) printDebug("[Shift] rewinding to "+rewindTo+".");
					sprite.setLocation(rewindTo);
				} else {
					sprite.setLocation(finalPoint);
				}
			}
			terminate(e);
		}
	}
}
