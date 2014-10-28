//: net/jack/chat/ChatSystem.java

package pokepon.net.jack.chat;

import pokepon.util.*;
import static pokepon.util.MessageManager.*;
import pokepon.net.jack.server.*;
import java.util.*;
import java.io.*;
import java.net.*;

/** This class is used by the server to handle the chat system.
 *
 * @author Giacomo Parolini
 */
public class ChatSystem {

	public ChatSystem() {
		this(null);
	}

	public ChatSystem(DatabaseServer server) {
		for(ChatUser.Role r : ChatUser.Role.values()) {
			globalPermissions.put(r, new EnumMap<ChatUser.Permission,Boolean>(ChatUser.Permission.class));
			for(ChatUser.Permission p : ChatUser.Permission.values())
				globalPermissions.get(r).put(p, null);
		}
		this.server = server;
		if(server != null) 
			reload();
	}

	/** If a DatabaseServer was given to this ChatSystem, read its database and
	 * fill a map { user: role } accordingly to its entries;
	 * the db should contain lines like:
	 * 	username	passwordhash	chatrole
	 * where 'chatrole' is a string beginning with the symbol of one of the 
	 * ChatUser.Role elements; if the third column is missing, the role is
	 * defaulted to USER; when a user is then renamed, assign him a role if
	 * the name is in this map.
	 * @return true if database was readable and the map was loaded correctly, false otherwise
	 */
	public boolean reload() {
		// flush previous entries
		registered.clear();
		if(server == null) {
			printDebug("[ChatSystem] Cannot reload users: server is null!");
			return false;
		}
		try {
			File db = new File(server.getDatabaseURL().toURI());
			if(db == null) {
				printDebug("[ChatSystem] Error: database not found at "+server.getDatabaseURL());
				return false;
			}
			try (BufferedReader scanner = new BufferedReader(new InputStreamReader(new FileInputStream(db)))) {
				String input = null;
				while((input = scanner.readLine()) != null) {
					if(input.length() < 1 || input.charAt(0) == '#') {
						continue;
					}
					String[] token = input.trim().split("\\s+");
					if(Debug.pedantic) printDebug("tokens: "+Arrays.asList(token));
					if(token.length >= 3) {
						ChatUser.Role r = ChatUser.Role.forSymbol(token[2].charAt(0));
						if(r == null) r = ChatUser.Role.USER;
						registered.put(token[0], r);
					} else if(token.length == 2) {
						// no role entry implies 'user'
						registered.put(token[0], ChatUser.Role.USER);
					} else {
						printDebug("[ChatSystem.reload] incorrect entry in database: "+input);
					}
				}
			} catch(FileNotFoundException e) {	
				printDebug("[ChatSystem.reload] File not found: ");
				e.printStackTrace();
				return false;
			} catch(Exception e) {
				e.printStackTrace();
				return false;
			} 
			if(Debug.on) {
				printDebug("[ChatSystem.reload] OK - entries reloaded successfully.");
				printDebug(getRolesTable());
			}
			return true;
		} catch(URISyntaxException ee) {
			printDebug("[ChatSystem.reload] Exception: "+ee);
			return false;
		}
	}

	/** Returns a string describing the currently registered roles. */
	public String getRolesTable() {
		StringBuilder sb = new StringBuilder("Chat Roles: {\n");
		Map<String,ChatUser.Role> sorted = new TreeMap<>(registered);
		for(Map.Entry<String,ChatUser.Role> entry : sorted.entrySet())
			if(entry.getValue() != ChatUser.Role.USER)
				sb.append("  - "+entry.getKey()+": "+entry.getValue()+"\n");
		sb.append("}\n");
		return sb.toString();
	}

	public void addUser(ChatClient c) {
		clients.add(c);
	}

	public boolean removeUser(ChatClient c) {
		return clients.remove(c);
	}

	public ChatClient removeUser(String name) {
		Iterator<ChatClient> it = clients.iterator();
		while(it.hasNext()) {
			ChatClient c = it.next();
			if(c.getUser().getName().equals(name)) {
				it.remove();
				return c;
			}
		}
		return null;
	}

	public Iterable<ChatClient> getClients() {
		return clients;
	}

	public ChatClient getClient(String name) {
		for(ChatClient c : clients)
			if(c.getUser().getName().equals(name))
				return c;
		return null;
	}

	public ChatUser getUser(String name) {
		for(ChatClient c : clients)
			if(c.getUser().getName().equals(name))
				return c.getUser();
		return null;
	}
	
	public Map<ChatUser.Role,Map<ChatUser.Permission,Boolean>> getGlobalPermissions() {
		return globalPermissions;
	}

	public void addGlobalPermission(ChatUser.Role role, ChatUser.Permission perm, boolean b) {
		globalPermissions.get(role).put(perm, b);
	}

	public void removeGlobalPermission(ChatUser.Role role, ChatUser.Permission perm) {
		globalPermissions.get(role).put(perm, null);
	}

	/** Checks if a user with name 'name' has permission 'perm';
	 * first check if a global permission is set for the user's role,
	 * and return it if so; else, just return the user.hasPermission().
	 */
	public boolean hasPermission(String name, ChatUser.Permission perm) {
		ChatUser usr = getUser(name);
		if(usr == null) return false;
		if(globalPermissions.get(usr.getRole()).get(perm) != null) {
			return globalPermissions.get(usr.getRole()).get(perm);
		}
		return usr.hasPermission(perm);
	}

	public boolean renameUser(String old, String newn) {
		Iterator<ChatClient> it = clients.iterator();
		ChatClient c = null;
		boolean found = false;
		while(it.hasNext()) {
			c = it.next();
			if(c.getUser().getName().equals(old)) {
				it.remove();
				found = true;
				break;
			}
		}
		if(!found) return false;
		if(registered.containsKey(newn)) {
			ChatUser usr = null;
			switch(registered.get(newn)) {
				case ADMIN:
					usr = new ChatAdmin(newn);
					break;
				case MODERATOR:
					usr = new ChatModerator(newn);
					break;
				default:
					usr = new ChatUser(newn);
			}
			clients.add(new ChatClient(c.getConnection(), usr));
		} else {
			clients.add(new ChatClient(c.getConnection(), new ChatUser(newn)));
		}
		return true;
	}

	public int connectedUsers() { return clients.size(); }

	private List<ChatClient> clients = new LinkedList<>();
	private DatabaseServer server;
	private Map<String,ChatUser.Role> registered = new HashMap<>();
	private Map<ChatUser.Role,Map<ChatUser.Permission,Boolean>> globalPermissions = 
		new EnumMap<ChatUser.Role,Map<ChatUser.Permission,Boolean>>(ChatUser.Role.class);
}
