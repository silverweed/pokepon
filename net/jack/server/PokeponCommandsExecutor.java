//: net/jack/server/PokeponCommandsExecutor.java

package pokepon.net.jack.server;

import pokepon.net.jack.*;
import pokepon.battle.*;
import pokepon.util.*;
import static pokepon.util.MessageManager.*;
import java.util.*;
import java.io.*;
import java.net.*;

/** This class extends the normal CommandsExecutor implementing pokepon-specific
 * commands.
 *
 * @author silverweed
 */
class PokeponCommandsExecutor extends CommandsExecutor {

	protected PokeponServer pServer;
	protected DataDealer dataDealer = new DataDealer();

	static {
		help.append(CMD_PREFIX+"battle <user> - request a battle vs an user.\n");
		help.append(CMD_PREFIX+"btldel <user> - delete battle request vs an user.\n");
		help.append(CMD_PREFIX+"battles - list battle schedule.\n");
		help.append(CMD_PREFIX+"data <name> - show info about pony/move/item/ability.\n");
		help.append(CMD_PREFIX+"eff <type>[,type2] - show typechart for a type.\n");
		help.append(CMD_PREFIX+"eff <typeA> -> <typeB>[,typeB2]  - show effectiveness of typeA atk vs typeB defense.\n");
	}

	@Override
	public int execute(String msg) {

		if(!(server instanceof PokeponServer)) {
			if(connection.getVerbosity() >= 2) printDebug("server is not a PokeponServer: PokeponCommandsExecutor returning 0.");
			return 0;
		}
		pServer = (PokeponServer)server;

		if(connection.getVerbosity() >= 3) printDebug("Called PokeponCommandsExecutor (msg="+msg+")");
		
		if(msg.charAt(0) != CMD_PREFIX) return 0;

		String[] token = msg.substring(1).split(" ");
		String cmd = token[0];
		
		if(connection.getVerbosity() >= 3) printDebug("cmd="+cmd+",token="+Arrays.asList(token));

		if(cmd.equals("battle")) {
			if(token.length != 2) {
				connection.sendMsg("Syntax error. Correct syntax is "+CMD_PREFIX+"battle <username>.");
				return 1;
			}
			if(token[1].equals(connection.getName())) {
				connection.sendMsg("You cannot challenge yourself to battle.");
				return 1;
			}
			for(Connection conn : server.getClients()) {
				if(conn.getName().equals(token[1])) {
					// send available formats with the !selectteam message
					StringBuilder sb = new StringBuilder(CMN_PREFIX+"selectteam ");
					for(Format fmt : pServer.getAvailableFormats())
						sb.append(fmt.getName().replaceAll(" ","") + " ");

					Format format = null;
					try {
						connection.getSocket().setSoTimeout(120000);
						connection.sendMsg(sb.toString());
						String response = "";
						format = RuleSet.Predefined.DEFAULT;
						response = connection.getInput().readLine();
						if(response == null || !response.startsWith(CMN_PREFIX+"ok")) {
							if(connection.getVerbosity() >= 1)
								printDebug("Battle request "+connection.getName()+"->"+token[1]+" aborted (team not selected).");
							connection.sendMsg("Aborted battle request to "+token[1]+" (team not selected).");
							return 1;
						} 
						// parse format (response is !ok @Format OR !ok @Custom: p:bannedPony$i:bannedItem...)
						String[] respToken = response.split(" ");
						if(respToken.length > 1) {
							if(respToken[1].equals("@Custom:")) {
								String[] rules = ConcatenateArrays.merge(respToken,2).split("\\$");
								format = new RuleSet("Custom",rules);
							} else {
								for(RuleSet.Predefined pre : RuleSet.Predefined.values()) {
									if(respToken[1].equals(pre.getName().replaceAll(" ",""))) {
										format = pre.getRuleSet();
										break;
									}
								}
							}
						}
					} catch(SocketTimeoutException e) {
						printDebug("[PKPCMDEXEC] Timeout with "+connection);
						connection.sendMsg("Aborted battle request to "+token[1]+" (Timeout).");
						pServer.dismissBattle(connection.getName(),token[1]);
						return 1;
					} catch(IOException e) {
						printDebug("[PKPCMDEXEC] Caught exception while reading line from socket "+connection.getSocket());
						e.printStackTrace();
						connection.sendMsg("Aborted battle request to "+token[1]+" (IOException).");
						pServer.dismissBattle(connection.getName(),token[1]);
						return 1;
					} finally {
						try {
							connection.getSocket().setSoTimeout(0);
						} catch(Exception e) {
							e.printStackTrace();
							return 1;
						}
					}
					pServer.scheduleBattle(connection,conn,format);
					connection.sendMsg("Sent battle request to "+conn.getName());
					return 1;
				}
			}
			connection.sendMsg("User "+token[1]+" not found.");
			return 1;
		} else if(cmd.equals("acceptbtl")) {
			if(token.length < 2) {
				if(connection.getVerbosity() >= 2) 
					printDebug("[PKPCMDEXEC ("+connection.getName()+")] Received /acceptblt with no argument!");
				return 1;
			}
			if(pServer.getBattleSchedule().containsKey(token[1])) {
				connection.sendMsg(CMN_PREFIX+"selectteam @"+
						pServer.getBattleSchedule().getFormat(token[1],connection.getName()).getName().replaceAll(" ",""));
				String response = "";
				try {
					response = connection.getInput().readLine();
					if(response == null || !response.startsWith(CMN_PREFIX+"ok")) {
						if(connection.getVerbosity() >= 1)
							printDebug("Battle request "+connection.getName()+"->"+token[1]+" aborted (team not selected).");
						connection.sendMsg("You declined "+token[1]+"'s battle request.");
						pServer.dismissBattle(connection.getName(),token[1]);
						return 1;
					} 
				} catch(IOException e) {
					printDebug("Caught exception while reading line from socket "+connection.getSocket());
					e.printStackTrace();
					connection.sendMsg("Aborted battle request to "+token[1]+" (IOException).");
					pServer.dismissBattle(connection.getName(),token[1]);
					return 1;
				}
				pServer.scheduleBattle(connection,pServer.getClient(token[1]),null);
			} else {
				if(connection.getVerbosity() >= 2)
					printDebug("[PKPCMDEXEC ("+connection.getName()+")] Tried to accept non-existing battle with "+token[1]);
			}
			return 1;
		} else if(cmd.equals("btldel")) {
			if(token.length != 2) {
				connection.sendMsg("Syntax error. Correct syntax is "+CMD_PREFIX+"btldel <username>.");
				return 1;
			}
			for(Connection conn : server.getClients()) {
				if(conn.getName().equals(token[1])) {
					if(pServer.dismissBattle(connection,conn)) {
						connection.sendMsg("Battle with "+token[1]+" dismissed.");
					} else {
						connection.sendMsg("You hadn't pending battle requests with "+token[1]+".");
					}
					return 1;
				}
			}
			connection.sendMsg("User "+token[1]+" not found.");
			return 1;
		} else if(cmd.equals("battles")) {
			if(token.length > 1) {
				connection.sendMsg("Syntax error. Correct syntax is "+CMD_PREFIX+"battles.");
				return 1;
			}
			StringBuilder sb = new StringBuilder("-- Battle schedule:\n");
			PokeponServer.BattleSchedule bs = pServer.getBattleSchedule();
			if(connection.getVerbosity() >= 3) printDebug("bs: "+bs);
			int ongoing = 0, requested = 0;
			// battleSchedule should contain only requested battles
			if(bs.size() != 0) {
				for(Map.Entry<String,List<Map.Entry<String,Format>>> entry : bs.entrySet()) {
					if(entry.getKey() == null || entry.getValue() == null) 
						continue;
					for(Map.Entry<String,Format> lEntry: entry.getValue()) {
						sb.append(entry.getKey()+" -> "+lEntry.getKey()+" (Format: "+lEntry.getValue()+")\n");
						++requested;
					}
				
			}
			}
			// we take ongoing battles directly from server.getBattles()
			Map<String,BattleTask> btls = pServer.getBattles();
			if(btls.size() != 0) {
				for(Map.Entry<String,BattleTask> entry : btls.entrySet()) {
					sb.append("[battle#"+entry.getKey()+"] "+
						entry.getValue().getConnection(1)+" <=> "+entry.getValue().getConnection(2)+
						" (format: "+entry.getValue().getFormat()+")\n");
						++ongoing;
				}
			}

			if(requested == 0 && ongoing == 0) {
				sb.append("<no battle scheduled>.\n");
			} else {
				sb.append("-------------------------\n"+
					ongoing+" battles active\n"+
					requested+" battle requests pending\n");
			}
			connection.sendMsg(sb.toString());
			return 1;
		} else if(cmd.equals("data")) {
			if(token.length < 2) {
				connection.sendMsg("Syntax error. Correct syntax is "+CMD_PREFIX+"data <query>.");
				return 1;
			}
			String data = dataDealer.getData(ConcatenateArrays.merge(token,1));
			if(data == null) 
				connection.sendMsg(ConcatenateArrays.merge(token,1)+": no data found.");
			else 
				connection.sendMsg(CMN_PREFIX+"htmlconv "+data);
			return 1;
		} else if(cmd.equals("eff")) {
			if(token.length < 2) {
				connection.sendMsg("Syntax error. Correct syntax is one of:<br>&nbsp;"+
					CMD_PREFIX+"eff <type1[,type2]><br>&nbsp;"+
					CMD_PREFIX+"eff <type1> -> <type2[,type3]>");
				return 1;
			}
			String data = dataDealer.getEffectiveness(ConcatenateArrays.merge(token,1));
			if(data == null) 
				connection.sendMsg(ConcatenateArrays.merge(token,1)+": no data found.");
			else 
				connection.sendMsg(CMN_PREFIX+"htmlconv "+data);
			return 1;
		} 	
		else return super.execute(msg);
	}
}		
