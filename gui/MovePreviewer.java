//: gui/MovePreviewer.java

package pokepon.gui;

import pokepon.move.*;
import pokepon.util.*;
import pokepon.pony.*;
import pokepon.battle.EffectDealer;
import pokepon.enums.*;
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

/** This class implements the previewer for the moves.
*
* @author silverweed
*/
@SuppressWarnings("unchecked") 
public class MovePreviewer extends PokeponPreviewer {

	private static Set<String> moves = new LinkedHashSet<>();
	private Set<Move> possibleMoves = new LinkedHashSet<>();
	private int moveIndex = -1;

	static {
		List<Class<?>> moveClasses = ClassFinder.findSubclasses(Meta.complete(MOVE_DIR),Move.class);
		Collections.sort(moveClasses,new Comparator<Class<?>>() {
			public int compare(Class<?> me,Class<?> other) {
				return me.getSimpleName().compareTo(other.getSimpleName());
			}
		});
		moveClasses.remove(Struggle.class);
		moveClasses.remove(HiddenTalent.class);
		for(Class<?> c : moveClasses) {
			try {
				moves.add(MoveCreator.create((Class<? extends Move>)c).getName());
			} catch(ReflectiveOperationException e) {
				printDebug("[MovePrev] Failed to create class: "+e);
			}
		}
	}

	public MovePreviewer() {
		this(null);
	}

	public int getMoveIndex() { return moveIndex; }
	public void setMoveIndex(final int i) { 
		moveIndex = i;
		if(Debug.on) printDebug("[MovePreviewer] set moveIndex to "+i);
	}

	public MovePreviewer(final Pony pony) {
		super(pony);
		loadPossibleMoves();
		renderer = new ListCellRenderer<Object>() {
			@Override
			public Component getListCellRendererComponent(	JList<? extends Object> list,
									Object value,
									int index,
									boolean isSelected,
									boolean cellHasFocus) {
				
				if(Debug.pedantic) printDebug("Called MovePreviewer.getListCellRendererComponent(value="+value+
					", index="+index+", selected="+isSelected+") [selIndex="+list.getSelectedIndex()+"]");
				JLabel renderer = new JLabel();

				if(value == null)
					return renderer;

				// special case (not actual move)
				if(((EffectDealer)value).getName() == null) {
					EffectDealer placeholder = (EffectDealer)value;
					renderer.setEnabled(false);
					if(placeholder.getDescription() == null) {
						renderer.setForeground(Color.GRAY);
						renderer.setText("No suggestions.");

					} else {
						renderer.setForeground(Color.BLACK);
						renderer.setText("== "+placeholder.getDescription()+" ==");
					}
					return renderer;
				}

				Move mv = (Move)value;

				String text = "<html><body";
				if(list.getSelectedIndex() == index)
					text += " style=\"height: 20px; background-color: #CCFFFF; border: 1px ridge #CCFFFF\"";

				text += "><p>"+mv.getName()+"&nbsp;&nbsp;";

				if(	MovePreviewer.super.pony != null &&
					!mv.getName().startsWith("Hidden Talent") &&
					!MovePreviewer.super.pony.canLearn(mv)
				) {
					text += "<span style=\"color: #AA2222; border: 1px solid #AA2222;\">"+
							"[ Illegal ]</span></p></body></html>";
				} else {
					text += "<img src=\""+mv.getType().getToken()+"\"/>"+
						"<img src=\""+Meta.getTokensURL()+Meta.DIRSEP+"moves"+Meta.DIRSEP+"movetypes"+
						Meta.DIRSEP+(mv.getMoveType() == Move.MoveType.PHYSICAL ? "Physical.png" :
						(mv.getMoveType() == Move.MoveType.SPECIAL ? "Special.png" : "Status.png"))+
						"\"/> "+
						" <span style=\"color:gray; font-size: 85%\">"+
						mv.getBriefDescription().replaceAll("<br>"," ")+"&nbsp;&nbsp;&nbsp;"+
						(mv.getMoveType() == Move.MoveType.STATUS ? "" : 
						"Pow: <font color=#222222><b>"+mv.getBaseDamage()+"</b></font> / ") + "Acc: <font color=#222222><b>"+
						(mv.getAccuracy() < 0 ? "-" : mv.getAccuracy())
						+"</b></font> / PP: <font color=#222222>"+mv.getMaxPP()+"</font></span>"
						+"</p></body></html>";
				}
				
				renderer.setText(text);
				return renderer; 
			}
		};
		_items.setCellRenderer(renderer);
		if(Debug.on) printDebug("[MovePreviewer] constructed with pony "+pony);
		showPreview(null);
	}

	@Override
	public void setPony(Pony pony) {
		super.setPony(pony);
		loadPossibleMoves();
	}

	public void loadPossibleMoves() {
		possibleMoves.clear();
		if(pony == null) return;
		Set<String> mvNames = pony.getLearnableMoves().keySet();
		if(Debug.on) printDebug("possibleMoves: "+mvNames);
		for(String move : moves) {
			for(String mvName : mvNames) {
				if(mvName.equals(move)) {
					try {
						possibleMoves.add(MoveCreator.create(move));
					} catch(ReflectiveOperationException e) {
						printDebug("[MovePrev] Failed to create move: "+e);
						e.printStackTrace();
					}
					break;
				}
			}
		}
		// add hidden talents (one per type)
		for(Type t : Type.values()) {
			Move ht = new HiddenTalent();
			ht.setType(t);
			possibleMoves.add(ht);
			moves.add(ht.getName());
		}
	}
		
	public void showPreview(final String str) {
		if(Debug.on) printDebug("Called MovePreviewer.showPreview("+str+"); pony = "+pony);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					listModel.clear();
				} catch(ArrayIndexOutOfBoundsException e) {
					// pretty much harmless: don't print entire stack trace
					printDebug("[MovePreviewer] Out of bounds: "+e);
				}
				List<Move> matched = new ArrayList<>();
				for(String a : moves) {
					// find out if move matches string
					if(	str != null && str.length() > 0 &&
						a.toLowerCase().startsWith(str.toLowerCase())
					) {
						try {
							if(a.startsWith("Hidden Talent")) {
								Move ht = new HiddenTalent();
								ht.setType(Type.forName(a.replaceAll("Hidden Talent ","")));
								matched.add(ht);
							} else {
								matched.add(MoveCreator.create(a));
							}
						} catch(ReflectiveOperationException e) {
							printDebug("[MovePreviewer] Exception: "+e);
						}
					}
				}
				if(Debug.pedantic) printDebug("matched: "+matched+"\npossible: "+possibleMoves);
				if(matched.size() != 0) {
					// this is the "title element"
					listModel.addElement(new EffectDealer() {
						public String getName() { return null; }
						public String getDescription() { return "Match"; }
					});
				}
				for(Move a : matched)
					listModel.addElement(a);
				if(possibleMoves.size() != 0) {
					listModel.addElement(new EffectDealer() {
						public String getName() { return null; }
						public String getDescription() { return "Possible"; }
					});
				}
				outer:
				for(Move a : possibleMoves) {
					inner:
					for(Move mv : matched)
						if(mv.getName().equals(a.getName()))
							continue outer;
					listModel.addElement(a);
				}
				
				if(listModel.getSize() == 0) {
					listModel.addElement(new Move() {
						public String getName() { return null; }
					});
				}

				validate();
				repaint();
			}
		});
	}
}
