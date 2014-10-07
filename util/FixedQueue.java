//: util/FixedQueue.java

package pokepon.util;

import java.util.*;
import static pokepon.util.MessageManager.*;

/** A Deque with fixed size: adding an element while size == limit
 * will cause the first element to be polled from the queue.
 *
 * @author Giacomo Parolini
 */
public class FixedQueue<T> extends ArrayDeque<T> {
	
	private int limit;

	public FixedQueue(int limit) {
		super();
		this.limit = limit;
	}

	@Override
	public boolean add(T elem)  {
		if(size() == limit) {
			pollFirst();
		}
		super.add(elem);
		return true;
	}

	public void setLimit(int newlim) {
		limit = newlim;
		while(size() > limit) {
			removeLast();
		}
	}
}
