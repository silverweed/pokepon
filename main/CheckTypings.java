//: main/CheckTypings.java

package pokepon.main;

import java.io.*;
import java.util.*;
import java.net.*;
import javax.tools.*;
import pokepon.battle.*;
import pokepon.pony.*;
import pokepon.player.*;
import pokepon.move.*;
import static pokepon.util.MessageManager.*;
import pokepon.main.TestingClass;
import pokepon.util.*;

/** Pok&#233Pon - by silverweed &#169 2013
 * Prints typings info
 *
 * @author silverweed
 */
public class CheckTypings implements TestingClass {
	
	public static void main(String[] args) throws IOException,URISyntaxException  {
		consoleHeader("   Launching CheckTypings...   ");

		File ponydir = new File(Meta.getPonyURL().toURI());
		SortedSet<Pony> ponehz = new TreeSet<Pony>();	//note that Pony implements Comparator<Pony>
		for(String s : ponydir.list()) {
			if(s.matches(".*Pony\\..*") || !s.endsWith(".java")) continue;
			try {
				ponehz.add(PonyCreator.create(s.split("\\.")[0]));
			} catch(ClassNotFoundException e) {
				System.err.println("Couldn't find class "+s.split("\\.")[0]+";\ntrying to compile on-the-fly...");
				JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
				if(javac.run(null,null,null,Meta.getPonyURL().toURI().getPath()+"/"+s) == 0) {
					System.err.println("[ OK ] Error recovered.");
					try {
						ponehz.add(PonyCreator.create(s.split("\\.")[0]));
					} catch(Exception e2) {
						System.err.println("Exception while creating "+s+": "+e2);
					}
				} else
					System.err.println("Compilation failed.");
			} catch(Exception e) {
				System.err.println("Exception while creating "+s+": "+e);
			}
		}
		
		for(Pony p : ponehz) {
			printMsg("Name: " + p.getName().toUpperCase());
			p.printTypingInfo();
			printMsg("");
		}
	}	
}

