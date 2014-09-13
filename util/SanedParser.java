//: util/SanedParser.java

package pokepon.util;

import static pokepon.util.MessageManager.*;
import java.util.*;

/** A Parser which has Saner methods to do orthographic checking.
 *
 * @author silverweed
 */

public class SanedParser extends Parser {

	public SanedParser() { super(); }
	public SanedParser(Map<String,Integer> commandTypes) {
		super(commandTypes);
	}

	/** This method is overridden to provide extra functionality of Saner */
	@Override
	public void parse(String str) {
		if(Debug.on) printDebug("Called SanedParser::parse("+str+")");
		List<String> tokens = Arrays.asList(str.split("\\s+"));	//split input in tokens (using whitespaces as delimiters)
		boolean commandRead = false;
		
		String saned = "";

		for(String s : tokens) {
			if(!commandRead) {
				saned = Saner.sane(s,type.keySet());
				if(Debug.on) printDebug("saned= "+saned);
				if(type.containsKey(saned)) {
					commandRead = true;
					command = saned;
					if(Debug.on) printDebug("Read command: "+command);
				 }
			} else {
				args.add(s);	//Note: only commands are saned; args aren't.
				if(Debug.on) printDebug("#args: "+args.size());
			}
		}
		
		/* parsing is OK if command was read AND enough arguments were supplied. */
		if(!commandRead) {
			parsedOK = false;
			parseErrMsg = "--syntax.error: invalid command provided.";
		}
		else {
			if(args.size() < type.get(command)) {
				parsedOK = false;
				parseErrMsg = "--syntax.error: you must provide at least "+type.get(command)+" arguments for command "+command;
			} else parsedOK = true;
		} 

		if(Debug.on) printDebug("Parsing finished.");
		
	}
}
