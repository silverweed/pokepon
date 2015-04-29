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
	
	public interface Command {
		public String getDescription();
		public int getNArgs();
	}

	public Parser(Set<? extends Command> commands) {
		this.commands = commands;
	}

	public Set<? extends Command> getCommands() { return commands; }
	
	public void parse(String str) {
		if(Debug.on) printDebug("Called Parser::parse("+str+")");
		List<String> tokens = Arrays.asList(str.split("\\s+"));	//split input in tokens (using whitespaces as delimiters)
		boolean commandRead = false;
		
		for(String s : tokens) {
			if(!commandRead) {
				Command cmd = toCommand(s);
				if(cmd != null) {
					commandRead = true;
					command = cmd;
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
			if(args.size() < command.getNArgs()) {
				parsedOK = false;
				parseErrMsg = "--syntax.error: you must provide at least "+command.getNArgs()+" arguments for command "+command;
			} else parsedOK = true;
		} 
		
	}

	public final boolean ok() { return parsedOK; }
	public final String getParsingError() { return parseErrMsg; }
	public final Command getCommand() { return command; }
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
		command = null;
		args.clear();
	}
	
	public String toString() {
		return "Command= "+command+"; Args= "+args.toString();
	}

	protected Command toCommand(final String cmdn) {
		for(Command cmd : commands)
			if(cmd.toString().equals(cmdn)) return cmd;
		return null;
	}
	
	/** This field contains the type of accepted commands (unary,binary,etc.) */
	protected Set<? extends Command> commands; 
	protected Command command;
	protected String parseErrMsg;
	protected List<String> args = new LinkedList<String>();
	protected boolean parsedOK = false;
}
