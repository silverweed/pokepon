//: battle/BattleLogger.java

package pokepon.battle;

import java.util.*;

/** This class holds a record of a single battle history;
 * it's kept as generic as possible to allow extending it with custom loggers.
 *
 * @author Giacomo Parolini
 */
public abstract class BattleLogger {
	
	public final static int DEFAULT_MAXLINES = 10000;

	protected int maxLines = DEFAULT_MAXLINES;
	protected Iterable<String> history = new ArrayDeque<String>(maxLines / 2);
	protected String feedbackMsg;
	
	public BattleLogger() {}	

	public BattleLogger(int maxLines) {
		this.maxLines = maxLines;
	}

	public void addLine(final String line) {
		ArrayDeque<String> hist = (ArrayDeque<String>)history;
		if(hist.size() < maxLines)
			hist.add(line);			
	}

	/** This method can be overridden to obtain different behaviours (e.g it
	 * may be used to generate a battle replayer);
	 */
	public abstract void processRecord(Map<String, Object> opts);

	public String getFeedbackMsg() { return feedbackMsg; }
}
