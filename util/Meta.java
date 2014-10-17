//: util/Meta.java

package pokepon.util;

import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.util.regex.*;
import java.net.*;
import static pokepon.util.MessageManager.*;
import static pokepon.util.ClassFinder.findSubclassesNames;
import pokepon.pony.Pony;
import pokepon.move.Move;
import pokepon.ability.Ability;
import pokepon.item.Item;

/** This class contains some static methods to retrieve information
 * on the Pokepon package itself.
 * THIS IS A VERY DELICATE CLASS: HANDLE WITH CARE!
 *
 * @author Giacomo Parolini
 */
public class Meta {
	public static final char DIRSEP = '/'; // probably superfluous
	protected static Path cwd;  // current working directory
	static {
		String tmp = Meta.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		// Strip the leading slash if on windows
		if(tmp.matches("^/[A-Z]:/.*")) {
			tmp = tmp.substring(1);
		}
		cwd = Paths.get(tmp);
	}
	protected static URL cwdURL;
	public static final boolean LAUNCHED_FROM_JAR = cwd.toString().endsWith(".jar");
	/**  Directory containing variable data (teams, confs, ...) */
	public static final String APPDATA_DIR = (System.getProperty("os.name").toUpperCase().contains("WIN")
						? System.getenv("APPDATA") + DIRSEP
						: System.getenv("HOME") + DIRSEP + ".")
						+ "pokepon";
	static {
		// if launched from a jar, use the parent as cwd
		if(cwd.toString().endsWith(".jar"))
			cwd = cwd.getParent();
		if(Debug.on) printDebug("[Meta] cwd: "+cwd+"\nLaunched from jar: "+LAUNCHED_FROM_JAR);
	}
	protected static String cwdStr = "file://"+cwd.toString();

	public static URL getCwd() {
		if(cwdURL != null) return cwdURL;
		try {
			cwdURL = new URL(cwdStr);
			return cwdURL;
		} catch(MalformedURLException e) {
			return null;
		}
	}

	public static Path getCwdPath() {
		return cwd;
	}

	private static URL getSubURL(String subdir) {
		if(LAUNCHED_FROM_JAR) {
			if(Debug.pedantic) printDebug("[Meta.getSubURL("+POKEPON_ROOTDIR+DIRSEP+subdir+")]: "+
				Meta.class.getClassLoader().getResource(POKEPON_ROOTDIR+DIRSEP+subdir));
			return Meta.class.getClassLoader().getResource(POKEPON_ROOTDIR+DIRSEP+subdir);
		} else {
			try {
				return new URL(getCwd().getProtocol()+"://"+getCwd().getPath()+DIRSEP+POKEPON_ROOTDIR+DIRSEP+subdir);
			} catch(MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private static URL getAppDataURL(String subdir) {
		try {
			if(Debug.pedantic) printDebug("[Meta.getAppDataURL("+subdir+")]: "+
				"file://"+APPDATA_DIR+DIRSEP+subdir);
			return new URL("file://"+APPDATA_DIR+DIRSEP+subdir);
		} catch(MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	/** Returns the URL of the package root */
	public static URL getRootURL() {
		return getSubURL("");
	}
	
	/** Returns the URL of the pony directory. */
	public static URL getPonyURL() {
		return getSubURL(PONY_DIR);
	}
	
	/** Returns the URL of the move directory. */
	public static URL getMoveURL() {
		return getSubURL(MOVE_DIR);
	}

	/** Returns the URL of the item directory. */
	public static URL getItemURL() {
		return getSubURL(ITEM_DIR);
	}

	/** Returns the URL of the ability directory. */
	public static URL getAbilityURL() {
		return getSubURL(ABILITY_DIR);
	}
	
	/** Returns the URL of the save directory. NOTE: this is in local AppData */
	public static URL getSaveURL() {
		return LAUNCHED_FROM_JAR ? getAppDataURL(SAVE_DIR) : getSubURL("data" + DIRSEP + SAVE_DIR);
	}
	
	/** Returns the URL of the battle directory. */
	public static URL getBattleURL() {
		return getSubURL(BATTLE_DIR);
	}
	
	/** Returns the URL of the data directory. NOTE: this is in local AppData */
	public static URL getDataURL() {
		return LAUNCHED_FROM_JAR ? getAppDataURL(DATA_DIR) : getSubURL("data");
	}

	/** Returns the URL of the resources directory */
	public static URL getResourcesURL() {
		return getSubURL(RESOURCE_DIR);
	}

	/** Returns the URL of the sprites directory */
	public static URL getSpritesURL() {
		return getSubURL(SPRITE_DIR);
	}

	/** Returns the URL of the tokens directory */
	public static URL getTokensURL() {
		return getSubURL(TOKEN_DIR);
	}

	/** Returns the URL of the hazards directory */
	public static URL getHazardsURL() {
		return getSubURL(HAZARD_DIR);
	}

	/** Returns the URL of the net directory */
	public static URL getNetURL() {
		return getSubURL(NET_DIR);
	}

	/** Returns the URL of the audiofiles directory */
	public static URL getAudioURL() {
		return getSubURL(AUDIO_DIR);
	}

	/** Returns the URL of the battle records directory */
	public static URL getBattleRecordsURL() {
		return LAUNCHED_FROM_JAR ? getAppDataURL(BATTLE_RECORDS_DIR) : getSubURL("data" + DIRSEP + BATTLE_RECORDS_DIR);
	}

	/** Takes a string and hides its extension */
	public static String hideExtension(String str) {
		String[] arr = str.split("\\.");
		
		if(arr.length <= 1) return str;	//there was no "." in str.
		
		StringBuilder sb = new StringBuilder("");
		for(int i = 0; i < arr.length - 1; ++i) {
			if(sb.length() > 0) sb.append(".");
			sb.append(arr[i]);
		}
		return sb.toString();
	}

	/** Given a simple class name, returns the package to which it belongs (stripped of
	 * the initial "pokepon.") or null if no file is found; NOTE: the only searched packages
	 * are pony, move, ability and item.
	 */
	public static String getPackage(String className) {
		if(findSubclassesNames(complete(PONY_DIR),Pony.class).contains(className)) return "pony";
		else if(findSubclassesNames(complete(MOVE_DIR),Move.class).contains(className)) return "move";
		else if(findSubclassesNames(complete(ABILITY_DIR),Ability.class).contains(className)) return "ability";
		else if(findSubclassesNames(complete(ITEM_DIR),Item.class).contains(className)) return "item";
		else return null;
	}
		
	/** Takes a path name and appends it to POKEPON_ROOTDIR to return a valid "relatively absolute" path
	 * (id est: absolute relatively to the java classpath directory)
	 */
	public static String complete(String path) {
		return POKEPON_ROOTDIR + DIRSEP + path;
	}

	/** This is used to cross-platform-ly locate resources in JAR file; to safely find a
	 * resource, do: Meta.complete2(Meta.SOME_DIR)+"/"+resourceName;
	 */
	public static String complete2(String path) {
		String cmp = DIRSEP + complete(path);
		//printDebug("cmp = "+cmp);	
		if(cmp.matches("^/[A-Z]:/.*")) return cmp.substring(1);
		else return cmp;
	}

	/** Convert all special tags in a string to local URL (e.g [sprite: NameOfPony] =&gt; 
	 * file://path/to/local/sprite.png) 
	 * @return The converted string
	 */
	public static String toLocalURL(String msg) {
		Pattern pattern = Pattern.compile("^[^\\[]*(?:\\[(?<sub>[^\\]]+): (?<sprite>[^\\]]+)\\])+?.*$");
		Matcher matcher = pattern.matcher(msg);
		String replaced = msg;
		if(Debug.on) printDebug("[Meta.toLocalURL] Text = "+msg);
		int cycles = 0;
		while(matcher.matches()) {
			if(cycles > 50) return replaced;
			if(Debug.on) printDebug("[Meta.toLocalURL] Matched: '"+matcher.group("sub")+"'.");
			if(matcher.group("sub").equals("sprite")) {
				try {
					Pony tmp = PonyCreator.create(matcher.group("sprite"));
					replaced = replaced.replaceFirst(
								"\\["+matcher.group("sub")+": [^\\]]+\\]",
								//Meta.class.getResource("/"+complete(SPRITE_DIR)+"/
								""+tmp.getFrontSprite()
								);

				} catch(ReflectiveOperationException e) {
					printDebug("[Meta.toLocalURL] Error creating "+matcher.group("sprite")+": "+e);
				}
			} else if(matcher.group("sub").equals("type")) {
				try {
					URL tk = pokepon.enums.Type.forName(matcher.group("sprite")).getToken();
					replaced = replaced.replaceFirst(
								"\\["+matcher.group("sub")+": [^\\]]+\\]",
								""+tk
								);
				} catch(NullPointerException e) {
					printDebug("[Meta.toLocalURL] Error creating type "+matcher.group("sprite"));
					e.printStackTrace();
				}
			} else if(matcher.group("sub").equals("movetype")) {
				try {
					URL tk = Move.MoveType.forName(matcher.group("sprite")).getToken();
					replaced = replaced.replaceFirst(
								"\\["+matcher.group("sub")+": [^\\]]+\\]",
								""+tk
								);
				} catch(NullPointerException e) {
					printDebug("[Meta.toLocalURL] Error creating movetype "+matcher.group("sprite"));
					e.printStackTrace();
				}
			}
			if(Debug.pedantic) printDebug("[Meta.toLocalURL] Replaced = "+replaced);
			matcher = pattern.matcher(replaced);
			++cycles;
		}
		if(Debug.on) printDebug("[Meta.toLocalURL] replaced:\n"+replaced);
		return replaced;
	}

	public static File ensureDirExists(String dirPath) {
		File dirpath = new File(dirPath); 
		if(!dirpath.isDirectory()) {
			if(!dirpath.exists()) {
				printDebug("[Meta] "+dirpath+" does not exist: creating it...");
				try {
					Files.createDirectories(Paths.get(dirpath.getPath()));
					return dirpath;
				} catch(IOException e) {
					printDebug("[Meta] Exception while creating directory:");
					e.printStackTrace();
					return null;
				}
			} else {
				printDebug("[Meta] Error: path `"+dirpath+"' is not a valid directory path, and could not create it.");
				return null;
			}
		} else return dirpath;
	}

	/** Path of the Pokepon root directory, relative to the java classpath */
	public final static String POKEPON_ROOTDIR = "pokepon";
	// These are relative to POKEPON_ROOTDIR
	public final static String PONY_DIR = "pony";
	public final static String MOVE_DIR = "move";
	public final static String HAZARD_DIR = "move"+DIRSEP+"hazard";
	public final static String BATTLE_DIR = "battle";
	public final static String MAIN_DIR = "main";
	public final static String ENUM_DIR = "enums";
	public final static String ITEM_DIR = "item";
	public final static String ABILITY_DIR = "ability";
	public final static String RESOURCE_DIR = "resources";
	public final static String SPRITE_DIR = "resources"+DIRSEP+"sprites";
	public final static String TOKEN_DIR = "resources"+DIRSEP+"tokens";
	public final static String AUDIO_DIR = "resources"+DIRSEP+"audio";
	public final static String NET_DIR = "net";
	public final static String ANIMATION_DIR = "gui"+DIRSEP+"animation";
	// These are in APPDATA when the game is launched from jar (i.e. in release version)
	public final static String DATA_DIR = "";
	public final static String SAVE_DIR = "teams";
	public final static String BATTLE_RECORDS_DIR = "battle_records";

	public static void main(String[] args) {
		consoleHeader("   META   ");
		printMsg("Rootdir:\t"+getRootURL());
		printMsg("PonyURL:\t"+getPonyURL());
		printMsg("MoveURL:\t"+getMoveURL());
		printMsg("HazardURL:\t"+getHazardsURL());
		printMsg("BattleURL:\t"+getBattleURL());
		printMsg("ItemURL:\t"+getItemURL());
		printMsg("AbilityURL:\t"+getAbilityURL());
		printMsg("ResourcesURL:\t"+getResourcesURL());
		printMsg("SpritesURL:\t"+getSpritesURL());
		printMsg("TokensURL:\t"+getTokensURL());
		printMsg("AudioURL:\t"+getAudioURL());
		printMsg("NetURL: \t"+getNetURL());
		printMsg("DataURL:\t"+getDataURL());
		printMsg("SaveURL:\t"+getSaveURL());
	}
}
