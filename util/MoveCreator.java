//: util/MoveCreator.java

package pokepon.util;

import pokepon.move.*;
import pokepon.pony.*;
import static pokepon.util.Meta.*;
import static pokepon.util.MessageManager.*;
import java.lang.reflect.*;

/** A "factory class" used to easily create Moves 
 *
 * @author silverweed
 */

@SuppressWarnings("unchecked")
public class MoveCreator {

	public static Move create() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	public static Move create(String name,Pony p) throws ReflectiveOperationException {
		if(Debug.pedantic) printDebug("Called MoveCreator.create("+name+")");

		Class<? extends Move> moveBuilder = (Class<? extends Move>)Class.forName(
							(POKEPON_ROOTDIR+DIRSEP+MOVE_DIR+DIRSEP+
							name.replaceAll("[^a-zA-Z0-9]","")).replaceAll(""+DIRSEP,"."));
		Move move = moveBuilder.getConstructor(Pony.class).newInstance(p);

		if(Debug.pedantic) printDebug("Created move: "+move.toString());

		return move;
	}
	
	public static Move create(String name) throws ReflectiveOperationException {
		if(Debug.pedantic) printDebug("Called MoveCreator.create("+name+")");

		Class<? extends Move> moveBuilder = (Class<? extends Move>)Class.forName(
							(POKEPON_ROOTDIR+DIRSEP+MOVE_DIR+DIRSEP+
							name.replaceAll("[^a-zA-Z0-9]","")).replaceAll(""+DIRSEP,"."));
		Move move = moveBuilder.getConstructor().newInstance();

		if(Debug.pedantic) printDebug("Created move: "+move.toString());

		return move;
	}

	public static Move create(Class<? extends Move> className,Pony p) throws ReflectiveOperationException {
		if(Debug.pedantic) printDebug("Called MoveCreator.create("+className+")");

		Move move = className.getConstructor(Pony.class).newInstance(p);

		if(Debug.pedantic) printDebug("Created move: "+move.toString());

		return move;
	}
	
	public static Move create(Class<? extends Move> className) throws ReflectiveOperationException {
		if(Debug.pedantic) printDebug("Called MoveCreator.create("+className+")");

		Move move = className.getConstructor().newInstance();

		if(Debug.pedantic) printDebug("Created move: "+move.toString());

		return move;
	}

}
