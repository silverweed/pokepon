//: net/jack/client/FileBattleLogger.java

package pokepon.net.jack.client;

import pokepon.gui.*;
import pokepon.battle.*;
import pokepon.util.*;
import static pokepon.util.MessageManager.*;
import java.util.*;
import java.io.*;

public class FileBattleLogger extends BattleLogger {

	protected final BattlePanel bp;

	public FileBattleLogger(final BattlePanel bp) {
		super();
		this.bp = bp;
	}

	public FileBattleLogger(final BattlePanel bp, final int lines) {
		super(lines);
		this.bp = bp;
	}

	/** Dumps the history content on a file */
	@Override
	public void processRecord(Map<String, Object> opts) {
		File outFile = null;
		if(opts != null && opts.containsKey("outFile")) {
			int i = 0;
			do {
				outFile = new File((String)opts.get("outFile"));
				++i;
			} while(outFile.exists());
		} else {
			if(Meta.ensureDirExists(Meta.getBattleRecordsURL().getPath()) == null) {
				printDebug("[BattleLogger] fatal error: directory not created. Aborting.");
				return;
			}
			int i = 0;
			do {
				outFile = new File(Meta.getBattleRecordsURL().getPath()+"/"+bp.getBattleID()+"_"+bp.getPlayer(1)+
					"_vs_"+bp.getPlayer(2)+"-"+bp.getFormat().replaceAll(" ","")+(i == 0 ? "" : "-"+i));
				++i;
			} while(outFile.exists());
		}
		if(outFile.canWrite()) {
			try (PrintWriter writer = new PrintWriter(new FileOutputStream(outFile))) {
				for(String line : history) 
					writer.println(line);
				feedbackMsg = "Battle saved in "+outFile+".";
			} catch(FileNotFoundException e) {
				printDebug("[BattleLogger] File not found: "+outFile);
			}
		}
	}
}
