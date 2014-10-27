//: net/jack/client/PokeponChat.java

package pokepon.net.jack.client;

import pokepon.util.*;
import pokepon.net.jack.chat.*;
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
public class PokeponChat extends JPanel implements AutoCloseable, Chat {

	protected ChatPanel chatP = new ChatPanel();
	protected DefaultListModel<ChatUser> users = new DefaultListModel<>();
	protected JList<ChatUser> usersL = new JList<>(users);
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
		usersL.setCellRenderer(new UsersListRenderer());
	}

	public void initialize(Socket s) {
		socket = s;
		chatP.initialize(s);
	}

	public String getNick() { return chatP.getNick(); }
	public void setNick(String n) { chatP.setNick(n); }
	public ChatPanel getChatPanel() { return chatP; }
	public String[] getUsers() { return (String[])users.toArray(); }

	public void userAdd(String name) {
		userAdd(name, ChatUser.Role.USER);
	}

	public void userAdd(final String name, ChatUser.Role role) {
		if(role == null) role = ChatUser.Role.USER;
		if(Debug.on) printDebug("Called userAdd("+name+","+role+")");
		users.addElement(new ChatUser(name, role));
	}

	public void userRename(final String old, final String newN) {
		userRename(old, newN, ChatUser.Role.USER);
	}

	/** Renames an user; it can match users whose name is contained within html tags, and the renamed user
	 * will have the same tags.
	 */
	public void userRename(final String old,final String newN,ChatUser.Role _role) {
		final ChatUser.Role role = _role == null ? ChatUser.Role.USER : _role;
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if(Debug.on) printDebug("Called userRename("+old+","+newN+")");
				// ugh, Enumerations...just because the API compels us <_<
				Enumeration<ChatUser> us = users.elements();
				while(us.hasMoreElements()) {
					ChatUser u = us.nextElement();
					if(u.getName().equals(old)) {
						u.setName(newN);
						u.setRole(role);
						usersL.repaint();
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
				Enumeration<ChatUser> us = users.elements();
				while(us.hasMoreElements()) {
					ChatUser u = us.nextElement();
					if(u.getName().equals(name)) {
						users.removeElement(u);
						usersL.repaint(); // just for insurance
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
			boolean myself = usersL.getSelectedValue().getName().equals(chatP.getNick()); 
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
						pw.println(CMD_PREFIX+(myself ? "whoami" : "whois "+usersL.getSelectedValue().getName()));
						break;
					case 2:
						if(myself) {
							String newnick = JOptionPane.showInputDialog("Choose new nick");
							pw.println(CMD_PREFIX+"nick "+newnick);
						} else {
							pw.println(CMD_PREFIX+"battle "+usersL.getSelectedValue().getName());
						}
						break;
					case 3:
						if(!myself) {
							String mesg = JOptionPane.showInputDialog("Whisper to "+usersL.getSelectedValue()+":");
							if(mesg != null) {
								pw.println(CMD_PREFIX+"pm "+usersL.getSelectedValue().getName()+" "+mesg);
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

	/** Renders the users list elements */
	class UsersListRenderer extends JLabel implements ListCellRenderer<ChatUser> {

		public UsersListRenderer() {}

		@Override
		public Component getListCellRendererComponent(	JList<? extends ChatUser> list,
								ChatUser value,
								int index,
								boolean isSelected,
								boolean cellHasFocus) {
			setText(value.toString());
			if(value.getName().equals(getNick()))
				setForeground(Color.BLUE);
			else
				switch(value.getRole()) {
					case ADMIN:
						setForeground(Color.RED);
						break;
					case MODERATOR:
						setForeground(Color.GREEN.darker());
						break;
					default:
						setForeground(Color.BLACK);
				}
			

			if(cellHasFocus)
				setBackground(list.getSelectionBackground());
			else
				setBackground(Color.WHITE);

			return this;
		}
	}
}
