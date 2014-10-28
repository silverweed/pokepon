//: net/jack/chat/ChatModerator.java

package pokepon.net.jack.chat;

import java.util.*;

/** A ChatUser with enhanced permissions.
 *
 * @author silverweed
 */
public class ChatModerator extends ChatUser {
	
	public static Set<Permission> defaultPermissions = (EnumSet<Permission>)EnumSet.copyOf(ChatUser.defaultPermissions);
	static {
		defaultPermissions.add(Permission.CAN_LOOKUP_IP);
		defaultPermissions.add(Permission.CAN_KICK_USERS);
		defaultPermissions.add(Permission.CAN_BAN_IP);
		defaultPermissions.add(Permission.CAN_LOOKUP_BANNED_IP);
		defaultPermissions.add(Permission.CAN_IGNORE_FLOOD_LIMIT);
		defaultPermissions.add(Permission.CAN_MUTE_UNMUTE_USERS);
	}

	public ChatModerator(String name) {
		super(name, Role.MODERATOR);
		
		permissions.addAll(defaultPermissions);
	}
}
