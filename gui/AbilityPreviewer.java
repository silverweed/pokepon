//: gui/AbilityPreviewer.java

package pokepon.gui;

import pokepon.ability.*;
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

/** This class implements the previewer for the abilities.
 *
 * @author silverweed
 */
public class AbilityPreviewer extends PokeponPreviewer {

	private static List<Class<?>> abilities = new ArrayList<>();
	private List<Ability> possibleAbilities = new ArrayList<>();

	static {
		abilities = ClassFinder.findSubclasses(Meta.complete(ABILITY_DIR),Ability.class);
		Collections.sort(abilities,new Comparator<Class<?>>() {
			public int compare(Class<?> me,Class<?> other) {
				return me.getSimpleName().compareTo(other.getSimpleName());
			}
		});
		if(Debug.on) printDebug("[AbilityPreviewer] all abilities: "+abilities);
	}

	public AbilityPreviewer() {
		this(null);
	}

	public AbilityPreviewer(final Pony pony) {
		super(pony);
		loadPossibleAbilities();
		renderer = new ListCellRenderer<Object>() {
			@Override
			public Component getListCellRendererComponent(	JList<? extends Object> list,
									Object value,
									int index,
									boolean isSelected,
									boolean cellHasFocus) {
				
				if(Debug.pedantic) printDebug("Called AbilityPreviewer.getListCellRendererComponent(value="+value+
					", index="+index+", selected="+isSelected+") [selIndex="+list.getSelectedIndex()+"]");
				JLabel renderer = new JLabel();

				if(value == null)
					return renderer;

				// special case (not actual ability)
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
				
				Ability ab = (Ability)value;
				if(list.getSelectedIndex() == index) {
					if(AbilityPreviewer.super.pony != null && !AbilityPreviewer.super.pony.canHaveAbility(ab)) {
						renderer.setText("<html><body style=\"height: 20px; background-color: #CCFFFF; border: 1px ridge #CCFFFF\"><p>"+
							ab.getName()+"&nbsp;&nbsp;"+
							"<span style=\"color: #AA2222; border: 1px solid #AA2222;\">[ Illegal ]</span>"+
							"</p></body></html>");
					} else {
						renderer.setText("<html><body style=\"height: 20px; background-color: #CCFFFF; border: 1px ridge #CCFFFF\"><p>"+
							ab.getName()+"&nbsp;&nbsp;<span style=\"color:gray; font-size: 85%\">"+
							ab.getDescription()+"</span></p></body></html>");
					}
				} else {
					if(AbilityPreviewer.super.pony != null && !AbilityPreviewer.super.pony.canHaveAbility(ab)) {
						renderer.setText("<html><p>"+
							ab.getName()+"&nbsp;&nbsp;"+
							"<span style=\"color: #AA2222; border: 1px solid #AA2222;\">[ Illegal ]</span>"+
							"</p></html>");
					} else {
						renderer.setText("<html><p>"+
							ab.getName()+"&nbsp;&nbsp;<span style=\"color:gray; font-size: 85%\">"+
							ab.getDescription()+
							"</span></p></html>");
					}
				}
				


				return renderer; 
			}
		};
		_items.setCellRenderer(renderer);
		if(Debug.on) printDebug("[AbilityPreviewer] constructed with pony "+pony);
		showPreview(null);
	}

	@Override
	public void setPony(Pony pony) {
		super.setPony(pony);
		loadPossibleAbilities();
	}
	
	@SuppressWarnings("unchecked")
	public void loadPossibleAbilities() {
		possibleAbilities.clear();
		if(pony == null) return;
		List<String> abNames = pony.getPossibleAbilities();
		if(Debug.on) printDebug("possibleAbilities: "+abNames);
		for(Class<?> ability : abilities) {
			for(String abName : abNames) {
				if(abName.replaceAll(" ","").equals(ability.getSimpleName())) {
					try {
						possibleAbilities.add(AbilityCreator.create((Class<? extends Ability>)ability));
					} catch(ReflectiveOperationException e) {
						printDebug("[AbilityPrev] Failed to create ability: "+e);
						e.printStackTrace();
					}
					break;
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void showPreview(final String str) {
		if(Debug.on) printDebug("Called AbilityPreviewer.showPreview("+str+"); pony = "+pony);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				listModel.clear();
				List<Ability> matched = new ArrayList<>();
				for(Class<?> a : abilities) {
					// find out if ability matches string
					if(	str != null && str.length() > 0 &&
						a.getSimpleName().toLowerCase().startsWith(str.replaceAll(" ","").toLowerCase())
					) {
					try {
						matched.add(AbilityCreator.create((Class<? extends Ability>)a));
					} catch(ReflectiveOperationException e) {
						printDebug("[AbilityPreviewer] Exception: "+e);
					}
				}
				}
				if(matched.size() != 0) {
					listModel.addElement(new EffectDealer() {
						public String getName() { return null; }
						public String getDescription() { return "Match"; }
					});
				}
				for(Ability a : matched)
					listModel.addElement(a);
				if(possibleAbilities.size() != 0) {
					listModel.addElement(new EffectDealer() {
						public String getName() { return null; }
						public String getDescription() { return "Possible"; }
					});
				}
				outer:
				for(Ability a : possibleAbilities) {
					inner:
					for(Ability ab : matched)
						if(ab.getName().equals(a.getName()))
							continue outer;
					listModel.addElement(a);
				}

				if(listModel.getSize() == 0)
					listModel.addElement(new Ability() {
						public String getName() { return null; }
					});

				validate();
				repaint();
			}
		});
	}
}
