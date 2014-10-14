//: util/DirectoryList.java

package pokepon.util;

import java.util.regex.*;
import java.io.*;

/** A simple directory lister, adapted from Bruce Eckel's example
 * in "Thinking in Java 4th edition" - I/O chapter.
 *
 * @author Bruce Eckel
 */
 
public class DirectoryList {
	
	class DirectoryFilter implements FilenameFilter {
	
		public DirectoryFilter(String regex) {
			pattern = Pattern.compile(regex);
		}
		
		public boolean accept(File dirpath, String name) {
			return pattern.matcher(name).matches();
		}
		
		private Pattern pattern;
	}
}
