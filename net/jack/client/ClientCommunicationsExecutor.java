//: pokepon.net.jack/client/ClientCommunicationsExecutor.java

package pokepon.net.jack.client;

import pokepon.net.jack.*;
import pokepon.util.*;
import static pokepon.util.MessageManager.*;
import java.security.*;
import java.security.spec.*;
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
		
		if(msg.charAt(0) != '!') return 0;
		
		String[] token = msg.substring(1).split(" ");
		String cmd = token[0];
		
		if(connection.getVerbosity() >= 3) printDebug("cmd="+cmd+",token="+Arrays.asList(token));
	
		if(cmd.equals("setnick")) {
			if(token.length < 2) return 1;
			client.setName(token[1]);
			printMsg("Set nick to "+token[1]);
			return 1;
		} else if(cmd.equals("givepasswd")) {
			if(!(client instanceof JFrame)) return 0;
			JPanel panel = new JPanel();
			JLabel label = new JLabel("Enter a password:");
			JPasswordField pass = new JPasswordField(15);
			panel.add(label);
			panel.add(pass);
			String[] options = new String[]{"OK", "Cancel"};
			int option = JOptionPane.showOptionDialog(
								(JFrame)client, 
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
					connection.sendMsg(CMN_PREFIX+"passwd "+new String(mDigest.digest()));
					mDigest.reset();
					System.gc();	
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
		} else if(cmd.equals("youros")) {
			connection.sendMsg(CMN_PREFIX+"myos "+System.getProperty("os.name")+" "+System.getProperty("os.version"));
			return 1;
		} else if(cmd.equals("disconnect")) {
			return 2;
		} else if(cmd.equals("ok")) {
			printMsg("Retreiving nick from server...");
			connection.sendMsg(CMN_PREFIX+"mynick"); //query for nick
			return 2;
		} else if(cmd.equals("drop")) {
			if(token.length > 1) consoleMsg(ConcatenateArrays.merge(token,1));
			else consoleMsg("Server dropped connection.");
			return -1;
		}
		
		return 1;
	}
}
