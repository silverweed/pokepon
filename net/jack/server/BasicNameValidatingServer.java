// pokepon.net.jack/server/BasicNameValidatingServer.java

package pokepon.net.jack.server;

import pokepon.net.jack.*;
import static pokepon.util.MessageManager.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;

/** A basic implementation of a NameValidatingServer.
 *
 * @author Giacomo Parolini
 */
public class BasicNameValidatingServer extends BasicServer implements NameValidatingServer {

	protected final static int MAX_ALLOWABLE_NICK_LEN = 30;
	protected final static int DEFAULT_MAX_NICK_LEN = 30;
	protected final static int DEFAULT_MIN_NICK_LEN = 3;
	protected int maxNickLen = DEFAULT_MAX_NICK_LEN;
	protected int minNickLen = DEFAULT_MIN_NICK_LEN;
	
	public BasicNameValidatingServer() throws IOException {
		this(ServerOptions.construct());
	}

	/** Accepted options: maxNickLen, port, address, verbosity, serverName, confFile */
	public BasicNameValidatingServer(ServerOptions opts) throws IOException {
		super(opts);
		loadOptions(opts);
		if(opts.serverName == null && !alreadySetName)
			serverName = getClass().getSimpleName();
		if(verbosity >= 0)
			printDebug("["+serverName+"] Constructed with maxNickLen = "+maxNickLen+" and "+forbiddenNames.size()+" forbidden name rules.");
	}

	@Override
	public BasicNameValidatingServer loadOptions(ServerOptions opts) {
		super.loadOptions(opts);
		if(verbosity >= 2) printDebug("[BasicNameValidatingServer] Called loadOptions");
		if(opts.maxNickLen > 0)
			maxNickLen = opts.maxNickLen;
		if(opts.minNickLen > 0)
			minNickLen = opts.minNickLen;
		if(opts.forbiddenNames != null)
			forbiddenNames.addAll(opts.forbiddenNames);
		if(minNickLen > maxNickLen) {
			if(verbosity >= 0) printDebug("[ WARNING ] minNickLen > maxNickLen: setting both to "+maxNickLen);
			minNickLen = maxNickLen;
		}
		if(maxNickLen > MAX_ALLOWABLE_NICK_LEN) {
			if(verbosity >= 0) printDebug("[ WARNING ] maxNickLen too high: reducing it to "+MAX_ALLOWABLE_NICK_LEN);
			maxNickLen = MAX_ALLOWABLE_NICK_LEN;
			minNickLen = Math.min(minNickLen, maxNickLen);
		}
		return this;
	}
	
	/** Checks if given string is valid or forbidden name. */
	public boolean isValidName(String name) {
		for(String s : forbiddenNames) {
			Pattern p = Pattern.compile(s);
			if(p.matcher(name).matches()) {
				return false;
			}
		}
		return true;
	}
	
	public int maxNickLen() {
		return maxNickLen;
	}

	public int minNickLen() {
		return minNickLen;
	}

	/** You can put strings (even regexes) in this list in order to forbid nicknames */
	protected Set<String> forbiddenNames = new HashSet<String>();
}
