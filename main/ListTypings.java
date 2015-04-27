//: main/ListTypings.java

package pokepon.main;

import java.io.*;
import java.util.*;
import java.net.*;
import pokepon.battle.*;
import pokepon.pony.*;
import pokepon.player.*;
import pokepon.move.*;
import pokepon.enums.Type;
import static pokepon.util.MessageManager.*;
import pokepon.main.TestingClass;
import pokepon.util.*;

/** Pok&#233Pon - by silverweed &#169 2013
 * Prints typings info
 *
 * @author silverweed
 */
public class ListTypings implements TestingClass {
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		consoleHeader("   Launching ListTypings...   ");
		
		int colsNum = args.length > 0 ? Integer.parseInt(args[0]) : 4;

		List<Class<?>> poniesC = ClassFinder.findSubclasses(Meta.complete(Meta.PONY_DIR), Pony.class);
		List<Pony> ponies = new LinkedList<>();
		for(Class<?> c : poniesC)
			ponies.add(PonyCreator.create((Class<? extends Pony>)c));

		Map<String,Integer> typeMap = new TreeMap<>();

		for(int i = 0; i < Type.values().length; ++i) {
			for(int j = i; j < Type.values().length; ++j) {
				Type t1 = Type.values()[i], t2 = Type.values()[j];
				String key = i == j ? t1.toString() : t1 + " / " + t2; 
				for(Pony pny : ponies) {
					if(	i == j && pny.getType(0) == t1 && pny.getType(1) == null ||
						i != j &&
							(pny.getType(0) == t1 && pny.getType(1) == t2 || 
							pny.getType(0) == t2 && pny.getType(1) == t1)
					) {
						if(typeMap.containsKey(key))
							typeMap.put(key,typeMap.get(key)+1);
						else
							typeMap.put(key, 1);
					} else if(!typeMap.containsKey(key)) {
						typeMap.put(key, 0);
					}
				}
			}
		}

		Set<String> rows = new LinkedHashSet<>();
		for(Map.Entry<String,Integer> entry : typeMap.entrySet())
			rows.add(String.format("%-21s  %d", entry.getKey(), entry.getValue()));

		consoleTable(rows, colsNum);
	}
}

