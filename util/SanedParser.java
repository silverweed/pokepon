//: util/SanedParser.java

package pokepon.util;

import static pokepon.util.MessageManager.*;
import java.util.*;

/** A Parser which has Saner methods to do orthographic checking.
 *
 * @author silverweed
 */
public class SanedParser extends Parser {

	public SanedParser(Set<? extends Command> commands) {
		super(commands);
	}

	/** This method is overridden to provide extra functionality of Saner */
	@Override
	public void parse(String str) {
		String[] cmd = str.split("\\s+");
		super.parse(Saner.sane(cmd[0], getCommandNames(commands)) + (cmd.length > 1 ? " " + ConcatenateArrays.merge(cmd, 1) : ""));
	}

	private Set<String> getCommandNames(Set<? extends Command> commands) {
		Set<String> set = new LinkedHashSet<>(); 
		for(Command c : commands)
			set.add(c.toString());
		return set;
	}
}
