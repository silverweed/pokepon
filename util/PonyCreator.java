//: util/PonyCreator.java

package pokepon.util;

import pokepon.pony.*;
import static pokepon.util.Meta.*;
import static pokepon.util.MessageManager.*;
import java.util.*;
import java.lang.reflect.*;

/** A "factory class" used to correctly initialize ponies and provide several
 * useful methods to create them.
 *
 * @author silverweed
 */

@SuppressWarnings("unchecked")
public class PonyCreator {

	public static Pony create() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/** Utility method to create a pony with maxed IVs, level and happiness.
	 * @param name The name of the pony to create (i.e. a simple class name)
	 * @return a Pony with maxed IV and happiness (random Nature) 
	 */
	public static Pony create(String name) throws ReflectiveOperationException {
		if(Debug.pedantic) printDebug("Called PonyCreator.create("+name+")");

		Class<? extends Pony> ponyBuilder = (Class<? extends Pony>)Class.forName((POKEPON_ROOTDIR+DIRSEP+PONY_DIR+DIRSEP+name.replaceAll("[\\s']","")).replaceAll(""+DIRSEP,"."));
		Pony pony = ponyBuilder.getDeclaredConstructor(int.class).newInstance(100);
		
		if(Debug.pedantic) printDebug("Created pony "+pony.getName());

		/* Set IV to 31 by default. */
		for(String s : Pony.STAT_NAMES) {
			pony.setIV(s,31);
		}
		/* Set happiness to MAX by default */
		pony.setHappiness(Pony.MAX_HAPPINESS);				
		/* Set hp = maxhp */
		pony.setHp(pony.maxhp());
		

		return pony;
	}

	/** Overloaded function as create(String), but with a Class object */
	public static Pony create(Class<? extends Pony> className) throws ReflectiveOperationException {
		if(Debug.pedantic) printDebug("Called PonyCreator.create("+className+")");

		Pony pony = className.getDeclaredConstructor(int.class).newInstance(100);
		
		if(Debug.pedantic) printDebug("Created pony "+pony.getName());

		/* Set IV to 31 by default. */
		for(String s : Pony.STAT_NAMES) {
			pony.setIV(s,31);
		}				
		/* Set happiness to MAX by default */
		pony.setHappiness(Pony.MAX_HAPPINESS);				
		/* Set hp = maxhp */
		pony.setHp(pony.maxhp());

		return pony;
	}		

	/** Utility method to create a pony with given level and random IV/Nature.
	 * @param name The name of the pony to create (i.e. a simple class name)
	 * @return a Pony with given level and random IVs/Nature 
	 */
	public static Pony create(String name,int level) throws ReflectiveOperationException {
		if(Debug.pedantic) printDebug("Called PonyCreator.create("+name+","+level+")");

		Class<? extends Pony> ponyBuilder = (Class<? extends Pony>)Class.forName((POKEPON_ROOTDIR+DIRSEP+PONY_DIR+DIRSEP+name.replaceAll("[\\s']","")).replaceAll(""+DIRSEP,"."));
		Pony pony = ponyBuilder.getDeclaredConstructor(int.class).newInstance(level);
		
		if(Debug.pedantic) printDebug("Created pony "+pony.getName());

		/* Set hp = maxhp */
		pony.setHp(pony.maxhp());

		return pony;
	}

	/** Calls create(String) for each given class name and returns a list of ponies. */
	public static List<Pony> create(String... names) throws ReflectiveOperationException {
		List<Pony> ponies = new ArrayList<Pony>();
		for(String s : names)
			ponies.add(create(s));

		return ponies;
	}
	
	/** @return a Pony 'name' with random IV / Nature and 0 happiness */
	public static Pony createRandom(String name) throws ReflectiveOperationException {
		Class<? extends Pony> ponyBuilder = (Class<? extends Pony>)Class.forName(POKEPON_ROOTDIR+"."+PONY_DIR+"."+name);
		Pony pony = ponyBuilder.getDeclaredConstructor(int.class).newInstance(100);

		/* Set hp = maxhp */
		pony.setHp(pony.maxhp());

		return pony;
	}

	/** @return a random Pony with random IV / Nature and 0 happiness */
	public static Pony createRandom() throws ReflectiveOperationException {
		List<Class<?>> lc = ClassFinder.findSubclasses(Meta.complete(PONY_DIR),Pony.class);
		String name = lc.get((new Random()).nextInt(lc.size())).getSimpleName();
		Class<? extends Pony> ponyBuilder = (Class<? extends Pony>)Class.forName(POKEPON_ROOTDIR+"."+PONY_DIR+"."+name);
		Pony pony = ponyBuilder.getDeclaredConstructor(int.class).newInstance(100);

		/* Set hp = maxhp */
		pony.setHp(pony.maxhp());

		return pony;
	}

	/** Calls createRandom() n times and returns a list of ponies */
	public static List<Pony> createRandom(int n) throws ReflectiveOperationException {
		List<Pony> ponies = new ArrayList<Pony>();
		for(int i = 0; i < n; ++i) {
			ponies.add(createRandom());
		}
		
		return ponies;
	}
}

