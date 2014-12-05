//: util/ConcatenateArrays.java

package pokepon.util;

import java.util.Arrays;
import java.util.List;

/** Class used to manipulate arrays with trivial operations that the standard
 * Java libraries don't support.
 */
public class ConcatenateArrays {
	
	public static <T> T[] concatenate(T[] first, T[] second) {
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}
	
	/** Given an array of Strings, returns a single string with array elements
	 * separated by whitespaces.
	 * @param array The array to merge into a string
	 * @param start The index to start from (default: 0)
	 * @param end The index to end to (default: array.length)
	 * @param sep The separator to use (default: " ")
	 */
	public static String merge(String[] array, int start, int end, String sep) {
		if(array == null || array.length == 0) return null;
		StringBuilder sb = new StringBuilder(array[start]);
		end = Math.min(array.length, end);
		for(int i = start + 1; i < end; ++i) {
			sb.append(sep);
			sb.append(array[i]);
		}
		return sb.toString();
	}

	public static String merge(String[] array, int start, int end) {
		return merge(array, start, end, " ");
	}

	public static String merge(String[] array, int start) {
		return merge(array, start, array.length);
	}

	public static String merge(String[] array) {
		return merge(array, 0);
	}
}
