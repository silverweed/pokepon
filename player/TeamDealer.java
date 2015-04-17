//: player/TeamDealer.java

package pokepon.player;

import pokepon.pony.*;
import pokepon.util.*;
import pokepon.move.*;
import pokepon.enums.*;
import static pokepon.util.MessageManager.*;
import static pokepon.util.Meta.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.nio.*;
import java.nio.file.*;

/** This class contains methods to save and load a Team of ponies to/from file, 
 * including a parser which constructs a pony from a pony's save data.
 *
 * @author silverweed
 */
 
public class TeamDealer {

	public static final String SAVE_EXT = ".pkp";

	private Pony pony = null;

	public TeamDealer() {}

	/** @return true - if the team was successfully saved; false - otherwise. */
	public boolean save(Team team,String filename) {
		if(Debug.pedantic) printDebug("[TeamDealer.save()] called with team="+team+", filename="+filename);
		boolean success = true;
		// ensure the save directory exists
		if(!Files.isDirectory(Paths.get(filename).getParent())) {
			if(!Files.exists(Paths.get(filename).getParent())) {
				try {
					Files.createDirectories(Paths.get(filename).getParent());
					if(Debug.on) printDebug("[TeamDealer] "+Paths.get(filename).getParent()+" does not exist: creating it.");
				} catch(IOException e) {
					printDebug("[TeamDealer] Caught IOException while creating save directory: ");
					e.printStackTrace();
					return false;
				}
			} else {
				printDebug("[TeamDealer] Error: could not create "+Paths.get(filename).getParent()+": file already exists with the same name!");
				return false;
			}
		}
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "utf-8"))) {
			if(!team.getName().equals("Untitled Team"))
				writer.write("$TEAM_NAME = "+team.getName()+"\n");
			for(Pony p : team) {
				if(p == null) continue;
				try {
					writer.write(p.getSaveData()+"\n");
				} catch(Exception e) {
					printDebug("Caught exception while calling writer.writer("+p.getName()+".getSaveData(): "+e);
					e.printStackTrace();
					success = false;
				}
			}
			writer.flush();
		} catch(IOException e){
			printDebug("Caught IOException while creating save file "+filename+": "+e);
			e.printStackTrace();
			success = false;
		} catch(Exception e) {
			printDebug("Caught Exception while creating save file "+filename+": "+e);
			e.printStackTrace();
			success = false;
		} 

		if(!success) return false;
		printDebug("Team successfully saved to "+filename);
		return true;
	}
	
	/** @return true - if the team was successfully loaded; false - otherwise. */
	public boolean load(Team team,String filename) {
		if(Debug.pedantic) printDebug("[TeamDealer.load()] called with team="+team+", filename="+filename);
		pony = null;
		team.setName("Untitled Team");
		boolean success = true;
		try (Scanner scanner = new Scanner(new File(filename))) {
			String input = "";
			if(success) {
				/* Reset current team */
				team.clear();
				while(scanner.hasNext()) {
					input = scanner.nextLine();
					if(Debug.pedantic) printDebug("scanner::input = "+input);

					/* Parse tokens */
					if(!parseSaveDataLine(input,team)) {
						success = false;
						printDebug("breaking.");
						break;
					} else if(Debug.pedantic) printDebug("parseSaveDataLine returned OK");				
					
					if(Debug.pedantic) printDebug("scanner::hasNext() = "+scanner.hasNext());
				}
			}				
		} catch(FileNotFoundException e) {
			printDebug("[TeamDealer] Error: file "+filename+" not found.");
			success = false;
		} catch(Exception e) {
			printDebug("Caught exception while opening file "+filename+": "+e);
			e.printStackTrace();
			success = false;
		} 

		if(!success) return false;
		for(Pony p : team)
			p.setHp(p.maxhp());
		printDebug("Team successfully loaded from "+filename);
		return true;
	}
	
	/** Returns list of possible save files in directory "path" (those ending with ".pkp") */
	public List<File> listSaveFiles(String path) {
		File dirpath = new File(path);
		// ensure the save path exists
		if(!dirpath.isDirectory()) {
			if(!dirpath.exists()) {
				printDebug("[TeamDealer] "+path+" does not exist: creating it...");
				try {
					Files.createDirectories(Paths.get(path));
					return Collections.<File>emptyList();
				} catch(IOException e) {
					printDebug("[TeamDealer] Exception while creating save directory:");
					e.printStackTrace();
					return null;
				}
			} else {
				printDebug("[TeamDealer] Error: path `"+path+"' is not a valid directory path, and could not create it.");
				return null;
			}
		}

		String[] namelist = dirpath.list(new FilenameFilter() {
			public boolean accept(File dirpath, String name) {
				Pattern pattern = Pattern.compile(".+\\.pkp$");
				return pattern.matcher(name).matches();
			}
		});
		
		List<File> filelist = new ArrayList<File>();
		for(String s : namelist) {
			try {
				filelist.add(new File(s));
			} catch(Exception e) {
				printDebug("[TeamDealer] Caught exception while creating file from string "+s+": "+e);
			}
		}
		
		Collections.sort(filelist);	
		return filelist;
	}
	
	/** Overloaded version of listSaveFile that uses the default save directory. */
	public List<File> listSaveFiles() {
		if(Debug.pedantic) printDebug("[TeamDealer] invoking listSavefiles("+Meta.getSaveURL().getPath()+")");
		return listSaveFiles(Meta.getSaveURL().getPath());
	}
	
	/** Parses a single line of SaveData and edits pony's stats consequently.
	 * @return true: parsed successfully; false: parsed incorrectly
	 */
	@SuppressWarnings("unchecked")
	public boolean parseSaveDataLine(String input,Team team) {
		/* Split line in tokens */
		String[] tmptoken = input.split(" ");
		
		if(Debug.pedantic) printDebug("[parseSaveDataLine] line: "+input);

		/* If entire line is a comment, return */
		if(tmptoken[0] != null && tmptoken[0].startsWith("#")) return true;

		/* Remove trailing comments */
		int j = 0;
		for( ; j < tmptoken.length; ++j) {
			tmptoken[j] = tmptoken[j].trim();
			if(tmptoken[j].startsWith("#"))
				break;
		}
		String[] token = Arrays.copyOf(tmptoken,j);
		if(Debug.pedantic) printDebug("Kept tokens: "+Arrays.asList(token));
		
		if(token[0] == null) {
			return false;
		} else if(token[0].equals("$TEAM_NAME") && token.length >= 3) {
			if(Debug.pedantic) printDebug("Parsed: team name");
			team.setName(ConcatenateArrays.merge(token,2));
		} else if(token[0].equals("Level:")) {
			if(Debug.pedantic) printDebug("Parsed: level");
			try {
				pony.setLevel(Integer.parseInt(token[1]));
			} catch(Exception e) { 
				printDebug("[parseSaveDataLine] Caught exception while parsing line: ");
				e.printStackTrace();
				return false;
			}
			if(Debug.pedantic) printDebug(pony+"'s level is now "+pony.getLevel());
		} else if(token[0].equals("Nature:")) {
			if(Debug.pedantic) printDebug("Parsed: nature");
			try {
				Pony.Nature newnature = Pony.Nature.forName(token[1]);
				if(newnature != null)
					pony.setNature(newnature);
				else
					printDebug("Unknown nature: "+token[1]);
			} catch(Exception e) { 
				printDebug("[parseSaveDataLine] Caught exception while parsing line: "+e);
				e.printStackTrace();
				return false;
			}
			if(Debug.pedantic) printDebug(pony+"'s nature is now "+pony.getNature());
		} else if(token[0].equals("Ability:")) {
			if(Debug.pedantic) printDebug("Parsed: ability");
			try {
				pony.setAbility(AbilityCreator.create(ConcatenateArrays.merge(token,1)));	
			} catch(Exception e) { 
				printDebug("[parseSaveDataLine] Caught exception while parsing line: "+e);
				e.printStackTrace();
				return false;
			}
			if(Debug.pedantic) printDebug(pony+"'s ability is now "+pony.getAbility());
		} else if(token[0].equals("Happiness:")) {
			if(Debug.pedantic) printDebug("Parsed: happiness");
			try {
				pony.setHappiness(Integer.parseInt(token[1]));
			} catch(Exception e) { 
				printDebug("[parseSaveDataLine] Caught exception while parsing line: "+e);
				e.printStackTrace();
				return false;
			}
			if(Debug.pedantic) printDebug(pony+"'s happiness is now "+pony.getHappiness());
		} else if(token[0].equals("EVs:")) {
			if(Debug.pedantic) printDebug("Parsed: EVs");
			Iterator<String> it = Arrays.asList(input.split("\\ +")).iterator();
			it.next();
			while(it.hasNext()) {
				try {
					int num = Integer.parseInt(it.next());
					if(!it.hasNext()) return false;
					String ev = it.next();
					if(!Arrays.asList(Pony.STAT_NAMES).contains(ev)) return false;
					pony.setEV(Pony.Stat.forName(ev), num);
					if(it.hasNext()) it.next();	//remove trailing "/"
				} catch(Exception e) { 
					printDebug("[parseSaveDataLine] Caught exception while parsing line: "+e);
					e.printStackTrace();
					return false;
				}
			}
		} else if(token[0].equals("IVs:")) {
			if(Debug.pedantic) printDebug("Parsed: IVs");
			Iterator<String> it = Arrays.asList(input.split("\\ +")).iterator();
			it.next();
			while(it.hasNext()) {
				try {
					int num = Integer.parseInt(it.next());
					if(!it.hasNext()) return false;
					String iv = it.next();
					if(!Arrays.asList(Pony.STAT_NAMES).contains(iv)) return false;
					pony.setIV(Pony.Stat.forName(iv), num);
					if(it.hasNext()) it.next();	//remove trailing "/"
				} catch(Exception e) { 
					printDebug("[parseSaveDataLine] Caught exception while parsing line: "+e);
					e.printStackTrace();
					return false;
				}
			}
		} else if(token[0].equals("-")) {
			if(Debug.pedantic) printDebug("Parsed: move");
			try {
				String movename = ConcatenateArrays.merge(token,1);
				String sanedMovename = null;
				Pattern pattern = Pattern.compile("\\s*Hidden Talent \\(([A-Z][a-z]+)\\)\\s*");
				Matcher matcher = pattern.matcher(movename);
				Type type = null;
				if(matcher.matches()) {
					sanedMovename = "Hidden Talent";
					type = Type.forName(matcher.group(1));
				} else {
					/* Next line is commented out because saning the move names with the
					 * Saner produces a huge team loading performance loss (more than 10 times
					 * slower). Just don't make typos in the save files.
					 */
					//sanedMovename = Saner.sane(movename,Meta.complete(MOVE_DIR),Move.class);
					sanedMovename = movename.replaceAll(" ","");
				}
				if(sanedMovename == null) {
					if(Debug.on) printDebug("[TeamDealer] Failed to parse move " + movename + ": giving up.");
					return false;
				}
				if(Debug.pedantic) printDebug("Movename: raw="+token[1]+"; saned="+sanedMovename);
				// Add the move (without checking if learnable)
				Move mv = MoveCreator.create(sanedMovename,pony);
				if(type != null) {
					mv.setType(type);
					mv.setName(mv.getName() + " ("+type+")");
				}
				if(pony.addMove(mv) == -1) {
					if(Debug.on) printDebug("[TeamDealer] Move " + sanedMovename + " was not added to pony " + pony);
				}
			} catch(Exception e) { 
				printDebug("[parseSaveDataLine] Caught exception while parsing line: "+e);
				e.printStackTrace();
				return false;
			}
			if(Debug.pedantic) printDebug(pony+"'s moves are now "+pony.getMoves());
		} else {	/* Either a pony's name or a blank line */
			if(Debug.pedantic) printDebug("Parsed: pony ("+token[0]+")");
			if((token[0].equals(""))) return true;
			
			String ponyName = token[0];
			int i = 1;
			/* Go on parsing the name until "~" or "@" are found, and remove whitespaces to form correct class name. */
			for( ; i < token.length; ++i) {
				if(token[i].equals("~") || token[i].equals("@")) break;
				ponyName += token[i];
			}
			
			try {
				pony = PonyCreator.create(ponyName);
				String nick = null;
				String item = null;
				/* No need to reset counter "i": start parsing from end of the pony's name */
				for( ; i < token.length; ++i) {
					if(Debug.pedantic) printDebug("parsing token "+token[i]+"; nick = "+nick+", item = "+item);
					if(token[i].equals("~")) {
						nick = "";
					} else if(token[i].equals("@")) {	
						item = "";
					} else {
						if(item == null)	// we are parsing nickname
							nick += token[i] + " ";
						else			// we are parsing item name (note that item MUST be after the nickname)
							item += token[i] + " ";
					}
				}
				if(nick != null) {
					pony.setNickname(nick.trim());
					if(Debug.pedantic) printDebug("Set pony's nick to "+pony.getNickname());
				}
				if(item != null) {
					try {
						pony.setItem(ItemCreator.create(item.trim()));
					} catch(ReflectiveOperationException e) {
						printDebug("[parseSaveDataLine] Error: couldn't give item "+item.trim()+" to "+pony.getFullName()+": "+e);
						e.printStackTrace();
					}
				}
				team.add(pony);	
			} catch(ReflectiveOperationException e) {
				printDebug("[parseSaveDataLine] Caught exception while creating pony: "+e);
				e.printStackTrace();
				return false;
			}
			if(Debug.pedantic) printDebug("Team is now: "+team);
		}		
		
		return true;
	}

	public static File ensureSaveDirExists() {
		return Meta.ensureDirExists(Meta.getSaveURL().getPath());
	}
}
