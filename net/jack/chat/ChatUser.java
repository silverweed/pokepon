//: net/jack/chat/ChatUser.java

package pokepon.net.jack.chat;

import java.util.*;

/** Base class representing a generic Chat user.
 *
 * @author Giacomo Parolini
 */
public class ChatUser {

	/** Chat roles */
	public static enum Role { 
		USER, MODERATOR, ADMIN;
		
		public char getSymbol() {
			switch(this) {
				case USER: return '\000';
				case MODERATOR: return '+';
				case ADMIN: return '@';
			}
			return '\000';
		}

		public static Role forSymbol(char c) {
			for(Role r : values())
				if(r.getSymbol() == c) return r;
			return null;
		}

		/** Given a String, if it starts with a character which is a 
		 * Role symbol, strips it; else, just return the unmodified String.
		 */
		public static String stripSymbol(String s) {
			for(Role r : values()) {
				if(s.charAt(0) == r.getSymbol())
					return s.substring(1);
			}
			return s;
		}
	};

	/** Basic permissions */
	public static enum Permission {
		CAN_TALK,
		CAN_WHISPER,
		CAN_CHANGE_NICK,
		CAN_REGISTER,
		CAN_LOOKUP_IP,
		CAN_ISSUE_COMMANDS,
		CAN_BAN_IP,
		CAN_KICK_USERS,
		CAN_MUTE_UNMUTE_USERS,
		CAN_MUTE_UNMUTE_MODERATORS,
		CAN_MUTE_UNMUTE_ADMINS,
		CAN_KICK_MODERATORS,
		CAN_KICK_ADMINS,
		CAN_IGNORE_FLOOD_LIMIT
	};

	public ChatUser(String name) {
		this(name, Role.USER);
	}

	public ChatUser(String name, Role role) {
		this.name = name;
		this.role = role;

		permissions.add(Permission.CAN_TALK);
		permissions.add(Permission.CAN_WHISPER);
		permissions.add(Permission.CAN_CHANGE_NICK);
		permissions.add(Permission.CAN_ISSUE_COMMANDS);
		permissions.add(Permission.CAN_REGISTER);
	}

	public void setName(String n) { name = n; }
	public void setRole(Role r) { role = r; }

	public String getName() { return name; }
	public Role getRole() { return role; }
	public boolean hasPermission(Permission p) { 
		return permissions.contains(p);
	}
	public Set<Permission> getPermissions() { return permissions; }
	
	@Override
	public String toString() {
		return role == Role.USER ? name : role.getSymbol() + name;
	}

	protected Set<Permission> permissions = (Set<Permission>)EnumSet.noneOf(Permission.class);
	protected Role role;
	protected String name;
}
