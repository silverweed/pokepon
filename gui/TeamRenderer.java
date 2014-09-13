//: gui/TeamRenderer.java

package pokepon.gui;

import pokepon.pony.*;
import pokepon.player.*;
import pokepon.util.Debug;
import static pokepon.util.MessageManager.*;
import javax.swing.*;
import javax.imageio.*;
import java.awt.*;
import java.io.*;

/** Class used to display a nice preview of the team in a JList or JComboBox.
 *
 * @author silverweed
 */
public class TeamRenderer implements ListCellRenderer<Team> {

	public TeamRenderer() {
		if(Debug.pedantic) printDebug("Constructed TeamRenderer.");
	}

	@Override
	public Component getListCellRendererComponent(	JList<? extends Team> list,
							Team value,
							int index,
							boolean isSelected,
							boolean cellHasFocus) {
		
		JPanel renderer = new JPanel(new GridBagLayout());

		if(Debug.pedantic) printDebug("Called TeamRenderer.getListCellRendererComponent\n"
						+"(index="+index+",\nvalue="+value+")");
		if(value == null)
			return renderer;

		if(isSelected) {
			renderer.setBackground(list.getSelectionBackground());
			renderer.setForeground(list.getSelectionForeground());
		} else {
			renderer.setBackground(list.getBackground());
			renderer.setForeground(list.getForeground());
		}

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = Team.MAX_TEAM_SIZE;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		renderer.add(new JLabel(value.getName()),c);
		
		c.gridwidth = 1;
		c.gridy = 1;
		c.insets = new Insets(2,2,0,2);
		c.anchor = GridBagConstraints.CENTER;

		for(int i = 0; i < Team.MAX_TEAM_SIZE; ++i) {
			if(value.getPony(i) != null) {
				try {
					ImageIcon img = new ImageIcon(ImageIO.read(value.getPony(i).getFrontSprite()).getScaledInstance(25,-1,Image.SCALE_SMOOTH));
					renderer.add(new JLabel(img),c);
				} catch(IOException e) {
					if(Debug.pedantic) printDebug("[TeamRenderer] IOException: "+e);
				}
			} else {
				renderer.add(new JLabel(""),c);
			}
			++c.gridx;
		}
		
		return renderer; 
	}
}
