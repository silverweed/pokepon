//: pokepon.net.jack/client/ClientCommunicationsExecutor.java

package pokepon.net.jack.client;

import pokepon.net.jack.*;
import pokepon.util.*;
import static pokepon.util.MessageManager.*;
import java.security.*;
import java.security.spec.*;
import java.awt.*;
import javax.swing.*;
import java.nio.*;
import java.nio.charset.*;
import java.util.*;

/** Executor which handles incoming messages;
 * supported messages are:
 * setnick 'nick' - changes communication name to 'nick'
 * givepasswd - prompts user (graphically) for password
 * youros - makes the client send its os information
 * disconnect - interrupts client's communication.
 *
 * @author silverweed
 */
class ClientCommunicationsExecutor extends ClientConnectionExecutor {
	
	@Override 
	public int execute(String msg) {
		
		if(connection.getVerbosity() >= 3) printDebug("Called ClientCommunicationExecutor (msg="+msg+")");
		
		if(msg.charAt(0) != CMN_PREFIX) return 0;
		
		String[] token = msg.substring(1).split(" ");
		String cmd = token[0];
		
		if(connection.getVerbosity() >= 3) printDebug("cmd="+cmd+",token="+Arrays.asList(token));
	
		switch(cmd) {
			case "ping":
				connection.sendMsg(CMN_PREFIX+"pong");
				return 1;
			case "setnick":
				if(token.length < 2) return 1;
				client.setName(token[1]);
				printMsg("Set nick to "+token[1]);
				return 1;
			case "givepasswd": {
				if(!(client instanceof Component)) return 0;
				JPanel panel = new JPanel();
				JLabel label = new JLabel("Enter a password:");
				JPasswordField pass = new JPasswordField(15);
				panel.add(label);
				panel.add(pass);
				String[] options = new String[] { "OK", "Cancel" };
				int option = JOptionPane.showOptionDialog(
									(Component)client, 
									panel, 
									"Please enter password",
									JOptionPane.NO_OPTION, 
									JOptionPane.PLAIN_MESSAGE,
									null, 
									options,
									options[0]);
				if(option == 0) {
					char[] passwd = null;
					try {
						passwd = pass.getPassword();
						// Hash password before sending it (server will re-hash it with a random salt)
						MessageDigest mDigest = MessageDigest.getInstance("SHA-1");
						mDigest.update(Charset.forName("UTF-8").encode(CharBuffer.wrap(passwd)).array());
						StringBuilder sb = new StringBuilder(CMN_PREFIX+"passwd ");
						// Workaround to ensure we don't send EOL that get misinterpreted by readLine().
						for(byte b : mDigest.digest()) {
							char c = (char)b;
							if(c == '\n' || c == '\f' || c == '\r') 
								c = 'x';
							sb.append(c);
						}
						connection.sendMsg(sb.toString());
						mDigest.reset();
						sb.setLength(0);
					} catch(NoSuchAlgorithmException e) {
						connection.sendMsg(CMN_PREFIX+"abort");
						printDebug("Couldn't hash password: "+e);
					} finally {
						passwd = null;
					}
				} else {
					connection.sendMsg(CMN_PREFIX+"abort");
				}
				return 1;
			}
			case "youros":
				connection.sendMsg(CMN_PREFIX+"myos "+System.getProperty("os.name")+" "+System.getProperty("os.version"));
				return 1;
			case "disconnect":
				return 2;
			case "ok":
				printMsg("Initial handshake completed.");
				return 2;
			case "drop":
				if(token.length > 1) consoleMsg(ConcatenateArrays.merge(token,1));
				else consoleMsg("Server dropped connection.");
				return -1;
		}
		
		return 1;
	}
}
