//: net/jack/chat/Chat.java

package pokepon.net.jack.chat;

public interface Chat {
	
	public String getNick();
	public void setNick(String nick);
	public void userAdd(final String name);
	public void userAdd(final String name, ChatUser.Role role);
	public void userRename(final String old, final String newN);
	public void userRename(final String old, final String newN, ChatUser.Role role);
	public void userRemove(final String name);
}
