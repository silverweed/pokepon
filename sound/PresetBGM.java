//: sound/PresetBGM.java
package pokepon.sound;

import pokepon.util.Meta;
import java.util.*;

/** Utility class that constructs Loopers and sets loop points for the default audio resources 
 *
 * @author Giacomo Parolini
 */
public class PresetBGM {

	/** Map: { audiofilename: [ start_loop, end_loop ] } */
	private static Map<String,double[]> presets = new HashMap<>();
	static {
		presets.put("xy-rival.wav", new double[] { 7.802, 58.634 });
		// put others here ...
	}

	public static Map<String,double[]> getPresets() { return presets; }

	/** @param filename The basename of the file to play */
	public static Looper getLooper(final String filename) {
		if(presets.get(filename) != null) {
			Looper looper = new Looper(PresetBGM.class.getResource(Meta.complete2(Meta.AUDIO_DIR)+"/"+filename));
			looper.setLoopPoints(presets.get(filename)[0], presets.get(filename)[1]);
			return looper;
		} else {
			return null;
		}
	}
}
