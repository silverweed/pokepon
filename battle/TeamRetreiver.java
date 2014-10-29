//: battle/TeamRetreiver.java

package pokepon.battle;

import pokepon.pony.*;
import pokepon.player.*;
import static pokepon.util.MessageManager.*;
import pokepon.util.*;
import pokepon.net.jack.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.*;

/** This class implements the task which retreives a team from a client;
 * the call() method yields True if the team was correctly retreived and
 * False otherwise; thus it's possible to retreive teams from multiple
 * clients at the same time.
 *
 * @author Giacomo Parolini
 */
class TeamRetreiver implements Callable<Boolean> {
	
	private final Connection c;
	private final Player p;
	private Boolean result = false;
	private boolean reading = false;
	private TeamDealer teamDealer = new TeamDealer();

	public TeamRetreiver(final Connection c,final Player p) {
		this.c = c;
		this.p = p;
	}

	/** What happens:
	 * - first, Battle sends a !sendteam to both clients;
	 * - Clients' PokeponClientCommunicationExecutors respond with a !team
	 * - This !team is read by the Connections and executed by the server's
	 *   PokeponCommunicationsExecutor, which locks its Connection's reading
	 *   routine;
	 * - The team is sent by the clients, parsed and constructed by the Battle;
	 * - At the end, the Battle sends either an "~ok" or a "~ko" to the clients
	 *   and unlocks the Connections' reading.
	 */
	public synchronized void run() {
		if(Debug.on) printDebug("++++++ CALLED TEAMRETREIVER.RUN("+c.getName()+",player="+p+") +++++++");
		try {
			c.sendMsg(CMN_PREFIX+"sendteam");
			// Prevent connection to process our messages
			if(Debug.on) printDebug("["+c.getName()+"] Locking connection...");
			c.lockReading();
			String line = null;
			while((line = c.getInput().readLine()) != null) {
				printDebug(p.getID()+"- Read: "+line);
				if(reading && line.charAt(0) != CMN_PREFIX) {
					printDebug("Unexpected message: "+line);
					result = false;
					return;
				}
				line = line.substring(1);
				if(line.equals("beginteam")) {
					if(reading) {
						printDebug("Error: received team start twice.");
						result = false;
						return;
					}
					reading = true;
					continue;
				}
				if(!reading) {
					printDebug(p.getID()+"- Not reading: continuing...");
					continue;
				}
				if(line.equals("endteam")) {
					result = true;
					printDebug("Team "+p+": "+p.getTeam());
					return;
				}
				// Team entries are sent like: !te PonyNameOrCharacteristic
				if(!line.startsWith("te ")) {
					printDebug("Ignoring unexpected line: "+line);
					continue;
				}
				teamDealer.parseSaveDataLine(line.split(" ",2)[1],p.getTeam());	
			}
			// we should return via endteam
			result = false;
		} catch(IOException e) {
			printDebug("Error retreiving team: "+e);
			result = false;
		} finally {
			// return incoming messages' control to ServerConnection
			c.unlockReading();
		}
	}

	public synchronized Boolean call() {
		run();
		if(Debug.on) printDebug("["+p.getID()+"] result is "+result);
		return result;
	}
}
