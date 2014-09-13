//: gui/MovePanel.java

package pokepon.gui;

import pokepon.pony.*;
import pokepon.move.*;
import pokepon.util.*;
import static pokepon.util.MessageManager.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;

public class MovePanel extends JPanel {
/*
	private List<JTextField> move = new ArrayList<>();
	private Pony pony;

	public MovePanel() {
		setLayout(new GridLayout(Pony.MOVES_PER_PONY+1,1,0,5));

		add(new JLabel("Moves"));

		for(int i = 0; i < Pony.MOVES_PER_PONY; ++i) {
			move.add(new JTextField(20));
			mvListener[i] = new MoveNameListener(i);
			move.get(i).addActionListener(mvListener[i]);
			add(move.get(i));
		}
	}

	public MovePanel(Pony pony) {
		this();
		setPony(pony);
	}

	public void setPony(final Pony pony) {
		this.pony = pony;
		if(Debug.on) printDebug("[MovePanel] set pony to "+pony);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				for(int i = 0; i < Pony.MOVES_PER_PONY; ++i) {
					move.get(i).removeActionListener(mvListener[i]);
					move.get(i).removeAllItems();
					validate();
					repaint();
					move.get(i).addItem("");
					for(String mv : pony.getLearnableMoves().keySet()) {
						move.get(i).addItem(mv);
					}
					
					if(Debug.on) printDebug("[MovePanel] pony's move "+i+" = "+pony.getMove(i));
					if(pony.getMove(i) != null)
						move.get(i).setSelectedItem(pony.getMove(i).getName());
					else
						move.get(i).setSelectedItem("");

					move.get(i).addActionListener(mvListener[i]);
				}
			}
		});
	}

	public Pony getPony() {
		return pony;
	}

	private MoveNameListener[] mvListener = new MoveNameListener[Pony.MOVES_PER_PONY];

	private class MoveNameListener implements ActionListener {
		
		final int num;

		public MoveNameListener(final int n) {
			num = n;
		}

		public void actionPerformed(ActionEvent e) {
			String selected = (String)move.get(num).getSelectedItem();
			if(Debug.on) printDebug("[MovePanel] selected("+num+"): "+selected);
			if(selected == null || selected.equals("")) 
				pony.setMove(num,null);
			else {
				try {
					pony.setMove(num,MoveCreator.create(selected,pony));
					if(Debug.on) printDebug("[MovePanel] set pony's move #"+num+" to "+pony.getMove(num));
				} catch(ReflectiveOperationException ee) {
					printDebug("[MovePanel] Failed to create move: "+selected);
				}
			}
		}
	}

*/
}
		
