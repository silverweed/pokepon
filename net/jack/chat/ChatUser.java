//: net/jack/chat/ChatUser.java

package pokepon.net.jack.chat;

import java.util.*;

/** Base class representing a generic Chat user.
 *
 * @author Giacomo Parolini
 */
public class ChatUser {

	public static enum Role { USER, MODERATOR, ADMIN };
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
		CAN_KICK_MODERATORS,
		CAN_KICK_ADMINS
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

	protected Set<Permission> permissions = (Set<Permission>)EnumSet.noneOf(Permission.class);
	protected Role role;
	protected String name;
}
