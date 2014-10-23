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
 * @author silverweed
 */

class PokeponClientCommunicationsExecutor extends ClientCommunicationsExecutor {

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

		if(cmd.equals("useradd")) {
			if(token.length < 2) return 1;
			if(token.length < 3)
				pClient.getChat().userAdd(token[1]);
			else
				pClient.getChat().userAdd(token[1],ChatUser.Role.forSymbol(token[2].charAt(0)));
			return 1;
		} else if(cmd.equals("userrm")) {
			if(token.length < 2) return 1;
			pClient.getChat().userRemove(token[1]);
			return 1;
		} else if(cmd.equals("userrnm")) {
			/* !userrnm old new [role] */
			if(token.length < 3) return 1;
			printDebug("userrnm: tokens = "+Arrays.asList(token)); 
			if(token.length > 3)
				pClient.getChat().userRename(token[1], token[2], ChatUser.Role.forSymbol(token[3].charAt(0)));
			else
				pClient.getChat().userRename(token[1],token[2]);
			return 1;
		} else if(cmd.equals("btlreq")) {
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
			// !selectteam Format1 Format2 ... OR !selectteam @FixedFormat
			// formats order is decided by the server.
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
				//FIXME? - seems to work now
				connection.sendMsg(CMN_PREFIX+"team"); //first message is parsed by PokeponCommunicationsExecutor
				connection.sendMsg(CMN_PREFIX+"beginteam"); // others from TeamRetreiver
				/*try {
					if(!connection.getInput().readLine().equals(CMN_PREFIX+"next"))
						return 1;*/	
					Team team = pClient.getTeam();
					for(String data : team.getTeamData()) {	
						for(String line : data.split("\n")) {
							connection.sendMsg(CMN_PREFIX+"te "+line);
							/*if(!connection.getInput().readLine().equals(CMN_PREFIX+"next")) {
								break;
							}*/
						}
					}
				/*} catch(IOException e) {
					printDebug("Exception while sending team: "+e);
					return 1;
				}*/
				connection.sendMsg(CMN_PREFIX+"endteam");
			}
			return 1;
		} else if(cmd.equals("drop")) {
			if(token.length > 1)
				pClient.append(ConcatenateArrays.merge(token,1));
			else
				pClient.append("Server dropped connection.");
			return -1;
		} else if(cmd.equals("spawnbtl")) {
			/* |spawnbtl [format] [bgNum] [bgmNum] */
			if(token.length < 2) return 1;
			float bgNum = -1, bgmNum = -1;
			String format = "";
			if(token.length > 2) {
				format = token[2];
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
			}
			if(bgNum != -1) {
				if(bgmNum != -1)
					pClient.spawnBattle(token[1], format, bgNum, bgmNum);
				else
					pClient.spawnBattle(token[1], format, bgNum);
			} else {
				pClient.spawnBattle(token[1], format);
			}
			return 1;
		} else if(cmd.equals("btlko")) {
			pClient.append("Couldn't start battle" + (token.length > 1 ? ": "+ConcatenateArrays.merge(token,1) : "."));
			return 1;

		} else if(cmd.startsWith("popup")) {
			/* !popup[-err|-warn] [Title] Actual message<br>with br tags instead<br>of newlines. */
			if(token.length < 2) return 1;
			int type = -1;
			if(cmd.equals("popup"))
				type = JOptionPane.PLAIN_MESSAGE;
			else if(cmd.equals("popup-warn"))
				type = JOptionPane.WARNING_MESSAGE;
			else if(cmd.equals("popup-err"))
				type = JOptionPane.ERROR_MESSAGE;
			else return 1;
			String title = "Message from the server";
			String mesg = "";
			Matcher matcher = Pattern.compile("^(?<title>\\[.*\\])\\s*(?<msg>.*)$").matcher(ConcatenateArrays.merge(token,1));
			if(matcher.matches()) {
				if(Debug.pedantic)
					printDebug("[PKPCMNEXEC] matcher matches.\ntitle="+matcher.group("title")+"\nmsg="+matcher.group("msg"));
				if(matcher.group("title") != null)
					title = matcher.group("title");
				if(matcher.group("msg") != null)
					mesg = matcher.group("msg");
			} else if(Debug.on) {
				printDebug("[PKPCMNEXEC] matcher not matched.");
			}

			JOptionPane.showMessageDialog((JPanel)pClient,
							mesg.replaceAll("<br>","\n"),
							title,
							type);
			return 1;
		} else if(cmd.equals("html") || cmd.equals("motd")) {
			if(token.length < 2) return 1;
			pClient.append(ConcatenateArrays.merge(token,1),false);
			return 1;
		} else if(cmd.equals("htmlconv")) {
			if(token.length < 2) return 1;
			pClient.append(Meta.toLocalURL(ConcatenateArrays.merge(token,1)),false);
			return 1;
		}
		else return super.execute(msg);
	}
}
