//: battle/MessageManager.java

package pokepon.util;

import pokepon.battle.Battle;
import pokepon.pony.Pony;
import java.util.*;
import java.io.PrintStream;

/** Class that provides an abstraction level for communication
 * between battle classes and UI.
 *
 * @author Giacomo Parolini
 */
 
public class MessageManager {

	public static final char CMD_PREFIX = '/';
	public static final char CMN_PREFIX = '!';
	public static final char BTL_PREFIX = '~';
	private static StrAppendable altOut, altErr;

	/** Do not instance me, plz! */
	private MessageManager() {}

	public static void setAltOut(StrAppendable alt) {
		altOut = alt;
	}
	
	public static void setAltErr(StrAppendable alt) {
		altErr = alt;
	}

	public static StrAppendable getAltOut() {
		return altOut;
	}

	public static StrAppendable getAltErr() {
		return altErr;
	}

	public static void printMsg(String str) {
		if(altOut == null)
			System.out.println(str);
		else
			altOut.append(str+"\n");
	}
	
	public static void printMsgnb(String str) {
		if(altOut == null)
			System.out.print(str);
		else
			altErr.append(str);
	}
		
	public static void printDebug(String str) {
		if(altErr == null)
			System.err.println(str);
		else
			altErr.append(str+"\n");
	}

	public static void printDebugnb(String str) {
		if(altErr == null)
			System.err.print(str);
		else
			altErr.append(str);
	}

	public static void printFormat(String str, Object... args) {
		if(altOut == null)
			System.out.format(str, args);
		else
			altOut.append(String.format(null, str, args));
	}

	public static void consoleMsg(String str) {
		System.out.println(str);
	}
	
	public static void consoleMsgnb(String str) {
		System.out.print(str);
	}
		
	public static void consoleDebug(String str) {
		System.err.println(str);
	}

	public static void consoleDebugnb(String str) {
		System.err.print(str);
	}

	public static void consoleFormat(String format,Object... args) {
		System.out.format(format,args);
	}

	public static <T> void consoleTable(Collection<T> c, int cols) {
		consoleTable(c, cols, 2);
	}

	/** Prints a collection in an ordered table with 'cols' columns 
	 * Spaces between columns are automatically resized to fit longest
	 * element's name in each column; note that this uses T.toString() 
	 * method.
	 * @param c The collection of elements to output
	 * @param cols The number of columns
	 * @param colspacing Spacing between columns (default: 2)
	 */
	public static <T> void consoleTable(Collection<T> c, int cols, int colspacing) {
		
		if(Debug.pedantic) {
			printDebug("passed collection: "+c.toString());
			printDebug("size: "+c.size()+" (size/cols: "+c.size()/cols+")");
		}

		/* Get elements' lengths */
		List<Integer> len = new ArrayList<Integer>();
		for(T t : c) {
			len.add(t.toString().length());
		}

		/* Convert collection into 2D array */
		String[][] matrix = new String[c.size()/cols+1][cols];

		for(int i = 0; i < c.size(); ++i) {
			matrix[i/cols][i%cols] = c.toArray()[i].toString();
		}

		/* Ensure no element is null */
		for(int i = 0; i < c.size()/cols+1; ++i)
			for(int j = 0; j < cols; ++j)
				if(matrix[i][j] == null) matrix[i][j] = "";

		if(Debug.pedantic) {
			printDebug("matrix:");
			for(int i = 0; i < c.size()/cols+1; ++i) {
				printDebugnb("["+i+"] ");
				for(int j = 0; j < cols; ++j)
					printDebugnb(matrix[i][j]+" ");
				printDebug("");
			}
		}

		/* Get max lengths of each columns */
		int[] maxLen = new int[cols];
		
		if(Debug.pedantic) {
			printDebug("maxLen:");
			for(int i = 0; i < cols; ++i)
				printDebugnb(maxLen[i]+" | ");
			printDebug("");
		}

		for(int j = 0; j < cols; ++j) {
			for(int i = 0; i < c.size()/cols+1; ++i)
				if(matrix[i][j].length() > maxLen[j]) maxLen[j] = matrix[i][j].length();
			if(Debug.pedantic) System.err.println("Column "+j+": maxlen= "+maxLen[j]);
		}


		for(int i = 0; i < c.size()/cols+1; ++i) {
			for(int j = 0; j < cols; ++j) {
				System.out.format("%-"+Math.max(1,maxLen[j])+"s",matrix[i][j]);
				if(j < cols-1) 
					for(int k = 0; k < colspacing; ++k) System.out.print(" ");
			}
			System.out.println("");
		}
		System.out.println("");
	}

	public static <T> void consoleFixedTable(Collection<T> c,int cols) {
		consoleFixedTable(c, cols, 2);
	}

	/** Prints a collection in ordered table with 'cols' columns;
	 * columns are equally spaced with enough space to fit longest element
	 * in collection; differs from consoleTable in that the cols' width is
	 * the same for each column, while consoleTable uses a different width
	 * for each column (i.e: here maxLen is global, in consoleTable is column-wise). 
	 */
	public static <T> void consoleFixedTable(Collection<T> c,int cols,int colspacing) {
		int maxLen = 1;
		for(T t : c) 
			if(t.toString().length() > maxLen) maxLen = t.toString().length();

		int i = 1;
		for(T t : c) {
			System.out.format("%-"+maxLen+"s",t.toString());
			if(i++ % cols != 0) 
				for(int k = 0; k < colspacing; ++k) System.out.print(" ");
			else System.out.println("");
		}
		System.out.println("");
	}
	
	/** Prints a 'header' in a nice format, boxed by char:
	 * e.g. if char='*', print a 'string' like this:
	 *      ************************
	 *      *                      *
	 *      *        STRING        *
	 *      *                      *
	 *      ************************
	 *
	 * @param text An array of Strings, where each element is a different row of the header
	 * @param d The box delimiter
	 * @param ps (Optional) A PrintStream where to output (default: System.out)
	 */
	public static void consoleHeader(String[] text,char d,PrintStream... ps) {
		PrintStream stream = System.out;
		if(ps.length > 0) {
	 		stream = ps[0];
	 	}
	 	/* First, get maximum length of a row */
	 	int maxLen = 0;
	 	for(String s : text) {
	 		if(s.length() > maxLen) maxLen = s.length();
	 	}
	 	
	 	maxLen += 2;	// leave some space
	 	
	 	StringBuilder sb = new StringBuilder("");
	 	for(int i = 0; i < maxLen; ++i)
	 		sb.append(d);
	 		
	 	/* Print first line of box */
	 	stream.format("%c%-"+maxLen+"s%c\n",d,sb.toString(),d);
	 	/* Print second line */
	 	stream.format("%c%-"+maxLen+"s%c\n",d," ",d);
	 	
	 	/* Print content lines (centered) */
	 	for(String s : text) {
	 		int lineLen = (maxLen-s.length())/2 + s.length() + (maxLen-s.length())/2;
	 		if(Debug.pedantic) printDebug("consoleHeader - lineLen="+lineLen+", maxLen="+maxLen);
	 		/* This is to fix integer rounding issues which may cause disalignment */
	 		if(lineLen < maxLen)
	 			stream.format("%c%-"+((maxLen-s.length())/2)+"s%s%-"+((maxLen-s.length())/2+1)+"s%c\n",d," ",s," ",d);
	 		else 
	 			stream.format("%c%-"+((maxLen-s.length())/2)+"s%s%-"+((maxLen-s.length())/2)+"s%c\n",d," ",s," ",d);
	 	}
	 	
	 	/* Print penultimate line */
	 	stream.format("%c%-"+maxLen+"s%c\n",d," ",d);
	 	/* Print last line */
	 	stream.format("%c%-"+maxLen+"s%c\n",d,sb.toString(),d);
	}

	/** Overloaded method that uses the 'default style' for the console header, ie
	  * '*' as delimiter char and System.out as printstream.
	  */
	public static void consoleHeader(String[] text) {
	 	consoleHeader(text,'*',System.out);
	}

	/** Overloaded method used to pass a single line to consoleHeader */
	public static void consoleHeader(String text,char d,PrintStream... ps) {
		consoleHeader(new String[] {text},d,ps);
	}

	/** Overloaded method used to pass a single line to default consoleHeader. */
	public static void consoleHeader(String text) {
		consoleHeader(new String[] {text});
	}

	public static void printDamageMsg(Pony pony,int inflictedDamage) {
		if(Battle.SHOW_HP_PERC) printMsg(pony.getNickname()+" lost "+(int)(100.*inflictedDamage/pony.maxhp())+"% of its HP!");
		else printMsg(pony.getNickname()+" lost "+inflictedDamage+" HP!");
	}
	 		
	/** This is a hand-made function that converts some HTML entities to their entity number;
	 * in future this may be replaced by a more advanced sanitizer.
	 */
	public static String sanitize(String str) {
		return str
			.replaceAll("<","&#60;")
			.replaceAll(">","&#62;")
			.replaceAll("/","&#47;")
			// remove "zalgo" effect (thanks, Zarel)
			.replaceAll("[\u0300-\u036f\u0483-\u0489\u0E31\u0E34-\u0E3A\u0E47-\u0E4E]{3,}","");
	}
}
