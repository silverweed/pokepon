Advanced Chat System
===================================================

When starting the Poképon Server, you have the option to enable the
"advanced chat system" with the `-C` or `--advanced-chat` option.

This option enhances the server chat with a roles and permissions system,
which allows differentiating clients' capabilities in the chat.

Chat Roles
---------------------------------------------------
There are (currently) three chat roles:  
1. USER  
2. MODERATOR  
3. ADMIN  

By default, *USERs* have the thinnest set of permissions, while *ADMIN* has 
the most extended (usually, all permissions).

### Permissions List
Here is the full list of permissions defined by the Chat System:

| Users defaults             |
| -------------------------- |
| CAN_TALK                   |
| CAN_WHISPER                |
| CAN_CHANGE_NICK            |
| CAN_ISSUE_COMMANDS         |
| CAN_REGISTER               |
| CAN_LIST_REGISTERED_USERS  |
| CAN_LIST_ROLES             |
| CAN_VIEW_SERVER_INFO       |

| Moderators defaults        |
| -------------------------- |
| *(all users defaults)*     |
| CAN_LOOKUP_IP              |
| CAN_LOOKUP_BANNED_IP       |
| CAN_LOOKUP_PERMISSIONS     |
| CAN_BAN_IP                 |
| CAN_KICK_USERS             |
| CAN_MUTE_UNMUTE_USERS      |
| CAN_IGNORE_FLOOD_LIMIT     |

| Admins defaults            |
| -------------------------- |
| *(all mods defaults)*      |
| CAN_MUTE_UNMUTE_MODERATORS |
| CAN_MUTE_UNMUTE_ADMINS     |
| CAN_KICK_MODERATORS        |
| CAN_KICK_ADMINS            |
| CAN_PROMOTE_TO_MODERATOR   |
| CAN_PROMOTE_TO_ADMIN       |
| CAN_DEMOTE_MODERATORS      |
| CAN_DEMOTE_ADMINS          |
| CAN_MANIPULATE_DB          |

Changing default chat permissions
-------------------------------------------------------------------
You can manipulate the default roles' permissions using the chat configuration file.
If you launched the game from the JAR, it will be in
* Linux / Mac
```
~/.pokepon/chat.conf
```
* Windows
```
%APPDATA%/pokepon/chat.conf
```

Else, it will be in `<pokepon root dir>/net/chat.conf`. If you can't find the file, you
can simply create a new one in the expected place.

The conf file consists of a series of "stanzas" where you can define roles' permissions;
a stanza has the format
```
@role
  * PERMISSION_ONE
  * PERMISSION_TWO
  [...]
```
-OR-
```
+role
  + PERMISSION_TO_ADD
  - PERMISSION_TO_REMOVE
```

In the first format, the specified role will lose ALL default permissions and only have the
ones you define in that stanza; e.g. if you write this:
```
@user
  * CAN_TALK
  * CAN_WHISPER
```

ALL the users in your server will only be able to send messages and whisper with each other.

The second format allows you to extend a predefined set of permissions, typically the default
one (see [Permissions List](#permissions-list)), by adding or removing permissions to it.
For example, if you don't want your moderator to be able to ban IPs, put this stanza in the conf:
```
+moderator
  - CAN_BAN_IP
```

Handling permissions at runtime
----------------------------------------------------------------
If you have the `CAN_PROMOTE_TO_*` or `CAN_DEMOTE_*` permissions, you'll be able to change
user's roles without restarting the server. Those changes, however, are only applied
*until the server restarts*, so if you want to make a change permanent see 
[the paragraph below](#setting-permanent-roles).

The commands for this is `/setrole <user> <role>`. 

Setting permanent roles
----------------------------------------------------------------
When the server starts up, it reads its database file for any defined permanent roles.
The roles are stored in the *third column* of the `server.db` file, next to the users'
password hash, in the format of a 'role symbol':
* '@' means 'admin'
* '+' means 'moderator'
* the null character ('\000') means 'user'

To change a registered user's role permanently, you need to manually change the database
file and set the correct role symbol at the desired line.
If you want to make the user 'foo' a moderator, open the server database file, find the
line starting with 'foo', which will look like:
```
foo	1000:e6a72ca2dcc23e5dc1bd72fd6d248e7b1b608cfb9261611d:3c38974ac2fbc0caf624a675bd6848a66ea4db20946ca1ab 
```
and simply add a '+' at the end of it (separated by at least one whitespace from the hash!):
```
# foo is now a moderator
foo	1000:e6a72ca2dcc23e5dc1bd72fd6d248e7b1b608cfb9261611d:3c38974ac2fbc0caf624a675bd6848a66ea4db20946ca1ab +
```

### Tip
You can set permanent roles at runtime by modifying the server db while the server is up; once the edit is done,
close the db file and issue the `/reload` command in the chat. If the user 'foo' is already logged in, he/she
can obtain the moderator status by `/nick foo <password>`.
