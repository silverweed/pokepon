//: util/Parser.java

package pokepon.util;

import static pokepon.util.MessageManager.*;
import java.util.*;

/** A basic class for parsing input: constructor takes the map of known commands 
 * (command,num of args); you can then call Parser.parse(string) and retreive
 * the command read, its arguments and whether the parsing was successful or not.
 *
 * @author silverweed
 */

public class Parser {
	
	public Parser() {}
	public Parser(Map<String,Integer> commandTypes) {
		type = commandTypes;
	}
	
	public void parse(String str) {
		if(Debug.on) printDebug("Called Parser::parse("+str+")");
		List<String> tokens = Arrays.asList(str.split("\\s+"));	//split input in tokens (using whitespaces as delimiters)
		boolean commandRead = false;
		
		for(String s : tokens) {
			if(!commandRead) {
				if(type.containsKey(s)) {
					commandRead = true;
					command = s;
					if(Debug.on) printDebug("Read command: "+command);
				}
			} else {
				args.add(s);
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
		
	}
	
	public final boolean ok() { return parsedOK; }
	public final String getParsingError() { return parseErrMsg; }
	public final String getCommand() { return command; }
	public final List<String> getArgs() { return args; }		
	public String popFirstArg() { 
		try {
			printDebug("Current args: "+args);
			String s = args.remove(0); 
			printDebug("Popped argument "+s);
			return s;
		} catch(NullPointerException e) {
			printDebug("Caught exception in popFirstArg: "+e);
			return null;
		} catch(IndexOutOfBoundsException e) {
			if(Debug.pedantic) printDebug("Array index out of bound in popFirstArg: "+e);
			return null;
		}
	}
	public void clear() {
		command = "";
		args.clear();
	}
	
	public String toString() {
		return "Command= "+command+"; Args= "+args.toString();
	}
	
	/** This field contains the type of accepted commands (unary,binary,etc.) */
	protected Map<String,Integer> type = new HashMap<String,Integer>();
	protected String command;
	protected String parseErrMsg;
	protected List<String> args = new LinkedList<String>();
	protected boolean parsedOK = false;
}
