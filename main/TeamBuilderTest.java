//: main/TeamBuilderTest

package pokepon.main;

import java.io.*;
import pokepon.battle.*;
import pokepon.pony.*;
import pokepon.player.*;
import pokepon.move.*;
import pokepon.main.TestingClass;
import static pokepon.util.MessageManager.*;
import pokepon.util.*;

/** Pok&#233Pon - by silverweed &#169 2013
 * Yet another testing class, this time for TBs 
 *
 * @author silverweed
 */
public class TeamBuilderTest implements TestingClass {
	
	public static void main(String[] args) throws IOException {
		consoleHeader("   Launching TeamBuilder...   ");

		/*******************************************************/
	
		TeamBuilder tb = null;

		for(String s : args) {
			if(s.equals("-cli"))
				tb = new CLITeamBuilder();
		}

		if(tb == null) {
			try {
				tb = new GUITeamBuilder();
			} catch(java.awt.HeadlessException e) {
				consoleDebug("Graphics Environment not available: using CLITeamBuilder...");
				tb = new CLITeamBuilder();
			}
		}
		tb.buildTeam();
	}
}
