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
	protected boolean usedByAlly = true;
	protected boolean persistent;
	protected Rectangle originalBounds;
	/** If true, the animations supporting it will put the sprite back
	 * to its original location after the animation ends.
	 */
	protected boolean rewind;
	/** If non-null, the animations supporting it will put the sprite
	 * at this location after the animation ends.
	 */
	protected Point rewindTo;
	/** The number of cycles used by the animation to complete; total duration
	 * of the animation will be delay * numIterations.
	 */
	protected float numIterations = 10f;

	/** @param panel The component where to perform the animation
	 * @param opts A map { name of option: opt value };
	 * Basic options (bold is mandatory):
	 * <ul>
	 *   <li><b>sprite</b>: the sprite to animate, a JComponent</li>
	 *   <li><b>allyBounds</b>: the Rectangle bounding the ally sprite</li>
	 *   <li><b>oppBounds</b>: the Rectangle bounding the opponent's sprite</li>
	 *   <li>usedByAlly: if true (default), it is assumed that the animation is being used by the "ally pony"</li>
	 *   <li>persistent: if false (default), the sprite gets removed from the panel after animation ends</li>
	 *   <li>delay: the delay between frames</li>
	 *   <li>rewind: (for the animations using it): if true, the sprite is put back at its 
	         original position/opacity after the animation ends.</li>
	 *   <li>rewindTo: (for the animations using it): if non-null, the sprite is put at
	         the specified Point (with its initialOpacity) at the end of the animation.</li>
	 *   <li>iterations: the number of frames that take to the animation to complete (default: 10)</li>
	 * </ul>
	 */
	public BasicAnimation(final JComponent panel,Map<String,Object> opts) {
		this.panel = panel;
		Iterator<Map.Entry<String,Object>> it = opts.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry<String,Object> entry = it.next();
			if(entry.getKey().equals("sprite")) {
				sprite = (JComponent)entry.getValue();

			} else if(entry.getKey().equals("delay")) {
				delay = (Integer)entry.getValue();

			} else if(entry.getKey().equals("allyBounds")) {
				allyBounds = (Rectangle)entry.getValue();

			} else if(entry.getKey().equals("oppBounds")) {
				oppBounds = (Rectangle)entry.getValue();

			} else if(entry.getKey().equals("usedByAlly")) {
				usedByAlly = (Boolean)entry.getValue();

			} else if(entry.getKey().equals("rewind")) {
				rewind = (Boolean)entry.getValue();

			} else if(entry.getKey().equals("rewindTo")) {
				rewindTo = parseShift((String)entry.getValue());

			} else if(entry.getKey().equals("iterations")) {
				numIterations = (Float)entry.getValue();

			} else if(entry.getKey().equals("persistent")) {
				persistent = (Boolean)entry.getValue();

			} else {
				continue; // keep this entry in the map
			}
			// remove the processed entry
			it.remove();
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
	 * usedByAlly, allyBounds and oppBounds must be properly set;
	 * the meaning of 'phrase' is the following:
	 * <ul>
	 *   <li>ally +100X -100Y: is just [ally point] + 100 x - 100 y</li>
	 *   <li>ally b100X f100Y: is [ally point] (back) 100 x (front) 100 y,
	 *       where 'back' and 'front' are relative to the animation user's POV,
	 *       e.g if the user is the opponent pony, "opp f100X" means an absolute
	 *       'ally +100X', whereas if the user is the ally pony, "opp b100X" is
	 *       translated as absolute 'opp +100X'. In the end, use +/- if you want
	 *       absolute coordinates, and use b/f if you want relative ones.</li>
	 * </ul>
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
