//: util/ClassFinder.java

package pokepon.util;

import static pokepon.util.MessageManager.*;
import java.util.*;
import java.io.*;
import java.net.URL;
import java.security.*;
import java.util.zip.*;

/** Utility class (adapted from one found on the internet) used to scan all classes
 * within a package; 
 *
 * @author silverweed
 */
public class ClassFinder {

	private final static String CLASS_SUFFIX = ".class";
	private final static Map<String, List<String>> filesCache = Collections.synchronizedMap(new HashMap<String, List<String>>());

	/** Given a path, returns a List of all subclasses of 'baseClass'. */
	public static List<Class<?>> findSubclasses(String path, Class<?> baseClass) {
		List<Class<?>> classes = new ArrayList<>();
		for(String str : allFilesIn(path)) {
			if(!str.endsWith(".class")) continue;
			try {
				Class<?> cls = Class.forName(path.replaceAll(""+Meta.DIRSEP,".")+"."+str.substring(0,str.length() - 6));
				if(baseClass.isAssignableFrom(cls) && cls != baseClass) 
					classes.add(cls);
			} catch(ClassNotFoundException e) {
				printDebug("[ClassFinder.findSubclasses("+path+","+baseClass+")] class not found: "+
					path.replaceAll(""+Meta.DIRSEP,".")+"."+str.substring(0,str.length()-6));
			}
		}
		return classes;
	}

	/** This function is analogous to findSubClasses, but returns a List of Strings with classes simple names */
	public static List<String> findSubclassesNames(String path,Class<?> baseClass) {
		List<String> list = new ArrayList<String>();
		for(Class<?> c : findSubclasses(path,baseClass)) {
			list.add(c.getSimpleName());
		}
		return list;
	}

	/** Given a path, returns a List of basenames of all files in it; for efficiency, file lists are
	 * cached the first time a new relpath is read and then read from the cache on next requests.
	 */
	public static List<String> allFilesIn(final String relpath) {
		synchronized(filesCache) {
			if(filesCache.get(relpath) != null) {
				if(Debug.on) printDebug("[allFilesIn] reading from cache: "+relpath);
				return filesCache.get(relpath);
			}
		}

		List<String> filenames = new ArrayList<>();
		if(Meta.LAUNCHED_FROM_JAR) {	
			try {
				CodeSource src = ClassFinder.class.getProtectionDomain().getCodeSource();
				if(src != null) {
					URL jar = src.getLocation();
					if(Debug.on) printDebug("jar location = "+jar+"; relpath: "+relpath);
					ZipInputStream zip = new ZipInputStream(jar.openStream());
					// XXX: this cycle is very time-expensive, so we try to limit its use
					// as much as possible, both with the cache and by calling this method
					// as few times as possible.
					while(true) {
						ZipEntry e = zip.getNextEntry();
						if(e == null) break;
						String[] fullname = e.getName().split(""+Meta.DIRSEP);
						String name = fullname[fullname.length-1];
						// don't include files in subdirectories
						if(e.getName().startsWith(relpath) && fullname.length == relpath.split(""+Meta.DIRSEP).length + 1) 
							filenames.add(name);
					}
				} else {
					printDebug("[ClassFinder.allFilesIn("+relpath+")] Can't find code source!");
				}
			} catch(IOException e) {
				printDebug("[ClassFinder.allFilesIn("+relpath+")]: IOException");
				e.printStackTrace();
				return null;
			}
		} else {
			if(Debug.on) printDebug("[ClassFinder] path = "+Meta.getCwd().getPath()+Meta.DIRSEP+relpath);
			File[] files = new File(Meta.getCwd().getPath()+Meta.DIRSEP+relpath).listFiles();
			if(files == null) return filenames;
			for(File f : files)
				filenames.add(f.getName());
		}
		if(Debug.pedantic) printDebug("filenames = "+filenames);
		// add entry to the cache
		synchronized(filesCache) {
			filesCache.put(relpath, filenames);
		}
		if(Debug.on) printDebug("[allFilesIn] added entry to cache: "+relpath);
		return filenames;
	}
}
