//: net/jack/client/ChatClientCommunicationsExecutor.java

package pokepon.net.jack.client;

import pokepon.net.jack.*;
import pokepon.net.jack.chat.*;
import pokepon.util.*;
import static pokepon.util.MessageManager.*;
import javax.swing.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;

/** A ClientCommunicationsExecutor with chat commands capabilities
 *
 * @author silverweed
 */

class ChatClientCommunicationsExecutor extends ClientCommunicationsExecutor {

	protected ChatClient chatClient;

	@Override
	public int execute(String msg) {

		if(!(client instanceof ChatClient)) return 0;
		chatClient = (ChatClient)client;

		if(connection.getVerbosity() >= 3) printDebug("Called ChatClientCommunicationExecutor (msg="+msg+")");
		
		if(msg.charAt(0) != CMN_PREFIX) return 0;
		
		String[] token = msg.substring(1).split(" ");
		String cmd = token[0];
		
		if(connection.getVerbosity() >= 3) printDebug("cmd="+cmd+",token="+Arrays.asList(token));

		if(cmd.equals("useradd")) {
			/* !useradd name role [name role ...] */
			if(token.length < 2) return 1;
			int i = 1;
			do {
				String username = token[i++].trim();
				if(i == token.length)
					break;
				String role = token[i++].trim();
				// By convention, if server sent a "role" with length > 1, it's a placeholder for "no role" (i.e. user)
				if(role.length() > 1)
					chatClient.getChat().userAdd(username);
				else
					chatClient.getChat().userAdd(username, ChatUser.Role.forSymbol(role.charAt(0)));
			} while (i < token.length);
			
			return 1;

		} else if(cmd.equals("userrm")) {
			/* !userrm username */
			if(token.length < 2) return 1;
			chatClient.getChat().userRemove(token[1].trim());
			return 1;

		} else if(cmd.equals("userrnm")) {
			/* !userrnm old new [role] */
			if(token.length < 3) return 1;
			printDebug("userrnm: tokens = "+Arrays.asList(token)); 
			if(token.length > 3)
				chatClient.getChat().userRename(token[1].trim(), token[2].trim(), ChatUser.Role.forSymbol(token[3].charAt(0)));
			else
				chatClient.getChat().userRename(token[1].trim(),token[2].trim());
			return 1;

		} else if(cmd.startsWith("popup")) {
			/* !popup[-err|-warn] [Title] Actual message<br>with br tags instead<br>of newlines. */
			if(token.length < 2) return 1;
			int type = -1;
			if(cmd.equals("popup"))
				type = JOptionPane.PLAIN_MESSAGE;
			else if(cmd.equals("popup-warn"))
				type = JOptionPane.WARNING_MESSAGE;
			else if(cmd.equals("popup-err"))
				type = JOptionPane.ERROR_MESSAGE;
			else return 1;
			String title = "Message from the server";
			String mesg = "";
			Matcher matcher = Pattern.compile("^(?<title>\\[.*\\])\\s*(?<msg>.*)$").matcher(ConcatenateArrays.merge(token,1));
			if(matcher.matches()) {
				if(Debug.pedantic)
					printDebug("[CHATCMNEXEC] matcher matches.\ntitle="+matcher.group("title")+"\nmsg="+matcher.group("msg"));
				if(matcher.group("title") != null)
					title = matcher.group("title");
				if(matcher.group("msg") != null)
					mesg = matcher.group("msg");
			} else if(Debug.on) {
				printDebug("[CHATCMNEXEC] matcher not matched.");
			}

			JOptionPane.showMessageDialog((JPanel)chatClient,
							mesg.replaceAll("<br>","\n"),
							title,
							type);
			return 1;

		} else if(cmd.equals("drop")) {
			if(token.length > 1)
				chatClient.append(ConcatenateArrays.merge(token,1));
			else
				chatClient.append("Server dropped connection.");
			return -1;
		}
		else return super.execute(msg);
	}
}
