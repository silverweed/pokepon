========= GAME INSTRUCTIONS FOR THE NON-BUNDLED VERSION OF POKEPON ===========
~~ by Silverweed91 (silverweed1991@gmail.com)
~~~ aka Discord on http://brony.it/forum/ and http://mylittlepony.it/forum/
~~~ other contacts:
	http://silverweed91.deviantart.com/
	https://github.com/silverweed/
~~~ game site:
	http://inle.freeserver.me/pokepon/
-----------------
TO COMPILE
-----------------
Linux / Mac:
export CLASSPATH="/path/to/directory-containing-pokepon" #NOT the pokepon directory itself, but its parent!
cd /path/to/pokepon
javac @files.txt 

(needed JDK >= 1.7)

Windows:
Same, except the CLASSPATH variable must be set from Computer->Properties->Advanced->Environment Variables

-----------------
TO EXECUTE
-----------------
Depends on which class you want to run; 

Linux / Mac:
The script run.sh may be used to launch various classes; it has the following arguments:

./run.sh <opt>

opt: LaunchedClass

s: PokeponServer
cl: PokeponClient
dex: PonyDex
type: TypeChart
t: GUITeamBuilder
ct: CLITeamBuilder

Examples:
./run.sh s [--ip <ip>]  #starts the PokeponServer 
./run.sh cl <ip>[:port] #attempts to connect the PokeponClient to server running at ip:port
./run.sh t		#starts the TeamBuilder

Windows:
The scripts *.bat may be used to quickly launch the main game classes;

All platforms:
You can also manually execute classes with
	
	java pokepon.<package>.<ClassName>

or use the launcher class:

	java pokepon.main.QuickLauncher <opt>

Use the -h flag to list the possible launchable classes.

-------------------
TO PLAY
-------------------
First, build your team. You can do it either from the Pokepon Client or using directly the
TeamBuilder class.

To launch the graphical teambuilder:
	./run.sh t		# linux/mac only
	OR
	teambuilder.bat		# windows only
	OR
	java pokepon.main.QuickLauncher tb	# all platforms
	OR
	java pokepon.player.GUITeamBuilder	# all platforms

(There is also a Command Line Teambuilder: java pokepon.player.CLITeamBuilder)

To build the team you must:
1) select a pony from the list
2) click "Save pony in team"
3) assign moves, ability, item, EVs, IVs, nickname, level, happiness
4) click the next empty slot in the team
5) goto 1 unless team is full
6) click "Save team"

(Optionally, you can give a name to your team: it can help you recognizing it among others).
You may also create a team by directly writing the save file: the file format is very easy:

# start save file (this is a comment)
$TEAM_NAME = "My Team"	# optional team name: must be the first non-comment line!

RealPonyName ~ Optional Nickname @ Optional Item
Nature: Some Nature
# all the following are optional:
Ability: Some Ability
EVs: 252 atk / 4 def / 252 speed
# if you don't put the IVs, default is all 31; else you can do like:
IVs: 30 hp / 31 atk / 31 def / 30 spatk / 31 spdef / 31 speed
# next are the moves:
- Move1
- Move2
- Move3
- Move4

NextPonyName [...]
[...]
# end save file


After building a team, you'll need a running Pokepon Server in order to play against someone else.

To start the server:
	./run.sh s		# linux / mac only
	OR
	start_server.bat	# windows only
	OR
	java pokepon.main.QuickLauncher server	# all platforms
	OR
	java pokepon.net.jack.server.PokeponServer	# all platforms

The server can be launched without any option, in which case it'll bind itself to
the local IP address at port 12344.
You may change this behaviour by passing the following options:
	-i, --ip IP_ADDRESS	set the IP address to bind the server to
	-p, --port PORT		set the port for the server to listen on
	-v VERBOSITY_LV		set the verbosity (-1 to 4)
	-m, --max-clients MAX_CLIENTS	set the maximum number of clients the server will accept
	--name SERVER_NAME	set the server name
	-d, --database	DB_URL	set the server's database URL
	--forbid REGEX1 REGEX2 [...]	add the given regexes to the names' blacklist

Each of these options may as well be given in a configuration file `pokepon/net/server.conf`, with the format:
ip: IP_ADDRESS
port: PORT
forbid: REGEX1 REGEX2
[...]


Once the server is started, you can connect to it via the Pokepon Client.

To start the client:
	./run.sh cl 		# linux / mac only
	OR
	start_client.bat	# windows only
	OR
	java pokepon.main.QuickLauncher client	# all platforms
	OR
	java pokepon.net.jack.client.PokeponClient	# all platforms

The client needs at least 1 mandatory argument, namely the server IP address. For example,
to connect to a Pokepon Server running at 192.168.1.100 at port 12344 you do:
	java pokepon.main.QuickLauncher client 192.168.1.100

If the port is not the default 12344 (say, 42424) you instead do:
	java pokepon.main.QuickLauncher client 192.168.1.100:42424
OR
	java pokepon.main.QuickLauncher client 192.168.1.100 42424

(You can replace `java pokepon.main.QuickLauncher client` with `./run.sh cl` if you run
Linux or Mac; start_client.bat will ask for the IP and the port interactively).

Once 2 or more people are connected to the server, you can issue challenges in 3 ways:
1) by clicking on the other users' nicknames and select "challenge"
2) by clicking the "challenge" button on the left and insert the nick of another user
3) by typing /battle <user> in the chat.

You'll be prompted to select a team and a battle format; your opponent will be too and,
once everything is set up, the battle will start!
