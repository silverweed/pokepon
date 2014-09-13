//: util/RandUtil.java

package pokepon.util;

import java.util.*;

public class RandUtil {

	/** Given a list { a, b, c, ...}, where a, b, ...are floats, interprets it as the
	 * probability distribution to return 1, 2, ...etc; this implementation is veery
	 * flawed, as it does not produce really uniform distributions where it should,
	 * so it should be fixed in future.
	 */
	public static int getRandWithDistribution(List<Float> dist) {
		float sum = 0f;
		for(float f : dist)
			sum += f;

		float r = (new Random()).nextFloat();

		if(r < dist.get(0)/sum) return 1;

		List<Float> cumul = new ArrayList<Float>();
		for(int i = 0; i < dist.size(); ++i) {
			float a = 0;
			for(int j = 0; j <= i; ++j) 
				a += dist.get(j);
			cumul.add(a);
		}

		for(int i = 0; i < cumul.size()-1; ++i) 
			if(r >= cumul.get(i)/sum && r < cumul.get(i+1)/sum) return i+2;

		return dist.size();
	}

	public static void main(String[] args) throws Exception {
		List<Float> l = new ArrayList<Float>();

		for(String s : args) 
			l.add(new Float(s));

		System.out.println(getRandWithDistribution(l));
	}
}

