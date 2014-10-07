//: main/QuickLauncher
package pokepon.main;

import pokepon.net.jack.server.PokeponServer;
import pokepon.player.GUITeamBuilder;
import pokepon.net.jack.client.PokeponClient;
import pokepon.battle.TypeDealer;
import pokepon.util.Debug;
import pokepon.gui.SwingConsole;
import static pokepon.util.MessageManager.*;
import java.util.*;
import java.lang.reflect.*;

/** Another launcher, less verbose than PokeponLauncher; with a
 * syntax more similar to run.sh; used as game entrypoint in the JAR.
 * @author silverweed
 */
public class QuickLauncher implements TestingClass {

	public static void main(String[] args) {
		/* Apple's default L&F doesn't work well with some features, like GradientButtons,
		 * so ensure to set the classic Metal L&F for Swing.
		 */
		SwingConsole.setCPLookAndFeel();

		if(args.length == 0) {
			GUILauncher.main(new String[0]);	
			return;
		}

		String myClass = "";
		List<String> testClassOpts = new ArrayList<String>();
		boolean classSet = false;
		
		/* Parse arguments: distinguish Launcher options (which must be given BEFORE the class to test)
		 * from testing class options (which must be given AFTER the class to test).
		 */
		for(int i = 0; i < args.length; ++i) {
			/* After testingClass is set, everything else which follows is treated as options for that class's main */
			if(classSet) {
				testClassOpts.add(args[i]);
			} else {
				/* These are the options for the launcher itself */
				if(args[i].equals("-d")) {
					pokepon.util.Debug.on = false;
					printDebug("[debug] Debug is OFF");

				} else if(args[i].equals("-p")) {
					pokepon.util.Debug.pedantic = true;
					printDebug("[debug] Debug set to PEDANTIC");

				} else if(args[i].equals("-m") || args[i].equals("--mute")) {
					pokepon.gui.GUIGlobals.soundOn = false;

				} else if(args[i].startsWith("-")) {
					printUsage();

				} else {
					myClass = args[i];
					if(Debug.on) printDebug("[debug] testingClass set to "+myClass);
					classSet = true;
				}
			}
		}
	
		try {
			Class<?> testingClass = null;
			if(myClass.equalsIgnoreCase("server"))
				testingClass = Class.forName("pokepon.net.jack.server.PokeponServer");
			else if(myClass.equalsIgnoreCase("client"))
				testingClass = Class.forName("pokepon.net.jack.client.PokeponClient");
			else if(myClass.equalsIgnoreCase("tb") || myClass.equalsIgnoreCase("teambuilder"))
				testingClass = Class.forName("pokepon.main.TeamBuilderTest");
			else if(myClass.equalsIgnoreCase("dex"))
				testingClass = Class.forName("pokepon.main.FastPonydex");
			else if(myClass.equalsIgnoreCase("cov") || myClass.equalsIgnoreCase("coverage"))
				testingClass = Class.forName("pokepon.main.Coverage");
			else if(myClass.equalsIgnoreCase("types"))
				testingClass = Class.forName("pokepon.battle.TypeDealer");
			else {
				GUILauncher.main(new String[0]);	
				return;
			}

			if(Debug.on) printDebug("[debug] class="+myClass);

			/* Retreive main method from testingClass */
			Method mainMethod = testingClass.getMethod("main",String[].class);
			String[] params = new String[testClassOpts.size()];
			for(int i = 0; i < params.length; ++i)
				params[i] = testClassOpts.get(i);
			if(Debug.on) printDebug("[debug] Launching "+myClass+".main("+Arrays.asList(params)+") ...");
			printDebug("[info] Debug level: " + (Debug.pedantic ? "PEDANTIC" : (Debug.on ? "ON" : "OFF")));
			printDebug("--->\n\n");
			mainMethod.invoke(null,(Object)params);
			return;
			
		} catch(InvocationTargetException e) {	//we want some more details in this case.
			printDebug("Caught InvocationTargetException while creating testingClass");
			printDebug("(caused by "+e.getTargetException()+")");
			e.getTargetException().printStackTrace();	
		} catch(Exception e) {
			printDebug("Caught exception while creating testingClass: "+e);
			e.printStackTrace();
		}
	}	

	private static void printUsage() {
		consoleMsg("\nUsage: QuickLauncher [-d | -p | -h] [ server | client | tb/teambuilder | dex | cov/coverage | types ] [opts]");
		consoleMsg("\t-d: turn debug OFF\n\t-p: turn PEDANTIC debug on (very verbose)\n\t-h: show this help\n");
		System.exit(1);
	}
}
