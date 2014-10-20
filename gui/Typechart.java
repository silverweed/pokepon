//: gui/Typechart.java

package pokepon.gui;

import pokepon.enums.Type;
import pokepon.battle.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import java.util.*;
import java.util.List;

/** A JPanel that shows the Typechart graphically.
 *
 * @author silverweed
 */
public class Typechart extends JPanel implements pokepon.main.TestingClass {

	private static final Dimension LABEL_SIZE = new Dimension(50, 50);

	public Typechart() {
		super(true); // double buffered
		setLayout(new GridLayout(Type.values().length + 1, Type.values().length + 1));
		
		List<Type> types = Arrays.asList(Type.values());
		Collections.sort(types,new Comparator<Type>() {
			public int compare(Type t1,Type t2) {
				return t1.toString().compareTo(t2.toString());
			}
		});
		JLabel corner = new RoundedLabel("");
		corner.setOpaque(true);
		add(corner);
		for(Type t : types) {
			JLabel typeName = new RoundedLabel(t.toString().substring(0, Math.min(t.toString().length(), 8)));
			typeName.setBackground(t.getBGColor());
			typeName.setForeground(t.getFGColor());
			typeName.setBorder(new TextBubbleBorder(typeName.getBackground(), 1, 5, 0));
			add(typeName);
		}
		for(Type t : types) {
			JLabel typeName = new RoundedLabel(t.toString().substring(0, Math.min(t.toString().length(), 8)));
			typeName.setBackground(t.getBGColor());
			typeName.setForeground(t.getFGColor());
			typeName.setBorder(new TextBubbleBorder(typeName.getBackground(), 1, 5, 0));
			add(typeName);
			for(Type t2: types) {
				Color bgcol = null, fgcol = null;
				float eff = TypeDealer.getEffectiveness(t,t2);
				if(eff == 0) {
					bgcol = new Color(0x222222); 
					fgcol = new Color(0xFFFF66);
				} else if(eff < 1) {
					bgcol = new Color(0xFF8888);
					fgcol = Color.RED;
				} else if(eff > 1) {
					bgcol = Color.GREEN.brighter();
					fgcol = Color.BLACK;
				} else {
					bgcol = Color.WHITE;
					fgcol = Color.WHITE;
				}
				JLabel effLab = new RoundedLabel(eff+"x");
				effLab.setBackground(bgcol);
				effLab.setForeground(fgcol);
				effLab.setBorder(new TextBubbleBorder(effLab.getBackground(), 1, 5, 0));
				add(effLab);
			}
		}
	}

	public void display() {
		JFrame f = new JFrame();
		f.add(this);
		SwingConsole.run(f, "Pokepon Typechart");
	}

	public static void main(String[] args) {
		new Typechart().display();
	}

	private class RoundedLabel extends JLabel {
		public RoundedLabel(String txt) {
			super(txt);
			setOpaque(true);
			setPreferredSize(LABEL_SIZE);
			setHorizontalAlignment(CENTER);
		}
	}
}
