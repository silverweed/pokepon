//: net/jack/client/FileBattleLogger.java

package pokepon.net.jack.client;

import pokepon.gui.*;
import pokepon.battle.*;
import pokepon.util.*;
import static pokepon.util.MessageManager.*;
import java.util.*;
import java.io.*;

/** Simple BattleLogger which saves the raw battle dump in a file.
 * 
 * @author silverweed
 */
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
			File outdir = null;
			if((outdir = Meta.ensureDirExists(Meta.getBattleRecordsURL().getPath())) == null) {
				printDebug("[FileBattleLogger] fatal error: directory not created. Aborting.");
				return;
			}
			int i = 0;
			do {
				outFile = new File(outdir.getPath()+Meta.DIRSEP+bp.getBattleID()+"_"+bp.getPlayer(1).getName()+
					"_vs_"+bp.getPlayer(2).getName()+"-"+bp.getFormat().replaceAll(" ","")+(i == 0 ? "" : "-"+i)+".log");
				++i;
				if(Debug.pedantic) printDebug("[FileBattleLogger] "+outFile+".exists() = "+outFile.exists());
			} while(outFile.exists());
		}
		if(Debug.on) printDebug("[FileBattleLogger] outfile = "+outFile);
		try (PrintWriter writer = new PrintWriter(new FileOutputStream(outFile))) {
			// append header
			writer.println("# Battle: " + bp.getPlayer(1).getName() + " vs " + bp.getPlayer(2).getName() + 
				" (" + bp.getFormat() + ")");
			writer.println("# Date: " + new Date());
			for(String line : history) 
				writer.println(line);
			feedbackMsg = "Battle saved in "+outFile+".";
		} catch(FileNotFoundException e) {
			printDebug("[FileBattleLogger] File not found: "+outFile);
		}
	}
}
