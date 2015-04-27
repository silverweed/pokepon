//: gui/PokeponPreviewer.java

package pokepon.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.html.*;
import pokepon.util.*;
import pokepon.pony.*;
import static pokepon.util.MessageManager.*;

/** Base abstract class for the previewers used in the Teambuilder
 *
 * @author silverweed
 */
public abstract class PokeponPreviewer extends JPanel {

	public PokeponPreviewer() {
		super(true);	//enables double buffering
		setLayout(new BorderLayout());
		_items.setFixedCellHeight(30);
		add(new JScrollPane(_items));
	}

	public PokeponPreviewer(Pony pony) {
		this();
		this.pony = pony;
	}

	public abstract void showPreview(final String str);

	public Pony getPony() { return pony; }
	public void setPony(Pony pony) { this.pony = pony; }
	public JList<Object> getPreviewerList() { return _items; } 

	protected DefaultListModel<Object> listModel = new DefaultListModel<>();
	/** List of _items to preview */
	protected PreviewerList<Object> _items = new PreviewerList<Object>(listModel);
	protected ListCellRenderer<Object> renderer;
	protected Pony pony;

	/** A JList endued with a 'selectedIndex' field, used mainly to highlight
	 * it on mouse hover (highlighting is done in the child classes by the
	 * renderer).
	 */
	protected class PreviewerList<T> extends JList<T> {
		protected int selectedIndex = -1;
		
		public PreviewerList(ListModel<T> mod) {
			super(mod);
			addMouseListener(mouseListener);
			addMouseMotionListener(mouseMotionListener);
		}

		public void setSelectedIndex(int index) {
			selectedIndex = index;
		}

		public int getSelectedIndex() { return selectedIndex; }

		/** This singleton listener sets the selectedIndex field in the previewer list
		 * when mouse enters it.
		 */
		protected MouseListener mouseListener = new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				if(Debug.pedantic) printDebug("MouseEntered. point = "+e.getPoint());
				if(_items != null) {
					_items.setSelectedIndex(_items.locationToIndex(e.getPoint()));
					_items.repaint();
				}
			}
			@Override
			public void mouseExited(MouseEvent e) {
				if(_items != null) {
					_items.setSelectedIndex(-1);
					_items.repaint();
				}
			}
		};

		/** This singleton listener sets the selectedIndex field in the previewer list
		 * when mouse moves while in it.
		 */
		protected MouseMotionListener mouseMotionListener = new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent e) {
				if(_items != null) {
					if(Debug.pedantic) printDebug("MouseMoved. point = "+e.getPoint());
					_items.setSelectedIndex(_items.locationToIndex(e.getPoint()));
					_items.repaint();
				}
			}
			@Override
			public void mouseDragged(MouseEvent e) {}
		};
	}
}
