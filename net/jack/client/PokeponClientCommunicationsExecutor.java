//: net/jack/client/PokeponClientCommunicationsExecutor.java

package pokepon.net.jack.client;

import pokepon.net.jack.*;
import pokepon.net.jack.chat.*;
import pokepon.util.*;
import pokepon.player.Team;
import static pokepon.util.MessageManager.*;
import javax.swing.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;

/** A Pokepon-specific ClientCommunicationsExecutor 
 *
 * @author Giacomo Parolini
 */

class PokeponClientCommunicationsExecutor extends ChatClientCommunicationsExecutor {

	protected PokeponClient pClient;

	@Override
	public int execute(String msg) {

		if(!(client instanceof PokeponClient)) return 0;
		pClient = (PokeponClient)client;

		if(connection.getVerbosity() >= 3) printDebug("Called PokeponClientCommunicationExecutor (msg="+msg+")");
		
		if(msg.charAt(0) != CMN_PREFIX) return 0;
		
		String[] token = msg.substring(1).split(" ");
		String cmd = token[0];
		
		if(connection.getVerbosity() >= 3) printDebug("cmd="+cmd+",token="+Arrays.asList(token));

		if(cmd.equals("btlreq")) {
			/* !btlreq <clientname> */
			if(token.length != 2) return 1;
			int choice = JOptionPane.showConfirmDialog(pClient,
				token[1]+" challenged you!\nAccept?",token[1]+" challenged you to a battle!",JOptionPane.YES_OPTION);	
			if(choice == JOptionPane.YES_OPTION) {
				connection.sendMsg(CMD_PREFIX+"acceptbtl "+token[1]);
				pClient.append("You accepted the battle with "+token[1]+"; please select a team.");
				return 1;
			} else {
				connection.sendMsg(CMD_PREFIX+"btldel "+token[1]);
				pClient.append("You declined "+token[1]+"'s battle request.");
				return 1;
			}
		} else if(cmd.equals("selectteam")) {
			/* !selectteam Format1 Format2 ... OR !selectteam @FixedFormat
			 * (first case is for challenger, second for challenged)
			 * formats order is decided by the server.
			 */
			Set<String> formats = new LinkedHashSet<>();
			for(int i = 1; i < token.length; ++i)
				formats.add(token[i]);
			if(connection.getVerbosity() >= 2) printDebug("Selecting team");

			if(formats.size() == 1 && formats.toArray(new String[0])[0].startsWith("@")) {
				// this is the challenged player
				if(pClient.showTeamChoiceDialog(formats.toArray(new String[0])[0].substring(1)))
					connection.sendMsg(CMN_PREFIX+"ok" + (pClient.getFormat() != null ? " "+pClient.getFormat() : ""));
				else
					connection.sendMsg(CMN_PREFIX+"abort");
			} else {
				// this is the challenger
				if(pClient.showTeamChoiceDialog(formats)) {
					connection.sendMsg(CMN_PREFIX+"ok" + (pClient.getFormat() != null ? " "+pClient.getFormat() : ""));
				}
				else
					connection.sendMsg(CMN_PREFIX+"abort");
			}
			return 1;
		
		} else if(cmd.equals("sendteam")) {
			if(pClient.getTeam() == null) 
				connection.sendMsg(CMN_PREFIX+"null");
			else {
				connection.sendMsg(CMN_PREFIX+"team"); //first message is parsed by PokeponCommunicationsExecutor
				connection.sendMsg(CMN_PREFIX+"beginteam"); // others from TeamRetreiver
				Team team = pClient.getTeam();
				for(String data : team.getTeamData()) {	
					for(String line : data.split("\n")) {
						connection.sendMsg(CMN_PREFIX+"te "+line);
					}
				}
				connection.sendMsg(CMN_PREFIX+"endteam");
			}
			return 1;
		} else if(cmd.equals("spawnbtl") || cmd.equals("watchbtl")) {
			/* |spawnbtl <id> <format> [bgNum] [bgmNum] */
			if(token.length < 3) return 1;
			String id = token[1], format = token[2];
			float bgNum = -1, bgmNum = -1;
			boolean asGuest = cmd.equals("watchbtl");
			if(token.length > 3) {
				try {
					bgNum = new Float(token[3]);
				} catch(NumberFormatException e) {
					printDebug("Invalid bgNum received from server: "+token[3]);
				}
				if(token.length > 4) {
					try {
						bgmNum = new Float(token[4]);
					} catch(NumberFormatException e) {
						printDebug("Invalid bgmNum received from server: "+token[4]);
					}
				}
			}
			pClient.spawnBattle(id, format, asGuest, bgNum, bgmNum);
			return 1;
		} else if(cmd.equals("btlko")) {
			/* !btlko <id> [msg] */
			if(token.length < 2) return 1;
			pClient.append("Couldn't start battle #" + token[1] + (token.length > 2 ? ": "+ConcatenateArrays.merge(token,1) : "."));
			pClient.getBattles().remove(token[1]);
			return 1;

		} else if(cmd.equals("htmlconv")) {
			/* !htmlconv <msg> */
			if(token.length < 2) return 1;
			pClient.append(Meta.toLocalURL(ConcatenateArrays.merge(token,1)),false);
			return 1;

		} else if(cmd.equals("html") || cmd.equals("motd")) {
			/* !html <msg> */
			if(token.length < 2) return 1;
			pClient.append(ConcatenateArrays.merge(token,1),false);
			return 1;
		}
		else return super.execute(msg);
	}
}
