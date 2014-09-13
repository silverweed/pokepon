//: util/AbilityCreator.java

package pokepon.util;

import pokepon.ability.*;
import static pokepon.util.Meta.*;
import static pokepon.util.MessageManager.*;
import java.lang.reflect.*;

/** A "factory class" used to easily create Moves 
 *
 * @author silverweed
 */

@SuppressWarnings("unchecked")
public class AbilityCreator {

	public static Ability create() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	public static Ability create(String name) throws ReflectiveOperationException {
		if(Debug.pedantic) {
			printDebug("Called AbilityCreator.create("+name+")");
			printDebug("Attempting to create ability from: "+(Meta.complete(ABILITY_DIR)+DIRSEP+name.replaceAll(" ","")).replaceAll(""+DIRSEP,"."));
		}

		Class<? extends Ability> abilityBuilder = (Class<? extends Ability>)Class.forName((Meta.complete(ABILITY_DIR)+DIRSEP+name.replaceAll(" ","")).replaceAll(""+DIRSEP,"."));
		Ability ability = abilityBuilder.getConstructor().newInstance();

		if(Debug.pedantic) printDebug("Created ability: "+ability.toString());

		return ability;
	}

	public static Ability create(Class<? extends Ability> className) throws ReflectiveOperationException {
		if(Debug.pedantic) printDebug("Called AbilityCreator.create("+className+")");

		Ability ability = className.getConstructor().newInstance();

		if(Debug.pedantic) printDebug("Created ability: "+ability.getClass());

		return ability;
	}
}
