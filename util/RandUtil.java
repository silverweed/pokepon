//: util/RandUtil.java

package pokepon.util;

import java.util.*;

public class RandUtil {

	/** Given a list { a, b, c, ...}, where a, b, ...are floats, interprets it as the
	 * probability distribution to return 1, 2, ...etc; 
	 * @param dist A list of floats whose sum is 100f.
	 * @return A random integer from 0 to dist.size() - 1
	 */
	public static int getRandWithDistribution(List<Float> dist) {
		float r = 100f * rng.nextFloat();
		float sum = 0f;
		for(int i = 0; i < dist.size(); ++i) {
			sum += dist.get(i);
			if(r < sum)
				return i;
		}
		return dist.size() - 1;
	}

	public static void main(String[] args) throws Exception {
		List<Float> l = new ArrayList<Float>();

		for(String s : args) 
			l.add(new Float(s));

		int[] results = new int[args.length];
		for(int i = 0; i < 1000; ++i)
			++results[getRandWithDistribution(l)];

		for(int i = 0; i < results.length; ++i)
			MessageManager.printMsg(i+": "+results[i]);
	}

	private static Random rng = new Random();
}

