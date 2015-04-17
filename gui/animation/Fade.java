//: gui/animation/Fade.java

package pokepon.gui.animation;

import pokepon.gui.animation.*;
import pokepon.util.Debug;
import pokepon.gui.*;
import static pokepon.util.MessageManager.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.Point;
import java.util.*;

/** Animation used to fade in/out a sprite from a point A to B;
 * the given {@link pokepon.gui.TransparentLabel TransparentLabel} will transition from initialOpacity
 * (default: 1) to finalOpacity (default: 0), which can be explicitly
 * set, or implicitly by giving the fadeOut option.
 * 
 * @author silverweed
 */
public class Fade extends Shift {

	private TransparentLabel tSprite; 
	private float initialOpacity = 1f, finalOpacity = 0f;

	/** @param opts Opts: 
	 * <ul>
	 *   {@inheritDoc}
	 *   <li>finalOpacity: final opacity of the sprite, from 0 to 1 (default: 0)</li>
	 *   <li>initialOpacity: initial opacity of the sprite, from 0 to 1 (default: 1)</li>
	 *   <li>fadeOut: if true, the fade is fade-out (default, equivalent to initialOpacity=1f, finalOpacity=0f), else it's a fade-in</li>
	 * </ul>
	 */
	public Fade(final JComponent panel,Map<String,Object> opts) {
		super(panel,opts);
		for(Map.Entry<String,Object> entry : opts.entrySet()) {
			if(entry.getKey().equals("fadeOut")) {
				if((Boolean)entry.getValue()) {
					finalOpacity = 0f;
					initialOpacity = 1f;
				} else {
					finalOpacity = 1f;
					initialOpacity = 0f;
				}

			} else if(entry.getKey().equals("finalOpacity")) {
				finalOpacity = (Float)entry.getValue();

			} else if(entry.getKey().equals("initialOpacity")) {
				initialOpacity = (Float)entry.getValue();

			} else if(entry.getKey().equals("persistent")) {
				persistent = (Boolean)entry.getValue();
			}
		}
		
		if(!(sprite instanceof TransparentLabel))
			throw new RuntimeException("[Fade] sprite should be a TransparentLabel!");

		tSprite = (TransparentLabel)sprite;

		tSprite.setBounds((int)initialPoint.getX(),(int)initialPoint.getY(),
			sprite.getWidth(),sprite.getHeight());

		tSprite.setOpacity(initialOpacity);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int x = tSprite.getX();
		int y = tSprite.getY();
		prevPerc = perc;
		perc = (float)distance(x,y,finalPoint) / initialDistance;
		if(accelerated) {
			vx *= 1.4;
			vy *= 1.4;
		}
		
		x += vx;
		y += vy;
		// update sprite location
		tSprite.setLocation(x, y); 
		tSprite.setOpacity(finalOpacity + perc * (initialOpacity - finalOpacity));
		panel.validate();
		panel.repaint();

		if(Debug.pedantic) {
			printDebug("Location: "+tSprite.getLocation());
			printDebug("perc: "+perc);
		}

		panel.repaint();

		if(prevPerc < perc) {
			if(!persistent) {
				panel.remove(tSprite);
				sprite.setLocation(finalPoint);
			} else {
				if(rewind) {
					if(Debug.on) printDebug("[Fade] rewinding.");
					tSprite.setOpacity(initialOpacity);
					tSprite.setLocation(initialPoint);
				} else if(rewindTo != null) {
					if(Debug.on) printDebug("[Fade] rewinding to "+rewindTo+".");
					tSprite.setOpacity(initialOpacity);
					tSprite.setLocation(rewindTo);
				} else {
					tSprite.setOpacity(finalOpacity);
					tSprite.setLocation(finalPoint);
				}
			}
			if(Debug.on) printDebug("[Fade] terminating; opacity = "+tSprite.getOpacity());
			terminate(e);
		}
	}
}

