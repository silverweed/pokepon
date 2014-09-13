//: gui/StatusLabel.java

package pokepon.gui;

import static pokepon.pony.Pony.Status;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

class StatusLabel extends GridLabel {

	private Status status;

	/** @param status The type of Status
	 * @param gridx The x coordinate of this label in the grid
	 * @param gridy The y coordinate of this label in the grid
	 */
	public StatusLabel(Status status,int gridx,int gridy) {
		
		super(gridx,gridy,1,1);

		this.status = status;

		setForeground(Color.WHITE);
		setOpaque(true);
		setHorizontalAlignment(JLabel.CENTER);

		switch(status) {
			case PARALYZED:
				setText("PAR");
				setBackground(new Color(0xCFA600));
				setBorder(new LineBorder(new Color(0xA18100)));
				break;
			case POISONED:
				setText("PSN");
				setBackground(new Color(0x9900CC));
				setBorder(new LineBorder(new Color(0x5C007A)));
				break;
			case INTOXICATED:
				setText("TOX");
				setBackground(new Color(0x9900CC));
				setBorder(new LineBorder(new Color(0x5C007A)));
				break;
			case BURNED:
				setText("BRN");
				setBackground(new Color(0xCC0000));
				setBorder(new LineBorder(new Color(0x7A0000)));
				break;
			case PETRIFIED:
				setText("PTR");
				setBackground(new Color(0x666699));
				setBorder(new LineBorder(new Color(0x47476B)));
				break;
			case ASLEEP:
				setText("SLP");
				setBackground(new Color(0xA3A3C2));
				setBorder(new LineBorder(new Color(0x7575A3)));
				break;
			default:
				setText("???");
				setBackground(Color.BLACK);
				setBorder(new LineBorder(Color.BLACK));
		}
	}

	public StatusLabel(Status status) {
		this(status,0,0);
	}

	public Status getStatus() { return status; }

	public String toString() {
		return "Status Label: {text="+getText()+",gridx="+gridx+",gridy="+gridy+"}";
	}

}
