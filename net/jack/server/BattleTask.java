//: net/jack/server/BattleTask.java

package pokepon.net.jack.server;

import pokepon.net.jack.*;
import pokepon.battle.*;
import pokepon.util.*;
import pokepon.pony.*;
import pokepon.move.*;
import pokepon.move.hazard.*;
import pokepon.ability.*;
import pokepon.item.*;
import pokepon.player.Team;
import pokepon.enums.*;
import static pokepon.util.MessageManager.*;
import static pokepon.util.ConcatenateArrays.merge;
import static pokepon.util.Meta.*;
import java.util.*;
import java.util.concurrent.*;

/** The class that manages battles for the Pokepon Server; here is where
 * happens most of the server-side battle (the other big class for this is
 * BattleEngine); this class also handles the team validation and the 
 * battle queries from the clients.
 *
 * @author silverweed
 */
public class BattleTask implements Runnable {

	/** When battle ends, if no client sends messages for this number of
	 * seconds, terminate this BattleTask.
	 */
	public static final int POSTBATTLE_SUICIDE_DELAY = 60;
	public static final int MAX_GUESTS = 50;

	public BattleTask(final PokeponServer server, String btlID, final Connection c1, final Connection c2) {
		this(server,btlID,c1,c2,RuleSet.Predefined.DEFAULT);
	}

	/** @param server The Pokepon Server
	 * @param btlID The unique ID of this battle
	 * @param c1 Player 1's ServerConnection
	 * @param c2 Player 2's ServerConnection
	 * @param format This battle's format
	 */
	public BattleTask(final PokeponServer server, String btlID, final Connection c1, final Connection c2, final Format format) {
		this.server = server;
		battleID = btlID;
		this.c1 = c1;
		this.c2 = c2;
		this.format = format;
		if(Debug.pedantic) printDebug(this+" constructed. Format: "+format);
	}
	
	public void joinAsGuest(final Connection guest) {
		guest.sendMsg(CMN_PREFIX+"watchbtl "+battleID+" "+format+" "+bgNum+" "+bgmNum);
		boolean added = false;
		for(int i = 0; i < MAX_GUESTS; ++i)
			if(guests[i] == null) {
				guests[i] = guest;
				added = true;
				++nGuests;
				String _ally = "ally", _opp = "opp";
				if(i % 2 == 0) {
					sendB(guest, "|join|ally:p1|"+c1.getName());
					sendB(guest, "|join|opp|"+c2.getName());
				} else {
					sendB(guest, "|join|ally:p2|"+c2.getName());
					sendB(guest, "|join|opp|"+c1.getName());
					_ally = "opp";
					_opp = "ally";
				}
				sendB("|join||"+guest.getName());
				if(engine.getWeather() != null && engine.getWeather().get() != Weather.CLEAR)
					sendB(guest, "|setweather|"+engine.getWeather().get());
				for(Hazard h : engine.getHazards(1))
					sendB(guest, "|addhazard|"+_ally+"|"+h.getClass().getSimpleName());
				for(Hazard h : engine.getHazards(2))
					sendB(guest, "|addhazard|"+_opp+"|"+h.getClass().getSimpleName());
				Pony ap1 = battle.getPlayer(1).getActivePony();
				Pony ap2 = battle.getPlayer(2).getActivePony();
				if(ap1 != null && ap1.getStatus() != null)
					sendB(guest, "|addstatus|"+_ally+"|"+battle.getPlayer(1).getActivePony().getStatus().toBrief());
				if(ap2 != null && ap2.getStatus() != null)
					sendB(guest, "|addstatus|"+_opp+"|"+battle.getPlayer(2).getActivePony().getStatus().toBrief());
				break;
			}
		if(added)
			initClientSideBattle(guest);
		else
			sendB(guest, "|error|Battle is full: cannot join.");
	}

	public void leaveGuest(final Connection guest) {
		for(int i = 0; i < MAX_GUESTS; ++i)
			if(guests[i] == guest) {
				guests[i] = null;
				--nGuests;
				return;
			}
	}

	public synchronized void run() {
		battle = new Battle1v1(c1,c2);
		printDebug("[BattleTask]: started Battle Task between "+c1.getName()+" and "+c2.getName()+" [ID: "+battleID+"]");
		try {
			if(battle.initialize(format.getSpecialFormats().contains("randombattle"))) {
				boolean sentKo = false;
				if(Debug.on) printDebug(this+" battle correctly initialized.");
				
				// only validate teams if not randombattle
				if(!format.getSpecialFormats().contains("randombattle")) {
					if(Debug.on) printDebug(this+" validating teams... (format: "+format+")");
				
					List<TeamValidator> tr = new ArrayList<TeamValidator>();
					tr.add(new TeamValidator(battle.getTeam(1),format));
					tr.add(new TeamValidator(battle.getTeam(2),format));
					List<Future<Boolean>> results = executor.invokeAll(tr);
					try {
						if(Debug.pedantic) printDebug("Future.get(1)...");
						if(!results.get(0).get(15,TimeUnit.SECONDS)) {
							printDebug(battle.getPlayer(1).getName()+"'s team is invalid!");
							printMsg("Error starting battle between "+battle.getPlayer(1)+" and "+
								battle.getPlayer(2)+": aborting battle.");
							c1.sendMsg(CMN_PREFIX+"popup-err [Invalid team] Your team was rejected for the following reasons:<br>"+
								tr.get(0).getReasons().replaceAll("\n","<br>"));
							if(!sentKo) {
								c1.sendMsg(CMN_PREFIX+"btlko " + battleID);
								c2.sendMsg(CMN_PREFIX+"btlko " + battleID + " opponent's team was rejected.");
								sentKo = true;
							}
						}
						if(Debug.pedantic) printDebug("Future.get(2)...");
						if(!results.get(1).get(15,TimeUnit.SECONDS)) {
							printDebug(battle.getPlayer(2).getName()+"'s team is invalid!");
							printMsg("Error starting battle between "+battle.getPlayer(1)+" and "+
								battle.getPlayer(2)+": aborting battle.");
							c2.sendMsg(CMN_PREFIX+"popup-err [Invalid team] Your team was rejected for the following reasons:<br>"+
								tr.get(1).getReasons().replaceAll("\n","<br>"));
							if(!sentKo) {
								c1.sendMsg(CMN_PREFIX+"btlko opponent's team was rejected.");
								c2.sendMsg(CMN_PREFIX+"btlko");
								sentKo = true;
							}
						}
					} catch(TimeoutException e) {
						printDebug("Timeout: "+e);
						c1.sendMsg(CMN_PREFIX+"btlko timeout.");
						c2.sendMsg(CMN_PREFIX+"btlko timeout.");
						server.dismissBattle(c1,c2);	
					} catch(ExecutionException e) {
						printDebug("Caught exception in result.get(): "+e);
						e.printStackTrace();
						c1.sendMsg(CMN_PREFIX+"btlko (unknown reason)");
						c2.sendMsg(CMN_PREFIX+"btlko (unknown reason)");
						server.dismissBattle(c1,c2);	
					}

					if(sentKo) {
						server.dismissBattle(c1,c2);
						return;
					}

					printDebug(this+" Teams validated correctly.");
				}

				/// CLIENT-SIDE BATTLE INITIALIZATION ///
				// construct BattleEngine
				engine = new BattleEngine(this);
				engine.setAttacker(1);	//to ensure teamAttacker and teamDefender have an initial valid reference
				// battle background
				bgNum = battle.getRNG().nextFloat();
				// battle background music
				bgmNum = battle.getRNG().nextFloat();
				sendM(CMN_PREFIX+"spawnbtl "+battleID+" "+format.getName().replaceAll(" ","_")+" "+bgNum+" "+bgmNum);
				// TODO: check both clients could spawn battle

				// initial joins
				sendB(c1,"|join|ally:p1|"+c1.getName());
				sendB(c2,"|join|ally:p2|"+c2.getName());
				sendB(c1,"|join|opp|"+c2.getName());
				sendB(c2,"|join|opp|"+c1.getName());
				
				if(format.getSpecialFormats().contains("randombattle")) {
					switchRoutine(1,0);
					switchRoutine(2,0);
					battleStarted = true;
				}
				initClientSideBattle(c1);
				initClientSideBattle(c2);
			
				// wait inputs from clients: the msgQueue is filled by the Server via the pushMsg() method.
				while(!terminated) {
					if(postBattle) // die if no messages arrive within POSTBATTLE_SUICIDE_DELAY seconds.
						processMsg(msgQueue.poll(POSTBATTLE_SUICIDE_DELAY,TimeUnit.SECONDS));
					else // wait indefinitely
						processMsg(msgQueue.take());
				}

				if(Debug.on) printDebug(this+" successfully terminated.");

			} else {
				printDebug(this+" battle failed to initialize: aborting.");
				return;
			}
		} catch(InterruptedException e) {	
			printDebug(this+" Interrupted.");
			return;
		} catch(Exception e) {
			sendB(c1,"|disconnect|Sorry: server Battle Task crashed.");
			sendB(c2,"|disconnect|Sorry: server Battle Task crashed.");
			e.printStackTrace();
		} finally {		
			server.dismissBattle(c1,c2);
		}
	}

	public void terminate() {
		terminated = true;
		if(Debug.on) printDebug(this+" terminate() was invoked.");
		msgQueue.offer("");
		for(int i = 0; i < guests.length; ++i)
			guests[i] = null;
	}

	/** Given a connection ID, returns the corresponding connection;
	 * 1 means 'c1', 2 means 'c2', else means guest[n-3]
	 */
	public Connection getConnection(int num) {
		switch(num) {
			case 1: return c1;
			case 2: return c2;
			default: return guests[num-3];
		}
	}

	public Battle getBattle() {
		return battle;
	}

	public Format getFormat() {
		return format;
	}
	
	public void pushMsg(String msg) {
		if(terminated) return;
		msgQueue.add(msg);
	}

	/** This method processes messages coming from the clients, and sends back answers;
	 * message has the form |TYPE|DATA, like for the interpret() function of BattlePanel.
	 */
	private void processMsg(String msg) {
		if(msg == null) {
			terminate();
			return;
		}
		if(terminated || msg.length() < 1) return;

		String[] token = msg.substring(1).split("\\|");

		if(token.length < 1) throw new RuntimeException("[BattlePanel.interpret()]: token length < 1!");
		
		if(token[0].equals("chat") && token.length > 2) {
			/* |chat|Player/Guest Name|Message */
			//try to sanitize the "<" or ">" tags and bounce the message back.
			sendB("|chat|"+token[1]+"|"+sanitize(merge(token,2)));

		} else if(token[0].equals("cmd") && token.length > 2) {
			/* |cmd|(playerID/Player Name)|Command */
			final int connId = parseID(token[1]);
			if(connId < 1) {
				printDebug(this+" error: received connId = "+connId);
				return;
			}
			final String cmd = token[2];
			// since this task may take time, do it asynchronously
			executor.execute(new Runnable() {
				public void run() {
					processCommand(connId, cmd);
				}
			});

		} else if(token[0].equals("bcmd") && token.length > 2) {
			/* |bcmd|(playerID/Player Name)|Command */
			final int connId = parseID(token[1]);
			if(connId < 1) {
				printDebug(this+" error: received connId = "+connId);
				return;
			}
			if(connId != 1 && connId != 2) {
				sendB(guests[3-connId], "|error|Guests are not allowed to issue broadcast commands.");
				return;
			}
			final String cmd = token[2];
			final String sender = battle.getPlayer(connId).getName();
			executor.execute(new Runnable() {
				public void run() {
					sendB("|info|"+sender+" issued command: `"+CMN_PREFIX+sanitize(cmd)+"`");
					processCommand(connId, cmd, true);
				}
			});

		} else if(token[0].equals("leave") && token.length > 1) {
			sendB("|leave|"+token[1]);
			int id = parseID(token[1]);
			if(id < 1) {
				printDebug(this+" error: received connId = "+id);
				return;
			}
			if(id > 2) {
				leaveGuest(guests[id-3]);
			} else {
				sendB("|error|player "+id+" left; other players wins.");
				sendB((id == 1 ? c2 : c1),"|win|ally");
				server.getBattles().remove(battleID);
				server.dismissBattle(c1,c2);
				terminate();
			}

		} else if(token[0].equals("forfeit") && token.length > 1) {
			/* |forfeit|(playerID/Player Name) */
			int pl = parseID(token[1]);
			if(pl != 1 && pl != 2) {
				printDebug("[BattleTask.processMsg(forfeit)]: unknown player: "+token[1]);
				return;
			}
			if(postBattle) {
				sendB("|leave|"+(pl == 1 ? c1.getName() : c2.getName()));
				return;
			}
			if(pl == 1) {
				sendB("|forfeit|"+c1.getName());
				sendB(c2,"|win|ally");
				sendB(c1,"|win|opp");
			} else {
				sendB("|forfeit|"+c2.getName());
				sendB(c1,"|win|ally");
				sendB(c2,"|win|opp");
			}
		
			// gently suicide to spare server memory
			server.getBattles().remove(battleID);
			server.dismissBattle(c1,c2);
			terminate();
		
		// TODO: timer
		//} else if(token[0].equals("timer") && token.length > 2) {
			/* |timer|(playerID/Player Name)|(on/off) */
		/*	int pl = parseID(token[1]);
			if(pl != 1 && pl != 2) {
				printDebug("[BattleTask.processMsg(timer)]: unknown player: "+token[1]);
				return;
			}
			Connection thisC = pl == 1 ? c1 : c2;
			Connection thatC = pl == 1 ? c2 : c1;
			//if(!token[2].equals("on") && !token[2].equals("off")) {
			if(token[2].equals("on")) {
				// check if timer was already on
				if(timer[0] || timer[1]) {
					sendB(thisC, "|info|Timer is already active.");
					return;
				}
				timer[pl-1] = true;
				sendB("|html|<font color=red>Timer is on. Players will lose after " + TIMER_TIMEOUT + " seconds of " +
					"inactivity (requested by " + thisC.getName() + ").</font>");

			} else if(token[2].equals("off")) {
				if(timer[pl-1]) {
					timer[pl-1] = false;
					sendB("|html|<font color=red>" + thisC.getName() + " set the timer off.</font>");
					return;
				} 
				sendB(thisC, "|info|You cannot stop the timer since you weren't the one to activate it.");
				return;
			} else {
				printDebug("[BattleTask.processMsg(timer)]: expecting on/off but received "+token[2]);
				return;
			}*/
			
		} else if(token[0].equals("switch") && token.length > 2) {
			/* |switch|(playerID/Player Name)|ponyNum */
			try {
				int num = Integer.parseInt(token[2]);
				if(num < 0 || num > Team.MAX_TEAM_SIZE-1) {
					printDebug("[BattleTask.processMsg(switch)]: ERROR - received "+num+" as pony num.");
					return;
				}
				
				Pony switched = null;
				int pl = parseID(token[1]);

				if(pl != 1 && pl != 2) {
					printDebug("[BattleTask.processMsg(switch)]: Unknown player: "+token[1]);
					return;
				}
				
				switched = battle.getTeam(pl).getPony(num);
				if(switched == null) {
					printDebug("[BattleTask.processMsg()]: ERROR - pony #"+num+
						" doesn't exist in team "+pl+"!");
					return;
				}

				decided[pl-1] = true;
				
				Connection thisC = (pl == 1 ? c1 : c2);
				Connection thatC = (pl == 1 ? c2 : c1);
				Pony curAP = battle.getTeam(pl).getActivePony();

				/* if player's pony is fainted, or is forced to switch, switch as 'free action':
				 * don't wait the other player to decide
				 * (unless both ponies are fainted of course)
				 */
				if(	battleStarted &&
					(battle.getTeam(pl).getActivePony().isFainted() &&
					!battle.getTeam(pl == 1 ? 2 : 1).getActivePony().isFainted()) ||
					engine.isForcedToSwitch(pl) == 1
				) {

					Pony.Volatiles volatiles = null;
					if(Debug.on) printDebug("[BT.freeSwitchRoutine] BE.currentmove = "+engine.getCurrentMove());
					if(engine.getCurrentMove() != null && engine.getCurrentMove().copyVolatiles() && curAP != null) {
						volatiles = curAP.new Volatiles(curAP.getVolatiles());
					}
					
					// remove substitute
					if(curAP.hasSubstitute()) {
						sendB(thisC,"|rmsubstitute|ally");
						sendB(thatC,"|rmsubstitute|opp");
						curAP.setSubstitute(false);
					}
					// don't trigger onSwitchOut (but clear volatiles unless copyVolatiles is true)
					if(engine.getCurrentMove() == null || !engine.getCurrentMove().copyVolatiles())
						engine.clearVolatiles(pl);

					if(battle.getPlayer(pl).switchPony(switched.getName(),engine)) {
						sendB(thisC,"|switch|ally|"+num+
									"|"+(switched.hp() == switched.maxhp() ?
									switched.maxhp() :
									switched.hp()+"|"+switched.maxhp()));
						sendB(thatC,"|switch|opp|"+num+
									"|"+(switched.hp() == switched.maxhp() ?
									switched.maxhp() :
									switched.hp()+"|"+switched.maxhp()));
						decided[pl-1] = false;
						engine.updateActive();
						if(Debug.pedantic) printDebug("[BT] engine updated. Triggering \"onSwitchIn\" for p"+pl+"...");
						battle.getPlayer(pl).getActivePony().trigger("onSwitchIn",engine);
						// update client's allyPony stats
						sendB(thisC, "|stats|"+switched.atk()+"|"+switched.def()+"|"+
							switched.spatk()+"|"+switched.spdef()+"|"+switched.speed());
						// if last move used had copyVolatiles = true, copy them now on the new AP:
						if(engine.getCurrentMove() != null && engine.getCurrentMove().copyVolatiles() && volatiles != null) {
							switched.setVolatiles(volatiles);	
							for(Pony.Stat s : Pony.Stat.values()) 
								if(switched.getBoost(s) != 0) {
									sendB(thisC,"|boost|ally|"+s+"|"+switched.getBoost(s)+"|quiet");
									sendB(thatC,"|boost|opp|"+s+"|"+switched.getBoost(s)+"|quiet");
								}
							if(switched.isConfused()) {
								sendB(thisC,"|addpseudo|ally|Confused");
								sendB(thatC,"|addpseudo|opp|Confused");
							}
							if(switched.hasSubstitute()) {
								sendB(thisC,"|substitute|ally");
								sendB(thatC,"|substitute|opp");
							}
						}
						// if the other player hasn't switched yet, trigger afterSwitchIn after he/she
						// switches, not now.
						if(battle.getPlayer(pl == 1 ? 2 : 1).getActivePony().isKO()) 
							mustResolveSwitch = true;
						else {
							if(mustResolveSwitch) {
								battle.getPlayer(pl == 1 ? 2 : 1).getActivePony().trigger("afterSwitchIn",engine);
								mustResolveSwitch = false;
							}
							battle.getPlayer(pl).getActivePony().trigger("afterSwitchIn",engine);
						}
						checkFainted();
						sendB(thatC,"|endwait");
						if(	battle.getPlayer(pl).getActivePony().isFainted() &&
							!battle.getPlayer(pl==1?2:1).getActivePony().isFainted()
						) { 
							sendB(thatC,"|wait");
						}

					} else {
						//TODO: callback
						printDebug(this+" error: couldn't switch to pony "+switched);
						sendB(thisC,"|error|Couldn't switch to pony "+switched);
						decided[pl-1] = false;
					}
					return;
				} else if(
					battleStarted &&
					battle.getPlayer(1).getActivePony().isFainted() &&
					battle.getPlayer(2).getActivePony().isFainted()
				) {
					// prevent deadloops
					sendB("|endwait");
				}

				scheduledSwitch[pl-1] = switched;
				scheduledSwitchNum[pl-1] = num;
				scheduledMove[pl-1] = null;
				
				if(decided[0] && decided[1]) {
					//both players have decided 
					performTurn();
				}
			} catch(IllegalArgumentException e) {
				printDebug("[BattleTask.processMsg()]: ERROR - received "+token[2]+" as pony num.");
				printDebug(e+"");
				return;
			}

		} else if(token[0].equals("move") && token.length > 2) {
			/* |move|(playerID/Player Name)|(moveNum/MoveName) */
			try {
				int num = -1;

				if(token[2].matches("[0-3]")) {
					num = Integer.parseInt(token[2]);
				}
				
				Move used = null;
				int pl = parseID(token[1]);

				if(pl != 1 && pl != 2) {
					printDebug("[BattleTask.processMsg()]: Unknown player: "+token[1]);
					return;
				}
				
				if(num == -1) {
					boolean found = false;
					for(Move m : battle.getTeam(pl).getActivePony().getMoves())
						if(m.getName().equals(token[2])) {
							used = m;
							found = true;
							break;
						}
					if(!found)
						used = MoveCreator.create(token[2], battle.getTeam(pl).getActivePony());
				} else {
					used = battle.getTeam(pl).getActivePony().getMove(num);
				}

				if(used == null) {
					printDebug("[BattleTask.processMsg()]: ERROR - move #"+num+" doesn't exist for active pony of player #"+pl+"!");
					return;
				}

				decided[pl-1] = true;
				
				scheduledMove[pl-1] = used;
				scheduledSwitch[pl-1] = null;

				if(decided[0] && decided[1]) {
					//both players have decided 
					performTurn();
				}
			} catch(ReflectiveOperationException e) {
				printDebug("Failed to create move: "+e);
				return;
			} catch(IllegalArgumentException e) {
				printDebug("[BattleTask.processMsg(move)]: ERROR - received "+token[2]+" as move num.");
				printDebug(e+"");
				return;
			}
		}
	}

	public void sendM(String msg) {
		c1.sendMsg(msg);
		c2.sendMsg(msg);
		for(int i = 0, sent = 0; i < guests.length && sent < nGuests; ++i)
			if(guests[i] != null) {
				guests[i].sendMsg(msg);
				++sent;
			}
	}

	/** Like sendB(Connection,String), but send msg to all clients (broadcast to guests) */
	public void sendB(String msg) {
		sendB(msg, true);
	}

	/** Like sendB(String), but can select whether broadcasting to guests or not. */
	public void sendB(String msg, boolean broadcastToGuests) {
		sendB(c1, msg);
		sendB(c2, msg, broadcastToGuests);
	}

	/** Send battle message to a connection (broadcast to clients too) */
	public void sendB(Connection cl, String msg) {
		sendB(cl, msg, true);
	}

	/** Shortcut method to send battle messages */
	public void sendB(Connection cl, String msg, boolean broadcastToGuests) {
		if(cl.getVerbosity() >= 2) printDebug("[BattleTask] sending msg to "+cl.getName()+": "+BTL_PREFIX+battleID+" "+msg);
		cl.sendMsg(BTL_PREFIX+battleID+" "+msg);
		if(!broadcastToGuests) return;
		if(cl == c1) 
			for(int i = 0, sent = 0; i < guests.length && sent < nGuests; i += 2) {
				if(guests[i] == null) continue;
				guests[i].sendMsg(BTL_PREFIX+battleID+" "+msg);
				++sent;
			}
		else if(cl == c2)
			for(int i = 1, sent = 0; i < guests.length && sent < nGuests; i += 2) {
				if(guests[i] == null) continue;
				guests[i].sendMsg(BTL_PREFIX+battleID+" "+msg);
				++sent;
			}
	}

	public String toString() {
		return "[BattleTask #"+battleID+": "+c1.getName()+" vs "+c2.getName()+"]";
	}
	
	/** Initializes a battle client-side by sending all setup messages; used both
	 * for players and for guests as they join
	 */
	private void initClientSideBattle(final Connection c) {
		// send teams data
		int gIdx = guestIndexOf(c);
		for(int j = 1; j < 3; ++j) {
			int tmpi = 0;
			boolean ally = (j == 1 ^ c == c2) || (gIdx > 0 && gIdx % 2 != 0);
			for(Pony p : battle.getTeam(j)) {
				p.setHp(p.maxhp());
				sendB(c,"|pony|" + (ally ? "ally" : "opp") +
							"|" + p.getName()+
							"|" + p.getLevel()+
							(p.getNickname().equals(p.getName()) 
								? "|" 
								: "|"+p.getNickname()
							) +
							(p.getAbility() != null
								? "|"+p.getAbility()
								: "|"
							) +
							(p.getItem() != null
								? "|"+p.getItem()
								: ""
							)
				);
				if(ally) {
					for(int i = 0; i < Pony.MOVES_PER_PONY; ++i) {
						if(p.getMove(i) != null && p.getMove(i).getName().startsWith("Hidden Talent"))
							p.getMove(i).setName("Hidden Talent");
						sendB(c,"|setmv|"+tmpi+"|"+i+"|"+
							(p.getMove(i) == null 
								? "none"  
								: p.getMove(i) +
								(p.getMove(i).fullPP() 
									? "" 
									: "|"+p.getMove(i).getPP()
								)
							)
						);
						if(p.getMove(i) != null && p.getMove(i).getName().equals("Hidden Talent"))
							sendB(c1,"|setmvtype|"+tmpi+"|"+i+"|"+p.getMove(i).getType());
					}
				}
				++tmpi;
			}
		}
		// send rules
		//sendB(c, "|rated");
		sendB(c, "|rule|Format: "+format.getName());
		for(String pn : format.getBannedPonies())
			sendB(c, "|rule|Pony "+pn+" is banned");
		for(String mn : format.getBannedMoves())
			sendB(c, "|rule|Move "+mn+" is banned");
		for(String an : format.getBannedAbilities())
			sendB(c, "|rule|Ability "+an+" is banned");
		for(String in : format.getBannedItems())
			sendB(c, "|rule|Item "+in+" is banned");
		for(String[] cn : format.getBannedCombos()) {
			StringBuilder sb = new StringBuilder("|rule|Combo ");
			for(String cb : cn) {
				sb.append(cb.substring(2,cb.length())+"+");
			}
			sb.delete(sb.length()-1,sb.length());
			sb.append(" is banned.");
			sendB(sb.toString());
		}
		// TODO: parse special formats to output them in a prettier form.
		for(String sf : format.getSpecialFormats()) {
			sendB(c, "|rule|"+sf);
		}
		if(!battleStarted)
			sendB(c, "|teampreview");
		else {
			Pony p1 = battle.getPlayer(1).getTeam().getActivePony();
			Pony p2 = battle.getPlayer(2).getTeam().getActivePony();
			boolean ally = c == c1 || guestIndexOf(c) % 2 != 0;
			sendB(c,"|switch|"+(ally ? "ally" : "opp") +
					"|"+battle.getPlayer(1).getTeam().indexOf(p1)+
					"|"+(p1.hp() == p1.maxhp() 
						? p1.maxhp()
						: p1.hp() + "|" + p1.maxhp())
			);
			sendB(c,"|switch|"+(ally ? "opp" : "ally") +
					"|"+battle.getPlayer(2).getTeam().indexOf(p2)+
					"|"+(p2.hp() == p2.maxhp() 
						? p2.maxhp()
						: p2.hp() + "|" + p2.maxhp())
			);
		}
	}

	/** This method looks at the scheduled events (like scheduledSwitch,
	 * scheduledMove, etc), converts them into messages and sends them;
	 * then it empties the scheduled events setting them to null;
	 * this method is invoked exactly once per turn, since calling this
	 * means increment the turn counter.
	 */
	private void performTurn() {
		Event[] event = new Event[2];
		Pony p1 = battle.getPlayer(1).getTeam().getActivePony();
		Pony p2 = battle.getPlayer(2).getTeam().getActivePony();

		engine.setChosenMove(1, scheduledMove[0]);
		engine.setChosenMove(2, scheduledMove[1]);

		if(p1 != null)
			p1.trigger("beforeTurnStart", engine);
		if(p2 != null)
			p2.trigger("beforeTurnStart", engine);
		engine.triggerEvent("beforeTurnStart");

		try {
			if(++turnCount != 0) {
				sendB("|turn|"+turnCount);
				engine.incrementTurn();
			}

			for(int i = 0; i < 2; ++i) {
				if(scheduledSwitch[i] != null) 
					event[i] = Event.SWITCH;
				else if(scheduledMove[i] != null) 
					event[i] = Event.MOVE;
				
			}

			if(Debug.on) printDebug(this+" Performing events: "+event[0]+","+event[1]);

			if(event[0] == null || event[1] == null)
				throw new RuntimeException("Tried to perform scheduled events, but one player hasn't decided yet!");
		
			int effSpeed1 = p1 != null ? (int)(p1.speed() * (p1.hasStatus(Pony.Status.PARALYZED) ? 0.5 : 1)) : 0;
			int effSpeed2 = p2 != null ? (int)(p2.speed() * (p2.hasStatus(Pony.Status.PARALYZED) ? 0.5 : 1)) : 0;
			int fastest = 	effSpeed1 > effSpeed2 ? 1 :
					effSpeed2 > effSpeed1 ? 2 :
					battle.getRNG().nextFloat() > 0.5f ? 1 : 2;
			int first =
				event[0] == Event.SWITCH ? 1 : 
				event[1] == Event.SWITCH ? 2 :
				scheduledMove[0].getPriority() > scheduledMove[1].getPriority() ? 1 :
				scheduledMove[0].getPriority() < scheduledMove[1].getPriority() ? 2 :
				fastest;

			int second = first == 1 ? 2 : 1;

			// special case: move "Stalking" gets +7 priority if opponent switched
			if(	event[first-1] == Event.SWITCH && 
				scheduledMove[second-1] != null && 
				scheduledMove[second-1].getName().equals("Stalking")
			) {
				int tmp = first;
				first = second;
				second = tmp;
				scheduledMove[first-1].setDamageBoost(scheduledMove[first-1].getBaseDamage());
			}
			
			Connection firstC = first == 1 ? c1 : c2;
			Connection secondC = second == 1 ? c1 : c2;

			if(Debug.on) {
				printDebug(">>> TURN "+turnCount+" <<<");
				printDebug("First: "+first+"; speed1 = "+effSpeed1+", speed2 = "+effSpeed2);
				printDebug("Scheduled events\n"+
						"p1: "+(scheduledSwitch[0] == null ? 
							"use move "+scheduledMove[0].getName() :
							"switch to "+scheduledSwitch[0].getName()) +
						"\np2: "+(scheduledSwitch[1] == null ? 
							"use move "+scheduledMove[1].getName() :
							"switch to "+scheduledSwitch[1].getName())
				);
				printDebug("[BattleTask] first = "+first);

				if(Debug.pedantic) {
					if(p1 != null) {
						printDebug("[BattleTask]AP1: "+p1+" "+p1.getBoosts()+" "+p1.getStatus()); 
						Pony pp1 = engine.getTeam1().getActivePony();
						if(pp1 != null)
							printDebug("[BE]AP1: "+pp1+" "+pp1.getBoosts()+" "+pp1.getStatus());
					} else 
						printDebug("[BattleTask]AP1: null");
					if(p2 != null) {
						printDebug("[BattleTask]AP2: "+p2+" "+p2.getBoosts()+" "+p2.getStatus());
						Pony pp1 = engine.getTeam2().getActivePony();
						if(pp1 != null)
							printDebug("[BE]AP2: "+pp1+" "+pp1.getBoosts()+" "+pp1.getStatus());
					}
					else
						printDebug("[BattleTask]AP2: null");
				}
			}

			// first event is a switch
			if(event[first-1] == Event.SWITCH) {
				if(Debug.pedantic) printDebug("event[first("+first+")] = SWITCH");
				switchRoutine(first, scheduledSwitchNum[first-1]);

				if(event[second-1] == Event.SWITCH) {	// both switched
					if(Debug.pedantic) printDebug("event[second("+second+")] = SWITCH");
					switchRoutine(second, scheduledSwitchNum[second-1]);
					battle.getTeam(first).getActivePony().trigger("afterSwitchIn",engine);
					battle.getTeam(second).getActivePony().trigger("afterSwitchIn",engine);
					engine.triggerEvent("afterSwitchIn");

				} else { //first player switched, second used move
					battle.getTeam(first).getActivePony().trigger("afterSwitchIn",engine);
					engine.triggerEvent("afterSwitchIn");
					battle.getTeam(first).getActivePony().trigger("onTurnStart",engine);
					battle.getTeam(second).getActivePony().trigger("onTurnStart",engine);
					engine.triggerEvent("onTurnStart");
					if(Debug.pedantic) printDebug("Second player ("+second+") used move. Setting attacker = "+second);
					engine.setAttacker(second);

					// onMoveUsage is triggered by the BattlEngine for both sides.
					engine.ponyUseMove(scheduledMove[second-1]);

					if(Debug.pedantic) printDebug("[BT] After move usage. Triggering afterMoveUsage for both sides...");
					battle.getPlayer(second).getActivePony().trigger("afterMoveUsage",engine);
					battle.getPlayer(first).getActivePony().trigger("afterMoveUsage",engine);
					engine.triggerEvent("afterMoveUsage");
					runAfterMove();
				}
			} else { // both used move, or first used >= +7 move and second switched
				battle.getTeam(first).getActivePony().trigger("onTurnStart",engine);
				battle.getTeam(second).getActivePony().trigger("onTurnStart",engine);
				engine.triggerEvent("onTurnStart");
				if(Debug.pedantic) printDebug("Both players used move. Setting attacker = "+first);
				engine.setAttacker(first);
				
				engine.ponyUseMove(scheduledMove[first-1]);

				if(Debug.pedantic) printDebug("[BT] After move usage. Triggering afterMoveUsage for both sides...");
				battle.getPlayer(first).getActivePony().trigger("afterMoveUsage",engine);
				battle.getPlayer(second).getActivePony().trigger("afterMoveUsage",engine);
				engine.triggerEvent("afterMoveUsage");
				
				runAfterMove();
				
				// scheduledMove[second-1] may be null in case of forced switch: in this case, or
				// if the second pony to go is already KO, the turn ends here.
				if(!engine.getDefender().isFainted()) {
					// this can happen if first player used a >= +7 priority move (e.g. Stalking)
					if(event[second - 1] == Event.SWITCH) {
						if(Debug.pedantic) printDebug("event[second("+second+")] = SWITCH");
						switchRoutine(second, scheduledSwitchNum[second-1]);
						battle.getTeam(second).getActivePony().trigger("afterSwitchIn",engine);
						engine.triggerEvent("afterSwitchIn");
					} else if(scheduledMove[second - 1] != null) {
						engine.swapSides();

						engine.ponyUseMove(scheduledMove[second-1]);

						if(Debug.pedantic) printDebug("[BT] After move usage. Triggering afterMoveUsage for both sides...");
						battle.getPlayer(second).getActivePony().trigger("afterMoveUsage",engine);
						battle.getPlayer(first).getActivePony().trigger("afterMoveUsage",engine);
						engine.triggerEvent("afterMoveUsage");
						
						runAfterMove();
					}
				}
			}

			// end-turn effects
			if(Debug.on) printDebug("[BT] Starting end-turn effects.");

			if(!battle.getPlayer(1).getActivePony().isKO())
				battle.getPlayer(1).getActivePony().trigger("onTurnEnd", engine);
			if(!battle.getPlayer(2).getActivePony().isKO())
				battle.getPlayer(2).getActivePony().trigger("onTurnEnd", engine);
			engine.triggerEvent("onTurnEnd");
			
			WeatherHolder weather = engine.getWeather();
			for(int i = 1; i < 3; ++i) {
				Pony ap = battle.getPlayer(i).getActivePony();
				// all following effects apply only when this AP is alive
				if(ap.isFainted())
					continue;
				Connection thisC = i == 1 ? c1 : c2;
				Connection thatC = i == 1 ? c2 : c1;
				boolean preventsSec = false;
				for(EffectDealer ed : ap.getEffectDealers()) 
					if(ed.ignoreSecondaryDamage()) {
						preventsSec = true;
						break;
					}
				switch(weather.get()) {
					case STORMY: 	// stormy weather damages all but alicorns and deals more damage to pegasi and gryphons.
						if(!preventsSec) {
							switch(ap.getRace()) {
								case ALICORN:
									break;
								case PEGASUS:
								case GRYPHON:
									sendB(thisC,"|damage|ally|"+
										(int)(ap.maxhp() / 12f)+
										"weather/Stormy|");
									sendB(thatC,"|damage|opp|"+
										(int)(ap.maxhp() / 12f)+
										"weather/Stormy|");
								default:
									sendB(thisC,"|damage|ally|"+
										(int)(ap.maxhp() / 16f)+
										"weather/Stormy|");
									sendB(thatC,"|damage|opp|"+
										(int)(ap.maxhp() / 16f)+
										"weather/Stormy|");
							}
						}
						break;

					default:
						break;
				}
				if(Debug.on) {
					printDebug("[BT] Active Pony #"+i+": "+ap.getStatus());
					printDebug("-- Counters:");
					if(ap.toxicCounter != 0)
						printDebug("\ttoxicCounter = "+ap.toxicCounter);
					if(ap.getVolatiles().deathCounter != 0) 
						printDebug("\tgetVolatiles().deathCounter = "+ap.getVolatiles().deathCounter);
					if(ap.getVolatiles().tauntCounter != 0)
						printDebug("\tgetVolatiles().tauntCounter = "+ap.getVolatiles().tauntCounter);
					if(ap.getVolatiles().confusionCounter != 0) 
						printDebug("\tgetVolatiles().confusionCounter = "+ap.getVolatiles().confusionCounter);
					if(ap.protectCounter != 0) 
						printDebug("\tprotectCounter = "+ap.protectCounter);
					if(ap.sleepCounter != 0)
						printDebug("\tsleepCounter = "+ap.sleepCounter);
				}
				
				if(!preventsSec) {
					if(ap.hasStatus(Pony.Status.BURNED)) {
						sendB(thisC,"|brn|ally");
						sendB(thatC,"|brn|opp");
						ap.damagePerc(Battle.BURN_DAMAGE * 100f);
					} else if(ap.hasStatus(Pony.Status.INTOXICATED)) {
						sendB(thisC,"|tox|ally|"+(++ap.toxicCounter));
						sendB(thatC,"|tox|opp|"+ap.toxicCounter);
						ap.damagePerc(Battle.BAD_POISON_DAMAGE * 100f * ap.toxicCounter);
					} else if(ap.hasStatus(Pony.Status.POISONED)) {
						sendB(thisC,"|psn|ally");
						sendB(thatC,"|psn|opp");
						ap.damagePerc(Battle.POISON_DAMAGE * 100f);
					} 
				}
				if(ap.isTaunted()) {
					if(--ap.getVolatiles().tauntCounter == 0) {
						sendB("|battle|"+ap.getNickname()+"'s taunt ended!");
						sendB(thisC,"|rmtaunt|ally");
						sendB(thatC,"|rmtaunt|opp");
					}
				}
				if(ap.isLockedOnMove() && engine.getLockingTurns()[i-1]-- == 0) {
					ap.setLockedOnMove(false);
					sendB(thisC,"|unlock");
					sendB(thisC,"|resultanim|ally|neutral|Unlocked!");
					sendB(thatC,"|resultanim|opp|neutral|Unlocked!");
					if(ap.getUnlockPhrase() != null) {
						sendB("|battle|"+ap.getUnlockPhrase());
						ap.setUnlockPhrase(null);
					} else {
						sendB("|battle|"+ap.getNickname()+" is locked no more!");
					}
				}
				if(ap.isDeathScheduled()) {
					if(Debug.on) printDebug("[BT] "+ap.getNickname()+" has death scheduled. count = "+ap.getVolatiles().deathCounter);
					if(ap.getVolatiles().deathCounter == 0) {
						sendB(thisC,"|damage|ally|"+ap.hp());
						sendB(thatC,"|damage|opp|"+ap.hp());
						ap.damagePerc(100f);
					} else {
						sendB("|battle|"+ap.getNickname()+" will faint in "+(ap.getVolatiles().deathCounter++)+" turns!");
					}
				}

				checkFainted();
			}
			// end weather
			if(	weather != null &&
				weather.get() != null && 
				weather.get() != Weather.CLEAR &&
				weather.count == 0
			) {
				sendB("|battle|The weather became clear!");
				engine.setWeather(WeatherHolder.getClearWeather());
			}

			if(checkWin()) {
				//TODO: update ladder
				if(Debug.on) printDebug(this+" Battle is over.");
				sendB("|html|<font color='gray'>If you have battle logging enabled,<br>type "+
					CMD_PREFIX+"save to export a battle log.</font>");
				// allow post-battle chatting: terminate this task only after a client disconnects
				// or if both remain idle for too long
				postBattle = true;
				msgQueue.offer("");
			}
		} finally {
			if(Debug.pedantic) printDebug(this+" Setting decided[1 and 2] = false");
			decided[0] = decided[1] = false;
		}
	}

	/** Converts passed id/name of connection to the correct integer;
	 * player 1 is #1, player 2 is #2, guest N is #N+3.
	 * @return ID of given connection id/name, or 0 if no connection matches.
	 */
	private int parseID(String token) {
		if(token.equals(c1.getName()) || token.equals("1")) return 1;
		if(token.equals(c2.getName()) || token.equals("2")) return 2;
		for(int i = 0; i < guests.length; ++i) {
			if(guests[i] == null) continue;
			if(token.equals(guests[i].getName()) || token.equals(Integer.toString(i+3)))
				return i + 3;
		}
		return 0;
	}

	/** @return True if battle is over, False otherwise */ 
	private boolean checkWin() {
		boolean allKO1 = battle.getPlayer(1).getTeam().allKO();	
		boolean allKO2 = battle.getPlayer(2).getTeam().allKO();	
		if(!(allKO1 || allKO2))
			return false;
		if(allKO1 && allKO2) {
			sendB("|draw");
		} else if(allKO1) {
			sendB(c2,"|win|ally");
			sendB(c1,"|win|opp");
		} else {
			sendB(c1,"|win|ally");
			sendB(c2,"|win|opp");
		}
		return true;
	}

	private void checkFainted() {
		if(	battle.getPlayer(1).getActivePony() != null &&
			battle.getPlayer(1).getActivePony().isFainted()
		) {
			engine.clearVolatiles(1);
			if(!engine.hasSentFaintedMsg(1)) { 
				sendB(c1,"|fainted|ally");
				sendB(c2,"|fainted|opp");
				engine.setSentFaintedMsg(1, true);
			}
		}
		if(	battle.getPlayer(2).getActivePony() != null &&
			battle.getPlayer(2).getActivePony().isFainted()
		) {
			engine.clearVolatiles(2);
			if(!engine.hasSentFaintedMsg(2)) {
				sendB(c2,"|fainted|ally");
				sendB(c1,"|fainted|opp");
				engine.setSentFaintedMsg(2, true);
			}
		}
	}

	// FIXME: check forcedToSwitch bug!
	private void runAfterMove() {
		// check forced switches
		if(Debug.on) printDebug("forcedToSwitch = { " + engine.isForcedToSwitch(1) + ", " + engine.isForcedToSwitch(2) + " }");
		for(int i = 1; i < 3; ++i) {
			Connection thisC = i == 1 ? c1 : c2;
			Connection thatC = i == 1 ? c2 : c1;
			if(!engine.getTeam(i).getActivePony().isKO()) {
				switch(engine.isForcedToSwitch(i)) {
					case 1: // switch to chosen pony
					case 3:
					{
						if(engine.getTeam(i).getViablePoniesCount() <= 1) break;
						Pony curAP = battle.getPlayer(i).getActivePony();
						sendB(thatC,"|wait");
						sendB(thisC,"|mustswitch");
						try {
							do {
								processMsg(msgQueue.take());
							} while(battle.getPlayer(i).getActivePony() == curAP);
						} catch(InterruptedException e) {
							printDebug("[runAfterMove] interrupted on take().");
						} finally {
							sendB(thatC,"|endwait");
						}
						break;
					}
					case 2: // switch to random pony
					case 4:
					{
						// don't apply effect if there are no more allies alive
						if(engine.getTeam(i).getViablePoniesCount() <= 1) break;
						int rand = -1;
						do {
							rand = engine.getRNG().nextInt(engine.getTeam(i).members());
						} while(engine.getTeam(i).getPony(rand) == engine.getTeam(i).getActivePony() ||
							engine.getTeam(i).getPony(rand).isKO());

						switchRoutine(i, rand);
						scheduledMove[i-1] = null;
						break;
					}
				}
			}
		}
		engine.resetForcedToSwitch();
		// check fainted ponies
		checkFainted();
		if(battle.getPlayer(1).getActivePony().isFainted() && !battle.getPlayer(2).getActivePony().isFainted()) 
			sendB(c2,"|wait");
		else if(battle.getPlayer(2).getActivePony().isFainted() && !battle.getPlayer(1).getActivePony().isFainted())
			sendB(c1,"|wait");
	}

	private void processCommand(int connId, String cmd) {
		processCommand(connId, cmd, false);
	}

	/** Process a command from a client and send response to it. */
	private void processCommand(int connId, String cmd, boolean broadcast) {
		Connection conn = getConnection(connId);
		if(conn == null) return;
		String[] token = cmd.split(" ");
		if(Debug.on) printDebug("[BT.processCommand] tokens = "+Arrays.asList(token));
		
		if(token[0].equals("data")) {
			if(token.length < 2) {
				sendB(conn,"|error|Syntax error: expected pony, move, item or ability name after 'data' command.", false);
				return;
			}
			String response = dataDealer.getData(ConcatenateArrays.merge(token, 1));
			if(response == null) {
				sendB(conn,"|error|"+ConcatenateArrays.merge(token,1)+": no data found.");
			} else if(response.startsWith("|")) {
				if(broadcast)
					sendB(response);
				else
					sendB(conn,response, false);
			} else {
				if(broadcast)
					sendB("|htmlconv|"+response);
				else
					sendB(conn,"|htmlconv|"+response, false);
			}
		} else if(token[0].startsWith("eff")) {
			if(token.length < 2) {
				sendB(conn,"|error|Syntax error: correct syntax is :<br>&nbsp;" +
					CMD_PREFIX + "eff type1[,type2]<br>&nbsp;"+
					CMD_PREFIX + "eff type1 -&gt; type2[,type3]", false);
				return;
			}
			String response = dataDealer.getEffectiveness(ConcatenateArrays.merge(token,1));
			if(response == null) {
				sendB(conn,"|error|"+ConcatenateArrays.merge(token,1)+": no data found.", false);
			} else if(response.startsWith("|")) {
				if(broadcast)
					sendB(response);
				else
					sendB(conn,response, false);
			} else {
				if(broadcast)
					sendB("|htmlconv|"+response);
				else
					sendB(conn,"|htmlconv|"+response, false);
			}
		} else {
			sendB(conn,"|error|Invalid command: "+token[0], false);
		}
	}

	/** Do all actions that need to happen when a player switches */
	private void switchRoutine(int pl, int num) {
		Connection plC = pl == 1 ? c1 : c2;
		Connection othC = pl == 1 ? c2 : c1;
		Pony curAP = battle.getTeam(pl).getActivePony();
		if(battleStarted) {
			if(battle.getPlayer(pl).getActivePony() != null) {
				if(Debug.pedantic) printDebug("Triggering onSwitchOut for p"+pl+"...");
				battle.getPlayer(pl).getActivePony().trigger("onSwitchOut",engine);
			}				
			engine.clearVolatiles(pl);
		}
		if(battle.getPlayer(pl).switchPony(num,engine)) {
			if(!battleStarted) {
				sendB("|start");
				battleStarted = true;
			}
			Pony switched = battle.getTeam(pl).getActivePony();
			if(Debug.pedantic) printDebug("switch[pl] successful.");
			sendB(plC,"|switch|ally|"+num+
						"|"+(switched.hp() == switched.maxhp()
							? switched.maxhp()
							: switched.hp()+"|"+switched.maxhp()));
			sendB(othC,"|switch|opp|"+num+
						"|"+(switched.hp() == switched.maxhp() 
							? switched.maxhp() 
							: switched.hp()+"|"+switched.maxhp()));
			// update battle engine
			engine.updateActive();
			if(Debug.pedantic) printDebug("[BT] engine updated. Triggering \"onSwitchIn\" for p"+pl+"...");
			switched.trigger("onSwitchIn",engine);
			// update client's allyPony stats
			sendB(pl == 1 ? c1 : c2, "|stats|"+switched.atk()+"|"+switched.def()+"|"+switched.spatk()+"|"+switched.spdef()+"|"+switched.speed());
		} else {
			printDebug(this+" error - switch failed.");
			sendB("|error|Couldn't switch to pony "+battle.getTeam(pl).getPony(num).getNickname());
		}
	}
	
	private int guestIndexOf(final Connection c) {
		for(int i = 0, n = 0; i < guests.length && n < nGuests; ++i) {
			if(guests[i] == null) continue;
			if(guests[i] == c) return i;
			++n;
		}
		return -1;
	}

	/** indicates if player (i+1) has already communicated a decision to server. */
	private boolean[] decided = new boolean[2];	
	private boolean battleStarted;
	private boolean postBattle;
	private float bgNum, bgmNum;
	private int[] scheduledSwitchNum = new int[2];
	private Pony[] scheduledSwitch = new Pony[2];
	private Move[] scheduledMove = new Move[2];
	private volatile boolean terminated;
	private final Format format;
	private final PokeponServer server;
	private final String battleID;
	private final Connection c1;
	private final Connection c2;
	private Battle battle;
	private BattleEngine engine;
	private ExecutorService executor = Executors.newFixedThreadPool(2);
	private BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>();
	private int turnCount = -1;
	private boolean mustResolveSwitch;
	private DataDealer dataDealer = new DataDealer();
	/** Guests are numbered like #3, #4, etc (#1 and #2 are c1 and c2); odd-numbered
	 * guests receive all messages for c1, and even-numbered ones for c2;
	 * we use an array so the positions (i.e numbers) of each guest are fixed.
	 */
	private Connection[] guests = new Connection[MAX_GUESTS];
	private int nGuests;
	/** Whether the timer is on or not and who activated it. */
	/*private boolean timer[2];
	BattleTimer timerTask;*/

	private static enum Event { 
		SWITCH,
		MOVE;
	};
}
