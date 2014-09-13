//: battle/NoActivePonyException.java

package pokepon.battle;

/** Custom exception for battle. 
 * @author Giacomo Parolini
 */

public class NoActivePonyException extends RuntimeException {
	
	public NoActivePonyException() {
		super();
	}
	
	public NoActivePonyException(String msg) {
		super(msg);
	}
	
	public NoActivePonyException(String msg,Throwable cause) {
		super(msg,cause);
	}
	
	public NoActivePonyException(Throwable cause) {
		super(cause);
	}
}
