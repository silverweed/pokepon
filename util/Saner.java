//: util/Saner.java

package pokepon.util;

import pokepon.pony.*;
import pokepon.move.*;
import static pokepon.util.MessageManager.*;
import java.util.*;
import java.net.*;

/** This class provides static methods to convert a given string to
 * a known valid string chosen from a given collection;
 * note that the sane() method shouldn't be called repeatedly wherever
 * performance is important, because it slows down things a lot.
 *
 * @author Giacomo Parolini
 */

public class Saner {

	public static String sane(String str,String path) {
		return sane(str,path,Object.class);
	}

	/** @param str The string to convert in a legit classname
	 * @param path The path where to search the classes to compare with str
	 * @param base (Optional): compare only with subclasses of class base.
	 */
	public static String sane(String str,String path,Class<?> baseClass) {

		/* A Classname must consist in string without whitespaces and 
		 * a capitalized first letter. */
		if(Debug.pedantic) printDebug("sane - str="+str+", path="+path+", baseClass="+baseClass);
		List<String> knownClasses = ClassFinder.findSubclassesNames(path,baseClass);	//find all classes in path

		if(Debug.pedantic) printDebug("Known classes: "+knownClasses.toString());
	
		return sane(str,knownClasses);
	}

	/** @param str The string to convert in a known one 
	 * @param known A class providing an iterator over known strings (typically, a Set)
	 * @param skipTryZero (Optional): skip try #0 (i.e. confronting case-insensitive-ly)
	 */
	public static String sane(String str,Iterable<String> known,boolean... skipTryZero) {
		if(Debug.pedantic) printDebug("Called sane("+str+", "+known+", "+(skipTryZero.length>0 ? skipTryZero[0] : "")+")");
		if(skipTryZero.length > 0 && skipTryZero[0] == true) {
			/* TRY #0: Simply confront not case-sensitive-ly */
			for(String s : known) {
				if(Debug.on) printDebugnb("(Try#0): Comparing "+s+" to "+str+" ...");
				if(s.equalsIgnoreCase(str)) {
					if(Debug.pedantic) printDebug("Matched.");
					else if(Debug.on) printDebug("Matched "+s+" with "+str+" at Try#0");
					return s;
				} else if(Debug.pedantic) printDebug("Not matched.");
			}
		}

		/* TRY #1: Simply remove whitespaces and try matching ignoring case */
		String tmp = str.trim().replaceAll("\\ +","");
		
		for(String s : known) {
			if(Debug.pedantic) printDebugnb("(Try#1): Comparing "+s+" to "+tmp+" ...");
			if(s.equalsIgnoreCase(tmp)) {
				if(Debug.pedantic) printDebug("Matched.");
				else if(Debug.on) printDebug("Matched "+s+" with "+tmp+" at Try#1");
				return s;
			} else if(Debug.pedantic) printDebug("Not matched.");
		}

		/* TRY #2: remove characters in (trimmed) given string to correct additional characters typos */
		for(String s : known) {
			try {
				for(int i = 1; i <= tmp.length(); ++i) {
					String tmp2 = "";
					tmp2 = tmp.substring(0,i-1) + tmp.substring(i);
					if(Debug.pedantic) printDebugnb("(Try#2): Comparing "+s+" to "+tmp2+" ...");
					if(s.equalsIgnoreCase(tmp2)) {
						if(Debug.pedantic) printDebug("Matched.");
						else if(Debug.on) printDebug("Matched "+s+" with "+tmp2+" at Try#2");
						return s;
					} else if(Debug.pedantic) printDebug("Not matched.");
				}
			} catch(Exception e) {
				printDebug("Caught exception in try #2: "+e);
			}
		}

		/* TRY #3: try removing characters from known strings to correct forgotten characters */
		for(String s : known) {
			try {
				for(int i = 1; i <= s.length(); ++i) {
					String s2 = "";
					s2 = s.substring(0,i-1) + s.substring(i);
					if(Debug.pedantic) printDebugnb("(Try#3): Comparing "+s2+" to "+tmp+" ...");
					if(s2.equalsIgnoreCase(tmp)) {
						if(Debug.pedantic) printDebug("Matched.");
						else if(Debug.on) printDebug("Matched "+s2+" with "+tmp+" at Try#3");
						return s;
					} else if(Debug.pedantic) printDebug("Not matched.");
				}
			} catch(Exception e) {
				printDebug("Caught exception in try #3: "+e);
			}
		}

		/* TRY #4: try removing characters from both known strings and given one to correct wrong characters */
		for(String s : known) {
			try {
				for(int i = 1; i <= s.length(); ++i) {
					String s2 = "";
					s2 = s.substring(0,i-1) + s.substring(i);
					for(int j = 1; j < tmp.length(); ++j) {
						String tmp2 = "";
						tmp2 = tmp.substring(0,j-1) + tmp.substring(j);
						if(Debug.pedantic) printDebugnb("(Try#4): Comparing "+s2+" to "+tmp2+" ...");
						if(s2.equalsIgnoreCase(tmp2)) {
							if(Debug.pedantic) printDebug("Matched.");
							else if(Debug.on) printDebug("Matched "+s2+" with "+tmp2+" at Try#4");
							return s;
						} else if(Debug.pedantic) printDebug("Not matched.");
					}
				}
			} catch(Exception e) {
				printDebug("Caught exception in try #4: "+e);
			}
		}

		/* TRY #5: try swapping pairs of characters of given string to correct dolan typos */
		for(String s : known) {
			try {
				for(int i = 0; i < tmp.length()-1; ++i) {
					String tmp2 = "";
					tmp2 = swap(tmp,i,i+1);
					if(Debug.pedantic) printDebugnb("(Try#5): Comparing "+s+" to "+tmp2+" ...");
					if(s.equalsIgnoreCase(tmp2)) {
						if(Debug.pedantic) printDebug("Matched.");
						else if(Debug.on) printDebug("Matched "+s+" with "+tmp2+" at Try#5");
						return s;
					} else if(Debug.pedantic) printDebug("Not matched.");
				}
			} catch(Exception e) {
				printDebug("Caught exception in try #5: "+e);
			}
		}

		return null;
	}

	/** Swaps 2 characters in a string */
	private static String swap(String str,int i,int j) throws IndexOutOfBoundsException {
		if(i < 0 || j < 0 || i > str.length() || j > str.length())
			throw new IndexOutOfBoundsException("Thrown from Saner::swap");
		
		if(i == j) return str;
		
		StringBuilder sb = new StringBuilder("");
		int l = (i < j ? i : j);
		int m = (i < j ? j : i);

		for(int k = 0; k < str.length(); ++k) {
			if(k == l) {
				sb.append(str.charAt(m));
			} else if(k == m) {
				sb.append(str.charAt(l));
			} else sb.append(str.charAt(k));
		}
		
		return sb.toString();
	}
	
}
