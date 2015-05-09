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
 * @author silverweed
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

	/** Reads a file and changes default roles' permissions accordingly;
	 * the conf file consists in several `stanzas' defining roles' permissions
	 * in this way:
	 * <pre>&at;role
	 *   * PERMISSION_1
	 *   * PERMISSION_2
	 * </pre>
	 * redefines `role`'s permissions to be ONLY the listed ones;
	 * <pre>+role
	 *   + ADDED_PERMISSION
	 *   - REMOVED_PERMISSION
	 * </pre>
	 * takes the pre-existing permissions for `role` and adds or removes listed ones.
	 */
	public boolean loadConfFromFile(String filename) {
		File file = new File(filename);
		if(!file.canRead()) {
			printDebug("[ChatSystem] cannot read conf file: "+filename);
			return false;
		}
		try (BufferedReader scanner = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
			String input = null;
			ChatUser.Role stanzaRole = null;
			// -: none, +: extend, @: rewrite
			char stanzaType = '-';
			while((input = scanner.readLine()) != null) {
				input = input.trim();
				if(Debug.pedantic) printDebug("read line: "+input);
				if(input.length() < 1 || input.charAt(0) == '#') 
					continue;
				String[] token = input.split("\\s+");
				if(Debug.pedantic) printDebug("tokens: "+Arrays.asList(token));
				
				if(token[0].charAt(0) == '@') {
					if(token[0].length() < 2) {
						printDebug("[ChatSystem] Invalid line in conf file: "+input);
						continue;
					}
					stanzaRole = ChatUser.Role.forName(token[0].substring(1));
					if(stanzaRole == null) {
						// TODO: define custom roles
						printDebug("[ChatSystem] Role not found: "+token[0].substring(1));
						continue;
					}
					switch(stanzaRole) {
						case ADMIN:
							ChatAdmin.defaultPermissions.clear();
							break;
						case MODERATOR:
							ChatModerator.defaultPermissions.clear();
							break;
						case USER:
							ChatUser.defaultPermissions.clear();
							break;
					}
					stanzaType = '@';

				} else if(token[0].charAt(0) == '+') {
					if(token[0].length() > 1) {
						// new stanza
						stanzaRole = ChatUser.Role.forName(token[0].substring(1));
						if(stanzaRole == null) {
							// TODO: define custom roles
							printDebug("[ChatSystem] Role not found: "+token[0].substring(1));
							continue;
						}
						stanzaType = '+';
						continue;
					} else if(stanzaRole != null && stanzaType == '+') {
						// extend role by adding permission
						if(token.length != 2) {
							printDebug("[ChatSystem] Malformed line in conf: "+input);
							continue;
						}
						ChatUser.Permission perm = ChatUser.Permission.forName(token[1]);
						if(perm == null) {
							printDebug("[ChatSystem] Unknown permission: "+token[1]);
							continue;
						}
						switch(stanzaRole) {
							case ADMIN:
								ChatAdmin.defaultPermissions.add(perm);
								break;
							case MODERATOR:
								ChatModerator.defaultPermissions.add(perm);
								break;
							case USER:
								ChatUser.defaultPermissions.add(perm);
								break;
						}
					} else {
						printDebug("[ChatSystem] Line out of stanza: "+input);
						continue;
					}
				} else if(token[0].equals("-") || token[0].equals("*")) {
					// extend role by removing permission
					if(stanzaRole == null) {
						printDebug("[ChatSystem] Line out of stanza: "+input);
						continue;
					}
					if(token[0].equals("-") && stanzaType == '@' || token[0].equals("*") && stanzaType == '+') {
						printDebug("[ChatSystem] Invalid instruction in stanza "+stanzaType + stanzaRole+": "+input);
						continue;
					}
					ChatUser.Permission perm = ChatUser.Permission.forName(token[1]);
					if(perm == null) {
						printDebug("[ChatSystem] Unknown permission: "+token[1]);
						continue;
					}
					switch(stanzaRole) {
						case ADMIN:
							if(token[0].equals("-"))
								ChatAdmin.defaultPermissions.remove(perm);
							else
								ChatAdmin.defaultPermissions.add(perm);
							break;
						case MODERATOR:
							if(token[0].equals("-"))
								ChatModerator.defaultPermissions.remove(perm);
							else
								ChatModerator.defaultPermissions.add(perm);
							break;
						case USER:
							if(token[0].equals("-"))
								ChatUser.defaultPermissions.remove(perm);
							else
								ChatUser.defaultPermissions.add(perm);
							break;
					}
				} else {
					printDebug("[ChatSystem] Invalid line in conf: "+input);
					continue;
				}
			}
			if(server != null && server.getVerbosity() >= 2 || Debug.pedantic) {
				printDebug("After reading conf file, chat permissions are:");
				printDebug("ADMIN: "+ChatAdmin.defaultPermissions);
				printDebug("MODERATOR: "+ChatModerator.defaultPermissions);
				printDebug("USER: "+ChatUser.defaultPermissions);
			}

		} catch(FileNotFoundException e) {
			printDebug("[ChatSystem.loadConfFromFile("+filename+")] File not found: "+filename);
		} catch(Exception e) {
			printDebug("Caught exception in nickExists: "+e);
			e.printStackTrace();
		} 
		return true;
	}

	/** If a DatabaseServer was given to this ChatSystem, read its in-memory db and
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
		for(Map.Entry<String,String[]> entry : server.getDBEntries().entrySet()) {
			String[] args = entry.getValue();
			if(args.length < 2) {
				// No explicit role implies 'user'
				registered.put(entry.getKey(), ChatUser.Role.USER);
			} else {
				ChatUser.Role r = ChatUser.Role.forSymbol(args[1].charAt(0));
				if(r == null) r = ChatUser.Role.USER;
				registered.put(entry.getKey(), r);
			}
		}
		return true;
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

	/** Adds a new ChatClient to the ChatSystem */
	public void addUser(ChatClient c) {
		clients.add(c);
	}

	/** Attempts to remove a ChatClient from the ChatSystem
	 * @param c The ChatClient to remove
	 * @return true on success, false if not found.
	 */
	public boolean removeUser(ChatClient c) {
		return clients.remove(c);
	}

	/** Attempts to remove a ChatClient from the ChatSystem
	 * @param name The name of the ChatClient to remove
	 * @return true on success, false if not found.
	 */
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

	/** @return An Iterable with the clients currently registered on this ChatSystem */
	public Iterable<ChatClient> getClients() {
		return clients;
	}

	/** Get a ChatClient by name; ChatClient contains information about both
	 * the ChatUser and its Connection.
	 */
	public ChatClient getClient(String name) {
		for(ChatClient c : clients)
			if(c.getUser().getName().equals(name))
				return c;
		return null;
	}

	/** Get a ChatUser by name; ChatUser contains information about name and
	 * role of this user; it does NOT contain Connection information (see
	 * {@link pokepon.net.jack.chat.ChatSystem#getClient}).
	 */
	public ChatUser getUser(String name) {
		for(ChatClient c : clients)
			if(	c.getUser() != null && c.getUser().getName() != null &&
				c.getUser().getName().equals(name)
			) {
				return c.getUser();
			}
		return null;
	}

	/** Gets the Global Permissions, if any; global permissions are per-role permissions
	 * which override the default group ones.
	 */
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

	/** If ChatClient with name 'old' is found, it is renamed to 'newn';
	 * this method does NOT check for duplicate names, nor it does
	 * notify other clients of the change, both of which must be done
	 * at a higher level.
	 */
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

	/** @return The number of users currently registered on this ChatSystem. */
	public int connectedUsers() { return clients.size(); }

	private List<ChatClient> clients = new LinkedList<>();
	private DatabaseServer server;
	private Map<String,ChatUser.Role> registered = new HashMap<>();
	private Map<ChatUser.Role,Map<ChatUser.Permission,Boolean>> globalPermissions = 
		new EnumMap<ChatUser.Role,Map<ChatUser.Permission,Boolean>>(ChatUser.Role.class);
}
