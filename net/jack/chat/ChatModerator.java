//: net/jack/chat/ChatModerator.java

package pokepon.net.jack.chat;

/** A ChatUser with enhanced permissions.
 *
 * @author Giacomo Parolini
 */
public class ChatModerator extends ChatUser {

	public ChatModerator(String name) {
		super(name, Role.MODERATOR);
		
		permissions.add(Permission.CAN_LOOKUP_IP);
		permissions.add(Permission.CAN_KICK_USERS);
		permissions.add(Permission.CAN_BAN_IP);
	}
}
