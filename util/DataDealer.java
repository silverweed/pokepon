//: util/DataDealer.java

package pokepon.util;

import pokepon.pony.*;
import pokepon.move.*;
import pokepon.ability.*;
import pokepon.item.*;
import pokepon.enums.*;
import pokepon.battle.TypeDealer;
import static pokepon.util.Meta.*;
import static pokepon.util.MessageManager.*;
import java.util.*;

/** This class handles queries for in-game data and responds with HTML strings.
 *
 * @author silverweed
 */
public class DataDealer {

	private static List<String> knownClasses = new ArrayList<>();
	private static List<String> knownPonies, knownMoves, knownAbilities, knownItems;
	private static List<String> knownTypeNames;

	static {
		init();
	}

	public static void init() {
		knownPonies = ClassFinder.findSubclassesNames(Meta.complete(PONY_DIR),Pony.class);
		knownMoves = ClassFinder.findSubclassesNames(Meta.complete(MOVE_DIR),Move.class);
		knownAbilities = ClassFinder.findSubclassesNames(Meta.complete(ABILITY_DIR),Ability.class);
		knownItems = ClassFinder.findSubclassesNames(Meta.complete(ITEM_DIR),Item.class);
		knownClasses.addAll(knownPonies);
		knownClasses.addAll(knownMoves);
		knownClasses.addAll(knownAbilities);
		knownClasses.addAll(knownItems);
		knownTypeNames = new ArrayList<>();
		for(Type t : Type.values())
			knownTypeNames.add(t.toString());
	}

	public static Iterable<String> getKnownClasses() { return knownClasses; }
	public static Iterable<String> getKnownPonies() { return knownPonies; }
	public static Iterable<String> getKnownMoves() { return knownMoves; }
	public static Iterable<String> getKnownAbilities() { return knownAbilities; }
	public static Iterable<String> getKnownItems() { return knownItems; }
	public static Iterable<String> getKnownTypeNames() { return knownTypeNames; }

	public String getData(String query) {
		
		String name = Saner.sane(query,knownClasses);
	
		StringBuilder sb = new StringBuilder("");
		// DISCLAIMER!! Since Swing cannot decently render even basic HTML tables, we use a shitty HTML, which
		// even the Swing library can understand.
		if(knownPonies.contains(name)) {
			try {
				Pony pony = PonyCreator.create(name);

				// FIXME: sprite URL is valid only on the same machine, if protocol is file:// 
				// Should either export sprites via HTML, or convert the URL into the client's local one.
				// ATM we use the syntax [sprite: Name Of Pony] to tell the client to convert it to the
				// local sprite URL.
				sb.append("<br><img src=\"[sprite: "+pony.getName()+"]\" height=25px width=30px />");
				sb.append("&nbsp;<b>"+pony.getName()+"</b>&nbsp;&nbsp;"+pony.getTypingHTMLSpecialTags()+"<br>");
				//sb.append("<table><tr>");
				for(int i = 0; i < pony.getPossibleAbilities().size(); ++i)
					if(pony.getPossibleAbilities().get(i) != null)
						//sb.append("<td>"+pony.getPossibleAbilities().get(i)+"</td>");
						sb.append(pony.getPossibleAbilities().get(i)+"&nbsp;&nbsp;&nbsp;");
				//sb.append("</tr></table><br>");
				sb.append("<br>");
				sb.append("<b>HP&nbsp;&nbsp;&nbsp;Atk&nbsp;&nbsp;&nbsp;Def&nbsp;&nbsp;&nbsp;SpA&nbsp;&nbsp;&nbsp;SpD&nbsp;&nbsp;&nbsp;Spe</b><br>");
				sb.append(pony.getBaseHp()+"&nbsp;&nbsp;");
				sb.append(pony.getBaseAtk()+"&nbsp;&nbsp;");
				sb.append(pony.getBaseDef()+"&nbsp;&nbsp;");
				sb.append(pony.getBaseSpatk()+"&nbsp;&nbsp;");
				sb.append(pony.getBaseSpdef()+"&nbsp;&nbsp;");
				sb.append(pony.getBaseSpeed()+"<br>");
				//sb.append("<table><tr><th>HP</th><th>Atk</th><th>Def</th><th>SpA</th><th>SpD</th><th>Spe</th></tr>");
				//sb.append("<tr><td>"+pony.getBaseHp()+"</td><td>"+pony.getBaseAtk()+"</td><td>"+pony.getBaseDef()+"</td>");
				//sb.append("<td>"+pony.getBaseSpatk()+"</td><td>"+pony.getBaseSpdef()+"</td><td>"+pony.getBaseSpeed()+"</td></tr>");
				//sb.append("</table>");
			} catch(ReflectiveOperationException e) {
				printDebug("[BT.processCommand(data)] Error creating pony "+query);
				return "|error|Couldn't create pony "+query;
			}
		} else if(knownMoves.contains(name)) {
			try {
				Move move = MoveCreator.create(name);

				sb.append("<br><font size=3 color=\"gray\">Move:</font>&nbsp;<b>"+move.getName()+"</b>&nbsp;");
				sb.append("<img src=\"[type: "+move.getType()+"]\"/>&nbsp;");
				sb.append("<img src=\"[movetype: "+move.getMoveType()+"]\"/></font>&nbsp;&nbsp;<br>");
				sb.append("<small><b>Pow.</b>&nbsp;&nbsp;"+(move.getBaseDamage() != 0 ? move.getBaseDamage() : "-")+"&nbsp;&nbsp;");
				sb.append("<b>Acc.</b>&nbsp;&nbsp;"+(move.getAccuracy() < 0 ? "-" : move.getAccuracy())+"&nbsp;&nbsp;");
				sb.append("<b>PP</b>&nbsp;&nbsp;"+(move.getMaxPP() < 0 ? "-" : move.getMaxPP())+"</small><br>");
				//sb.append("<table><tr><th><small>Pow.</small></th><th><small>Acc.</small></th><th><small>PP</small></th></tr><br>");
				//sb.append("<tr><td><small>"+(move.getBaseDamage() != 0 ? move.getBaseDamage() : "-")+"</small></td>");
				//sb.append("<td><small>"+(move.getAccuracy() < 0 ? "-" : move.getAccuracy())+"</small></td>");
				//sb.append("<td><small>"+(move.getMaxPP() < 0 ? "-" : move.getMaxPP())+"</small></td></tr></table><br>");
				sb.append("<font color=\"gray\">"+move.getBriefDescription()+"</font><br>");
			} catch(ReflectiveOperationException e) {
				printDebug("[BT.processCommand(data)] Error creating move "+query);
				return "|error|Couldn't create move "+query;
			}
		} else if(knownAbilities.contains(name)) {
			try {
				Ability ability = AbilityCreator.create(name);

				sb.append("<br><font size=3 color=\"gray\">Ability:</font>&nbsp;<b>"+ability.getName()+"</b><br>");
				sb.append("<font color=\"gray\">"+ability.getBriefDescription()+"</font><br>");
			} catch(ReflectiveOperationException e) {
				printDebug("[BT.processCommand(data)] Error creating ability "+query);
				return "|error|Couldn't create ability "+query;
			}
		} else if(knownItems.contains(name)) {
			try {
				Item item = ItemCreator.create(name);

				sb.append("<br><font size=3 color=\"gray\">Item:</font>&nbsp;<b>"+item.getName()+"</b><br>");
				sb.append("<font color=\"gray\">"+item.getBriefDescription()+"</font><br>");
			} catch(ReflectiveOperationException e) {
				printDebug("[BT.processCommand(data)] Error creating item "+query);
				return "|error|Couldn't create item "+query;
			}
		} else return null;

		return sb.toString();
	}

	/** @param query type1[,type2] - OR - type1 -&gt; type2[,type3] */
	public String getEffectiveness(String query) {
		String[] token = query.split("->",2);

		if(token.length == 1) {
			// query type 1: /eff type1[,type2]
			// prints all effectiveness table for given typing.
			String[] tmp = token[0].trim().split("\\s*,\\s*");
			Type t1 = Type.forName(Saner.sane(tmp[0].trim(),knownTypeNames));
			if(t1 == null) {
				return "|error|Type "+tmp[0]+" not found.";
			}
			Type t2 = null;
			if(tmp.length > 1) {
				t2 = Type.forName(Saner.sane(tmp[1].trim(),knownTypeNames));
				if(t2 == null) {
					return "|error|Type "+tmp[1]+" not found.";
				}
			}
			return getEffectiveness(new Type[] { t1, t2 });

		} else {
			// query type 2: /eff type1 -> type2[,type3]
			// prints effectiveness of attacking type1 vs defending typing 2[,3].
			Type t1 = Type.forName(Saner.sane(token[0].trim(),knownTypeNames));
			if(t1 == null) {
				return "|error|Type "+token[0]+" not found.";
			}
			String[] tmp = token[1].trim().split("\\s*,\\s*");
			Type t3 = Type.forName(Saner.sane(tmp[0].trim(),knownTypeNames));
			if(t3 == null) {
				return "|error|Type "+tmp[0]+" not found.";
			}
			Type t4 = null;
			if(tmp.length > 1) {
				t4 = Type.forName(Saner.sane(tmp[1].trim(),knownTypeNames));
				if(t4 == null) {
					return "|error|Type "+tmp[1]+" not found.";
				}
			}
			return getEffectiveness(t1, new Type[] { t3, t4 });
		}
	}

	public String getEffectiveness(Type[] type) {
		StringBuilder sb = new StringBuilder("");
		if(type.length == 1 || type[1] == null) {
			// if only 1 type is given, print both offensive and defensive table
			sb.append("<b>Offensive</b><br>");
			for(Type t : Type.values()) {
				float eff = TypeDealer.getEffectiveness(type[0], t);
				String[] effS = TypeDealer.toEffString(eff);
				if(eff != 1f) {
					sb.append("<img src=\"[type: "+type[0]+"]\"/> "+
						"<b><font color=\""+effS[1]+"\">"+effS[0]+"</font></b>"+
						" =&gt; <img src=\"[type: " + t + "]\"/><br>");
				}
			}
			sb.append("<b>Defensive</b><br>");
			for(Type t : Type.values()) {
				float eff = TypeDealer.getEffectiveness(t, type[0]);
				String[] effS = TypeDealer.toEffString(eff);
				if(eff != 1f) {
					sb.append("<img src=\"[type: "+ t +"]\"/> "+
						"<b><font color=\""+effS[1]+"\">"+effS[0]+"</font></b>"+
						" =&gt; <img src=\"[type: " + type[0] + "]\"/><br>");
				}
			}

		} else {
			// if dual type, print only defensive table
			sb.append("<b>Defensive</b><br>");
			for(Type t : Type.values()) {
				float eff = TypeDealer.getEffectiveness(t, type);
				String[] effS = TypeDealer.toEffString(eff);
				if(eff != 1f) {
					sb.append("<img src=\"[type: "+ t +"]\"/> "+
						"<b><font color=\""+effS[1]+"\">"+effS[0]+"</font></b>"+
						" =&gt; <img src=\"[type: " + type[0] + "]\"/>"+
						"<img src=\"[type: " + type[1] + "]\" /><br>");
				}
			}
		}
		return sb.toString();
	}

	/** @return An HTML string describing the effectiveness of type1 vs type2. */
	public String getEffectiveness(Type typeAtk, Type[] typeDef) {
		float eff = TypeDealer.getEffectiveness(typeAtk, typeDef);
		String[] effS = TypeDealer.toEffString(eff);
		
		return "<img src=\"[type: " + typeAtk + "]\"/> <b><font color=\"" + effS[1] + "\">" + effS[0] + 
			"</font></b> =&gt; <img src=\"[type: " + typeDef[0] + "]\"/>" + (typeDef[1] != null ? 
			"<img src=\"[type: " + typeDef[1] + "]\"/>" : "");
	}
}
