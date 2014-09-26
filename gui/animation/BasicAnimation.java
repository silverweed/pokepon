//: gui/animation/BasicAnimation.java

package pokepon.gui.animation;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import pokepon.util.Debug;
import static pokepon.util.MessageManager.*;

/** Basic class for the animations; animations have a javax.swing.Timer member whose
* ActionListener is implemented by the Animation class itself;
* the contract is that when an animation ends, it notifies
* the external world by calling notifyAll on itself;
* The policy of the children animations should be to call super(panel,opts) in the 
* constructor, then set initialX, delay and other optional parameters.
*
* @author silverweed
*/
public abstract class BasicAnimation implements ActionListener, Animation {

	protected int delay = -1;
	protected JComponent sprite;
	protected JComponent panel;
	protected Timer timer;
	protected int initialX, initialY;
	protected Rectangle allyBounds;
	protected Rectangle oppBounds;
	protected boolean persistent;
	protected boolean usedByAlly = true;
	protected Rectangle originalBounds;

	@SuppressWarnings("unchecked")
	/** @param panel The component where to perform the animation
	 * @param opts A map { name of option: opt value }
	 */
	public BasicAnimation(final JComponent panel,Map<String,Object> opts) {
		this.panel = panel;
		if(opts.containsKey("sprite")) {
				sprite = (JComponent)opts.remove("sprite");
			}
		if(opts.containsKey("delay")) {
			delay = (Integer)opts.remove("delay");
		}
		if(opts.containsKey("allyBounds")) {
			allyBounds = (Rectangle)opts.remove("allyBounds");
		} 
		if(opts.containsKey("oppBounds")) {
			oppBounds = (Rectangle)opts.remove("oppBounds");
		}
		if(opts.containsKey("persistent")) {
			persistent = (Boolean)opts.remove("persistent");
		}
		if(opts.containsKey("usedByAlly")) {
			usedByAlly = (Boolean)opts.remove("usedByAlly");
		}
		
		if(sprite != null)
			originalBounds = sprite.getBounds();

		if(Debug.pedantic) printDebug("[Animation] sprite="+(sprite == null ? "null" : "non-null")+",delay="+delay+
				",allyBounds="+allyBounds+",oppBounds="+oppBounds+",panel height="+panel.getHeight()+",panel width = "+panel.getWidth());
	}

	public void setPanel(final JPanel panel) {
		this.panel = panel;
	}

	public void start() {
		timer = new Timer(delay,this);
		timer.start();
		if(Debug.pedantic) printDebug("Started animation "+getClass().getSimpleName());
	}

	public void terminate(ActionEvent e) {
		if(Debug.on) printDebug("Stopping animation. isRunning="+((Timer)e.getSource()).isRunning());
		((Timer)e.getSource()).stop();
		((Timer)e.getSource()).removeActionListener(this);
		synchronized(this) {
			notifyAll();
		}
	}

	public JComponent getSprite() {
		return sprite;
	}

	public boolean isPersistent() {
		return persistent;
	}

	public abstract void actionPerformed(ActionEvent e);

	protected Point parseShift(String phrase) {
		String[] token = phrase.split(" ");
		Point pt = new Point(0,0);
		for(int i = 1; i < token.length; ++i) {
			byte sign = (token[i].charAt(0) == '+' ? (byte)1 : (token[i].charAt(0) == '-' ? (byte)-1 : 0));
			if(sign == 0) continue;
					
			char coord = token[i].charAt(token[i].length() - 1);
			try { 
				int shift = Integer.parseInt(token[i].substring(1,token[i].length() -1));
				switch(coord) {
					case 'Y':
					case 'y':
						pt.setLocation(pt.getX(), sign * shift);
						break;
					case 'X':
					case 'x':
						pt.setLocation(sign * shift, pt.getY());
						break;
				}

			} catch(IllegalArgumentException e) {
				printDebug("[Fade.parseShift] illegal argument: "+e);
			}
		}

		return pt;
	}
}
