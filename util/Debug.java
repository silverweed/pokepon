//: util/Debug.java

package pokepon.util;

/** Utility class used for debugging.
 *
 * @author Giacomo Parolini
 */
 
public abstract class Debug {
	
	/* Utility methods and fields */

	public static boolean on = true;	// switch to 'false' to turn debugging off.
	/** Note that due to a flaw in design, pedantic = true does NOT imply on = true;
	 * likewise, on = false does not imply pedantic = false.
	 */
	public static boolean pedantic = false;
}
