//: gui/AttackAnimation.java

package pokepon.gui.animation;

import pokepon.gui.animation.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import pokepon.util.Debug;
import static pokepon.util.MessageManager.*;

/** This class provides a pool of generic utilities which are often
 * used in attack animations.
 *
 * @author silverweed
 */
public abstract class AttackAnimation extends BasicAnimation {
	
	/** Max speed of sprite motion */
	protected int maxV = 45;
	/** Min speed of sprite motion */
	protected int minV = 15;
	/** Utility generic counter */
	protected byte count;
	protected int horizOffset;
	protected int vertOffset;
	protected int finalX;
	protected boolean bounceBack = true;
	protected boolean forward = true;
	protected boolean avoided;
	protected int leftLimit, rightLimit;

	/** @param opts Opts: 
	 * <ul>
	 *   <li>bounceBack: if true (default), a point-to-point animation will trace its path backwards
	         after hitting the final point, else it will just stop there.</li>
	 *   <li>horizOffset: offsets the entire animation on the horizontal axis</li>
	 *   <li>vertOffset: offsets the entire animation on the vertical axis</li>
	 * </ul>
	 */
	public AttackAnimation(final javax.swing.JComponent panel,Map<String,Object> opts) {
		super(panel,opts);
		for(Map.Entry<String,Object> entry : opts.entrySet()) {
			if(entry.getKey().equals("bounceBack"))
				bounceBack = (Boolean)entry.getValue();
			else if(entry.getKey().equals("horizOffset"))
				horizOffset = (Integer)entry.getValue();
			else if(entry.getKey().equals("vertOffset"))
				vertOffset = (Integer)entry.getValue();
			else if(entry.getKey().equals("avoided"))
				avoided = (Boolean)entry.getValue();
		}
	}
	
	public abstract void actionPerformed(ActionEvent e);
}
