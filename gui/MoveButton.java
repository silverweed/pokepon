//: gui/MoveButton.java

package pokepon.gui;

import pokepon.move.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/** A GradientButton which automatically changes its color and tooltip
 * according to the move assigned to it.
 *
 * @author Giacomo Parolini
 */
class MoveButton extends GradientButton {

	private Move move;

	public MoveButton(Move move) {
		super();
		setMove(move);
		addMouseListener(myMouseListener);
	}

	public void setMove(final Move _move) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				move = _move;
				if(move == null) {
					setText("");
					setBackground(Color.GRAY);
					setForeground(Color.WHITE);
					setEnabled(false);
				} else {
					setText("<html>"+(move.getName().length() > Move.MAX_NAME_LENGTH ? 
						move.getName().substring(0,Move.MAX_NAME_LENGTH) : move.getName())+
						"<br>PP: "+(move.getMaxPP() <= 0 ? "---" : move.getPP()+" / "+move.getMaxPP())+"</html>");
					setBackground(move.getType().getBGColor());
					setForeground(move.getType().getFGColor());
					setEnabled(move.getPP() != 0);
				}
			}
		});
	}

	public final Move getMove() { return move; }

	public void deductPP() {
		if(move == null) return;
		move.deductPP();
		setText("<html>"+(move.getName().length() > Move.MAX_NAME_LENGTH ? 
			move.getName().substring(0,Move.MAX_NAME_LENGTH) : move.getName())+
			"<br>PP: "+(move.getMaxPP() <= 0 ? "---" : move.getPP()+" / "+move.getMaxPP())+"</html>");
		if(move.getPP() == 0)
			setEnabled(false);
		else if(!isEnabled())
			setEnabled(true);
	}

	private MouseListener myMouseListener = new CustomToolTipListener(this) {
		@Override
		protected void setText() {
			if(move == null) return;
			text = 	"<html><b>"+move.getName()+"</b><br>"+
				"Type: "+(move.isTypeless() ? "<b>???</b>" : "<img src=\""+
					move.getType().getToken()+"\"/>")+"&nbsp;<img src=\""+
					move.getMoveType().getToken()+"\"/><br>"+
				"Damage: <b>"+(move.getBaseDamage() <= 0 ? "-" : move.getBaseDamage())+"</b><br>"+
				"Accuracy: <b>"+(move.getAccuracy() < 0 ? "-" : move.getAccuracy())+"</b><br>"+
				move.getBriefDescription()+"<br>"+
				(move.isContactMove() ? "Makes contact.<br>" : "")+
				(move.getBasePriority() != 0 ? move.getBasePriority()+" priority" : "");
		}
		@Override
		protected boolean showCondition() {
			return move != null;
		}
		// Show tooltip in fixed position, not where the cursor enters the button 
		@Override
		protected void showToolTip(MouseEvent e) {
			setText();
			toolTip.setTipText(text);
			int linecount = (text.length() - text.replaceAll("<br>", "").length()) / 4;
			int x = (int)MoveButton.this.getLocationOnScreen().getX();
			int y = (int)MoveButton.this.getLocationOnScreen().getY() - linecount*15;
			popup = popupFactory.getPopup(component,toolTip,x,y);
			popup.show();
		}
	};
}
