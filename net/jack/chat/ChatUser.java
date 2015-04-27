//: net/jack/chat/ChatUser.java

package pokepon.net.jack.chat;

import java.util.*;

/** Base class representing a generic Chat user.
 *
 * @author silverweed
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

		public static Role forName(String name) {
			for(Role r : values())
				if(r.toString().toLowerCase().equals(name.toLowerCase())) return r;
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
		CAN_LIST_REGISTERED_USERS,
		CAN_LIST_ROLES,
		CAN_VIEW_SERVER_INFO,
		CAN_LOOKUP_IP,
		CAN_LOOKUP_BANNED_IP,
		CAN_LOOKUP_PERMISSIONS,
		CAN_ISSUE_COMMANDS,
		CAN_BAN_IP,
		CAN_KICK_USERS,
		CAN_MUTE_UNMUTE_USERS,
		CAN_MUTE_UNMUTE_MODERATORS,
		CAN_MUTE_UNMUTE_ADMINS,
		CAN_KICK_MODERATORS,
		CAN_KICK_ADMINS,
		CAN_PROMOTE_TO_MODERATOR,
		CAN_PROMOTE_TO_ADMIN,
		CAN_DEMOTE_MODERATORS,
		CAN_DEMOTE_ADMINS,
		CAN_IGNORE_FLOOD_LIMIT,
		CAN_MANIPULATE_DB;

		public static Permission forName(String name) {
			for(Permission p : values())
				if(p.toString().equals(name)) return p;
			return null;
		}
	};

	public static Set<Permission> defaultPermissions = EnumSet.noneOf(Permission.class);
	static {
		defaultPermissions.add(Permission.CAN_TALK);
		defaultPermissions.add(Permission.CAN_WHISPER);
		defaultPermissions.add(Permission.CAN_CHANGE_NICK);
		defaultPermissions.add(Permission.CAN_ISSUE_COMMANDS);
		defaultPermissions.add(Permission.CAN_REGISTER);
		defaultPermissions.add(Permission.CAN_LIST_REGISTERED_USERS);
		defaultPermissions.add(Permission.CAN_LIST_ROLES);
		defaultPermissions.add(Permission.CAN_VIEW_SERVER_INFO);
	}

	public ChatUser(String name) {
		this(name, Role.USER);
	}

	public ChatUser(String name, Role role) {
		this.name = name;
		this.role = role;
		
		permissions.addAll(defaultPermissions);
	}

	public void setName(String n) { name = n; }
	public void setRole(Role r) { role = r; }
	public void copyRoleFrom(ChatUser u) {
		permissions.clear();
		permissions.addAll(u.permissions);
		role = u.role;
	}

	public String getName() { return name; }
	public Role getRole() { return role; }
	/** @return true if user has AT LEAST one of the listed permissions, false otherwise */
	public boolean hasPermission(Permission p, Permission... ps) { 
		boolean b = permissions.contains(p);
		for(Permission pp : ps)
			b |= permissions.contains(pp);
		return b;
	}
	public Set<Permission> getPermissions() { return permissions; }
	public void removePermission(Permission p, Permission... ps) {
		permissions.remove(p);
		for(Permission pp : ps)
			permissions.remove(pp);
	}
	public void addPermission(Permission p, Permission... ps) {
		permissions.add(p);
		for(Permission pp : ps)
			permissions.add(pp);
	}
	
	@Override
	public String toString() {
		return role == Role.USER ? name : role.getSymbol() + name;
	}

	protected Set<Permission> permissions = (Set<Permission>)EnumSet.noneOf(Permission.class);
	protected Role role;
	protected String name;
}
