//: gui/animation/Shake.java

package pokepon.gui.animation;

import pokepon.gui.animation.*;
import pokepon.util.Debug;
import static pokepon.util.MessageManager.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.*;

/** An animation that mimics the Pok&#233mon Showdown 'shake' animation.
 * 
 * @author Giacomo Parolini
 */
public class Shake extends BasicAnimation {

	/** Determines the formula used to compute velocity */
	private enum AnimStyle { CONST, LINEAR, HARMONIC }
	private AnimStyle animStyle = AnimStyle.CONST;
	private final int MAX_V = 6;
	private int motionWidth;
	private int count = 0;
	private int shakes = 3;
	private boolean forward = true;
	private byte v = 0;

	@SuppressWarnings("unchecked")
	/** @param opts Opts: motionWidth, style, count */
	public Shake(final JComponent panel,Map<String,Object> opts) {
		super(panel,opts);
		if(delay == -1) delay = 15;
		initialX = sprite.getX();
		motionWidth = 30;//(int)(sprite.getIcon().getIconWidth()/2.5);
		for(Map.Entry<String,Object> entry : opts.entrySet()) {
			if(entry.getKey().equals("motionWidth")) {
				motionWidth = (Integer)entry.getValue();
			} else if(entry.getKey().equals("style")) {
				if(((String)entry.getValue()).toLowerCase().equals("harmonic")) 
					animStyle = AnimStyle.HARMONIC;
				else if(((String)entry.getValue()).toLowerCase().equals("linear"))
					animStyle = AnimStyle.LINEAR;
			} else if(entry.getKey().equals("shakes")) {
				shakes = (Integer)entry.getValue();
			}
		}
	}

	public void setStyle(String style) {
		if(style.toLowerCase().equals("harmonic"))
			animStyle = AnimStyle.HARMONIC;
		else if(style.toLowerCase().equals("linear"))
			animStyle = AnimStyle.LINEAR;
		else if(style.toLowerCase().equals("const"))
			animStyle = AnimStyle.CONST;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int x = sprite.getX();
		if(Debug.pedantic) printDebug("x = "+x+", initialX = "+initialX+", finalX = "+(initialX+motionWidth));
	
		switch(animStyle) {
			case CONST:
				v = (byte)((forward ? 1 : -1) * MAX_V);
				break;
			case LINEAR:
				v = (byte) (MAX_V * (forward ? 1 : -1) * (float)Math.abs((1 - Math.abs(x - initialX)/(float)motionWidth)));
				break;
			case HARMONIC:
				v = (byte) (MAX_V * (forward ? 1 : -1) * Math.cos(Math.PI / (2*motionWidth) * (x - initialX)));
				break;
		}


		if(animStyle == AnimStyle.CONST) {
			if(x+v >= initialX + motionWidth) {
				x = initialX + motionWidth;
				forward = false;
				++count;
			} else if(x+v <= initialX - motionWidth) {
				x = initialX - motionWidth;
				forward = true;
				++count;
			}
		} else {
			if(v == 0) {
				forward = !forward;
				v = (byte)(forward ? 1 : -1);
				++count;
			}
		}

		if(Debug.pedantic) printDebug("v = "+v);

		// update sprite location
		x += v;
		sprite.setLocation(x,sprite.getY());

		if(Debug.pedantic) {
			printDebug("Location: "+sprite.getLocation());
			printDebug("count: "+count);
		}

		panel.repaint();

		if(Debug.pedantic) printDebug("dist = "+Math.abs(x-initialX));
		if(count > shakes && Math.abs(x - initialX) < 3) {
			// put the sprite to its original coordinates
			sprite.setLocation(initialX,sprite.getY());
			terminate(e);
		}
	}
}

