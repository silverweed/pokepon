//: gui/ItemPreviewer.java

package pokepon.gui;

import pokepon.item.*;
import pokepon.util.*;
import pokepon.battle.EffectDealer;
import pokepon.pony.*;
import static pokepon.util.Meta.*;
import static pokepon.util.MessageManager.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import java.util.*;
import java.util.List;

/** This class implements the previewer for the items.
 *
 * @author Giacomo Parolini
 */
public class ItemPreviewer extends PokeponPreviewer {

	private List<Class<?>> items = new ArrayList<>();
	private List<Item> allItems = new ArrayList<>();

	public ItemPreviewer() {
		this(null);
	}

	public ItemPreviewer(final Pony pony) {
		super(pony);
		items = ClassFinder.findSubclasses(Meta.complete(ITEM_DIR),Item.class);
		Collections.sort(items, new Comparator<Class<?>>() {
			public int compare(Class<?> me,Class<?> other) {
				return me.getSimpleName().compareTo(other.getSimpleName());
			}
		});
		if(Debug.on) printDebug("[ItemsPreviewer] all items: "+items);
		loadItems();
		renderer = new ListCellRenderer<Object>() {
			@Override
			public Component getListCellRendererComponent(	JList<? extends Object> list,
									Object value,
									int index,
									boolean isSelected,
									boolean cellHasFocus) {
				
				if(Debug.pedantic) printDebug("Called ItemPreviewer.getListCellRendererComponent(value="+value+
					", index="+index+", selected="+isSelected+") [selIndex="+list.getSelectedIndex()+"]");
				JLabel renderer = new JLabel();

				if(value == null)
					return renderer;

				// special case (not actual item)
				if(((EffectDealer)value).getName() == null) {
					EffectDealer placeholder = (EffectDealer)value;
					renderer.setEnabled(false);
					if(placeholder.getDescription() == null) {
						renderer.setForeground(Color.GRAY);
						renderer.setText("No suggestions.");

					} else {
						// FIXME: how to correctly set the color???
						renderer.setForeground(Color.BLACK);
						//renderer.setText("<html><body style=\"height:15px;\"><p style=\"color: black; background-color: #99CCFF; font-weight: bold\">"+
						//	placeholder.getDescription()+"</p></body></html>");
						renderer.setText("== "+placeholder.getDescription()+" ==");
					}
					return renderer;
				}
				
				Item it = (Item)value;
				if(list.getSelectedIndex() == index) {
					renderer.setText("<html><body style=\"height: 20px; background-color: #CCFFFF; border: 1px ridge #CCFFFF\"><p>"+
						it.getName()+"&nbsp;&nbsp;<span style=\"color:gray; font-size: 85%\">"+
						it.getDescription()+"</span></p></body></html>");
				} else {
					renderer.setText("<html><p>"+
						it.getName()+"&nbsp;&nbsp;<span style=\"color:gray; font-size: 85%\">"+
						it.getDescription()+
						"</span></p></html>");
				}

				return renderer; 
			}
		};
		_items.setCellRenderer(renderer);
		if(Debug.on) printDebug("[ItemPreviewer] constructed with pony "+pony);
		showPreview(null);
	}

	@Override
	public void setPony(Pony pony) {
		super.setPony(pony);
		//loadPossibleAbilities();
	}
	
	@SuppressWarnings("unchecked")
	public void loadItems() {
		for(Class<?> item : items) {
			try {
				allItems.add(ItemCreator.create((Class<? extends Item>)item));
			} catch(ReflectiveOperationException e) {
				printDebug("[ItemPrev] Failed to create item: "+e);
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void showPreview(final String str) {
		if(Debug.on) printDebug("Called ItemPreviewer.showPreview("+str+"); pony = "+pony);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				listModel.clear();
				List<Item> matched = new ArrayList<>();
				for(Item it : allItems) {
					// find out if item matches string
					if(	str != null && str.length() > 0 &&
						it.getName().toLowerCase().startsWith(str.replaceAll(" ","").toLowerCase())
					) {
					matched.add(it);
				}
				}
				if(matched.size() != 0) {
					listModel.addElement(new EffectDealer() {
						public String getName() { return null; }
						public String getDescription() { return "Match"; }
					});
				}
				for(Item it : matched)
					listModel.addElement(it);
				if(allItems.size() != 0) {
					listModel.addElement(new EffectDealer() {
						public String getName() { return null; }
						public String getDescription() { return "Possible"; }
					});
				}
				outer:
				for(Item it : allItems) {
					// don't add duplicates
					inner:
					for(Item it2 : matched)
						if(it2.getName().equals(it.getName()))
							continue outer;
					listModel.addElement(it);
				}

				if(listModel.getSize() == 0)
					listModel.addElement(new EffectDealer() {
						public String getName() { return null; }
					});

				validate();
				repaint();
			}
		});
	}
}
