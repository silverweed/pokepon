//: main/PokeponLauncher.java

package pokepon.main;

import pokepon.battle.*;
import pokepon.pony.*;
import pokepon.player.*;
import pokepon.move.*;
import pokepon.util.*;
import pokepon.main.*;
import static pokepon.util.MessageManager.*;
import static pokepon.util.Meta.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;

/** Pok&#233Pon - by silverweed &#169 2013
 * The launcher for all testing classes: takes flags `-d' (no debug), `-p' (pedantic debug),
 * `--package PACKAGE' (for searching in a package other than pokepon.main)
 * and argument `testingClass', which is a valid class in package pokepon.main which
 * implements TestingClass.
 *
 * @author silverweed
 */
public class PokeponLauncher {
	
	static {
		// enable anti-aliased text
		System.setProperty("awt.useSystemAAFontSettings","on");
		System.setProperty("swing.aatext", "true");
	}

	public static void main(String[] args) throws IOException {
		printMsg("[info] PokeponLauncher starting...");
		if(Debug.on) printDebug("[debug] Memory: "+Runtime.getRuntime().freeMemory()/1024+" / "+Runtime.getRuntime().totalMemory()/1024+" MB (free / tot)");
		if(Debug.on) printDebug("[debug] Args: "+Arrays.asList(args).toString());
		String searchPackage = POKEPON_ROOTDIR + "." + MAIN_DIR;
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
				} else if(args[i].equals("--package")) {
					if(i == args.length || args[i+1].startsWith("-")) {
						throw new RuntimeException("Illegal argument for option --package");
					} else {
						searchPackage = args[++i];
					}
				} else {
					myClass = args[i];
					if(Debug.on) printDebug("[debug] testingClass set to "+myClass);
					classSet = true;
				}
			}
		}
	
		try {
			if(Debug.on) printDebug("[debug] searchPackage="+searchPackage+"; class="+myClass);
			/* Create testingClass using reflection */
			Class<?> testingClass = Class.forName(searchPackage+"."+myClass);
			/* Check if created class implements the interface TestingClass, and throw exception if not */
			if(!Arrays.asList(testingClass.getInterfaces()).contains(Class.forName(POKEPON_ROOTDIR+"."+MAIN_DIR+".TestingClass"))) {
				throw new RuntimeException("Error: class "+testingClass.getName()+" does not implement "+POKEPON_ROOTDIR+"."+MAIN_DIR+".TestingClass!");
			}
			/* Retreive main method from testingClass */
			Method mainMethod = testingClass.getMethod("main",String[].class);
			/* This is apparently the only way to make invoke() work */
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
}

