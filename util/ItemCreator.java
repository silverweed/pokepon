//: util/ItemCreator.java

package pokepon.util;

import pokepon.item.*;
import static pokepon.util.Meta.*;
import static pokepon.util.MessageManager.*;
import java.lang.reflect.*;

/** A "factory class" used to easily create Items 
 *
 * @author silverweed
 */

@SuppressWarnings("unchecked")
public class ItemCreator {

	public static Item create() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	public static Item create(String name) throws ReflectiveOperationException {
		if(Debug.pedantic) printDebug("Called ItemCreator.create("+name+")");

		Class<? extends Item> itemBuilder = (Class<? extends Item>)Class.forName(
				(POKEPON_ROOTDIR+DIRSEP+ITEM_DIR+DIRSEP+name.replaceAll("[^a-zA-Z0-9]",""))
				.replaceAll(""+DIRSEP,"."));
		Item item = itemBuilder.getConstructor().newInstance();

		if(Debug.pedantic) printDebug("Created item: "+item.toString());

		return item;
	}

	public static Item create(Class<? extends Item> className) throws ReflectiveOperationException {
		if(Debug.pedantic) printDebug("Called MoveCreator.create("+className+")");

		Item item = className.getConstructor().newInstance();

		if(Debug.pedantic) printDebug("Created item: "+item.toString());

		return item;
	}
}
