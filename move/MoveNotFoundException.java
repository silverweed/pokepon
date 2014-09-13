//: move/MoveNotFoundException.java

package pokepon.move;

/** Custom exception returned when a Move is not found. 
 * @author silverweed
 */

public class MoveNotFoundException extends RuntimeException {
	
	public MoveNotFoundException() {
		super();
	}
	
	public MoveNotFoundException(String msg) {
		super(msg);
	}
	
	public MoveNotFoundException(String msg,Throwable cause) {
		super(msg,cause);
	}
	
	public MoveNotFoundException(Throwable cause) {
		super(cause);
	}
}
