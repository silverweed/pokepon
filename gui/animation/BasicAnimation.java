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
	protected boolean rewind;
	protected Point rewindTo;
	/** The number of cycles used by the animation to complete; total duration
	 * of the animation will be delay * numIterations.
	 */
	protected float numIterations = 10f;

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
		if(opts.containsKey("rewind")) {
			rewind = (Boolean)opts.remove("rewind");
		} else if(opts.containsKey("rewindTo")) {
			rewindTo = parseShift((String)opts.remove("rewindTo"));
		}
		if(opts.containsKey("iterations")) {
			numIterations = (Float)opts.remove("iterations");
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

	/** Parses a string of the form: opp/ally (+/-/f/b)[0-9]+(X/Y) and returns a
	 * Point with the shifted absolute coordinates; for a correct functioning,
	 * usedByAlly, allyBounds and oppBounds must be properly set.
	 */
	protected Point parseShift(String phrase) {
		String[] token = phrase.split(" ");
		
		if(!(token[0].equals("ally") || token[0].equals("opp"))) 
			throw new IllegalArgumentException("[parseShift] token[0] is neither ally nor opp, but "+token[0]+"!");

		Point pt = !(usedByAlly ^ token[0].equals("ally"))
				? new Point((int)allyBounds.getX(), (int)allyBounds.getY())
				: new Point((int)oppBounds.getX(), (int)oppBounds.getY());

		for(int i = 1; i < token.length; ++i) {
			byte sign = 0;
			char coord = Character.toUpperCase(token[i].charAt(token[i].length() - 1));

			switch(token[i].charAt(0)) {
				case '+':
					sign = (byte)1;
					break;
				case '-':
					sign = (byte)-1;
					break;
				case 'f':
				case 'F':
					sign = (byte)((!(usedByAlly ^ token[0].equals("ally")))
							? coord == 'X' ? 1 : -1
							: coord == 'X' ? -1 : 1
						);
					/*sign = (byte)(usedByAlly  
							? token[0].equals("ally")
								? coord == 'X' ? 1 : -1
								: coord == 'X' ? -1 : 1
							: token[0].equals("ally")
								? coord == 'X' ? -1 : 1
								: coord == 'X' ? 1 : -1
						);*/
					break;
				case 'b':
				case 'B':
					sign = (byte)((!(usedByAlly ^ token[0].equals("ally")))
							? coord == 'X' ? -1 : 1
							: coord == 'X' ? 1 : -1
						);
					break;
			}
			if(sign == 0) continue;			

			try { 
				int shift = Integer.parseInt(token[i].substring(1,token[i].length() -1));
				switch(coord) {
					case 'Y':
						pt.setLocation(pt.getX(), pt.getY() + sign * shift);
						break;
					case 'X':
						pt.setLocation(pt.getX() + sign * shift, pt.getY());
						break;
				}

			} catch(IllegalArgumentException e) {
				printDebug("[Fade.parseShift] illegal argument: "+e);
			}
		}
		
		if(Debug.pedantic) printDebug("[Fade.parseShift("+phrase+")] returning "+pt);
		return pt;
	}
}
