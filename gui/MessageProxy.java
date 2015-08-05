//: gui/MessageProxy.java

package pokepon.gui;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import pokepon.util.*;
import static pokepon.util.MessageManager.*;

/** Provides two StrAppendables to use as altOut and altErr for MessageManager,
 * allowing to redirect stdout and stderr to a JTextPane rather than the console.
 *
 * @author silverweed
 */
public class MessageProxy {

	private final JTextPane pane = new JTextPane();
	private final HTMLDocument doc;
	private StrAppendable out, err;
	private JScrollPane scrollbar;
	private int _defaultCloseOperation;

	public MessageProxy() {
		this(JFrame.EXIT_ON_CLOSE);
	}
	
	public MessageProxy(final int defaultCloseOperation) {
		_defaultCloseOperation = defaultCloseOperation;
		pane.setEditable(false);
		pane.setContentType("text/html");
		pane.setFont(new Font(Font.MONOSPACED,Font.PLAIN,GUIGlobals.FONT_SIZE));
		pane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES,Boolean.TRUE);
		doc = (HTMLDocument)pane.getStyledDocument();
		scrollbar = new JScrollPane(pane);
		out = new StrAppendable() {
			public void append(String str) {
				try {
					doc.insertBeforeEnd(doc.getParagraphElement(doc.getLength()), 
						MessageManager.sanitize(str).replaceAll("\n", "<br>"));	
					if(isViewAtBottom())
						scrollToBottom();
				} catch(BadLocationException|IOException e) {
					System.err.println("[MessageProxy] exception: "+e);
				}
			}
		};
		err = new StrAppendable() {
			public void append(String str) {
				try {
					str = "<font color='red'>" + MessageManager.sanitize(str) + "</font>";
					doc.insertBeforeEnd(doc.getParagraphElement(doc.getLength()), str.replaceAll("\n", "<br>"));	
					if(isViewAtBottom())
						scrollToBottom();
				} catch(BadLocationException|IOException e) {
					System.err.println("[MessageProxy] exception: "+e);
				}
			}
		};
	}

	public StrAppendable getAltOut() {
		return out;
	}
	
	public StrAppendable getAltErr() {
		return err;
	}
	
	public void startGUI(final String title) {
		final JFrame f = new JFrame();
		f.add(scrollbar);
		pane.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int sel = JOptionPane.showConfirmDialog(
						f,
						"Save debug log to file?",
						"Save to file",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);

				switch(sel) {
					case JOptionPane.YES_OPTION:
						JFileChooser fileChooser = new JFileChooser() {
							@Override
							public void approveSelection() {
								File f = getSelectedFile();
								if(f.exists() && getDialogType() == SAVE_DIALOG) {
									int result = JOptionPane.showConfirmDialog(
											this,
											"The file exists, overwrite?",
											"Existing file",
											JOptionPane.YES_NO_CANCEL_OPTION);
									switch(result) {
										case JOptionPane.YES_OPTION:
											super.approveSelection();
											return;
										case JOptionPane.NO_OPTION:
											return;
										case JOptionPane.CLOSED_OPTION:
											return;
										case JOptionPane.CANCEL_OPTION:
											cancelSelection();
											return;
									}
								}
								super.approveSelection();
							}
						};
						fileChooser.setSelectedFile(new File(System.getProperty("user.home")
									+ Meta.DIRSEP + "pkpdebug.log"));
						switch(fileChooser.showSaveDialog(f)) {
							case JFileChooser.CANCEL_OPTION:
								return;
							case JFileChooser.ERROR_OPTION:
								printDebug("An error occurred while saving team.");
								return;
							case JFileChooser.APPROVE_OPTION:
								printDebug("Selected file: "+fileChooser.getSelectedFile().getName());
								break;
						}
						try (BufferedWriter fileOut = new BufferedWriter(new FileWriter(fileChooser.getSelectedFile()))) {
							    pane.write(fileOut);
						} catch(IOException ex) {
							printDebug("Exception while writing to file: " + ex);
						}
						break;
					default:
						break;
				}
			}
		});
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				f.setDefaultCloseOperation(_defaultCloseOperation);
				f.setTitle(title);
				f.setSize(800,600);
				f.setVisible(true);
			}
		});
	}
	private boolean isViewAtBottom() {
		JScrollBar sb = scrollbar.getVerticalScrollBar();
		int min = sb.getValue() + sb.getVisibleAmount();
		int max = sb.getMaximum();
		return min >= max / 2;
	}
	private void scrollToBottom() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				scrollbar.getVerticalScrollBar().setValue(scrollbar.getVerticalScrollBar().getMaximum());
		    }
		});
	}
}
