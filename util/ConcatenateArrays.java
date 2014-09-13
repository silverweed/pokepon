//: util/ConcatenateArrays.java

package pokepon.util;

import java.util.Arrays;
import java.util.List;

/** Class used to concatenate 2 arrays */
public class ConcatenateArrays {
	
	public static <T> T[] concatenate(T[] first, T[] second) {
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}
	
	/** Given an array of Strings, returns a single string with array elements
	 * separated by whitespaces.
	 */
	public static String merge(String[] array,int... index) {
		if(array == null || array.length == 0) return null;
		int start = 0;
		int end = array.length;
		if(index.length > 0) {
			start = index[0];
			if(index.length > 1)
				end = index[1];
		}
		StringBuilder sb = new StringBuilder(array[start]);
		for(int i = start+1; i < Math.min(array.length,end); ++i)
			sb.append(" " + array[i]);
		
		return sb.toString();
	}

	public static String merge(List<String> array, int...index) {
		return merge(array.toArray(new String[0]),index);
	}
}
