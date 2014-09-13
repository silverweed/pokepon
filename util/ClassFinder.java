//: util/ClassFinder.java

// FIXME: path resolution won't work on Windows.

package pokepon.util;

import static pokepon.util.MessageManager.*;
import java.util.*;
import java.io.*;
import java.net.URL;
import java.security.*;
import java.util.zip.*;

/** Utility class (found on the internet) used to scan all classes
 * within a package; very messy: should rewrite it - or not use it at all.
 *
 * @author sp00m (stackoverflow.com), Giacomo Parolini
 */
public final class ClassFinder {

	/*private final static char DOT = '.';
	private final static char SLASH = '/';
	private final static char BACKSLASH = '\\';
	private final static char SEP;
	static {
		//if(System.getProperty("os.name").startsWith("Windows")) SEP = BACKSLASH;
		//else SEP = SLASH;
	}*/
	private final static String CLASS_SUFFIX = ".class";
	private final static String BAD_PACKAGE_ERROR = "Unable to get resources from path '%s'. Are you sure the given '%s' package exists?";

	/** This function returns a List of all found classes that extend baseClass found in directory path.
	 * @param path The path where to search (relative to the java classpath)
	 *@author Giacomo Parolini
	 */
	/*public static List<Class<?>> findSubclasses(String path,Class<?> baseClass) {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		try {
			if(Debug.pedantic) printDebug("findSubClasses - path="+path);
			File[] files = new File(Meta.getCwd().getPath()+Meta.DIRSEP+path).listFiles();

			if(files == null || files.length == 0) {
				if(Debug.on) printDebug("[ClassFinder] Warning: found no files in "+Meta.getCwd().getPath()+Meta.DIRSEP+path);
				return classes;
			}
				
			if(Debug.pedantic) printDebug("[ClassFinder] files found = "+Arrays.asList(files));

			Arrays.sort(files);
			for(File f : files) {
				if(Debug.pedantic) printDebug("file: "+f.getName());
				if(!f.toString().endsWith(".class") || f.toString().matches(".*\\$.+\\.class$")) continue;
				try {
					// FIXME ?
					String classname = f.toString().split("[^a-zA-Z0-9\\.]")[f.toString().split("[^a-zA-Z0-9\\.]").length-1].split("\\.")[0];
					if(Debug.pedantic) printDebug("findSubClasses: classname="+classname);

					// Get only subclasses of baseClass (exclude baseClass itself) 
					if(baseClass.isAssignableFrom(Class.forName(path.replace(SEP,'.')+"."+classname)) && ! Class.forName(path.replace(SEP,'.')+"."+classname).equals(baseClass)) {
						if(Debug.pedantic) printDebug("classname ok: "+f.toString());
						try { 
							classes.add(Class.forName(path.replace(SEP,'.')+"."+classname));
						} catch(Exception e) {
							printDebug("Caught exception: "+e);
						}
					}

				} catch(ArrayIndexOutOfBoundsException|ClassNotFoundException ee) {
					printDebug("Caught exception in ClassFinder::findSubClasses("+path+","+baseClass+"): "+ ee);
					ee.printStackTrace();
				}
			}
			
		} catch(Exception e){
			printDebug("Caught an exception: "+e);
			e.printStackTrace();
		}

		return classes;
	}*/

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

	public static List<String> allFilesIn(final String relpath) {
		List<String> filenames = new ArrayList<>();
		if(Meta.LAUNCHED_FROM_JAR) {	
			try {
				CodeSource src = ClassFinder.class.getProtectionDomain().getCodeSource();
				if(src != null) {
					URL jar = src.getLocation();
					if(Debug.on) printDebug("jar location = "+jar+"; relpath: "+relpath);
					ZipInputStream zip = new ZipInputStream(jar.openStream());
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
		return filenames;
	}

	/** This function returns a List of all classes within a package.
	 * @author sp00m 
	 */
	public static List<Class<?>> find(final String scannedPackage) {
		final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		final String scannedPath = scannedPackage.replace(".", ""+Meta.DIRSEP);
		final Enumeration<URL> resources;
		try {
			resources = classLoader.getResources(scannedPath);
		} catch (IOException e) {
			throw new IllegalArgumentException(String.format(BAD_PACKAGE_ERROR, scannedPath, scannedPackage), e);
		}
		final List<Class<?>> classes = new LinkedList<Class<?>>();
		while (resources.hasMoreElements()) {
			final File file = new File(resources.nextElement().getFile());
			classes.addAll(find(file, new String()));
		}
		return classes;
	}

	/** @author sp00m */
	public static List<Class<?>> find(final File file, final String scannedPackage) {
		final List<Class<?>> classes = new LinkedList<Class<?>>();
		final String resource = scannedPackage + "." + file.getName();
		if (file.isDirectory()) {
			for (File nestedFile : file.listFiles()) {
				classes.addAll(find(nestedFile, resource));
			}
		} else if (resource.endsWith(CLASS_SUFFIX)) {
			final int beginIndex = 1;
			final int endIndex = resource.length() - CLASS_SUFFIX.length();
			final String className = resource.substring(beginIndex, endIndex);
			try {
				classes.add(Class.forName(className));
			} catch (ClassNotFoundException ignore) {}
		}
		return classes;
	}
}
