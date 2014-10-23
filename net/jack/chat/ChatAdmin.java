//: net/jack/chat/ChatAdmin.java

package pokepon.net.jack.chat;

import java.util.*;

/** A ChatUser with all permissions.
 *
 * @author Giacomo Parolini
 */
public class ChatAdmin extends ChatUser {

	public ChatAdmin(String name) {
		super(name, Role.ADMIN);
		
		permissions.addAll(EnumSet.allOf(Permission.class));
	}
}
