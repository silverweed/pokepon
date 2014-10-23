//: net/jack/client/PokeponChat.java

package pokepon.net.jack.client;

import pokepon.util.*;
import static pokepon.util.MessageManager.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.util.regex.*;

/** The Chat embedded in the PokeponClient; provides methods to manage users
 * and allows decoupling the PokeponClient from the chat functionality
 *
 * @author silverweed
 */
public class PokeponChat extends JPanel implements AutoCloseable {

	protected ChatPanel chatP = new ChatPanel();
	protected DefaultListModel<String> users = new DefaultListModel<>();
	protected JList<String> usersL = new JList<>(users);
	protected Socket socket;

	public PokeponChat() {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(1,1,1,1);
		// Central Chat Panel
		c.gridx = 0;
		c.gridheight = 3;
		c.weightx = 0.8;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		add(chatP,c);
		// Right Users Panel
		c.insets = new Insets(5,1,5,1);
		c.gridx = 3;
		c.gridheight = 2;
		c.weightx = 0.4;
		add(new JScrollPane(usersL),c);
		usersL.addMouseListener(usersML);
	}

	public void initialize(Socket s) {
		socket = s;
		chatP.initialize(s);
	}

	public String getNick() { return chatP.getNick(); }
	public void setNick(String n) { chatP.setNick(n); }
	public ChatPanel getChatPanel() { return chatP; }
	public String[] getUsers() { return (String[])users.toArray(); }
	//public JList<String> getUsersL() { return usersL; }
	//public DefaultListModel<String> getUsers() { return users; }

	public void userAdd(String name) {
		if(Debug.pedantic) printDebug("Called userAdd("+name+")");
		users.addElement(name);
	}
	public void userAdd(String name,String color) {
		if(Debug.pedantic) printDebug("Called userAdd("+name+","+color+")");
		users.addElement("<html><font color="+color+">"+name+"</font></html>");
	}

	/** Renames an user; it can match users whose name is contained within html tags, and the renamed user
	 * will have the same tags.
	 */
	public void userRename(final String old,final String newN) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if(Debug.pedantic) printDebug("Called userRename("+old+","+newN+")");
				// ugh, Enumerations...just because the API compels us <_<
				Enumeration<String> us = users.elements();
				Pattern pattern = Pattern.compile("^(?<starttags><.*>)?(?<main>"+old+")(?<endtags><.*>)?$");
				if(Debug.pedantic) printDebug("userRename: pattern = "+pattern);
				while(us.hasMoreElements()) {
					String nick = us.nextElement();
					Matcher matcher = pattern.matcher(nick);
					if(Debug.pedantic) printDebug("userRename: nick = "+nick);
					if(matcher.matches()) {
						if(Debug.pedantic) {
							printDebug("userRename: matched. Groups:"); 
							printDebug("<starttags> = "+matcher.group("starttags"));
							printDebug("<main> = "+matcher.group("main"));
							printDebug("<endtags> = "+matcher.group("endtags"));
						}

						if(users.removeElement(nick)) {
							if(matcher.group("starttags") == null || matcher.group("endtags") == null) {
								if(Debug.pedantic)
									printDebug("a group is null. New nick is "+newN);
								users.addElement(newN);
							} else {
								if(Debug.pedantic)
									printDebug("groups are non-null. New nick is "+
										matcher.group("starttags")+newN+matcher.group("endtags"));
								users.addElement(matcher.group("starttags")+newN+matcher.group("endtags"));
							}
						}
						return;
					}
				}
			}
		});
	}

	/** Removes an user from the list; it can match the user even if its name is within html tags. */
	public void userRemove(final String name) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if(Debug.pedantic) printDebug("Called userRemove("+name+")");
				Enumeration<String> us = users.elements();
				while(us.hasMoreElements()) {
					String nick = us.nextElement();
					if(nick.matches("^(<.*>)?"+name+"(<.*>)?$")) {
						users.removeElement(nick);
						return;
					}
				}
			}
		});
	}

	/** This MouseListener determines what action is performed when a username on the
	 * side list is clicked.
	 */
	protected MouseListener usersML = new MouseAdapter() {
		private JComboBox<String> c;
		private boolean clicked;
		public void mouseClicked(MouseEvent e) {
			if(usersL.getSelectedValue() == null) return;
			String[] opts;
			boolean myself = usersL.getSelectedValue().matches("^(<.*>)?"+chatP.getNick()+"(<.*>)?$");
			if(myself) {
				opts = new String[] { "exit","whoami","change nick","register" };
			} else {
				opts = new String[] { "exit","whois","challenge","whisper" };
			}
			int sel = JOptionPane.showOptionDialog(PokeponChat.this,usersL.getSelectedValue(),null,
					JOptionPane.DEFAULT_OPTION,JOptionPane.INFORMATION_MESSAGE,null,opts,opts[0]);
					
			try {
				PrintWriter pw = new PrintWriter(socket.getOutputStream(),true);
				switch(sel) {
					case 0:
						return;
					case 1:
						pw.println(CMD_PREFIX+(myself ? "whoami" : "whois "+usersL.getSelectedValue()));
						break;
					case 2:
						if(myself) {
							String newnick = JOptionPane.showInputDialog("Choose new nick");
							pw.println(CMD_PREFIX+"nick "+newnick);
						} else {
							pw.println(CMD_PREFIX+"battle "+usersL.getSelectedValue());
						}
						break;
					case 3:
						if(!myself) {
							String mesg = JOptionPane.showInputDialog("Whisper to "+usersL.getSelectedValue()+":");
							if(mesg != null) {
								pw.println(CMD_PREFIX+"pm "+usersL.getSelectedValue()+" "+mesg);
							}
						} else {
							String newnick = JOptionPane.showInputDialog("Choose nick to register",chatP.getNick());
							if(newnick == null) return;
							pw.println(CMD_PREFIX+"register "+newnick);
						}
						break;
				}

			} catch(IOException ee) {
				printDebug("Caught IOException in MouseListener.mouseClicked: "+ee);
			}
		}
	};

	// implement AutoCloseable
	public void close() {
		chatP.close();
	}
	public void dispose() {
		chatP.close();
	}
}
