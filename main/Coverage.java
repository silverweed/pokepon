//: main/Coverage.java

package pokepon.main;

import pokepon.pony.*;
import pokepon.battle.TypeDealer;
import pokepon.util.*;
import pokepon.enums.*;
import pokepon.move.*;
import static pokepon.util.MessageManager.*;
import java.io.*;
import java.util.*;

/** Pok&#233Pon - by jp &#169 2014
 * Calculates the coverage of a pony.
 *
 * @author Giacomo Parolini
 */
public class Coverage implements TestingClass {
	
	public static void main(String[] args) {
		if(args.length < 1) {
			consoleMsg("Usage: Coverage [-n] [-v] [-e] <name of pony>");
			consoleMsg("  -n: show also normal effectiveness");
			consoleMsg("  -v: verbose - show list of ponies for each typing");
			consoleMsg("  -e: show also non-existing typings in results.");
			return;
		}

		int tmp = 0;
		boolean verbose = false;
		boolean shownormal = false;
		boolean shownonexisting = false;
		while(args[tmp].startsWith("-")) {
			if(args[tmp].equals("-n"))
				shownormal = true;
			else if(args[tmp].equals("-v"))
				verbose = true;
			else if(args[tmp].equals("-e"))
				shownonexisting = true;
			tmp++;
		}
		String name = ConcatenateArrays.merge(args,tmp);
		consoleHeader("   Calculating coverage of "+name+"   ");
		Coverage coverage = new Coverage();
	
		try {
			Pony pony = PonyCreator.create(name);
			Set<Type> viableTypes = new TreeSet<>();
			for(String s : pony.getLearnableMoves().keySet()) {
				Move move = MoveCreator.create(s);
				if(move.getMoveType() != Move.MoveType.STATUS && move.getBaseDamage() > 0)
					viableTypes.add(move.getType());
			}
			
			/* Load existing tyings */
			coverage.loadExistingTypings();

			/* Display viable types */
			consoleMsg("VIABLE DAMAGING MOVES TYPES: ");
			int j = 1;
			for(Type t : viableTypes)
				consoleMsg(j++ + ") " + t);

			consoleMsgnb("Select up to 4 types: (enter numbers separated by a space" + 
				(viableTypes.size() <= Pony.MOVES_PER_PONY ? " @ for all" : "") + ") >> ");

			/* Read user input */
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String[] set = in.readLine().split(" ");
		
			/* Parse selected types */
			Set<Type> selectedTypes = new TreeSet<>();
			if(set[0].equals("@") && viableTypes.size() <= Pony.MOVES_PER_PONY)
				selectedTypes.addAll(viableTypes);
			else 
				for(String s : set) {
					int i = 0;
					int k = Integer.parseInt(s);
					Iterator<Type> it = viableTypes.iterator();
					Type cur = null;
					while(it.hasNext() && i < k) {
						cur = it.next();
						++i;
					}
					selectedTypes.add(cur);
				}

			consoleDebug("Selected types: "+selectedTypes);
			
			/* Calculate coverage */
			Set<String> 	supereff = new TreeSet<>(),
					normal = new TreeSet<>(), 
					notveryeff = new TreeSet<>(), 
					noeff = new TreeSet<>();

			Type[] types = Type.values();
			byte[][] effTable = new byte[types.length][types.length];
			for(Type selected : selectedTypes) {
				byte effectiveness = 0;
				for(int i = 0; i < types.length; ++i) {
					for(int k = i; k < types.length; ++k) { 
						float eff = 	i == k
								? TypeDealer.getEffectiveness(selected, types[i])
								: TypeDealer.getEffectiveness(selected, new Type[] { types[i], types[k] });
						if(eff > 1f)
							effectiveness = 3;
						else if(eff > 0.5f)
							effectiveness = 2;
						else if(eff > 0f)
							effectiveness = 1;
						else
							effectiveness = 0;
						
						if(effectiveness > effTable[i][k])
							effTable[i][k] = effectiveness;
					}
				}
			}

			for(int i = 0; i < types.length; ++i)
				for(int k = i; k < types.length; ++k) {
					if(!shownonexisting && !coverage.existsTyping(types[i],types[k])) continue;
					switch(effTable[i][k]) {
						case 0:
							noeff.add(types[i] + (types[k] == types[i] ? "" : " / " + types[k]) +
								(verbose ? "   " + coverage.getPoniesWithTyping(types[i],types[k]) : ""));
							break;
						case 1:
							notveryeff.add(types[i] + (types[k] == types[i] ? "" : " / " + types[k]) +
								(verbose ? "   " + coverage.getPoniesWithTyping(types[i],types[k]) : ""));
							break;
						case 2:
							normal.add(types[i] + (types[k] == types[i] ? "" : " / " + types[k]) +
								(verbose ? "   " + coverage.getPoniesWithTyping(types[i],types[k]) : ""));
							break;
						case 3:
							supereff.add(types[i] + (types[k] == types[i] ? "" : " / " + types[k]) +
								(verbose ? "   " + coverage.getPoniesWithTyping(types[i],types[k]) : ""));
							break;
					}
				}
			
			/* Output results */
			if(supereff.size() > 0) {
				consoleMsg("SUPEREFFECTIVE VS:");
				consoleTable(supereff, 3, 4);
			}
			if(shownormal && normal.size() > 0) {
				consoleMsg("NORMAL DAMAGE VS:");
				consoleTable(normal, 3, 4);
			}
			if(notveryeff.size() > 0) {
				consoleMsg("NOT VERY EFFECTIVE VS:");
				consoleTable(notveryeff, 3, 4);
			}
			if(noeff.size() > 0) {
				consoleMsg("NOT EFFECTIVE VS:");
				consoleTable(noeff, 3, 4);
			}
	
		} catch(ReflectiveOperationException e) {
			consoleDebug("Couldn't create pony "+name);
		} catch(IllegalArgumentException e) {
			consoleDebug("Illegal argument given: "+e);
		} catch(IndexOutOfBoundsException e) {
			consoleDebug("Index out of bounds: "+e);
			e.printStackTrace();
		} catch(IOException e) {
			consoleDebug("IOException: "+e);
		}
	}	

	private static class TypeArrComparator implements Comparator<Type[]> {
		public int compare(Type[] t1, Type[] t2) {
			int tmp = t1[0].toString().compareTo(t2[0].toString());
			if(tmp == 0)
				return t1[1].toString().compareTo(t2[1].toString());
			else
				return tmp;
		}
	}

	@SuppressWarnings("unchecked")
	private void loadExistingTypings() throws ReflectiveOperationException {
		List<Class<?>> lc = ClassFinder.findSubclasses(Meta.complete(Meta.PONY_DIR),Pony.class);
		for(Class<?> c : lc) {
			Pony pony = PonyCreator.create((Class<? extends Pony>)c);
			Type[] typing = new Type[] { pony.getType(0), (pony.getType(1) == null ? pony.getType(0) : pony.getType(1)) };
			if(existingTypes.get(typing) != null)
				existingTypes.get(typing).add(pony);
			else {
				existingTypes.put(typing,new LinkedList<Pony>());
				existingTypes.get(typing).add(pony);
			}
		}
	}
	
	private boolean existsTyping(Type t1, Type t2) {
		for(Type[] t : existingTypes.keySet())
			if(t[0] == t1 && t[1] == t2 || t[1] == t1 && t[0] == t2) return true;
		return false;
	}

	private String getPoniesWithTyping(Type t1, Type t2) {
		StringBuilder sb = new StringBuilder("[");
		for(Map.Entry<Type[],List<Pony>> entry : existingTypes.entrySet()) {
			Type[] type = entry.getKey();
			if(type[0] == t1 && type[1] == t2 || type[0] == t2 && type[1] == t1) {
				for(Pony p : entry.getValue())
					sb.append(p.getName()+", ");
			}
		}
		if(sb.length() > 1)
			sb.delete(sb.length() - 2, sb.length());
		sb.append("]");
		return sb.toString();
	}
	
	private Map<Type[],List<Pony>> existingTypes = new HashMap<Type[],List<Pony>>();
}

