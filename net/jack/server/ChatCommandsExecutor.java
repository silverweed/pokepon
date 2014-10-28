//: pokepon.net.jack/server/ChatCommandsExecutor.java

package pokepon.net.jack.server;

import pokepon.net.jack.*;
import pokepon.net.jack.chat.*;
import pokepon.util.*;
import static pokepon.util.MessageManager.*;
import static pokepon.net.jack.chat.ChatUser.Permission.*;
import java.util.*;
import java.io.*;

/** A ConnectionExecutor which executes special chat commands, typically
 * moderation actions etc.
 *
 * @author silverweed
 */
class ChatCommandsExecutor extends ServerConnectionExecutor {

	/** If a connection gives more than this number of commands in a minute,
	 * ignore following.
	 */
	protected static final int ISSUED_CMD_BAN_LIMIT = 40;
	protected static StringBuilder help = new StringBuilder("");
	static {
		help.append(CMD_PREFIX+"role [user] - prints an user's role (default: your role).\n");
		help.append(CMD_PREFIX+"kick <user> - kicks an user out of this server.\n");
		help.append(CMD_PREFIX+"mute <user|["+ChatUser.Role.USER.getSymbol()+ChatUser.Role.MODERATOR.getSymbol()+
			"]> - prevents the selected user / group from talking.\n");
		help.append(CMD_PREFIX+"unmute <user> - unmutes the selected user.\n");
		help.append(CMD_PREFIX+"ban <ip> - ban an IP from this server.\n");
	}

	@SuppressWarnings("unchecked")
	@Override
	public int execute(String msg) {
			
		if(server.chat == null) {
			if(connection.getVerbosity() >= 2) printDebug("server.chat is null: ChatCommandsExecutor returning 0.");
			return 0;
		}
		if(connection.getVerbosity() >= 2) printDebug("Called ChatCommandsExecutor.execute(msg="+msg+")");

		if(msg.charAt(0) != CMD_PREFIX) return 0;

		ChatUser chatUser = server.chat.getUser(connection.getName());

		if(!chatUser.hasPermission(CAN_ISSUE_COMMANDS))
			return 1;

		// refuse to execute command if this connection sent more than ISSUED_CMD_BAN_LIMIT commands in the last minute.
		int issued = 0;
		long startTime = -1;
		Iterator<Map.Entry<Long,String>> it = connection.getLatestMessages().descendingIterator();
		while(it.hasNext()) {
			Map.Entry<Long,String> entry = it.next();
			if(startTime == -1) {
				startTime = entry.getKey();
				continue;
			}
			if(startTime - entry.getKey() > 60 * 1000)
				break;
			++issued;
		}
		if(	issued >= ISSUED_CMD_BAN_LIMIT - 1 &&
			!chatUser.hasPermission(CAN_IGNORE_FLOOD_LIMIT)
		) {
			if(connection.getVerbosity() >= 2) 
				printDebug("[CMDEXEC] connection "+connection.getName() + "issued " + issued +
					" commands in a minute. Ignoring next commands until queue is emptied.");
			return 1;
		}
			
		
		String[] token = msg.substring(1).split(" ");
		String cmd = token[0];
	
		if(connection.getVerbosity() >= 3) printDebug("cmd="+cmd+",token="+Arrays.asList(token).toString());

		
		if(cmd.equals("help")) {
			connection.sendMsg(help.toString());
			return 0; // if there are other CommandsExecutors, send their help msg too.
		} else if(cmd.equals("role")) {
			if(token.length < 2) {
				String roleStr = getRoleString(chatUser);
				connection.sendMsg(CMN_PREFIX+"html Your role is "+roleStr);
				return 1;
			} else if(token.length < 3) {
				ChatUser cu = server.chat.getUser(token[1]);
				if(cu != null) {
					String roleStr = getRoleString(cu);
					connection.sendMsg(CMN_PREFIX+"html " + cu.getName()+"'s role is "+roleStr);
					return 1;
				}
				connection.sendMsg("User "+token[1]+" not found.");
			}
			return 1;
		} else if(cmd.equals("kick")) {
			if(token.length < 2) {
				connection.sendMsg("Syntax error. Correct syntax is "+CMD_PREFIX+"kick <user>");
				return 1;
			}
			if(!chatUser.hasPermission(CAN_KICK_USERS)) {
				connection.sendMsg("You cannot kick other users.");
				return 1;
			}
			ChatUser cu = server.chat.getUser(token[1]);
			if(connection.getVerbosity() >= 2)
				printDebug("[CHATCMDEXEC] Kick requested by "+connection.getName()+" versus "+token[1]);
			if(cu == null) {
				connection.sendMsg("User "+token[1]+" not found.");
				return 1;
			} else {
				switch(cu.getRole()) {
					case ADMIN:
						if(chatUser.hasPermission(CAN_KICK_ADMINS)) {
							server.kickUser(cu.getName(), chatUser.toString());
						} else {
							connection.sendMsg("You cannot kick admins out of this server.");
						}
						return 1;
					case MODERATOR:
						if(chatUser.hasPermission(CAN_KICK_MODERATORS)) {
							server.kickUser(cu.getName(), chatUser.toString());
						} else {
							connection.sendMsg("You cannot kick moderators out of this server.");
						}
						return 1;
					default:
						// already checked CAN_KICK_USERS
						server.kickUser(cu.getName(), chatUser.toString());
						return 1;
				}
			}
		}
		// let someone else process this command
		return 0;
	}

	private String getRoleString(ChatUser user) {
		switch(user.getRole()) {
			case ADMIN:
				return "<b><font color='red'>ADMIN</font></b>.";
			case MODERATOR:
				return "<b><font color='#009900'>MODERATOR</font><b>.";
			default:
				return "<b><font color='blue'>USER</font></b>.";
		}
	}
}
