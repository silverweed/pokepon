package pokepon.sound;
import javax.sound.sampled.*;
import java.io.File;

/** Testing class */
public class ClipTest implements Runnable {
	
	private AudioInputStream sound;
	private volatile boolean shouldStop;
	private Clip clip;

	public ClipTest() throws Exception {
		File soundFile = new File("../resources/audio/bossbattle.wav");
		sound = AudioSystem.getAudioInputStream(soundFile);
	}

	public void run() {
		try {
			// load sound into memory (as a Clip)
			DataLine.Info info = new DataLine.Info(Clip.class, sound.getFormat());
			clip = (Clip) AudioSystem.getLine(info);
			clip.open(sound);
			int start = timeToSamples(7, sound.getFormat());
			int end = timeToSamples(77, sound.getFormat());
			clip.setLoopPoints(start, end);
			clip.loop(Clip.LOOP_CONTINUOUSLY);
			while(!shouldStop) {
				synchronized(clip) {
					clip.wait();
				}
			}
			clip.stop();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		System.err.println("run(): terminated.");
	}

	public void stop() {
		shouldStop = true;
		synchronized(clip) {
			clip.notifyAll();
		}
	}

	public static void main(String[] args) throws Exception {
		ClipTest clip = new ClipTest();
		new Thread(clip).start();
		int min = 0, sec = 0;
		while(true) {
			try {
				System.out.format("%d : %d \r", min, sec++);
				if(sec == 15) {
					clip.stop();
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

	private static int timeToSamples(double time, AudioFormat format) {
		return (int)(time * format.getSampleRate());
	}
}
