// pokepon.net.jack/server/BasicNameValidatingServer.java

package pokepon.net.jack.server;

import pokepon.net.jack.*;
import static pokepon.util.MessageManager.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;

/** A basic implementation of a NameValidatingServer.
 *
 * @author silverweed
 */
public class BasicNameValidatingServer extends BasicServer implements NameValidatingServer {

	protected final static int DEFAULT_MAX_NICK_LEN = 30;
	protected int maxNickLen = DEFAULT_MAX_NICK_LEN;
	
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
		if(opts.maxNickLen != -1)
			maxNickLen = opts.maxNickLen;
		if(opts.forbiddenNames != null)
			forbiddenNames.addAll(opts.forbiddenNames);
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

	/** You can put strings (even regexes) in this list in order to forbid nicknames */
	protected Set<String> forbiddenNames = new HashSet<String>();
}
