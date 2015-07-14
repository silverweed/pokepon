//: battle/TypeDealer.java

package pokepon.battle;

import pokepon.enums.*;
import pokepon.util.*;
import pokepon.main.TestingClass;
import static pokepon.util.MessageManager.*;
import java.util.*;

/** This class contains relations among Types and static methods to
 * get weaknesses / resistances / etc;
 *
 * @author silverweed
 */

public class TypeDealer implements TestingClass {
	
	private final static String RED = "\033[0;31m";
	private final static String GREEN = "\033[0;32m";
	private final static String PURPLE = "\033[0;35m";
	private final static String NOCOL = "\033[0;00m";
	private final static String GREY = "\033[1;30m";

	//////////////// PUBLIC METHODS / FIELDS ////////////////

	// GET METHODS //
	/** @return Set containing Types against whom the given type is weak. */
	public static Set<Type> getWeaknesses(Type type) {
		if(type == null || weaknesses.get(type).length == 0)
			return (Set<Type>)EnumSet.noneOf(Type.class);
		return (Set<Type>)EnumSet.copyOf(Arrays.asList(weaknesses.get(type)));
	}
	
	/** @return List containing Types against whom the given type resists. */
	public static Set<Type> getResistances(Type type) {
		if(type == null || resistances.get(type).length == 0)
			return (Set<Type>)EnumSet.noneOf(Type.class);
		return (Set<Type>)EnumSet.copyOf(Arrays.asList(resistances.get(type)));
	}

	/** @return List containing Types against whom the given type is immune. */
	public static Set<Type> getImmunities(Type type) {
		if(type == null || immunities.get(type).length == 0)
			return (Set<Type>)EnumSet.noneOf(Type.class);
		return (Set<Type>)EnumSet.copyOf(Arrays.asList(immunities.get(type)));
	}

	/** @return String containing human-readable Types against whom the given type is weak. */
	public static String printWeaknesses(Type type) {
		if(type == null) return "";
		String s = "";
		for(Type t : weaknesses.get(type)) {
			if(t != null) {
				if(s.length() > 0) s += " ";
				s = s + t;
			}
		}
		return s;
	}
	
	/** @return String containing human-readable Types against whom the given type resists. */
	public static String printResistances(Type type) {
		if(type == null) return "";
		String s = "";
		for(Type t : resistances.get(type)) {
			if(t != null) {
				if(s.length() > 0) s += " ";
				s = s + t;
			}
		}
		return s;
	}

	/** @return String containing human-readable Types against whom the given type is immune. */
	public static String printImmunities(Type type) {
		if(type == null) return "";
		String s = "";
		for(Type t : immunities.get(type)) {
			if(t != null) {
				if(s.length() > 0) s += " ";
				s = s + t;
			}
		}
		return s;
	}
	
	/** @return Map containing entries (Type,multiplier) of weaknesses */
	public static Map<Type,Integer> getWeaknesses(Type[] type) {
		Map<Type,Integer> map = new EnumMap<Type,Integer>(Type.class);

		for(Map.Entry<Type,Float> f : getEffectiveness(type).entrySet()) {
			if(f.getValue() >= 2f) {
				map.put(f.getKey(),f.getValue().intValue());
			}
		}
		
		return map;
	}
	
	/** @return Map containing entries (Type,1/multiplier) of resistances */
	public static Map<Type,Integer> getResistances(Type[] type) {
		Map<Type,Integer> map = new EnumMap<Type,Integer>(Type.class);
			
		for(Map.Entry<Type,Float> f : getEffectiveness(type).entrySet()) {
			if(f.getValue() <= 0.5f && f.getValue() > 0) {
				map.put(f.getKey(),(int)(1/f.getValue()));
			}
		}
		
		return map;
	}
	
	/** @return Set containing immunities of 'type' */
	public static Set<Type> getImmunities(Type[] type) {
		Set<Type> set = (Set<Type>)EnumSet.noneOf(Type.class);
		
		for(Type t : type) {
			for(Type t2 : getImmunities(t)) {
				set.add(t2);
			}
		}
			
		return set;
	}

	/** @return Map containing (Type,damage_multiplier) for 'type'. */
	public static Map<Type,Float> getEffectiveness(Type[] type) {
		Map<Type,Float> map = new EnumMap<Type,Float>(Type.class);
		for(Type t : Type.values()) {
			map.put(t,1f);
		}
		for(Type t : type) {
			for(Type t2 : getWeaknesses(t)) {
				map.put(t2,2f*map.get(t2));
			}
			for(Type t2 : getResistances(t)) {
				map.put(t2,0.5f*map.get(t2));
			}
			for(Type t2 : getImmunities(t)) {
				map.put(t2,0f);
			}
		}

		return map;
	}
	
	/** @return String containing human-readable Types against whom the given multiple typing is weak (and how much). */
	public static String printWeaknesses(Type[] type) {
		
		StringBuilder sb = new StringBuilder("[");
		
		for(Map.Entry<Type,Integer> entry : getWeaknesses(type).entrySet()) {
			sb.append(entry.getKey());
			sb.append(" (");
			sb.append(entry.getValue());
			sb.append("x), ");
		}
		
		sb.delete(sb.length()-2,sb.length());
		sb.append("]");
		
		return sb.toString();
	}
	
	/** @return String containing human-readable Types against whom the given multiple typing resists (and how much). */
	public static String printResistances(Type[] type) {
		StringBuilder sb = new StringBuilder("[");
		
		for(Map.Entry<Type,Integer> entry : getResistances(type).entrySet()) {
			sb.append(entry.getKey());
			sb.append(" (1/");
			sb.append(entry.getValue());
			sb.append("x), ");	
		}
		
		sb.delete(sb.length()-2,sb.length());
		sb.append("]");
		
		return sb.toString();
	}
	
	/** @return String containing human-readable Types against whom the given multiple typing is immune. */
	public static String printImmunities(Type[] type) {
		return getImmunities(type).toString();
	}
	
	/** @return Float representing multiplicator of damage when type_atk attacks type_def.
	 * @param type_atk The type of the attacker
	 * @param type_def The type of the defender.
	 */
	public static float getEffectiveness(Type type_atk, Type type_def) {
		if(Arrays.asList(weaknesses.get(type_def)).contains(type_atk)) return 2f;
		else if(Arrays.asList(resistances.get(type_def)).contains(type_atk)) return 0.5f;
		else if(Arrays.asList(immunities.get(type_def)).contains(type_atk)) return 0f;
		else return 1f;
	}

	public static float getEffectiveness(Type type_atk, Type[] type_def) {
		return getEffectiveness(type_atk, Arrays.asList(type_def));
	}

	public static float getEffectiveness(Type type_atk, Iterable<Type> type_def) {
		float mod = 1;
		for(Type t : type_def) {
			if(t == null) continue;
			if(Arrays.asList(weaknesses.get(t)).contains(type_atk)) mod *= 2f;
			else if(Arrays.asList(resistances.get(t)).contains(type_atk)) mod /= 2f;
			else if(Arrays.asList(immunities.get(t)).contains(type_atk)) return 0f;
		}
		return mod;
	}

	/** Prints table of weaknesses / resistances. */
	public static void printTypesTable(boolean colored) {
		String format = " %-8s ";
		String format2 = " %-22s ";
		System.out.printf(format," ");
		List<Type> types = Arrays.asList(Type.values());
		Collections.sort(types,new Comparator<Type>() {
			public int compare(Type t1,Type t2) {
				return t1.toString().compareTo(t2.toString());
			}
		});
		for(Type t : types) {
			System.out.printf(format,t.toString().substring(0,Math.min(t.toString().length(),8)));
		}
		printMsg("");
		for(Type t : types) {
			System.out.printf(format,t.toString().substring(0,Math.min(t.toString().length(),8)));
			for(Type t2: types) {
				if(colored) {
					String color = NOCOL;
					float eff = getEffectiveness(t,t2);
					if(eff == 0) color = PURPLE;
					else if(eff < 1) color = RED;
					else if(eff > 1) color = GREEN;
					System.out.printf(format2,color+eff+NOCOL);
				} else System.out.printf(format,getEffectiveness(t,t2));
			}
			printMsg("");
			System.out.printf((colored ? GREY : "")+"-------");
			for(Type t2: Type.values()) {
				System.out.printf("----------");
			}
			printMsg(colored ? NOCOL : "");
		}
	}

	public static Type randomType() {
		return (Type.values()[(new Random()).nextInt(Type.values().length)]);
	}
	
	public static void main(String[] args) {
		for(String s : args) {
			if(s.equals("-c")) {
				printTypesTable(true);
				return;
			} else {
				consoleMsg("Usage: TypeDealer [-c]");
				return;
			}
		}
		printTypesTable(false);
	}

	/** Given a float effectiveness, yields a string to represent it
	 * in a nicer form and a color fitting it.
	 */
	public static String[] toEffString(float eff) {
		String effec = "";
		String color = "black";
		if(eff < 0.25f) {
			effec = "0x";
			color = "purple";
		} else if(eff < 0.5f) {
			effec = "1/4x";
			color = "red";
		} else if(eff < 1f) {
			effec = "1/2x";
			color = "red";
		} else if(eff > 2f) {
			effec = "4x";
			color = "green";
		} else if(eff > 1f) {
			effec = "2x";
			color = "green";
		} else {
			effec = "1x";
		}
		return new String[] { effec, color };
	}

	//////////////// PRIVATE METHODS / FIELDS /////////////////

	/** Prevents this class from being instantiated */
	private TypeDealer() {
		throw new RuntimeException("Type dealer cannot be instantiated!");
	}

	private static EnumMap<Type,Type[]> weaknesses = new EnumMap<Type,Type[]>(Type.class);
	private static EnumMap<Type,Type[]> resistances = new EnumMap<Type,Type[]>(Type.class);
	private static EnumMap<Type,Type[]> immunities = new EnumMap<Type,Type[]>(Type.class);

	/* Typechart initialization */
	static {

		/* Initializating weaknesses/resistances/immunities
		 * Note that you *HAVE TO* initialize also
		 * empty arrays to avoid further NullPointerExceptions.
		 */

		weaknesses.put(Type.MAGIC,       new Type[] { Type.HONESTY, Type.LAUGHTER, Type.SPIRIT });
		weaknesses.put(Type.LOYALTY,     new Type[] { Type.MAGIC, Type.CHAOS, Type.HONESTY });
		weaknesses.put(Type.HONESTY,     new Type[] { Type.LOYALTY, Type.CHAOS, Type.PASSION });
		weaknesses.put(Type.LAUGHTER,    new Type[] { Type.LAUGHTER, Type.GENEROSITY, Type.LOVE, Type.MUSIC });
		weaknesses.put(Type.KINDNESS,    new Type[] { Type.LOYALTY, Type.NIGHT });
		weaknesses.put(Type.GENEROSITY,  new Type[] { Type.LOYALTY, Type.CHAOS });
		weaknesses.put(Type.CHAOS,       new Type[] { Type.MAGIC, Type.KINDNESS, Type.PASSION, Type.MUSIC });
		weaknesses.put(Type.NIGHT,       new Type[] { Type.LAUGHTER, Type.SPIRIT, Type.LIGHT });
		weaknesses.put(Type.SHADOW,      new Type[] { Type.LOVE, Type.PASSION, Type.MUSIC });
		weaknesses.put(Type.SPIRIT,      new Type[] { Type.SHADOW, Type.SPIRIT, Type.PASSION });
		weaknesses.put(Type.LOVE,        new Type[] { Type.HONESTY, Type.GENEROSITY, Type.PASSION });
		weaknesses.put(Type.PASSION,     new Type[] { Type.MAGIC, Type.LOYALTY, Type.KINDNESS, Type.SHADOW, Type.MUSIC });
		weaknesses.put(Type.MUSIC,       new Type[] { Type.GENEROSITY, Type.CHAOS, Type.NIGHT });
		weaknesses.put(Type.LIGHT,       new Type[] { Type.CHAOS, Type.SHADOW });


		resistances.put(Type.MAGIC,      new Type[] { Type.MAGIC, Type.CHAOS, Type.SHADOW, Type.LOVE });
		resistances.put(Type.LOYALTY,    new Type[] { Type.NIGHT, Type.PASSION });
		resistances.put(Type.HONESTY,    new Type[] { Type.HONESTY, Type.LAUGHTER, Type.GENEROSITY, Type.LOVE });
		resistances.put(Type.LAUGHTER,   new Type[] { Type.MAGIC, Type.NIGHT });
		resistances.put(Type.KINDNESS,   new Type[] { Type.KINDNESS, Type.GENEROSITY, Type.PASSION });
		resistances.put(Type.GENEROSITY, new Type[] { Type.HONESTY, Type.MUSIC, Type.SPIRIT });
		resistances.put(Type.CHAOS,      new Type[] { Type.HONESTY, Type.GENEROSITY, Type.CHAOS, Type.SHADOW, Type.LOVE, Type.LIGHT });
		resistances.put(Type.NIGHT,      new Type[] { Type.KINDNESS, Type.CHAOS, Type.NIGHT, Type.SHADOW, Type.PASSION, Type.MUSIC });
		resistances.put(Type.SHADOW,     new Type[] { Type.LOYALTY, Type.HONESTY, Type.LAUGHTER, Type.KINDNESS,
								Type.GENEROSITY, Type.CHAOS, Type.LIGHT });
		resistances.put(Type.SPIRIT,     new Type[] { Type.MAGIC, Type.LAUGHTER, Type.CHAOS });
		resistances.put(Type.LOVE,       new Type[] { Type.CHAOS, Type.SPIRIT, Type.LOVE });
		resistances.put(Type.PASSION,    new Type[] { Type.HONESTY, Type.LOVE });
		resistances.put(Type.MUSIC,      new Type[] { Type.SHADOW, Type.SPIRIT, Type.MUSIC });
		resistances.put(Type.LIGHT,      new Type[] { Type.MAGIC, Type.LOYALTY, Type.HONESTY, Type.LAUGHTER, Type.KINDNESS, Type.GENEROSITY });


		immunities.put(Type.MAGIC,       new Type[] {});
		immunities.put(Type.LOYALTY,     new Type[] {});
		immunities.put(Type.HONESTY,     new Type[] {});
		immunities.put(Type.LAUGHTER,    new Type[] { Type.CHAOS });
		immunities.put(Type.KINDNESS,    new Type[] {});
		immunities.put(Type.GENEROSITY,  new Type[] {});
		immunities.put(Type.CHAOS,       new Type[] { Type.LOYALTY });
		immunities.put(Type.NIGHT,       new Type[] {});
		immunities.put(Type.SHADOW,      new Type[] {});
		immunities.put(Type.SPIRIT,      new Type[] {});
		immunities.put(Type.LOVE,        new Type[] { Type.SHADOW });
		immunities.put(Type.PASSION,     new Type[] {});
		immunities.put(Type.MUSIC,       new Type[] {});
		immunities.put(Type.LIGHT,       new Type[] {});
	}
}
