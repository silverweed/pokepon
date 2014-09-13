//: sound/Looper.java
package pokepon.sound;

import pokepon.util.*;
import static pokepon.util.MessageManager.*;
import javax.sound.sampled.*;
import java.io.*;
import java.net.*;

/** This class plays an audio file, providing ways to loop from point to point
 * and stop the playback; should be invoked from a separate Thread.
 * Due to bugs in OpenJDK, this class probably won't work on some Linux distros,
 * e.g. Debian Wheezy.
 *
 * @author silverweed
 */
public class Looper implements Runnable {
	
	private AudioInputStream sound;
	private volatile boolean shouldStop;
	private Clip clip;
	private int start, end;

	/** Construct a Looper which reproduces an audio file */
	public Looper(final String file) {
		try {
			File soundFile = new File(file);
			sound = AudioSystem.getAudioInputStream(soundFile);
		} catch(FileNotFoundException e) {
			printDebug("File not found: "+file);
		} catch(UnsupportedAudioFileException e) {
			printDebug("Unsupported audio file:");
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public Looper(final URL url) {
		try {
			sound = AudioSystem.getAudioInputStream(url);
		} catch(UnsupportedAudioFileException e) {
			printDebug("Unsupported audio file:");
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	/** Start playing the audio file; if setLoopPoints has already been called, loop it
	 * continuously from point secStart to secEnd.
	 */
	public void run() {
		try {
			// load sound into memory (as a Clip)
			DataLine.Info info = new DataLine.Info(Clip.class, sound.getFormat());
			clip = (Clip) AudioSystem.getLine(info);
			clip.open(sound);
			if(end != 0)
				clip.setLoopPoints(start, end);
			clip.loop(Clip.LOOP_CONTINUOUSLY);
			while(!shouldStop) {
				synchronized(clip) {
					clip.wait();
				}
			}
			clip.stop();
			if(Debug.on) printDebug("[Looper] Stopped.");
		} catch(LineUnavailableException e) {
			printDebug("[ Looper ] WARNING! Cannot play sound: disabling BGM.");
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		if(Debug.on) printDebug("run(): terminated.");
	}

	/** @param secStart The second whence to start the loop
	 * @param secEnd The ending second of the loop
	 */
	public void setLoopPoints(final double secStart, final double secEnd) {
		start = timeToSamples(secStart, sound.getFormat());
		end = timeToSamples(secEnd, sound.getFormat());
	}

	/** Stop the looper and terminate the run() method */
	public void stop() {
		if(Debug.on) printDebug("[Looper] Stopping...");
		shouldStop = true;
		synchronized(clip) {
			clip.notifyAll();
		}
	}

	/** Convert a time expressed in seconds to number of samples */
	private static int timeToSamples(final double time, final AudioFormat format) {
		return (int)(time * format.getSampleRate());
	}

	public static void main(String[] args) throws Exception {
		Looper looper = new Looper(Meta.getAudioURL().getPath()+Meta.DIRSEP+"xy-rival.wav");
		looper.setLoopPoints(7.802, 58.634);
		new Thread(looper).start();
		int min = 0, sec = 0;
		while(true) {
			try {
				System.out.format("%d : %d \r", min, sec++);
				if(sec == 105) {
					looper.stop();
					return;
				}
				if(sec == 60) {
					sec = 0;
					++min;
				}
				Thread.sleep(1000);
			} catch(InterruptedException ignore) {}
		}
	}
}
