//:util/ReorderLists.java

package pokepon.util;

import java.util.*;

/** Utility class used to sort lists */
public abstract class ReorderLists {

	/** This method quicksorts 3 associated lists by comparing the third given. */
	@SuppressWarnings("unchecked")
 	public static <T,U extends Comparable<U>> void doubleQuicksortComparingSecond(List<T> list1, List<U> list2, int low, int high) {
		
		// Get the pivot element from the middle of the list
		U pivot = list2.get(low + (high-low)/2);

		int i = low;
		int j = high;
		// Divide into two lists
		while (i <= j) {
		      // If the current value from the left list is smaller then the pivot
		      // element then get the next element from the left list
			while (list2.get(i).compareTo(pivot) < 0) {
				++i;
			}
			// If the current value from the right list is larger then the pivot
			// element then get the next element from the right list
			while (list2.get(j).compareTo(pivot) > 0) {
				--j;
			}

			// If we have found a values in the left list which is larger then
			// the pivot element and if we have found a value in the right list
			// which is smaller then the pivot element then we exchange the
			// values.
			// As we are done we can increase i and j
			if (i <= j) {
				exchange(list1,i,j);
				exchange(list2,i,j);
				++i;
				--j;
			}
		}
		// Recursion
		if (low < j)
			doubleQuicksortComparingSecond(list1,list2,low, j);
		if (i < high)
			doubleQuicksortComparingSecond(list1,list2,i, high);
	}

	private static <T> void exchange(List<T> list,int i, int j) {
		T tmp = list.get(i);
		list.set(i,list.get(j));
		list.set(j,tmp);
	}
}

						
