//: main/FastPonydex.java

package pokepon.main;

import pokepon.main.*;
import pokepon.pony.*;
import pokepon.util.*;
import static pokepon.util.MessageManager.*;
import static pokepon.util.Meta.*;
import java.util.*;

/** Utility class that dinamically compiles a Ponydex (reordering ponies by selected stat)
 *
 * @author silverweed
 */
public class FastPonydex implements TestingClass {

	private static enum Order { NORMAL, REVERSED };

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void main(String[] args) throws Exception {
		if(args.length > 2) {
			printMsg("Usage: FastPonydex [hp|atk|def|spa|spd|spe|bst (default)|type|name] [rev]");
			System.exit(1);
		}
		String stat = "bst";
		Order order = Order.NORMAL;
		if(args.length > 0) {
			if(args[0].startsWith("-")) {
				printMsg("Usage: FastPonydex [hp|atk|def|spa|spd|spe|bst (default)|type] [rev]");
				System.exit(1);
			}
			stat = args[0].toLowerCase();
			if(!Arrays.asList(("hp atk def spatk spdef speed spa spd spe bst type name".split(" "))).contains(stat)) {
				printDebug("Error: stat "+stat+" does not exist!");
				System.exit(2);
			} else if(stat.equals("spa")) stat = "spatk";
			  else if(stat.equals("spd")) stat = "spdef";
			  else if(stat.equals("spe")) stat = "speed";
			if(args.length > 1 && args[1].equals("rev")) order = Order.REVERSED;
		}

		List<Class<?>> lc = ClassFinder.findSubclasses(Meta.complete(PONY_DIR),Pony.class);
		List<Pony> ponies = new ArrayList<Pony>();
		List bsts = new ArrayList<>();

		for(Class<?> c : lc) {
			Pony p = null;
			ponies.add((p = PonyCreator.create((Class<? extends Pony>)(c))));
			if(stat.equals("type")) bsts.add(p.getTyping());
			else if(stat.equals("name")) bsts.add(p.getName());
			else bsts.add(p.getBaseStat(Pony.Stat.forName(stat)));
		}

		ReorderLists.doubleQuicksortComparingSecond(ponies,bsts,0,ponies.size()-1);
		if(order == Order.NORMAL ^ (stat.equals("name") || stat.equals("type"))) 
			Collections.reverse(ponies);

		printFormat("%-20s %-3s %-3s %-3s %-3s %-3s %-3s %-3s    %-20s\n",
			"NAME","HP","Atk","Def","SpA","SpD","Spe","BST","TYPING"
		);
		for(Pony p : ponies) {
			printFormat("%-20s %-3d %-3d %-3d %-3d %-3d %-3d %-3d    %-20s\n",
				p.getName(),
				p.getBaseHp(),
				p.getBaseAtk(),
				p.getBaseDef(),
				p.getBaseSpatk(),
				p.getBaseSpdef(),
				p.getBaseSpeed(),
				p.bst(),
				p.getTyping()
			);
		}
	}
}
