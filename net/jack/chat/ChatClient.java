//: net/jack/chat/ChatClient.java

package pokepon.net.jack.chat;

import pokepon.net.jack.Connection;

public class ChatClient {
	
	public ChatClient(Connection conn, ChatUser usr) {
		connection = conn;
		user = usr;
	}
	
	public Connection getConnection() {
		return connection;
	}

	public ChatUser getUser() {
		return user;
	}

	private Connection connection;
	private ChatUser user;
}
