Poképon
====================================================
Poképon is a multiplayer Pokémon-like battle simulator with ponies.

[Click here](https://pokepon.center/playing.html) for a TL;DR quick start:
following are more detailed and technical information on the project.

The game consists mainly of a Server, a Client and a Teambuilder (plus several
minor utilities); in order to play, a player must host a Poképon Server,
to which several players can connect with their Clients and battle each other.

Download
-----------------------------------------------------
If you just want the executable JAR file, use this download link:
[Poképon 0.01](https://pokepon.center/downloads/pokepon.jar)
(warning: the pre-packaged game probably won't be the latest version).

Launching the game
-----------------------------------------------------
Poképon is compatible with any OS supporting a JVM, like
Linux, BSD, Windows and MacOS.
The simplest way to launch the game is to double-click pokepon.jar,
or to type in some terminal `java -jar /path/to/pokepon.jar`.
In case the game doesn't start upon double-clicking, try right-clicking
it and select "Open with Java JDK 7" or something alike.
Java 1.7 or later is required to play Poképon.

To connect to a running server, click "Client" and insert the server IP
(the port is usually the default one, unless the server mantainer has
decided otherwise).

Here is a list of public Poképon Servers to which anyone can connect:
*none: they all ded, lol*

Building
-----------------------------------------------------
If you want the bleeding-edge version of the game (beware: it won't necessarily be
stable), or in case you want to do some experimentation, get a JDK 1.7 or later and ensure that
the pokepon parent directory is in your CLASSPATH environment variable.
To compile all the game classes, do `javac "@files.txt"` or, if you have Make
installed, `make`.

An utility `create_pokepon_jar.sh` can be found in `data/` to create a
new JAR from the compiled classes, but requires Bash. The script should be launched
from the parent directory of the pokepon root. For example:
```bash
$ pwd
/path/to/pokepon
$ cd ..
$ ls
pokepon/
$ bash ./pokepon/data/create_pokepon_jar.sh
... (some output from jar)
$ ls
pokepon/   pokepon.jar
```

How to connect
--------------------------------------------------------
There are basically 2 ways to play Poképon:

  1. By connecting to a public server
  2. By creating a local server and use it to battle locally (either in LAN or not)

(See paragraph "Server Setup" for information on the second option.)

To connect to a running Poképon Server start the Client, either from
the graphical launcher or issuing the command 
```bash
java -jar /path/to/pokepon.jar client <serverIP>
```

The client is effectively a chat endued with some extra features, most notably
a Teambuilder, which can be used to create, save, load and edit teams.

The teams are saved as text files in the following directory:
* Linux / Mac
```
~/.pokepon/teams
```

* Windows
```
%APPDATA%/pokepon/teams
```

These text files can be safely edited as long as you use the correct syntax
(which you can infer from an auto-generated save file). If you like the Vim
editor, syntax highlighting files are available in the game repository, 
under data/vim.

Protip: the `/help` command can be used to obtain a list of the
available chat commands. 

Battling
----------------------------------------------------------
*Hint: typechart is [here](https://github.com/silverweed/pokepon/blob/master/enums/README.md)*

You can challenge other players connected to your server in 3 ways:
* simply click on the player's name in the right panel of the chat and
  select 'Challenge'

* Use the Challenge button on the left panel and insert the other player's
  name

* Issue the chat command /battle &lt;player's name&gt;

Unless you select the Random Battle format, you'll be prompted to select a
team; you cannot challenge other players to formats other than Random Battle
if you don't have any team.
Be aware that your team and your opponent's will be validated from the server
before starting a battle, so check the rules of the format you're going to use.

If you want to bypass any format restriction, just choose the 'Custom' format
and don't specify any rule.

### Guests ###
You can watch an ongoing battle as a guest. At the moment, the only way to do
so is via the chat command `/watch <battleID>`, like this:  

1. in the chat, issue `/battles`. You'll get an output like this:
```
-- Battle schedule: 
[battle#0] blank_flank-1 <=> blank_flank-2 (format: Classic)
[battle#1] blank_flank-3 <=> blank_flank-4 (format: RandomBattle)
------------------------- 
1 battles active 
0 battle requests pending
```  
2. choose the battle you'd like to watch: its battleID is the number after the `#`
   symbol: `[battle#0]  -> the ID of this battle is 0`.
3. issue the chat command `/watch 0`

### Exporting and replaying battles ###
At any time during a battle, whether you're a player or a guest, you can export
a battle log with `/save`, or `/export` (you must have the `Enable battle
logging` option enabled, which is the default).

The battle log will be saved as a file, either in `~/.pokepon/battle_records/`
(on Linux or Mac), or in `%APPDATA%/pokepon/battle_records` on Windows. 
If you're using the unpackaged version of the game, it will be instead in
`(pokepon rootdir)/data/battle_records`.

You can replay a saved battle from the launcher by clicking the Replay button
and selecting the battle record you exported.

Default battle formats
------------------------------------------------------------
These are the battle formats available by default:  
* **Classic** - "uber" ponies and moves are banned; species clause
* **RandomBattle** - species clause, teams are randomized (levels are balanced for both teams)
* **Monotype** - all rules of Classic, plus monotype clause: all ponies in team must share at least 1 type
* **SpeciesClauseOnly** - no banned ponies/moves/etc, but species clause enabled
* **MonotypeOnly** - only monotype clause
* **NoUberOnly** - ubers are banned, but no species clause
* **ItemClauseOnly** - item clause: only 1 copy of each item is allowed in the team
* **Default** - no restrictions
* **Custom** - see *Creating custom formats*

(You can see the rules of each format by clicking the `?` button aside the drop-down menu).

Creating custom formats
------------------------------------------------------------
If you select Custom format for a battle, you'll be prompted to insert rules
specifications in a text area.
At the moment, this interface is not very user-friendly, but it's not too
complicated either:

a 'rule' is a line with the format `X:name`, where 'X' is a letter
specifying what kind of restriction you're applying (whether you're banning
a pony, a move, an item, an ability, a combo or if you're specifying a special
format), and 'name' is the name of the pony/item/etc you're banning.
The letters are as follows:
* p: ban a pony
* m: ban a move
* i: ban an item
* a: ban an ability
* c: ban a combo (see later)
* S (or nothing): specify a special format (see later)

Using one of the first 4 letters is immediate: if, for instance, you want to ban
Twilight Sparkle, just add the line `p:Twilight Sparkle` followed by
a newline. Use the exact number of spaces in the pony/item/etc's name.

Banning a 'combo' means you specify a set of 'simple rules' (i.e. a rule using one
of the first 4 letters), and their intersection gets banned. For instance, you may
want to prevent players to use Princess Celestia with the move Friendship Cannon.
The format of a combo restriction rule is:
`c:{X:name, Y:name, ...}`
In the case described above, you should then insert the line: 
`c:{p:Princess Celestia, m:Friendship Cannon}`

A 'special format' is a predefined rule which cannot be described by the simple
rules. The available special formats are:
* S:speciesclause  - disallows duplicate ponies in the same team
* S:canon  - disallows non-canon ponies (ponies not appearing in the show)
* S:monotype  - forces all ponies in a team to share at least 1 type
* S:itemclause  - disallows using the same item on more than 1 pony in a team.

You can use as many rules as you wish, and the union of them will be applied to
the custom match.

Server Setup
-----------------------------------------------------
You may wish to launch a Poképon Server on your own machine, either to
do a local battle on-the-fly (this is possible even if you're not connected
to the Internet, as long as you have some way to communicate with the other
players) or to host a dedicated server.

The server should be able to run out of the box, but some configuration may be
required or desirable.

To start a server, open the game and select the "Server" option in the
graphical launcher; a configuration window will open, but usually you can
simply click "OK" without touching it (the only thing you may be forced to
change is the server IP, in case the game cannot guess the correct one).

As an alternative, the server can be started in batch mode via the command
```bash
java -jar /path/to/pokepon.jar server [opts]
```

(The flag `-h` can be used to obtain a quick summary of the server
options).

If you're using the unpackaged version of the game, the script `run.sh` can
be used as a shortcut launcher on \*nix environments.

If the server was started from the JAR package, the configuration file
it will use can be found in:
* Linux / Mac
```
~/.pokepon/server.conf
```

* Windows
```
%APPDATA%/pokepon/server.conf
```

Else it will use `pokepon/net/server.conf`.

The configuration file itself contains explanation about the possible options.

The server will use a file as a database to store nick / password pairs (the
passwords are never saved in plain text, but are pre-hashed by the client
and then hashed again with a random salt by the server, so don't attempt to
change them manually: if you need to change an user's password, delete the
record altogether and re-register).
If the server was started from the JAR package, the database file can be found in
the same directory as the conf file (`~/.pokepon/server.db` or
`%APPDATA%/pokepon/server.db`), else it will reside in 
`pokepon/data/server.db`).

Both the configuration file and the database will be recreated every time
the server cannot find them in the expected locations, so if you want a
fresh conf file or an empty db, you can delete or move those files and
have them recreated.

Running a dedicated server
--------------------------------------------------------
If you plan running a dedicated server, or a server that will host
several (read: more than a couple of) clients, you should use the unpackaged
version of the game, which is slightly faster accessing Java classes than the
JAR. In that case, these are the instructions to follow:
* Download the game, either the Zip archive or using Git. The latter is preferred,
since it'll be much easier to grab the updates:
```bash
git clone https://github.com/silverweed/pokepon.git
```
* Set the CLASSPATH environment variable to the directory containing the pokepon
root directory. E.g. if you downloaded the repository in /home/me/, do:
```bash
export CLASSPATH=/home/me  # only valid on *nix systems
```
* Compile the package (you'll need JDK 1.7 or later):
```bash
javac "@files.txt" # or 'make', if you have it installed.
```
* Start the server <b>as an unprivileged user</b> with:
```bash
java pokepon.net.jack.server.PokeponServer [opts]
```

### Tips
* Use the default port: it'll be easier for clients to connect to your server if they
don't have to remember a custom port.
* To keep a server log (on \*nix):
```bash
java pokepon.net.jack.server.PokeponServer [opts] &> server.log
```
-OR-
```bash
java pokepon.net.jack.server.PokeponServer [opts] |& tee server.log
```
* It's not recommended setting the `max-clients` option to a higher value than the default one
* Isolate the server as much as possible: being in alpha stage, there may be security issues or unknown
server exploits. Using a Virtual Machine or [a Docker container](https://docker.com) is a
good security measure. For the latter, a Dockerfile for a PokeponServer is available in `data/docker` 
(see the instruction in [data/docker/README.md](https://github.com/silverweed/pokepon/blob/master/data/docker/README.md))

Reporting bugs
-----------------------------------------------------------------
Poképon is no longer actively maintained. You are free to submit PRs and/or fork the repository if you're interested in bugfixes.

Chat System
-----------------------------------------------------------------
For details about the embedded chat system, see 
[here](https://github.com/silverweed/pokepon/blob/master/net/jack/chat/README.md)

License
-----------------------------------------------------------------
Poképon is free software distributed under the GNU General Public License
(see COPYING for details).
The rights upon the artwork under the resources/ directory belong to their
respective owners.

Credits
-----------------------------------------------------------------
* The currently used game sprites are mostly taken from Desktop Ponies
* Several game fx are from 
  [Pokémon Showdown](https://github.com/zarel/Pokemon-Showdown)
* Some of the typings take inspiration from [Ponymon](http://ponymondawndusk.wikia.com/wiki/Ponymon_Dawn/Dusk_Wiki)
* Other credits are reported in data/credits.txt
* Special thanks to Bram Moolenaar for creating Vim, which I used for
  writing the entire Poképon code.

*Note*: if you see your artwork in the game and wish it to be removed,
or if your name does not appear in the credits and you wish it to be
added, contact me and I will (of course, you must prove you're
the owner of said artwork).

Author & Mantainers
-----------------------------------------------------------------
* Author: [silverweed](https://github.com/silverweed)
