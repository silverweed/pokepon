//: util/Debug.java

package pokepon.util;

/** Utility class used for debugging.
 *
 * @author silverweed
 */
 
public class Debug {
	
	/** Prevent this class from being instantiated */
	private Debug() {
		throw new RuntimeException("Debug class cannot be instantiated!");
	}

	/* Utility methods and fields */

	public static boolean on = true;	// switch to 'false' to turn debugging off.
	public static boolean pedantic = false;
 
}
