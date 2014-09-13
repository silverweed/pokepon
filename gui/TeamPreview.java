//: gui/TeamPreview.java

package pokepon.gui;

import pokepon.player.*;
import static pokepon.util.MessageManager.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import javax.imageio.*;

public class TeamPreview extends JPanel {

	public TeamPreview(Team team) {
		setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = Team.MAX_TEAM_SIZE;
		c.anchor = GridBagConstraints.LINE_START;
		add(new JLabel(team.getName()),c);
		
		c.gridwidth = 1;
		c.gridy = 1;
		c.insets = new Insets(2,2,0,2);
		c.anchor = GridBagConstraints.CENTER;

		for(int i = 0; i < Team.MAX_TEAM_SIZE; ++i) {
			if(team.getPony(i) != null) {
				try {
					ImageIcon img = new ImageIcon(ImageIO.read(team.getPony(i).getFrontSprite()).getScaledInstance(25,-1,Image.SCALE_SMOOTH));
					add(new JLabel(img),c);
				} catch(IOException e) {
					printDebug("[TeamPreview] IOException: "+e);
				}
			} else {
				add(new JLabel(""),c);
			}
			++c.gridx;
		}
	}

}
