//: player/Ponydex.java

package pokepon.gui;

import pokepon.pony.*;
import pokepon.enums.*;
import pokepon.battle.*;
import static pokepon.util.Meta.*;
import static pokepon.util.MessageManager.*;
import pokepon.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.net.*;

/** Provides a JList of all ponies and allows to select one of them; the selected
 * pony's characteristics are displayed via a PonyPanel.
 *
 * @author silverweed
 */
public class Ponydex extends JPanel {

	protected DefaultListModel<String> lPonies = new DefaultListModel<String>();
	protected JList<String> ponies = new JList<String>(lPonies);
	protected PonyPanel ponyPanel = new PonyPanel();
	protected Map<String,Pony> cachedPonies = new HashMap<>();

	protected ListSelectionListener poniesSelectionListener = new ListSelectionListener() {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if(e.getValueIsAdjusting()) return;		
			String selected = ponies.getSelectedValue();
			try {
				Pony pony = null;
				if(cachedPonies.get(selected) == null) {
					pony = PonyCreator.create(selected);
					if(Debug.on) printDebug("[Ponydex] Created pony "+pony.getName());
					cachedPonies.put(selected, pony);
				} else {
					pony = cachedPonies.get(selected);
					if(Debug.on) printDebug("[Ponydex] Using cached "+pony.getName());
				}
				pony.setNature(Pony.Nature.FRIENDLY);
				if(Debug.on) printDebug("[Ponydex] Set nature to Friendly.");
				ponyPanel.setPony(pony);
				validate();
			} catch(Exception ex) {
				printDebug("Caught exception: ");
				ex.printStackTrace();
			}
		}
	};

	public Ponydex() {
		super(true);	//enable double buffering
		// setup ponies list
		ponies.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		List<Class<?>> lc = ClassFinder.findSubclasses(complete(PONY_DIR),Pony.class);
		Collections.sort(lc, new Comparator<Class<?>>() {
			public int compare(Class<?> a, Class<?> b) {
				return a.getSimpleName().compareTo(b.getSimpleName());
			}
		});
		for(Class<?> cl : lc) {
			lPonies.addElement(cl.getSimpleName());
		}
		ponies.addListSelectionListener(poniesSelectionListener);
		ponies.setSelectedIndex(0);

		// add components
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(14,14,4,14);
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		//c.weighty = 1;
		c.weightx = 1;
		JScrollPane scrp = new JScrollPane(ponies);
		scrp.setMinimumSize(new Dimension(ponies.getPreferredSize().width, ponies.getSize().height));
		c.fill = GridBagConstraints.VERTICAL;
		add(scrp,c);
	
		c.gridheight = 1;
		c.gridwidth = 2;
		c.gridx = 1;
		c.weightx = 0;
		add(ponyPanel,c);
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.add(new Ponydex());
		pokepon.gui.SwingConsole.run(frame,800,600,"Ponydex");
	}

	public PonyPanel getPonyPanel() {
		return ponyPanel;
	}

	public Pony getSelectedPony() {
		return ponyPanel.getPony();
	}
}
