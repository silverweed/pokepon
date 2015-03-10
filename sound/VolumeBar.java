//: sound/VolumeBar.java
package pokepon.sound;

import pokepon.gui.SwingConsole;
import pokepon.util.Debug;
import static pokepon.util.MessageManager.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/** GUI element that allows controlling the volume of a Looper; note that
 * the Controls available differ among Java implementations; for example,
 * the Mute Control is unavailable on Ubuntu.
 *
 * @author silverweed
 */
public class VolumeBar extends JPanel {

	private JSlider slider;
	private JToggleButton muteBtn = new JToggleButton("M");
	private Looper looper;

	public VolumeBar(final Looper looper, boolean vertical) {
		this.looper = looper;
		if(looper.canChangeVolume()) {
			slider = new JSlider(vertical ? JSlider.VERTICAL : JSlider.HORIZONTAL, 
					(int)looper.getMinVolume(),
					(int)(Math.pow(looper.getMaxVolume(),2)/40000),
					(int)(looper.getMaxVolume() - looper.getMinVolume()) / 2
			);
			slider.setValue((int)(Math.pow(looper.getVolume(),2)/40000));
			slider.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					float vol = (float)Math.sqrt(((JSlider)e.getSource()).getValue())*200;
					if(Debug.pedantic) printDebug("[VolumeBar] Volume: "+vol); 
					looper.setVolume(vol);
				}
			});
		} else {
			slider = new JSlider(vertical ? JSlider.VERTICAL : JSlider.HORIZONTAL, 0, 1, 0);
			slider.setEnabled(false);
		}
		if(looper.canMute()) {
			if(looper.isMute())
				muteBtn.setSelected(true);
			muteBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					looper.setMute(muteBtn.isSelected());
				}
			});
		} else {
			muteBtn.setEnabled(false);
		}

		setLayout(new BorderLayout(3, 0));
		add(slider);
		add(muteBtn, vertical ? BorderLayout.SOUTH : BorderLayout.WEST);
		muteBtn.setHorizontalAlignment(SwingConstants.CENTER);
		muteBtn.setPreferredSize(new Dimension(20, 20));
		muteBtn.setMargin(new Insets(0,0,0,0));
		if(Debug.on) printDebug("[VolumeBar] Constructed with volume: "+looper.getMinVolume()+" < "+
				looper.getVolume() + " < "+looper.getMaxVolume());
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		Looper looper = PresetBGM.getLooper("xy-rival.wav");
		VolumeBar vb = new VolumeBar(looper, true);
		frame.add(vb);
		new Thread(looper).start();
		SwingConsole.run(frame);
	}
}	
