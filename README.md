Poképon
====================================================
<a>http://inle.freeserver.me/pokepon/</a>

Poképon is a multiplayer Pokémon-like battle simulator with ponies.

The game consists mainly of a Server, a Client and a Teambuilder (plus several
minor utilities); in order to play, a player must host a Poképon Server,
to which several players can connect with their Clients and battle each other.

Download
-----------------------------------------------------
If you just want the executable JAR file, use this download link:
<a href='http://inle.freeserver.me/pokepon/downloads/pokepon-0.01.jar'>Poképon 0.01</a>
(warning: the pre-packaged game probably won't be the latest version).

Launching the game
-----------------------------------------------------
Being written in Java, Poképon is compatible with any Java-endued OS, like
Linux, Windows and MacOS.
The simplest way to launch the game is to double-click pokepon.jar,
or to type in some terminal <code>java -jar /path/to/pokepon.jar</code>.
In case the game doesn't start upon double-clicking, try right-clicking
it and select "Open with Java JDK 7" or something alike.
Java 1.7 or later is required to play Poképon.

To connect to a running server, click "Client" and insert the server IP
(the port is usually the default one, unless the server mantainer has
decided otherwise).

<table>
  <tr>
    <th colspan=2>List of public Poképon Servers</th>
  </tr>
  <tr>
    <th>Server IP</th>
    <th>Mantainer</th>
  </tr>
  <tr>
    <td>lyrawearspants.com</td>
    <td>RedEnchilada</td>
  </tr>
  <tr>
    <td colspan=2><em><small>Want to host a server? Feel free <br>
    to <a href='mailto:silverweed1991@gmail.com'>contact me</a>!</small></em></td>
  </tr>
</table>

Building
-----------------------------------------------------
You don't need to build anything if you just wanna play, but in case you
want to do some experimentation, get a JDK 1.7 or later and ensure that
the pokepon parent directory is in your CLASSPATH environment variable.
<code>javac "@files.txt"</code> or <code>make</code> (if you have Make
installed of course) can be used to compile all the game classes.

An utility <code>create_pokepon_jar.sh</code> can be found in data/
but can only be used within a Unix shell. The script should be launched
from the parent directory of the pokepon root. For example:
<pre>$ pwd
/path/to/pokepon
$ cd ..
$ ls
pokepon/
$ bash ./pokepon/data/create_pokepon_jar.sh
... (some output from jar)
$ ls
pokepon/   pokepon.jar
</pre>

Server Setup
-----------------------------------------------------
The server should be able to run out of the box, but some configuration may be
required or desirable.

To start a server, open the game and select the "Server" option in the
graphical launcher; a configuration window will open, but usually you can
simply click "OK" without touching it (the only thing you may be forced to
change is the server IP, in case the game cannot guess the correct one).

As an alternative, the server can be started in batch mode via the command
<code>java -jar /path/to/pokepon.jar server [opts]</code>

(The flag <code>-h</code> can be used to obtain a quick summary of the server
options).

If the server was started from the JAR package, the configuration file
it will use can be found in:
* Linux / Mac
<pre>~/.pokepon/server.conf</pre>

* Windows
<pre>%APPDATA%/pokepon/server.conf</pre>

Else it will use <code>pokepon/net/server.conf</code>.

The configuration file itself contains explanation about the possible options.

The server will use a file as a database to store nick / password pairs (the
passwords are never saved in plain text, but are pre-hashed by the client
and then hashed again with a random salt by the server, so don't attempt to
change them manually: if you need to change an user's password, delete the
record altogether and re-register).
If the server was started from the JAR package, the database file can be found in
the same directory as the conf file (<code>~/.pokepon/server.db</code> or
</code>%APPDATA%/pokepon/server.db</code>), else it will reside in 
<code>pokepon/data/server.db</code>).

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
<pre>git clone https://github.com/silverweed/pokepon.git</pre>
* Set the CLASSPATH environment variable to the directory containing the pokepon
root directory. E.g. if you downloaded the repository in /home/me/, do:
<pre>export CLASSPATH=/home/me  # only valid on *nix systems</pre>
* Start the server <b>as an unprivileged user</b> with:
<pre>java pokepon.net.jack.server.PokeponServer [opts]</pre>

<b>Tips</b>
* Use the default port: it'll be easier for clients to connect to your server if they
don't have to remember a custom port.
* To keep a server log (on *nix):
<pre>java pokepon.net.jack.server.PokeponServer [opts] &> server.log
-OR-
java pokepon.net.jack.server.PokeponServer [opts] |& tee server.log
</pre>
* It's not recommended setting the <code>max-clients</code> option to a higher value than the default one
* Isolate the server as much as possible: being in alpha stage, there may be security issues or unknown
server exploits. Using a Virtual Machine or <a href='https://docker.com'>a Docker container</a> is a
good security measure. For the latter, a Dockerfile for a PokeponServer is available in <code>data/docker</code> 
(see the instruction in data/docker/README.md)


How to connect
--------------------------------------------------------
To connect to a running Poképon Server start the Client, either from
the graphical launcher or issuing the command 
<code>java -jar /path/to/pokepon.jar client &lt;serverIP&gt;</code>

The client is effectively a chat endued with some extra features, most notably
a Teambuilder, which can be used to create, save, load and edit teams.

The teams are saved as text files in the following directory:
* Linux / Mac
<pre>~/.pokepon/teams</pre>

* Windows
<pre>%APPDATA%/pokepon/teams</pre>

These text files can be safely edited as long as you use the correct syntax
(which you can infer from an auto-generated save file). If you like the Vim
editor, syntax highlighting files are available in the game repository, 
under data/vim.

Protip: the <code>/help</code> command can be used to obtain a list of the
available chat commands. 

Battling
----------------------------------------------------------
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

Creating custom formats
------------------------------------------------------------
If you select Custom format for a battle, you'll be prompted to insert rules
specifications in a text area.
At the moment, this interface is not very user-friendly, but it's not too
complicated either:

a 'rule' is a line with the format <code>X:name</code>, where 'X' is a letter
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
Twilight Sparkle, just add the line <code>p:Twilight Sparkle</code> followed by
a newline. Use the exact number of spaces in the pony/item/etc's name.

Banning a 'combo' means you specify a set of 'simple rules' (i.e. a rule using one
of the first 4 letters), and their intersection gets banned. For instance, you may
want to prevent players to use Princess Celestia with the move Friendship Cannon.
The format of a combo restriction rule is:
<pre>c:{X:name, Y:name, ...}</pre>
In the case described above, you should then insert the line: 
<code>c:{p:Princess Celestia, m:Friendship Cannon}</code>

A 'special format' is a predefined rule which cannot be described by the simple
rules. The available special formats are:
* S:speciesclause  - disallows duplicate ponies in the same team
* S:canon  - disallows non-canon ponies (ponies not appearing in the show)
* S:monotype  - forces all ponies in a team to share at least 1 type
* S:itemclause  - disallows using the same item on more than 1 pony in a team.

You can use as many rules as you wish, and the union of them will be applied to
the custom match.

Reporting bugs
-----------------------------------------------------------------
Since this is a pre-alpha release, I expect many bugs (some of which are already known,
see TODO); if you find a new one, you're encouraged to report it in the 'issues'
section on GitHub (https://github.com/silverweed/Pokepon/issues). If you don't
want to get a GitHub account, please mail me at silverweed1991@gmail.com.

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
  <a href='https://github.com/zarel/Pokemon-Showdown'>Pokémon Showdown</a>
* Other credits are reported in data/credits.txt
* Special thanks to Bram Moolenaar for creating Vim, which I used for
  writing the entire Poképon code.

Author & Mantainers
-----------------------------------------------------------------
* Author: silverweed [silverweed]
* Public server hosting: RedEnchilada
