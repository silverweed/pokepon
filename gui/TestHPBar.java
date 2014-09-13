package pokepon.gui;

import pokepon.util.*;
import pokepon.pony.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class TestHPBar extends JFrame {

	private HPBar hp;
	private JTextField pseudo = new JTextField(20);
	private Pony pony;

	public TestHPBar() throws Exception {
		setLayout(new FlowLayout());

		pony = PonyCreator.create("Trixie");

		hp = new HPBar(pony);
		
		JButton b1 = new JButton("Boost!");
		
		final Random rand = new Random();

		b1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String status = "atk def spatk spdef speed evasion accuracy".split(" ")[rand.nextInt(7)];
				int value = (rand.nextFloat() > 0.5f ? 1 : -1)*(1+rand.nextInt(6));
				System.out.println("STATUS: "+status+", VALUE: "+value);
				hp.boost(status,value);
				//hp.update();
				//hp.boost(status,val);
				validate();
				repaint();
			}
		});

		JButton b2 = new JButton("Add Pseudo");
		b2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String ps = pseudo.getText();
				if(ps == null || ps.length() == 0) return;
				hp.addPseudoStatus(ps,rand.nextFloat() > 0.5f);
				pseudo.setText("");
				validate();
				repaint();
			}
		});
	
		add(hp);
		add(b1);
		add(pseudo);
		add(b2);

	}

	public static void main(String[] args) throws Exception {
		SwingConsole.run(new TestHPBar());
	}
}
