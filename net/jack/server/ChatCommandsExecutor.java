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
 * @author Giacomo Parolini
 */
class ChatCommandsExecutor extends ServerConnectionExecutor {

	protected static StringBuilder help = new StringBuilder("");
	static {
		help.append("Advanced chat commands:\n");
		help.append(CMD_PREFIX+"role [user] - prints an user's role (default: your role).\n");
		help.append(CMD_PREFIX+"roles - lists all assigned roles.\n");
		help.append(CMD_PREFIX+"perm [user] - prints an user's permissions (default: yours).\n");
		help.append(CMD_PREFIX+"perms - lists all existing permissions.\n");
		help.append(CMD_PREFIX+"kick <user> - kicks an user out of this server.\n");
		help.append(CMD_PREFIX+"mute [user|group] - prevents the selected user / group from talking.\n");
		help.append(CMD_PREFIX+"unmute <user> - unmutes the selected user.\n");
		help.append(CMD_PREFIX+"ban <ip> - ban an IP from this server (use addresses, not hostnames)\n");
		help.append(CMD_PREFIX+"unban <ip> - unban an IP from this server\n");
		help.append(CMD_PREFIX+"banned [ip] - list banned IPs, or tells whether 'ip' is banned or not\n");
		help.append(CMD_PREFIX+"database [dburl] - if argument is given, change db location to 'dburl', else print it.\n");
		help.append(CMD_PREFIX+"reload - flush and reload chat roles according to server db\n");
		help.append(CMD_PREFIX+"setrole <user> <role> - temporarily assign role to user.\n");
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

		// refuse to execute command if this connection sent more than server.cmdBanLimit commands in the last minute.
		if(server.cmdBanLimit > -1) {
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
			if(	issued >= server.cmdBanLimit &&
				!chatUser.hasPermission(CAN_IGNORE_FLOOD_LIMIT)
			) {
				if(connection.getVerbosity() >= 2) 
					printDebug("[CMDEXEC] connection "+connection.getName() + " issued " + issued +
						" commands in a minute. Ignoring next commands until queue is emptied.");
				return 1;
			}
		}
			
		String[] token = msg.substring(1).split(" ");
		String cmd = token[0];
	
		if(connection.getVerbosity() >= 3) printDebug("cmd="+cmd+",token="+Arrays.asList(token).toString());

		
		if(cmd.equals("help")) {
			connection.sendMsg(help.toString());
			return 0; // if there are other CommandsExecutors, send their help msg too.
		} else if(cmd.equals("role")) {
			if(token.length > 2) {
				connection.sendMsg("Syntax error. Correct syntax is "+CMD_PREFIX+"role [user]");
				return 1;
			}
			if(token.length < 2) {
				String roleStr = getRoleString(chatUser);
				connection.sendMsg(CMN_PREFIX+"html Your role is "+roleStr);
				return 1;
			} else {
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
		} else if(cmd.equals("mute")) {
			if(token.length > 2) {
				connection.sendMsg("Syntax error. Correct syntax is "+CMD_PREFIX+"mute [user|group]\n"+
					"  where group =\n  '"+ChatUser.Role.USER.getSymbol()+"': users\n"+
					"  '"+ChatUser.Role.MODERATOR.getSymbol()+"': users and mods");
				return 1;
			}
			if(!chatUser.hasPermission(CAN_MUTE_UNMUTE_USERS)) {
				connection.sendMsg("You cannot mute/unmute users.");
				return 1;
			}
			if(token.length == 1) {
				// mute all users
				server.chat.addGlobalPermission(ChatUser.Role.USER, CAN_TALK, false);
				server.broadcast(null, CMN_PREFIX+"html <em>"+chatUser+" muted all USERS.</em>");
				return 1;
			} else {
				if(token[1].length() == 1) {
					for(ChatUser.Role r : ChatUser.Role.values()) {
						if(r.getSymbol() == token[1].charAt(0)) {
							// mute group
							if(!canMuteUnmute(chatUser, r)) {
								return 1;
							}
							for(ChatUser.Role rr : ChatUser.Role.values()) {
								if(rr.compareTo(r) <= 0)
									server.chat.addGlobalPermission(rr, CAN_TALK, false);
							}
							server.broadcast(null, CMN_PREFIX+"html <em>"+chatUser+" muted all up to "+r+"S</em>");
							return 1;
						}
					}
				}
				// mute user
				if(server.chat.getUser(token[1]) != null) {
					if(!canMuteUnmute(chatUser, server.chat.getUser(token[1]).getRole())) {
						return 1;
					}
					server.chat.getUser(token[1]).removePermission(CAN_TALK);
					server.broadcast(null, CMN_PREFIX+"html <em>"+chatUser+" muted "+server.chat.getUser(token[1])+"</em>");
					return 1;
				} else {
					connection.sendMsg("No group nor user "+token[1]+" was found.");
					return 1;
				}
			}
		} else if(cmd.equals("unmute")) {
			if(token.length > 2) {
				connection.sendMsg("Syntax error. Correct syntax is "+CMD_PREFIX+"unmute [user|group]\n"+
					"  where group =\n  '"+ChatUser.Role.USER.getSymbol()+"': users\n"+
					"  '"+ChatUser.Role.MODERATOR.getSymbol()+"': users and mods");
				return 1;
			}
			if(!chatUser.hasPermission(CAN_MUTE_UNMUTE_USERS)) {
				connection.sendMsg("You cannot mute/unmute users.");
				return 1;
			}
			if(token.length == 1) {
				// unmute all 
				for(ChatUser.Role r : ChatUser.Role.values())
					server.chat.removeGlobalPermission(r, CAN_TALK);
				server.broadcast(null, CMN_PREFIX+"html <em>"+chatUser+" unmuted all USERS.</em>");
				return 1;
			} else {
				if(token[1].length() == 1) {
					for(ChatUser.Role r : ChatUser.Role.values()) {
						if(r.getSymbol() == token[1].charAt(0)) {
							// unmute group
							if(!canMuteUnmute(chatUser, r)) {
								return 1;
							}
							for(ChatUser.Role rr : ChatUser.Role.values()) {
								if(rr.compareTo(r) >= 0)
									server.chat.removeGlobalPermission(rr, CAN_TALK);
							}
							server.broadcast(null, CMN_PREFIX+"html <em>"+chatUser+" unmuted all down to "+r+"S</em>");
							return 1;
						}
					}
				}
				// unmute user
				if(server.chat.getUser(token[1]) != null) {
					if(!canMuteUnmute(chatUser, server.chat.getUser(token[1]).getRole())) {
						return 1;
					}
					server.chat.getUser(token[1]).addPermission(CAN_TALK);
					server.broadcast(null, CMN_PREFIX+"html <em>"+chatUser+" unmuted "+server.chat.getUser(token[1])+"</em>");
					return 1;
				} else {
					connection.sendMsg("No group nor user "+token[1]+" was found.");
					return 1;
				}
			}
		} else if(cmd.equals("ban")) {
			if(token.length < 2) {
				connection.sendMsg("Syntax error. Correct syntax is "+CMD_PREFIX+"ban <ip> [ip...]");
				return 1;
			}
			if(!chatUser.hasPermission(CAN_BAN_IP)) {
				connection.sendMsg("You are not allowed to ban IPs from this server.");
				return 1;
			}
			StringBuilder sb = new StringBuilder("");
			for(int i = 1; i < token.length; ++i) {
				server.banIP(token[i]);
				sb.append("Banned IP "+token[i]+"\n");
			}
			connection.sendMsg(sb.toString());
			return 1;
		} else if(cmd.equals("unban")) {
			if(token.length < 2) {
				connection.sendMsg("Syntax error. Correct syntax is "+CMD_PREFIX+"unban <ip> [ip...]");
				return 1;
			}
			if(!chatUser.hasPermission(CAN_BAN_IP)) {
				connection.sendMsg("You are not allowed to unban IPs from this server.");
				return 1;
			}
			StringBuilder sb = new StringBuilder("");
			for(int i = 1; i < token.length; ++i) {
				server.unbanIP(token[i]);
				sb.append("Unbanned IP "+token[i]+"\n");
			}
			connection.sendMsg(sb.toString());
			return 1;
		} else if(cmd.equals("banned")) {
			if(!chatUser.hasPermission(CAN_LOOKUP_BANNED_IP)) {
				connection.sendMsg("You are not allowed to list banned IPs for this server.");
				return 1;
			}
			if(token.length > 1) {
				StringBuilder sb = new StringBuilder("");
				for(int i = 1; i < token.length; ++i) {
					if(server.getBannedIP().contains(token[i]))
						sb.append(token[i] + " is BANNED\n");
					else
						sb.append(token[i] + " is NOT banned.\n");
				}
				connection.sendMsg(sb.toString());
				return 1;
			}
			StringBuilder sb = new StringBuilder("-- Banned IP:");
			for(String ip : server.getBannedIP())
				sb.append(ip+"\n");
			connection.sendMsg(sb.toString());
			return 1;
		} else if(cmd.equals("database")) {
			if(!chatUser.hasPermission(CAN_MANIPULATE_DB)) {
				connection.sendMsg("You are not allowed to manipulate the server's DB location.");
				return 1;
			}
			if(!(server instanceof DatabaseServer)) {
				connection.sendMsg("This server doesn't support a database.");
				return 1;
			}
			if(token.length > 1) {
				if(((DatabaseServer)server).setDatabaseLocation(token[1])) 
					connection.sendMsg("[ OK ] Database switched correctly to "+((DatabaseServer)server).getDatabaseURL());
				else
					connection.sendMsg("Errors switching database: see server logs for details.");
			} else {
				connection.sendMsg("Server DB: "+((DatabaseServer)server).getDatabaseURL());
			}
			return 1;
		} else if(cmd.equals("reload")) {
			if(!chatUser.hasPermission(CAN_MANIPULATE_DB)) {
				connection.sendMsg("You are not allowed to manipulate chat roles.");
				return 1;
			}
			if(!(server instanceof DatabaseServer)) {
				connection.sendMsg("Cannot reload roles from DB: this server doesn't support a database.");
				return 1;
			}
			if(server.chat.reload()) {
				connection.sendMsg("[ OK ] chat roles reloaded successfully. New roles:");
				connection.sendMsg(server.chat.getRolesTable());
			} else {
				connection.sendMsg("Errors reloading chat roles: see server logs for details.");
			}
			return 1;
		} else if(cmd.equals("roles")) {
			if(!chatUser.hasPermission(CAN_LIST_ROLES)) {
				connection.sendMsg("You are not allowed to list server registered roles.");
				return 1;
			}
			connection.sendMsg(server.chat.getRolesTable());
			return 1;
		} else if(cmd.equals("setrole")) {
			if(token.length != 3) {
				connection.sendMsg("Syntax error. Correct syntax is "+CMD_PREFIX+"setrole <user> <role>\n"+
					"  where role = user|moderator|admin");
				return 1;
			}
			ChatUser usr = server.chat.getUser(token[1]);
			if(usr == null) {
				connection.sendMsg("User "+token[1]+" not found.");
				return 1;
			}
			ChatUser.Role role = ChatUser.Role.forName(token[2]);
			if(role == null) {
				connection.sendMsg("Role does not exist: "+token[2]);
				return 1;
			}
			if(role == usr.getRole()) {
				connection.sendMsg(usr+" already has role "+role+".");
				return 1;
			}
			switch(usr.getRole()) {
				case ADMIN:
					if(!chatUser.hasPermission(CAN_DEMOTE_ADMINS)) {
						connection.sendMsg("You cannot demote an ADMIN.");
						return 1;
					}
					switch(role) {
						case MODERATOR:	// admin -> mod
							usr.copyRoleFrom(new ChatModerator("template"));
							break;
						case USER:	// admin -> user
							usr.copyRoleFrom(new ChatUser("template"));
					}
					server.broadcast(null, chatUser + " changed " + usr.getName() + "'s role to " + role);
					server.broadcast(null, CMN_PREFIX+"userrnm "+usr.getName()+" "+usr.getName()+" "+role.getSymbol());
					break;
				case MODERATOR:
					switch(role) {
						case ADMIN:	// mod -> admin
							if(!chatUser.hasPermission(CAN_PROMOTE_TO_ADMIN)) {
								connection.sendMsg("You cannot promote users to ADMIN.");
								return 1;
							}
							usr.copyRoleFrom(new ChatAdmin("template"));
							break;
						case USER:	// mod -> user
							if(!chatUser.hasPermission(CAN_DEMOTE_MODERATORS, CAN_DEMOTE_ADMINS)) {
								connection.sendMsg("You cannot demote a MODERATOR.");
								return 1;
							}
							usr.copyRoleFrom(new ChatUser("template"));
					}
					server.broadcast(null, chatUser + " changed " + usr.getName() + "'s role to " + role);
					server.broadcast(null, CMN_PREFIX+"userrnm "+usr.getName()+" "+usr.getName()+" "+role.getSymbol());
					break;
				default:
					switch(role) {
						case ADMIN:	// user -> admin
							if(!chatUser.hasPermission(CAN_PROMOTE_TO_ADMIN)) {
								connection.sendMsg("You cannot promote users to ADMIN.");
								return 1;
							}
							usr.copyRoleFrom(new ChatAdmin("template"));
							break;
						case MODERATOR:	// user -> mod
							if(!chatUser.hasPermission(CAN_PROMOTE_TO_MODERATOR, CAN_PROMOTE_TO_ADMIN)) {
								connection.sendMsg("You cannot promote users to MODERATOR.");
								return 1;
							}
							usr.copyRoleFrom(new ChatModerator("template"));
					}
					server.broadcast(null, chatUser + " changed " + usr.getName() + "'s role to " + role);
					server.broadcast(null, CMN_PREFIX+"userrnm "+usr.getName()+" "+usr.getName()+" "+role.getSymbol());
			}
			return 1;
		} else if(cmd.equals("perm")) {
			if(token.length > 2) {
				connection.sendMsg("Syntax error. Correct syntax is "+CMD_PREFIX+"perm [user]");
				return 1;
			}
			if(token.length < 2) {
				StringBuilder sb = new StringBuilder("Your permissions are:\n");
				for(ChatUser.Permission perm : chatUser.getPermissions()) {
					sb.append("  - "+perm+"\n");
				}
				connection.sendMsg(sb.toString());
				return 1;
			} else {
				if(!chatUser.hasPermission(CAN_LOOKUP_PERMISSIONS)) {
					connection.sendMsg("You cannot list other users' permissions.");
					return 1;
				}
				ChatUser cu = server.chat.getUser(token[1]);
				if(cu == null) {
					connection.sendMsg("User "+token[1]+" not found.");
					return 1;
				}
				StringBuilder sb = new StringBuilder(cu+"'s permissions are:\n");
				for(ChatUser.Permission perm : cu.getPermissions()) {
					sb.append("  - "+perm+"\n");
				}
				connection.sendMsg(sb.toString());
			}
		} else if(cmd.equals("perms")) {
			if(token.length > 1) {
				connection.sendMsg("Syntax error. Correct syntax is "+CMD_PREFIX+"perms");
				return 1;
			}
			StringBuilder sb = new StringBuilder("-- All permissions:\n");
			for(ChatUser.Permission perm : ChatUser.Permission.values())
				sb.append("  - "+perm+"\n");
			connection.sendMsg(sb.toString());
			return 1;
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
	
	private boolean canMuteUnmute(ChatUser chatUser, ChatUser.Role r) {
		switch(r) {
			case ADMIN:
				if(!chatUser.hasPermission(CAN_MUTE_UNMUTE_MODERATORS)) {
					connection.sendMsg("You cannot mute ADMINS.");
					return false;
				} else return true;
			case MODERATOR:
				if(!chatUser.hasPermission(CAN_MUTE_UNMUTE_MODERATORS)) {
					connection.sendMsg("You cannot mute MODERATORS.");
					return false;
				} else return true;
			default:
				if(!chatUser.hasPermission(CAN_MUTE_UNMUTE_USERS)) {
					connection.sendMsg("You cannot mute USERS.");
					return false;
				} else return true;
		}
	}
}
