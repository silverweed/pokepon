package pokepon.main;

import pokepon.gui.*;
import pokepon.main.TestingClass;
import pokepon.enums.Type;
import javax.swing.*;
import java.awt.*;

public class ColorsTest extends JFrame implements TestingClass {
	
	public ColorsTest() {
		setLayout(new FlowLayout());

		for(pokepon.enums.Type t : pokepon.enums.Type.values()) {
			GradientButton b = new GradientButton(t.getBGColor(),t+"");
			//b.setBackground(t.getBGColor());
			b.setForeground(t.getFGColor());
			add(b);
		}
	}

	public static void main(String[] args) {
		SwingConsole.run(new ColorsTest(),400,600);
	}
}
