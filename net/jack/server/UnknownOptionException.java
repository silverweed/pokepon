//: net/jack/server/UnknownOptionException.java

package pokepon.net.jack.server;

public class UnknownOptionException extends Exception {

	public UnknownOptionException() {
		super();
		quiet = true;
	}
	public UnknownOptionException(String str) {
		super(str);
	}
	public boolean isQuiet() { return quiet; }

	private boolean quiet;
}
