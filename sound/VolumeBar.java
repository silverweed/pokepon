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
 * @author Giacomo Parolini
 */
public class VolumeBar extends JPanel {

	private JSlider slider;
	private JCheckBox muteBtn = new JCheckBox("Mute");
	private Looper looper;

	public VolumeBar(final Looper looper) {
		this.looper = looper;
		if(looper.canChangeVolume()) {
			slider = new JSlider(JSlider.VERTICAL, 
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
			slider = new JSlider(JSlider.VERTICAL, 0, 0);
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

		setLayout(new BorderLayout());
		add(slider);
		add(BorderLayout.SOUTH, muteBtn);
		if(Debug.on) printDebug("[VolumeBar] Constructed with volume: "+looper.getMinVolume()+" < "+
				looper.getVolume() + " < "+looper.getMaxVolume());
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		Looper looper = PresetBGM.getLooper("xy-rival.wav");
		VolumeBar vb = new VolumeBar(looper);
		frame.add(vb);
		new Thread(looper).start();
		SwingConsole.run(frame);
	}
}	
