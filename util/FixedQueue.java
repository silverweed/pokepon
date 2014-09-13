//: util/FixedQueue.java

package pokepon.util;

import java.util.*;
import static pokepon.util.MessageManager.*;

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
