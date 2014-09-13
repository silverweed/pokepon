//: util/HazardCreator.java

package pokepon.util;

import pokepon.move.hazard.*;
import static pokepon.util.Meta.*;
import static pokepon.util.MessageManager.*;
import java.lang.reflect.*;

/** A "factory class" used to easily create Hazards 
 *
 * @author Giacomo Parolini
 */

@SuppressWarnings("unchecked")
public class HazardCreator {

	public static Hazard create() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	public static Hazard create(String name) throws ReflectiveOperationException {
		if(Debug.pedantic) printDebug("Called HazardCreator.create("+name+")");

		Class<? extends Hazard> hazardBuilder = (Class<? extends Hazard>)Class.forName((POKEPON_ROOTDIR+DIRSEP+HAZARD_DIR+DIRSEP+name.replaceAll(" ","")).replaceAll(""+DIRSEP,"."));
		Hazard hazard = hazardBuilder.getConstructor().newInstance();

		if(Debug.pedantic) printDebug("Created hazard: "+hazard.toString());

		return hazard;
	}

	public static Hazard create(Class<? extends Hazard> className) throws ReflectiveOperationException {
		if(Debug.pedantic) printDebug("Called HazardCreator.create("+className+")");

		Hazard hazard = className.getConstructor().newInstance();

		if(Debug.pedantic) printDebug("Created hazard: "+hazard.toString());

		return hazard;
	}

}
