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

	protected final static int DEFAULT_MAX_NICK_LEN = 30;
	protected int maxNickLen = DEFAULT_MAX_NICK_LEN;
	
	public BasicNameValidatingServer() throws IOException {
		this(ServerOptions.construct());
	}

	/** Accepted options: maxNickLen, port, address, verbosity, serverName, confFile */
	public BasicNameValidatingServer(ServerOptions opts) throws IOException {
		super(opts);
		loadOptions(opts);
		if(verbosity >= 0)
			printDebug("["+serverName+"] Constructed with maxNickLen = "+maxNickLen+" and "+forbiddenNames.size()+" forbidden name rules.");
	}

	@Override
	public BasicNameValidatingServer loadOptions(ServerOptions opts) {
		if(opts.maxNickLen != -1)
			maxNickLen = opts.maxNickLen;
		if(opts.serverName == null)
			serverName = BasicNameValidatingServer.class.getSimpleName();
		if(opts.forbiddenNames != null)
			forbiddenNames.addAll(opts.forbiddenNames);
		super.loadOptions(opts);
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

	@Override
	public void loadConfiguration(ServerOptions opts) {
		super.loadConfiguration(opts);
		if(opts.maxNickLen != -1)
			maxNickLen = opts.maxNickLen;
		if(opts.forbiddenNames != null)
			forbiddenNames.addAll(opts.forbiddenNames);
	}
	
	/** You can put strings (even regexes) in this list in order to forbid nicknames */
	protected Set<String> forbiddenNames = new HashSet<String>(Arrays.asList(new String[] {
		"null",		
		"^<.*>.*$",	//prevent names to contain html tags
	}));
}
