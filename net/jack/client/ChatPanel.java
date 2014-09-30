//: net/jack/client/ChatPanel.java

package pokepon.net.jack.client;

import pokepon.util.*;
import pokepon.gui.GUIGlobals;
import static pokepon.util.MessageManager.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import java.net.*;
import java.nio.*;
import java.nio.charset.*;
import java.security.*;
import java.security.spec.*;

/** A Chat with history and HTML capabilities.
 *
 * @author silverweed
 */
public class ChatPanel extends JPanel implements AutoCloseable {
	
	private static final int MAX_HIST_SIZE = 50;
	private JTextPane inA = new JTextPane();
	private HTMLDocument inD;
	private JTextField inputF = new JTextField(37);
	//TODO: make inputF a TextArea (fix issues with history and newlines)
	//private JTextArea inputF = new JTextArea(5,37);
	private JScrollPane inScroll;
	private BufferedReader in;
	private PrintWriter out;
	private String nick = "No nick";
	private Pattern chatPattern = Pattern.compile("^(?<nick>\\S+) said:.*$");
	private Pattern whisperPattern = Pattern.compile("^(?<nick>\\[\\S+ whispered\\]):.*$");
	private Pattern meWhisperPattern = Pattern.compile("^(?<nick>You whispered to [^ :]+):.*$");

	public ChatPanel() {
		super(true);	// enables double buffering
		setLayout(new BorderLayout());
		DefaultCaret caret = (DefaultCaret)inA.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		inA.setEditable(false);
		inA.setContentType("text/html");
		inA.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,GUIGlobals.FONT_SIZE));
		inA.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES,Boolean.TRUE);
		inD = (HTMLDocument)inA.getStyledDocument();
		inScroll = new JScrollPane(inA);
		add(inScroll);
		add(inputF,BorderLayout.SOUTH);
		addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				inputF.requestFocusInWindow();
			}
			public void focusLost(FocusEvent e) {}
		});
	}

	public void initialize(Socket s) {
		try {
			in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			out = new PrintWriter(s.getOutputStream(),true);
			inputF.addKeyListener(outKeyListener);
			inputF.requestFocusInWindow();
			if(Debug.on) printDebug("[ChatPanel] initialized.");
		} catch(IOException e) {
			printDebug("Exception while constructing ChatPanel: ");
			e.printStackTrace();
		}
	}

	/** This is to implement AutoCloseable */
	public void close() {
		dispose();
	}

	public void dispose() {
		try {
			in.close();
			out.close();
		} catch(IOException e) {}
	}

	//public JTextArea getInA() { return inA; }
	public void append(String str) {
		append(str, true);
	}
	public void append(String str, boolean escapeHTML) {
		if(Debug.pedantic) printDebug("[ChatPanel] called with str="+str+"; escapeHTML="+escapeHTML);
		String _str = escapeHTML ? MessageManager.sanitize(str) : str;
		// highlight nicknames
		Matcher matcher = chatPattern.matcher(_str);
		if(matcher.matches() && matcher.group("nick") != null) {
			if(matcher.group("nick").equals(nick))
				_str = _str.replace(matcher.group("nick"),"<b><font color='blue'>"+matcher.group("nick")+"</font></b>");
			else
				_str = _str.replace(matcher.group("nick"),"<b><font color='green'>"+matcher.group("nick")+"</font></b>");
		} else if((matcher = whisperPattern.matcher(_str)).matches() && matcher.group("nick") != null) {
			_str = _str.replace(matcher.group("nick"),"<b><em><font color='#00B800'>"+matcher.group("nick")+"</font></em></b>");
		} else if((matcher = meWhisperPattern.matcher(_str)).matches() && matcher.group("nick") != null) {
			_str = _str.replace(matcher.group("nick"),"<b><em><font color='#00AAFF'>"+matcher.group("nick")+"</font></em></b>");
		}
		try {
			inD.insertBeforeEnd(inD.getParagraphElement(inD.getLength()),_str+"<br>");	
			if(isViewAtBottom())
				scrollToBottom();
		} catch(BadLocationException|IOException e) {
			printDebug("[ChatPanel.append] exception: "+e);
		}
	}
	public void setNick(String nick) { this.nick = nick; }
	public String getNick() { return nick; }
	public JTextField getInputField() { return inputF; }
	private boolean isViewAtBottom() {
		JScrollBar sb = inScroll.getVerticalScrollBar();
		int min = sb.getValue() + sb.getVisibleAmount();
		int max = sb.getMaximum();
		return min == max;
	}
	private void scrollToBottom() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				inScroll.getVerticalScrollBar().setValue(inScroll.getVerticalScrollBar().getMaximum());
		    }
		});
	}

	public void setKeyListener(KeyListener keyListener) {
		inputF.removeKeyListener(outKeyListener);
		outKeyListener = keyListener;
		inputF.addKeyListener(outKeyListener);
	}

	protected KeyListener outKeyListener = new KeyListener() {
		private int index;
		private LinkedList<String> history = new LinkedList<String>();

		public void keyPressed(KeyEvent e) {
			//sane history index
			if(index <= -1) index = 0;
			else if(index >= history.size()) index = history.size();

			switch(e.getKeyCode()) {
				case KeyEvent.VK_ENTER:
					if(inputF.getText() != null && inputF.getText().length() > 0) {
						// if not a command, echo it on screen before sending it
						if(inputF.getText().charAt(0) != CMD_PREFIX) 
							append(nick+" said: "+inputF.getText());
						else {
							String[] tkn = inputF.getText().split("\\s+");
							// these commands require a parameter to be hashed before sending it
							if(	(tkn[0].equals(CMD_PREFIX+"nick") || 
								tkn[0].equals(CMD_PREFIX+"register")) &&
								tkn.length > 2
							) {
								// hash the password before sending it
								if(Debug.on) printDebug("Hashing token[2]...");
								try {
									MessageDigest mDigest = MessageDigest.getInstance("SHA-1");
									mDigest.update(Charset.forName("UTF-8")
											.encode(CharBuffer.wrap(tkn[2].toCharArray())).array());
									tkn[2] = new String(mDigest.digest());
									synchronized(out) {
										out.println(ConcatenateArrays.merge(tkn)+"\n");
									}
									mDigest.reset();
								} catch(NoSuchAlgorithmException ee) {
									append("[error] no such algorithm: "+ee);
									append("Please, try not sending the password on the command line");
								} finally {
									tkn[2] = null;
									history.add(inputF.getText());
									if(history.size() > MAX_HIST_SIZE) history.removeFirst();
									index = history.size()-1;
									inputF.setText("");
								}
								break;
							}
						}
						synchronized(out) {
							out.println(inputF.getText());
						}
						history.add(inputF.getText());
						if(history.size() > MAX_HIST_SIZE) history.removeFirst();
						index = history.size()-1;
						inputF.setText("");
					}
					break;
				case KeyEvent.VK_UP:
					if(history.size() > 0 && index > -1) {
						inputF.setText(history.get(index--));
					}
					break;
				case KeyEvent.VK_DOWN:
					if(history.size() > 0 && index < history.size()-1) {
						inputF.setText(history.get(++index));
					} else {
						if(	inputF.getText() != null && inputF.getText().length() > 0 && 
							!history.get(history.size()-1).equals(inputF.getText())
						) {
							history.add(inputF.getText());
							if(history.size() > MAX_HIST_SIZE) history.removeFirst();
							index = history.size()-1;
						}
						inputF.setText("");
					}
					break;
			}
		}
		public void keyReleased(KeyEvent e) {}
		public void keyTyped(KeyEvent e) {}
	};
}
