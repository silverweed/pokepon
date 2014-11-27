//: battle/BattleEngine.java

package pokepon.battle;

import pokepon.pony.*;
import pokepon.move.*;
import pokepon.move.hazard.*;
import pokepon.enums.*;
import pokepon.enums.Type;
import pokepon.util.*;
import pokepon.player.*;
import pokepon.net.jack.*;
import pokepon.net.jack.server.*;
import static pokepon.util.MessageManager.*;	
import java.util.*;
import java.lang.reflect.*;

/** Manages the events occurring when a pony attacks another one;
 *
 * @author Giacomo Parolini
 */
public class BattleEngine {
	
	/** Max depth at which submoves can be spawned */
	private final static int MAX_MOVE_STACK = 8;
	
	private void setup() {
		dc = new DamageCalculator(rng);
		hazards1 = Collections.synchronizedSet(new HashSet<Hazard>());
		hazards2 = Collections.synchronizedSet(new HashSet<Hazard>());
	}
		
	/** team1 and team2 are fixed references to the p1's and p2's teams; p1 and p2 also remain the
	 * same during all the battle: what changes is the attacker's and defender's ids, which
	 * determine which team is teamAttacker and which is teamDefender; hazards always stay the same
	 * and refer to a player's id rather than its role (atk/def).
	 */
	public BattleEngine(Team team1,Team team2) throws NoActivePonyException {
		rng = new Random();
		setup();
		this.team1 = team1;
		this.team2 = team2;
		if(Debug.on) printDebug("[BE] Constructed.\nTeam1 = "+team1+"\nTeam2 = "+team2);
	}

	public BattleEngine(BattleTask battleTask) throws NoActivePonyException {
		rng = battleTask.getBattle().getRNG();
		setup();
		this.battleTask = battleTask;
		ally = battleTask.getConnection(1);
		opp = battleTask.getConnection(2);
		this.team1 = battleTask.getBattle().getTeam(1);
		this.team2 = battleTask.getBattle().getTeam(2);
		if(Debug.on) printDebug("[BE] Constructed with battletask.\nTeam1 = "+team1+"\nTeam2 = "+team2);
	}

	// GET METHODS //

	public Team getTeam1() { return team1; }
	public Team getTeam2() { return team2; }
	public Team getTeam(int pl) {
		if(pl == 1) return team1;
		else if(pl == 2) return team2;
		else return null;
	}
	public Team getTeamAttacker() { return teamAttacker; }
	public Team getTeamDefender() { return teamDefender; }
	public Pony getAttacker() { return attacker; }
	public Pony getDefender() { return defender; }
	public Pony getOpponent(Pony pony) {
		return getTeam(getOppositeSide(pony)).getActivePony();
	}
	public WeatherHolder getWeather() { return weather; }
	public synchronized Set<Hazard> getHazards(int i) {
		if(i == 1) return hazards1;
		else if(i == 2) return hazards2;
		else return null;
	}
	public synchronized List<Set<Hazard>> getHazards() {
		List<Set<Hazard>> hz = new ArrayList<Set<Hazard>>();
		hz.add(hazards1);
		hz.add(hazards2);
		return hz;
	}
	// these have been replaced by BattleEvents
	/*public List<PersistentEffect> getPersistentEffects() {
		return persistentEffects;
	}
	public List<PersistentEffect> getPersistentEffects(int i) {
		List<PersistentEffect> eff = new LinkedList<>();
		for(PersistentEffect pe : persistentEffects) {
			if(pe.getSide() == i)
				eff.add(pe);
		}
		return eff;
	}*/
	public int getSide(Pony pony) {
		if(team1.contains(pony)) return 1;
		else if(team2.contains(pony)) return 2;
		else return 0;
	}
	public int getOppositeSide(Pony pony) {
		int side = getSide(pony);
		if(side == 1) return 2;
		else if(side == 2) return 1;
		else return 0;
	}
	public Connection getConnection(int pl) {
		int cur = currentPlayer();
		if(pl == 1) return cur == 1 ? ally : opp;
		else if(pl == 2) return cur == 2 ? ally : opp;
		else return null;
	}
	public final BattleTask getBattleTask() { return battleTask; }
	public final Connection getAlly() { return ally; }
	public final Connection getOpp() { return opp; }
	public final Random getRNG() { 
		return rng;
	}
	public Move getCurrentMove() { return currentMove; }
	public int getInflictedDamage() { return inflictedDamage; }
	public int getLatestInflictedDamage() { return latestInflictedDamage; }
	public List<BattleEvent> getBattleEvents() { return battleEvents; }

	/** @param echo If true (default), the BattleEngine will print battle messages on console, else not. */
	public void setEcho(boolean echo) {
		echoBattle = echo;
		if(Debug.on) printDebug("[BE] Set echoBattle = "+echoBattle);
	}
	public boolean hasSentFaintedMsg(int pl) {
		return sentFaintedMsg[pl - 1];
	}
	/** @return True if calling ponyUseMove with this engine will not throw NullPointerExceptions due to the
	 * state of teams or active ponies; False otherwise; meant to check the state of the engine before calling
	 * the ponyUseMove method. 
	 */
	public boolean isReady() {
		return teamAttacker != null && teamDefender != null && attacker != null && defender != null;
	}

	public int currentPlayer() {
		return teamAttacker == team1 ? 1 : (teamAttacker == team2 ? 2 : 0);
	}

	public Move getChosenMove(int pl) {
		return chosenMove[pl - 1];
	}

	public byte isForcedToSwitch(int pl) {
		return forcedToSwitch[pl - 1];
	}

	public int[] getLockingTurns() {
		return lockingTurns;
	}
	
	// SET METHODS //

	/** This method sets teams, active ponies and weather; it can be called after the BE is constructed
	 * in order to reset it without creating a new one; obviously this method also sets the defender.
	 */
	public void setAttacker(int attackerID) throws NoActivePonyException {
		if(Debug.pedantic) printDebug("Called BattleEngine.set(attackerID="+attackerID+")");
		if((attackerID != 1 && attackerID != 2)) {
			throw new RuntimeException("Error in BattleEngine.set(): attackerID is "+attackerID);
		}
		teamAttacker = (attackerID == 1 ? team1 : (attackerID == 2 ? team2 : null));
		teamDefender = (attackerID == 2 ? team1 : (attackerID == 1 ? team2 : null));
		if(battleTask != null) {
			ally = battleTask.getConnection(attackerID);
			opp = (attackerID == 1 ? battleTask.getConnection(2) : battleTask.getConnection(1));
		}
		// attacker and defender may be null here (namely, if it's the first turn and neither player has
		// chosen their leader yet), but that's OK: we can set them later with updateActive().
		attacker = teamAttacker.getActivePony();
		defender = teamDefender.getActivePony();
		if(Debug.on) printDebug("[BE] Attacking team is now "+(teamAttacker == team1 ? "team1" : "team2"));
	}


	/** This function is to be called after a player switched pony, to set the correct reference to attacker and defender. */
	public void updateActive() {
		if(teamAttacker == null || teamDefender == null) {
			throw new NullPointerException("[BE] error: "+
					(teamAttacker == null ? "teamAttacker is null!" : "") + " " +
					(teamDefender == null ? "teamDefender is null!" : "")
			);
		}
		attacker = teamAttacker.getActivePony();
		defender = teamDefender.getActivePony();
		// checkActivePony is not done here yet, since one among attacker and defender may still be unset.
	}		

	public void setChosenMove(int pl,Move move) {
		if(pl == 1 || pl == 2)
			chosenMove[pl -1] = move;
		else
			throw new RuntimeException("[BE] error: given pl = "+ pl +"to setChosenMove.");
	}

	/** Swaps the attacking and the defending sides, changing teams and ponies references. */
	public void swapSides() throws NoActivePonyException {
		if(Debug.on) printDebug("[BE] Swapping sides.");
		if(teamAttacker == team1) {
			setAttacker(2);
		} else if(teamAttacker == team2) {
			setAttacker(1);
		} else {
			throw new RuntimeException("Error in BattleEngine.swapSides(): teamAttacker is neither team1 nor team2.");
		}
	}

	public void setWeather(WeatherHolder wh) {
		weather = wh;
	}

	/** breakCycle can be manipulated from TriggeredEffectDealers to prevent a move
	 * to hit the defender for the turn being. The DamageCalculator won't be called at all.
	 */
	public void setBreakCycle(boolean b) {
		breakCycle = b;
	}

	public void setSentFaintedMsg(int pl, boolean b) {
		if(pl == 1 || pl == 2)
			sentFaintedMsg[pl - 1] = b;
		else
			throw new RuntimeException("[BE] error: given pl = "+pl+" to setSentFaintedMsg.");
	}

	/** Used by onDamage triggers to change the inflicted damage on the fly */
	public void setInflictedDamage(int infDamage) {
		latestInflictedDamage = inflictedDamage = infDamage;
	}

	/** Method that manages subsequent events that happen when a pony attacks another with a move; 
	 * (Roughly following the documentation of Pok&#233mon Showdown)
	 */
	public void ponyUseMove(Move move) throws NoActivePonyException, MoveNotFoundException {
		if(Debug.on) printDebug("Called with: " + move);
		latestInflictedDamage = 0;
	
		checkActivePony();
		if(attacker.isKO()) {
			if(Debug.on) printDebug("Attacker "+attacker.getNickname()+" is KO; not using move...");
			return;
		}

		if(Debug.pedantic) {
			printDebug("[BE] PONY1: "+attacker+" ("+attacker.hashCode()+")");
			printDebug("[BE] PONY2: "+defender+" ("+defender.hashCode()+")");
		}
		if(Debug.on) {
			printDebug("Status attacker: " + attacker.getStatus());
			printDebug("Status defender: " + defender.getStatus());
			printDebug("Attacker boosts: "+attacker.getBoosts());
			printDebug("Defender boosts: "+defender.getBoosts());
			//printDebug("PersistentEffects: ");
			//for(PersistentEffect pe : persistentEffects) printDebug("  "+pe);
			printDebug("Battle Events: ");
			for(BattleEvent be : battleEvents) printDebug("  "+be);
			printDebug("---end BattleEvents");
		}

		if(move == null) {
			if(Debug.on) printDebug("[BE] move is null in ponyUseMove(): returning.");
			return;
		}

		/// CHECK PP	
		if(move.getPP() < 1 && move.getMaxPP() != -1 && moveStack == 0) {
			printMsg("move = "+move+", PP = "+move.getPP()+"/"+move.getMaxPP());
			if(battleTask != null)
				battleTask.sendB(ally,"|battle|You have not enough PP for this move.");
			if(echoBattle) printMsg("You have not enough PP for this move.");
			return;
		}
		
		/// MOVE PRE-USAGE
		if(fullParalysis(attacker)) {
			if(battleTask != null) {
				battleTask.sendB(ally,"|par|ally");
				battleTask.sendB(opp,"|par|opp");
			}
			if(echoBattle) printMsg(attacker.getNickname()+" is paralyzed and can't move!");
			move.reset();
			return;
		} else if(staysPetrified(attacker)) {
			if(battleTask != null) {
				battleTask.sendB(ally,"|ptr|ally");
				battleTask.sendB(opp,"|ptr|opp");
			}
			if(echoBattle) printMsg(attacker.getNickname()+" is petrified and can't move!");
			move.reset();
			return;
		} else if(attacker.isFlinched()) {
			if(battleTask != null) {
				battleTask.sendB(ally,"|flinch|ally");
				battleTask.sendB(opp,"|flinch|opp");
			}
			if(echoBattle) printMsg(attacker.getNickname()+" is flinched and can't move!");
			attacker.setFlinched(false);
			move.reset();
			return;
		} else if(staysAsleep(attacker)) {
			if(battleTask != null) {
				battleTask.sendB(ally,"|slp|ally");
				battleTask.sendB(opp,"|slp|opp");
			}
			if(echoBattle) printMsg(attacker.getNickname()+" is fast asleep.");
			move.reset();
			return;
		} else if(attacker.isTaunted() && move.getMoveType() == Move.MoveType.STATUS) {
			if(battleTask != null) {
				battleTask.sendB("|battle|"+attacker.getNickname()+" cannot use "+move+" because of the taunt!");
				move.reset();
				return;
			}
		} // TODO: handle blocking moves
		
		// else:
		
		/* Detract 1 from confusionCounter if attacker was confused. */
		if(attacker.isConfused()) 
			if(--attacker.confusionCounter == 0) {
				attacker.setConfused(false);
				if(battleTask != null) {
					battleTask.sendB(ally,"|rmstatus|ally|cnf");
					battleTask.sendB(opp,"|rmstatus|opp|cnf");
				}
			}
		
		/* If still confused, throw coin to see if attacks itself */
		if(attacker.isConfused()) {
			if(echoBattle) printMsg(attacker.getNickname()+" is confused.");
			if(battleTask != null) {
				battleTask.sendB(ally,"|cnf|ally");
				battleTask.sendB(opp,"|cnf|opp");
			}
			if(rng.nextFloat() < Battle.CHANCE_SELF_DAMAGE_FOR_CONFUSION) {
				int dmg = attacker.damage(dc.calculateBattleDamage(
					new Move("Confusion self damage") {
						public int getBaseDamage() { return 40; }
						public int getMaxPP() { return 1; }
						public int getPP() { return maxpp; }
						public int getAccuracy() { return -1; }
						// actually is typeless, but we don't have type '???', so...
						public Type getType() { return Type.HONESTY; }
						public Move.MoveType getMoveType() { return Move.MoveType.PHYSICAL; }
						public boolean isTypeless() { return true; }
					},this,true));
				if(echoBattle) printMsg("It hurt itself in its confusion!");
				if(battleTask != null) {
					battleTask.sendB("|battle|It hurt itself in its confusion!");
					battleTask.sendB(ally,"|damage|ally|"+dmg);
					battleTask.sendB(opp,"|damage|opp|"+dmg);
				}
				if(attacker.isKO()) {
					if(echoBattle) printMsg(attacker.getNickname()+" fainted!");
					if(battleTask != null && !sentFaintedMsg[currentPlayer() -1]) {
						battleTask.sendB(opp,"|fainted|opp");
						battleTask.sendB(ally,"|fainted|ally");
						sentFaintedMsg[currentPlayer() - 1] = true;
					}
					return;
				}
				move.reset();
				return;
			}
		}			
	
		if(battleTask != null) {
			battleTask.sendB(ally,"|battle|"+move.getName()+"|move|ally");
			battleTask.sendB(opp,"|battle|"+move.getName()+"|move|opp");
		}
		if(echoBattle) printMsg(attacker.getFullName()+" used "+move.getName()+"!");

		/* Deduct PP */
		if(moveStack == 0 && !(move.isBlockingDelayed() && attacker.isLockedOnMove())) {
			if(Debug.pedantic) printDebug("Deducting 1 PP from "+attacker.getNickname());
			move.deductPP();
			if(battleTask != null) 
				battleTask.sendB(ally,"|deductpp|"+move.getName());
		}

		/* Check for valid internal conditions */
		if(!move.validConditions(this)) {
			if(battleTask != null) {
				battleTask.sendB(ally,"|fail|ally");
				battleTask.sendB(opp,"|fail|opp");
			}
			if(echoBattle) printMsg("But it failed...");
			move.reset();
			return;
		}

		/* This is set LATER than validConditions, so moves like Dodge can check what was actual
		 * previous move.
		 */
		attacker.setLastMoveUsed(move);

		/// MOVE USAGE
		
		currentMove = move;
		if(Debug.pedantic) printDebug("[BE] Before move usage. Triggering onMoveUsage for both sides...");
		attacker.trigger("onMoveUsage",this);
		defender.trigger("onMoveUsage",this);
		triggerEvent("onMoveUsage");

		if(manageTurnDelay(move) < 0) {
			triggerEvent("delayInit");	
			if(Debug.on) printDebug("[BE] manageTurnDelay returned -1: finishing.");
			return;
		} else {
			triggerEvent("delayInit");	
		}
		
		/* check if move spawns a sub-move and, in this case, do it */
		if(move.startsSubMove()) {
			String prevname = move.getName();
			move = move.spawnSubMove(this);	//simply re-reference move from now on
			move.setPony(attacker);
			if(echoBattle) printMsg(prevname+" becomes "+move.getName()+"!");
			attacker.setLastMoveUsed(move);
			if(battleTask != null) {
				battleTask.sendB("|battle|"+prevname+" becomes "+move.getName()+"!");
			}
			// prevent infinite loops
			if(++moveStack >= MAX_MOVE_STACK) {
				if(battleTask != null)
					battleTask.sendB("|battle|But it failed...(stack exceeded)");
				printDebug("But it failed...(stack exceeded)");
				moveStack = 0;
				move.reset();
				return;
			}
			ponyUseMove(move);
			return;
		}

		moveStack = 0;	// if we managed to get here, reset the engine stack

		/// MOVE EXECUTION
		/* Check for valid target */
		if(move.getMoveType() != Move.MoveType.STATUS && (defender == null || defender.isKO())) {	
			if(battleTask != null) {
				battleTask.sendB("|battle|There is no target...");
			}
			if(echoBattle) printMsg("There is no target...");
			move.reset();
			return;
		} 

		/* If status move, apply effects now */
		if(move.getMoveType() == Move.MoveType.STATUS) {
			if(!missed(move)) {
				if(battleTask != null) {
					battleTask.sendB(ally,"|move|ally|"+move.getName());
					battleTask.sendB(opp,"|move|opp|"+move.getName());
				}
				applyAdditionalEffects(move);
			} else {
				if(battleTask != null) {
					battleTask.sendB(ally,"|move|ally|"+move.getName()+"|avoid");
					battleTask.sendB(opp,"|move|opp|"+move.getName()+"|avoid");
					//battleTask.sendB(ally,"|avoid|opp");
					//battleTask.sendB(opp,"|avoid|ally");
				}
				if(echoBattle) printMsg(defender.getNickname()+" avoids the attack!");
				move.onMoveFail(this);
			}
			move.reset();
			return;
		}

		/// MOVE HIT
		/* If move is OHKO, set accuracy */
		if(move.isOHKO()) {
			move.setAccuracy(attacker.getLevel() - defender.getLevel() + 30);
		}

		/* Hit or miss? */
		if(missed(move)) {
			if(battleTask != null) {
				battleTask.sendB(ally,"|move|ally|"+move.getName()+"|avoid");
				battleTask.sendB(opp,"|move|opp|"+move.getName()+"|avoid");
			}
			if(echoBattle) printMsg(defender.getNickname()+" avoids the attack!");
			move.onMoveFail(this);
			move.reset();
			return;
		} else {
			if(battleTask != null) {
				battleTask.sendB(ally,"|move|ally|"+move.getName());
				battleTask.sendB(opp,"|move|opp|"+move.getName());
			}
			breakCycle = false;
			// these may set breakCycle = true
			attacker.trigger("beforeMoveHit",this);
			defender.trigger("beforeMoveHit",this);
			triggerEvent("beforeMoveHit");
			int i = 0;
			int hits = 1;
			/* If multiple hits move, first decide the hits count. */
			if(move.getHits() > 1) {
				if(	(!attacker.cannotUseItems() && attacker.getItem() != null && attacker.getItem().maximizeHits()) ||
					(!attacker.hasAbilityDisabled() && attacker.getAbility() != null && attacker.getAbility().maximizeHits())
				) 
					hits = move.getHits();
				else
					hits = RandUtil.getRandWithDistribution(move.getHitsChance());
			}
			for(; i < hits && !breakCycle; ++i) {

				if(!checkProtect(move,false)) {
					move.onMoveFail(this);
					move.reset();
					return;
				}
				
				inflictedDamage = 0;
				hadSubstitute = defender.hasSubstitute();

				/* (Finally) calculate the damage! */
				if(move.isOHKO()) {
					boolean sturdy = false;
					String sturdyMsg = "";
					String sturdyFallback = "";
					for(EffectDealer d : defender.getEffectDealers()) {
						if(d.preventsUserOHKO()) {
							sturdy = true;
							sturdyFallback = d.getName();
							if(d.getPhrase() != null) {
								if(sturdyMsg.length() > 0)
									sturdyMsg += "<br>" + d.getPhrase().replaceAll("\\[pony\\]",defender.getNickname());
								else
									sturdyMsg = d.getPhrase().replaceAll("\\[pony\\]",defender.getNickname());
							}
						}
					}
					for(EffectDealer d : attacker.getEffectDealers()) {
						if(d.preventsTargetOHKO()) {
							sturdy = true;
							sturdyFallback = d.getName();
							if(d.getPhrase() != null) {
								if(sturdyMsg.length() > 0)
									sturdyMsg += "<br>" + d.getPhrase().replaceAll("\\[pony\\]",defender.getNickname());
								else
									sturdyMsg = d.getPhrase().replaceAll("\\[pony\\]",defender.getNickname());
							}
						}
					}
					if(sturdy) {
						if(sturdyMsg.length() > 0) {
							if(battleTask != null)
								battleTask.sendB("|battle|"+sturdyMsg);
							if(echoBattle)
								printMsg(sturdyMsg);
						} else {
							if(battleTask != null)
								battleTask.sendB("|battle|"+move.getName()+" didn't work because of "+sturdyFallback+"!");
							if(echoBattle)
								printMsg(move.getName()+" didn't work because of "+sturdyFallback+"!");
						}
					} else {
						if(defender.hasSubstitute()) {
							substitute[currentPlayer() - 1] = 0;
							defender.setSubstitute(false);
							if(battleTask != null) {
								battleTask.sendB("|battle|"+defender.getNickname()+"'s substitute faded!");
								battleTask.sendB(ally,"|rmsubstitute|opp");
								battleTask.sendB(opp,"|rmsubstitute|ally");
							}
							if(echoBattle) printMsg(defender.getNickname()+"'s substitute faded!");
						} else {
							//If successful OHKO, just KO the opponent.
							inflictedDamage = defender.hp();
							// ...unless an item or ability has something to say.
							defender.trigger("onDamage",this);
							triggerEvent("onDamage");
							latestInflictedDamage = inflictedDamage;
							defender.damage(inflictedDamage); 
							if(battleTask != null && inflictedDamage != 0) {
								battleTask.sendB(ally,"|damage|opp|"+inflictedDamage);
								battleTask.sendB(opp,"|damage|ally|"+inflictedDamage);
								if(defender.hp() <= 0)
									battleTask.sendB("|battle|It's a one hit KO!");
							}
							if(echoBattle && defender.hp() <= 0) printMsg("It's a one hit KO!");
						}
					}
				} else { // NON-OHKO MOVE: call DamageCalculator
					inflictedDamage = dc.calculateBattleDamage(move,this);
					defender.trigger("onDamage", this); // this may change inflictedDamage
					triggerEvent("onDamage");
					if(inflictedDamage != 0) {
						latestInflictedDamage = inflictedDamage;
						if(defender.hasSubstitute()) {
							substitute[currentPlayer() - 1] -= inflictedDamage;
							if(echoBattle) printMsg("The substitute took damage for "+defender.getNickname()+"!");
							if(battleTask != null) {
								battleTask.sendB(ally,"|resultanim|opp|bad|Damage!");
								battleTask.sendB(opp,"|resultanim|ally|bad|Damage!");
							}
							if(substitute[currentPlayer() - 1] <= 0) {
								substitute[currentPlayer() - 1] = 0;
								defender.setSubstitute(false);
								if(battleTask != null) {
									battleTask.sendB("|battle|"+defender.getNickname()+"'s substitute faded!");
									battleTask.sendB(ally,"|rmsubstitute|opp");
									battleTask.sendB(opp,"|rmsubstitute|ally");
								}
								if(echoBattle) printMsg(defender.getNickname()+"'s substitute faded!");
							}
						} else {
							defender.damage(inflictedDamage);
							if(battleTask != null && inflictedDamage != 0) {
								battleTask.sendB(ally,"|damage|opp|"+inflictedDamage);
								battleTask.sendB(opp,"|damage|ally|"+inflictedDamage);
							}
							if(echoBattle) printDamageMsg(defender,inflictedDamage);
						}
					}
				}

				if(defender.isKO()) {
					if(echoBattle) printMsg(defender.getNickname()+" fainted!");
					if(battleTask != null && !sentFaintedMsg[currentPlayer() == 1 ? 1 : 0]) {
						battleTask.sendB(ally,"|fainted|opp");
						battleTask.sendB(opp,"|fainted|ally");
						sentFaintedMsg[(currentPlayer() == 1 ? 1 : 0)] = true;
					}
					breakCycle = true; //if multi-hit move, break.
				}

				/* Apply recoil */
				applyRecoilDamage(move,inflictedDamage);
				if(attacker.isKO()) {
					if(echoBattle) printMsg(attacker.getNickname()+" fainted!");
					if(battleTask != null && !sentFaintedMsg[currentPlayer() -1]) {
						battleTask.sendB(opp,"|fainted|opp");
						battleTask.sendB(ally,"|fainted|ally");
						sentFaintedMsg[currentPlayer() - 1] = true;
					}
					breakCycle = true; //if multi-hit move, break.
				}
				
				/* If move is Struggle, subtract 25% of attacker's max HP as recoil */
				if(move.getName().equals("Struggle") && inflictedDamage != 0) {
					int damage = attacker.damagePerc(25f);
					if(battleTask != null) {
						battleTask.sendB(ally,"|recoil|ally|"+damage);
						battleTask.sendB(opp,"|recoil|opp|"+damage);
					}
					if(echoBattle) printMsg(attacker.getNickname()+" got "+damage+" damage from the recoil!");
				}

				// these are triggered even if move didn't inflict any damage, so it's the
				// callee's job to check that.
				attacker.trigger("afterMoveHit",this);
				defender.trigger("afterMoveHit",this);
				triggerEvent("afterMoveHit");

				/* After move hit successfully, apply additional effects (only if move actually inflicted damage) */
				if(inflictedDamage != 0 || move.effectsAlwaysApply())
					applyAdditionalEffects(move);
			}

			if(move.getHits() > 1) {
				if(battleTask != null) {
					battleTask.sendB("|battle|"+i+" hits!");
				}
				if(echoBattle) printMsg(i+" hits!");
			}
		}

	 	// remove temporary damage and priority boosts
		move.setDamageBoost(0);
		move.setBonusPriority((byte)0);
		if(Debug.pedantic) printDebug("[BattleEngine] ponyUseMove("+move+") ended.");
		return;
	}

	/** Overloaded method that accepts a String as the used move name, and 
	 * throws a MoveNotFoundException if the attacker doesn't have that move. */
	public void ponyUseMove(String moveName) throws MoveNotFoundException {
		if(moveName.equals("Struggle")) {
			ponyUseMove(new Struggle(attacker));
			return;
		}
		for(Move m : attacker.getMoves()) {
			if(m != null && m.getName().equals(moveName)) {
				ponyUseMove(m);
				return;
			}
		}
		throw new MoveNotFoundException(moveName);
	}

	@Override
	public String toString() {
		return "BattleEngine: " + this.hashCode() + 
			"\nattacker= " + attacker +
			"\ndefender= " + defender + 
			"\nteamAttacker= " + teamAttacker +
			"\nteamDefender= " + teamDefender +
			"\nweather= " + weather +
			"\nweatherDuration= "+ (weather == null ? 0 : weather.count); 
	}
	////////////////// PRIVATE METHODS / FIELDS /////////////////
	
	private boolean fullParalysis(final Pony pony) {
		/** Full Paralysis: 25% of chance to happen. */
		return pony.isParalyzed() && rng.nextFloat() < Battle.CHANCE_FULL_PARALYSIS;
	}
	
	private boolean staysPetrified(Pony pony) {
		if(!pony.isPetrified()) return false;

		/** Chance of 10% to heal from Petrification */
		if(rng.nextFloat() < Battle.CHANCE_DEPETRIFICATE) {
			pony.setPetrified(false);
			if(battleTask != null) {
				battleTask.sendB(ally,"|rmstatus|ally|ptr");
				battleTask.sendB(opp,"|rmstatus|opp|ptr");
			}
			if(echoBattle) printMsg(attacker.getNickname()+"'s body's back to normal!");
			return false;
		} else return true;
	}
	
	private boolean staysAsleep(Pony pony) {
		/* Detract 1 from sleepCounter if attacker is asleep */
		if(attacker.isAsleep())
			if(--attacker.sleepCounter <= 0) {
				attacker.setAsleep(false);
				if(battleTask != null) {
					battleTask.sendB(ally,"|rmstatus|ally|slp");
					battleTask.sendB(opp,"|rmstatus|opp|slp");
				}
				if(echoBattle) printMsg(attacker.getNickname()+" woke up!");
			}
			
		return attacker.isAsleep();
	}
	
	/** Subtract 1 from all delayed moves' countDelay, remove flinch flags etc */
	public void incrementTurn() {
		if(Debug.on) printDebug("Delayed Moves: ");
		/*for(Map.Entry<Move,Integer> entry : delayedMoves) {
			--entry.getKey().countDelay;
			if(Debug.on) printDebug("P"+entry.getValue()+": "+entry.getKey()+" [count = "+entry.getValue()+"]");
		}*/
		if(attacker != null) {
			attacker.setFlinched(false);
			attacker.setProtected(false);
			++attacker.activeTurns;
		}
		if(defender != null) {
			defender.setFlinched(false);
			defender.setProtected(false);
			++defender.activeTurns;
		}
		/*Iterator<PersistentEffect> it = persistentEffects.iterator();
		while(it.hasNext()) {
			PersistentEffect pe = it.next();
			if(--pe.count == 0) {
				String side = pe.getSide() == currentPlayer() ? "ally" : "opp";
				if(battleTask != null) {
					battleTask.sendB(ally,"|rmpersistent|"+side+"|"+pe.getName());
					battleTask.sendB(opp,"|rmpersistent|"+(side.equals("ally") ? "opp" : "ally")+"|"+pe.getName());
					battleTask.sendB("|battle|"+pe.getEndPhrase());
				}
				it.remove();
			}
		}*/
		if(weather != null && weather.get() != null && weather.get() != Weather.CLEAR && weather.count > 0)
			--weather.count; // check is done by BattleTask on turn's end.
		sentFaintedMsg[0] = sentFaintedMsg[1] = false;
		sentProtectedMsg[0] = sentProtectedMsg[1] = false;
		resetForcedToSwitch();
	}

	public void clearVolatiles(int side) {
		if(getTeam(side).getActivePony().hasSubstitute()) {
			if(battleTask != null) {
				battleTask.sendB(getConnection(side),"|rmsubstitute|ally|noanim");
				battleTask.sendB(getConnection(side == 1 ? 2 : 1),"|rmsubstitute|opp|noanim");
			}
		}
		Iterator<Hazard> it = getHazards(side).iterator();
		while(it.hasNext()) {
			Hazard hz = it.next();
			if(hz.isVolatile()) { 
				it.remove();
				if(battleTask != null) {
					battleTask.sendB(getConnection(side),"|rmhazard|ally|"+hz.getName()+"|quiet");
					battleTask.sendB(getConnection(side == 1 ? 2 : 1),"|rmhazard|opp|"+hz.getName()+"|quiet");
				}
				break;
			}
		}
	}

	public void resetForcedToSwitch() {
		forcedToSwitch[0] = forcedToSwitch[1] = 0;
	}

	public void triggerEvent(String what) {
		if(Debug.on) printDebug("[BE] called triggerEvent("+what+")");
		Iterator<BattleEvent> it = (Iterator<BattleEvent>)battleEvents.iterator();
		while(it.hasNext()) {
			BattleEvent event = it.next();
			if(Debug.on) printDebug("[BE] Triggering BattleEvent "+event.getName());
			try {
				Method called = event.getClass().getMethod(what, BattleEngine.class);
				called.setAccessible(true);
				try {
					if(Debug.on) printDebug("[BE] triggering "+event.getName()+"::"+what);
					called.invoke(event, this);
				} catch(InvocationTargetException e) {
					printDebug("InvocationTargetException in trigger("+what+")");
					printDebug("Caused by: "+e.getCause());
				} catch(Exception ee) {
					throw new RuntimeException(ee);
				}
			} catch(NoSuchMethodException e) {
				//ok, means effect dealer doesn't activate with that trigger
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
			if(event.getCount() == 0 && !event.survive())
				it.remove();
		}	
	}

	/** This method manages moves which has a delay before resolving;
	 * it pushes all the future BattleEvents generated by the move into
	 * a List, where they'll be triggered just like normal TriggeredEffectDealers.
	 * @return -1 if ponyUseMove should return after this function ended, 1 otherwise.
	 */
	private int manageTurnDelay(Move move) {
		for(BattleEvent event : move.getBattleEvents()) {
			if(Debug.on) printDebug("[BE] inserting BattleEvent "+event.getName());
			event.setSource(attacker);
			battleEvents.add(event);
		}
		if(!attacker.isLockedOnMove()) {
			if(move.getLockingTurns() != 0) {
				attacker.setLockedOnMove(true);
				attacker.setUnlockPhrase(move.getPhrase().replaceAll("\\[pony\\]",attacker.getNickname()));
				if(Debug.on) printDebug("[BE] Locking "+attacker);
				lockingTurns[currentPlayer() - 1] = move.getLockingTurns();
				if(battleTask != null) 
					battleTask.sendB(ally, "|lockon|"+move.getName());
				
				if(move.isBlockingDelayed()) {
					if(battleTask != null)
						battleTask.sendB(ally, "|battle|"+move.getPhrase().replaceAll("\\[pony\\]",attacker.getNickname()));
					return -1;
				} else return 1;
			}
		} 
		return 1;
	}
	
	private boolean missed(Move move) {
		// Sure-fire move
		if(move.getAccuracy() == -1) return false;
		
		int tmpAccMod = 0;
		int tmpEvaMod = 0;

		if(weather != null && weather.get() != null) {
			switch(weather.get()) {
				case DARK:
					if(attacker.getTypes().indexOf(Type.NIGHT) == -1 && attacker.getTypes().indexOf(Type.SHADOW) == -1) {
						tmpAccMod -= 1;
					}
					break;
				default:
					break;
			}
		}

		// probability of hitting
		float probability = move.getAccuracy() *
				Pony.getSpecialStatMod(attacker.accuracyMod() + tmpAccMod) / 
				Pony.getSpecialStatMod(defender.evasionMod() + tmpEvaMod);
		
		float random = 100*rng.nextFloat();
		if(Debug.on) printDebug("[hitcheck] RandomGeneratedNumber: "+random+" vs probability: "+probability);
		return (random > probability);
	}
	
	private void boostStat(boolean isally, Pony pony, String stat, int value) {
		String name = (isally ? "" : "enemy ") + pony.getNickname();
		pony.boost(stat, value);
		if(battleTask != null) {
			battleTask.sendB(ally,"|boost|"+(isally ? "ally" : "opp")+"|"+stat+"|"+value);
			battleTask.sendB(opp,"|boost|"+(isally ? "opp" : "ally")+"|"+stat+"|"+value);
		}
		stat = Pony.toLongStat(stat);
		if(echoBattle) {
			if(value > 2) {
				printMsg("Hey, "+name+"'s "+stat+" rose drastically!");
			} else if(value > 1) {
				printMsg("Hey, "+name+"'s "+stat+" sharply rose!");
			} else if (value > 0) {
				printMsg("Hey, "+name+"'s "+stat+" rose!");
			} else if (value == 0) {
				return;
			} else if(value > -2) {
				printMsg("Hey, "+name+"'s "+stat+" fell!");
			} else if(value > -1) {
				printMsg("Hey, "+name+"'s "+stat+" harshly fell!");
			} else {
				printMsg("Hey, "+name+"'s "+stat+" fell drastically!");
			}
		}

		if(Debug.on) printDebug("Attacker boosts: "+attacker.getBoosts()+"\nDefender boosts: "+defender.getBoosts());
	}
	
	private void printEffectMsg(String side,Pony pony,String effect) {
		boolean alreadyWas;
		String eff = "";
		if(effect.equals("paralyzed")) {
			alreadyWas = pony.isParalyzed();
			eff = "par";
		} else if(effect.equals("confused")) {
			alreadyWas = pony.isConfused();
			eff = "cnf";
		} else if(effect.equals("asleep")) {
			alreadyWas = pony.isAsleep();
			eff = "slp";
		} else if(effect.equals("burned")) {
			alreadyWas = pony.isBurned();
			eff = "brn";
		} else if(effect.equals("petrified")) {
			alreadyWas = pony.isPetrified();
			eff = "ptr";
		} else if(effect.equals("poisoned")) {
			alreadyWas = pony.isPoisoned();
			eff = "psn";
		} else if(effect.equals("badly poisoned")) {
			alreadyWas = pony.isIntoxicated();
			eff = "tox";
		} else alreadyWas = false;
		
		if(effect.equals("flinched")) return;	//flinched status is set quietly, then applied the following (semi-)turn.
		
		if(alreadyWas) {
			if(battleTask != null) {
				battleTask.sendB("|battle|"+pony.getNickname()+" is already "+effect+"!");
			}
			if(echoBattle) printMsg(side+pony.getNickname()+" is already "+effect+"!");
		} else {
			if(battleTask != null) {
				battleTask.sendB(ally,"|addstatus|"+(side.equals("") ? "ally" : "opp")+"|"+eff);
				battleTask.sendB(opp,"|addstatus|"+(side.equals("") ? "opp" : "ally")+"|"+eff);
			}
			if(echoBattle) printMsg(side+pony.getNickname()+" is now "+effect+"!");
		}
	}
		
	/** Checks if additional effects apply and, if so, apply them;
	 * Note: add 'checkProtect(dealer)' to condition if that condition is prevented
	 * by protection.
	 */
	// this method may probably be reorganized better...
	private void applyAdditionalEffects(final EffectDealer dealer) {
		int allyn = team1 == teamAttacker ? 0 : 1;
		int oppn = allyn == 0 ? 1 : 0;
		// Transform effects
		if(dealer.transformsUser() && !attacker.isKO()) {
			if(echoBattle) printMsgnb(attacker.getFullName() + " transformed into ");
			if(dealer.transformUserInto(this) != null)
				teamAttacker.transformActivePonyInto(dealer.transformUserInto(this));
			else
				teamAttacker.transformActivePonyInto(dealer.transformInto(this));
			attacker = teamAttacker.getActivePony();
			if(echoBattle) printMsg(attacker.getName() + "!");
			if(battleTask != null) {
				battleTask.sendB(ally,"|transform|ally|"+attacker.getName());
				battleTask.sendB(opp,"|transform|opp|"+attacker.getName());
				int ponynum = teamAttacker.indexOf(attacker);
				for(int i = 0; i < Pony.MOVES_PER_PONY; ++i) 
					battleTask.sendB(ally,"|setmv|"+ponynum+"|"+i+"|"+
								(attacker.getMove(i) == null ? "none" : attacker.getMove(i) + 
								"|5")
							);
				for(String s : Pony.statNames()) {
					if(defender.getBoost(s) != 0) {
						battleTask.sendB(ally,"|boost|ally|"+s+"|"+defender.getBoost(s));
						battleTask.sendB(opp,"|boost|opp|"+s+"|"+defender.getBoost(s));
					}
				}

			}
		} 
		if(dealer.transformsTarget() && !defender.isKO() && checkProtect(dealer)) {
			if(echoBattle) printMsgnb(defender.getFullName() + " transformed into ");
			if(dealer.transformTargetInto(this) != null)
				teamDefender.transformActivePonyInto(dealer.transformInto(this));
			else
				teamDefender.transformActivePonyInto(dealer.transformTargetInto(this));
			defender = teamDefender.getActivePony();
			if(echoBattle) printMsg(defender.getName() + "!");
			if(battleTask != null) {
				battleTask.sendB(ally,"|transform|opp|"+defender.getName());
				battleTask.sendB(opp,"|transform|ally|"+defender.getName());
				for(int i = 0; i < Pony.MOVES_PER_PONY; ++i) 
					battleTask.sendB(opp,"|setmv|"+i+"|"+
								(defender.getMove(i) == null ? "none" : defender.getMove(i) + 
								"|5")
							);
				for(String s : Pony.statNames()) {
					if(attacker.getBoost(s) != 0) {
						battleTask.sendB(ally,"|boost|opp|"+s+"|"+attacker.getBoost(s));
						battleTask.sendB(opp,"|boost|ally|"+s+"|"+attacker.getBoost(s));
					}
				}
			}
		}

		// Forced switches flags
		if(dealer.forceUserSwitch() > 0 && !attacker.isKO()) {
			if(forcedToSwitch[allyn] == 0 && checkProtect(dealer) /*&& (dealer.forceUserSwitch() < 3 || inflictedDamage > 0) */)
				forcedToSwitch[allyn] = dealer.forceUserSwitch();
		} else {
			forcedToSwitch[allyn] = 0;
		}
		if(dealer.forceTargetSwitch() > 0 && !defender.isKO()) {
			if(forcedToSwitch[oppn] == 0 && checkProtect(dealer) /*&& (dealer.forceTargetSwitch() < 3 || inflictedDamage > 0)*/)
				forcedToSwitch[oppn] = dealer.forceTargetSwitch();
		} else {
			forcedToSwitch[oppn] = 0;
		}

		// Protection effects
		if(dealer.protectUser() && !attacker.isKO()) {
			attacker.setProtected(true);
		}

		// Damage Effects
		if(dealer.damageUser() != 0 && !attacker.isKO()) {
			int inflictedDamage = dealer.damageUser();
			attacker.damage(inflictedDamage);
			if(battleTask != null && inflictedDamage != 0) {
				battleTask.sendB(ally,"|damage|ally|"+inflictedDamage);
				battleTask.sendB(opp,"|damage|opp|"+inflictedDamage);
			}
		}
		if(dealer.damageTarget() != 0 && !defender.isKO() && checkProtect(dealer,false)) {
			int inflictedDamage = dealer.damageTarget();
			defender.damage(inflictedDamage);
			if(battleTask != null && inflictedDamage != 0) {
				battleTask.sendB(ally,"|damage|opp|"+inflictedDamage);
				battleTask.sendB(opp,"|damage|ally|"+inflictedDamage);
			}
		}
		if(dealer.damageUserPerc() != 0 && !attacker.isKO()) {
			int inflictedDamage = attacker.damagePerc(dealer.damageUserPerc());
			if(battleTask != null && inflictedDamage != 0) {
				battleTask.sendB(ally,"|damage|ally|"+inflictedDamage);
				battleTask.sendB(opp,"|damage|opp|"+inflictedDamage);
			}
		}
		if(dealer.damageTargetPerc() != 0 && !defender.isKO() && checkProtect(dealer,false)) {
			int inflictedDamage = defender.damagePerc(dealer.damageTargetPerc());
			if(battleTask != null && inflictedDamage != 0) {
				battleTask.sendB(ally,"|damage|opp|"+inflictedDamage);
				battleTask.sendB(opp,"|damage|ally|"+inflictedDamage);
			}
		}
		/*if(dealer.spawnPersistentEffect() != null) {
			PersistentEffect pe = dealer.spawnPersistentEffect().getValue();
			boolean ok = true;
			String side = dealer.spawnPersistentEffect().getKey();
			if(side.equals("ally")) {
				pe.setSide(currentPlayer());
			} else if(side.equals("opp")) {
				pe.setSide(currentPlayer() == 1 ? 2 : 1);
			} else {
				printDebug("[BE] Error: side is " + dealer.spawnPersistentEffect().getKey() + " for spawnPersistentEffect!");
				ok = false;
			}
			if(ok) {
				if(battleTask != null) {
					battleTask.sendB(ally,"|persistent|"+side+"|"+pe.getName());
					battleTask.sendB(opp,"|persistent|"+(side.equals("ally") ? "opp" : "ally")+"|"+pe.getName());
					battleTask.sendB("|battle|"+pe.getPhrase());
				}
				persistentEffects.add(pe);
			}
		}*/
		if(!attacker.isKO()) {
			if(dealer.spawnSubstitute()) {
				if(attacker.hp() <= attacker.maxhp() / 4) {
					if(battleTask != null)
						battleTask.sendB("|battle|"+attacker.getNickname()+" is too weak to create a Substitute!");
				} else {
					int selfDmg = attacker.damagePerc(25f);	
					if(battleTask != null) {
						battleTask.sendB(ally,"|damage|ally|"+selfDmg+"|quiet");
						battleTask.sendB(opp,"|damage|opp|"+selfDmg+"|quiet");
					}
					//if(echoBattle && dealer.getPhrase().length() > 0) printMsg(dealer.getPhrase());
					substitute[allyn] = attacker.setSubstitute(true);
					if(Debug.on) printDebug("Substitute lives: ["+substitute[0]+","+substitute[1]+"]");
					if(battleTask != null) {
						battleTask.sendB(ally,"|substitute|ally");
						battleTask.sendB(opp,"|substitute|opp");
						if(dealer.getPhrase().length() > 0)
							battleTask.sendB("|battle|"+
								dealer.getPhrase().replaceAll("\\[pony\\]",attacker.getNickname()));
						else
							battleTask.sendB("|battle|"+
								attacker.getNickname()+" put itself behind a Substitute!");
					}
				}
			}
			// Stats Modifiers
			if(rng.nextFloat() < dealer.boostUserAtk().getValue()) {
				int boost = dealer.boostUserAtk().getKey();
				tryStatChange(attacker,"atk",boost);
			}
			if(rng.nextFloat() < dealer.boostUserDef().getValue()) {
				int boost = dealer.boostUserDef().getKey();
				tryStatChange(attacker,"def",boost);
			}
			if(rng.nextFloat() < dealer.boostUserSpatk().getValue()) {
				int boost = dealer.boostUserSpatk().getKey();
				tryStatChange(attacker,"spatk",boost);
			}
			if(rng.nextFloat() < dealer.boostUserSpdef().getValue()) {
				int boost = dealer.boostUserSpdef().getKey();
				tryStatChange(attacker,"spdef",boost);
			}
			if(rng.nextFloat() < dealer.boostUserSpeed().getValue()) {
				int boost = dealer.boostUserSpeed().getKey();
				tryStatChange(attacker,"speed",boost);
			}
			if(rng.nextFloat() < dealer.boostUserAccuracy().getValue()) {
				int boost = dealer.boostUserAccuracy().getKey();
				tryStatChange(attacker,"accuracy",boost);
			}
			if(rng.nextFloat() < dealer.boostUserEvasion().getValue()) {
				int boost = dealer.boostUserEvasion().getKey();
				tryStatChange(attacker,"evasion",boost);
			}
		}
		if(!defender.isKO() && !defender.hasSubstitute()) {
			// effects to defender apply only if it isn't protected.
			if(rng.nextFloat() < dealer.boostTargetAtk().getValue() && checkProtect(dealer)) {
				int boost = dealer.boostTargetAtk().getKey();
				tryStatChange(defender,"atk",boost);
			}
			if(rng.nextFloat() < dealer.boostTargetDef().getValue() && checkProtect(dealer)) {
				int boost = dealer.boostTargetDef().getKey();
				tryStatChange(defender,"def",boost);
			}
			if(rng.nextFloat() < dealer.boostTargetSpatk().getValue() && checkProtect(dealer)) {
				int boost = dealer.boostTargetSpatk().getKey();
				tryStatChange(defender,"spatk",boost);
			}
			if(rng.nextFloat() < dealer.boostTargetSpdef().getValue() && checkProtect(dealer)) {
				int boost = dealer.boostTargetSpdef().getKey();
				tryStatChange(defender,"spdef",boost);
			}
			if(rng.nextFloat() < dealer.boostTargetSpeed().getValue() && checkProtect(dealer)) {
				int boost = dealer.boostTargetSpeed().getKey();
				tryStatChange(defender,"speed",boost);
			}
			if(rng.nextFloat() < dealer.boostTargetAccuracy().getValue() && checkProtect(dealer)) {
				int boost = dealer.boostTargetAccuracy().getKey();
				tryStatChange(defender,"accuracy",boost);
			}
			if(rng.nextFloat() < dealer.boostTargetEvasion().getValue() && checkProtect(dealer)) {
				int boost = dealer.boostTargetEvasion().getKey();
				tryStatChange(defender,"evasion",boost);
			}
			if(rng.nextFloat() < dealer.getTargetConfusion() && !defender.isConfused() && checkProtect(dealer)) {
				float ignore = 0f;
				for(EffectDealer ed : defender.getEffectDealers())
					ignore += ed.preventNegativeCondition("cnf");
				if(rng.nextFloat() < ignore) {
					if(battleTask != null)
						battleTask.sendB("|battle|"+defender.getNickname()+" doesn't become confused!");
				} else {
					printEffectMsg("enemy ",defender,"confused");
					defender.setConfused(true);
					defender.confusionCounter = rng.nextInt(Battle.MAX_CONFUSION_DURATION+1);	//confusion lasts 1-Y turns.
					if(Debug.on) printDebug("Confusion count = "+defender.confusionCounter);
				}
			}
			if(rng.nextFloat() < dealer.getTargetFlinch() && checkProtect(dealer)) {
				float ignore = 0f;
				for(EffectDealer ed : defender.getEffectDealers())
					ignore += ed.preventNegativeCondition("flinch");
				if(rng.nextFloat() < ignore) {
					if(battleTask != null)
						battleTask.sendB("|battle|"+defender.getNickname()+" doesn't flinch!");
				} else {
					printEffectMsg("enemy ",defender,"flinched");
					defender.setFlinched(true);
				}
			}
			if(rng.nextFloat() < dealer.healTargetStatus() && defender.hasNegativeCondition()) {
				defender.healStatus();
				if(battleTask != null) {
					battleTask.sendB(ally,"|rmstatus|opp");
					battleTask.sendB(opp,"|rmstatus|ally");
				}
			}
		}	
		// Side effects (special conditions etc)
		// defender
		if(!defender.isKO() && !defender.hasNegativeCondition() && !defender.hasSubstitute()) {
			if(rng.nextFloat() < dealer.getTargetParalysis() && checkProtect(dealer)) {
				float ignore = 0f;
				for(EffectDealer ed : defender.getEffectDealers())
					ignore += ed.preventNegativeCondition("par");
				if(rng.nextFloat() < ignore) {
					if(battleTask != null)
						battleTask.sendB("|battle|"+defender.getNickname()+" doesn't become paralyzed!");
				} else {
					printEffectMsg("enemy ",defender,"paralyzed");
					defender.setParalyzed(true);
				}
			}
			if(!defender.hasNegativeCondition() && rng.nextFloat() < dealer.getTargetPoison() && checkProtect(dealer)) {
				float ignore = 0f;
				for(EffectDealer ed : defender.getEffectDealers())
					ignore += ed.preventNegativeCondition("psn");
				if(rng.nextFloat() < ignore) {
					if(battleTask != null)
						battleTask.sendB("|battle|"+defender.getNickname()+" doesn't become poisoned!");
				} else {
					printEffectMsg("enemy ",defender,"poisoned");
					defender.setPoisoned(true);
				}
			}
			if(!defender.hasNegativeCondition() && rng.nextFloat() < dealer.getTargetToxic() && checkProtect(dealer)) {
				float ignore = 0f;
				for(EffectDealer ed : defender.getEffectDealers())
					ignore += ed.preventNegativeCondition("psn");
				if(rng.nextFloat() < ignore) {
					if(battleTask != null)
						battleTask.sendB("|battle|"+defender.getNickname()+" doesn't become poisoned!");
				} else {
					printEffectMsg("enemy ",defender,"badly poisoned");
					defender.setIntoxicated(true);
				}
			}
			if(!defender.hasNegativeCondition() && rng.nextFloat() < dealer.getTargetBurn() && checkProtect(dealer)) {
				float ignore = 0f;
				for(EffectDealer ed : defender.getEffectDealers())
					ignore += ed.preventNegativeCondition("brn");
				if(rng.nextFloat() < ignore) {
					if(battleTask != null)
						battleTask.sendB("|battle|"+defender.getNickname()+" doesn't become burned!");
				} else {
					printEffectMsg("enemy ",defender,"burned");
					defender.setBurned(true);
				}
			}
			if(!defender.hasNegativeCondition() && rng.nextFloat() < dealer.getTargetSleep() && checkProtect(dealer)) {
				float ignore = 0f;
				for(EffectDealer ed : defender.getEffectDealers())
					ignore += ed.preventNegativeCondition("slp");
				if(rng.nextFloat() < ignore) {
					if(battleTask != null)
						battleTask.sendB("|battle|"+defender.getNickname()+" doesn't become asleep!");
				} else {
					printEffectMsg("enemy ",defender,"asleep");
					defender.setAsleep(true);
					defender.sleepCounter = rng.nextInt(Battle.MAX_SLEEP_DURATION + 1);	//sleep lasts 1-X turns.
				}
			}
			if(!defender.hasNegativeCondition() && rng.nextFloat() < dealer.getTargetPetrify() && checkProtect(dealer)) {
				float ignore = 0f;
				for(EffectDealer ed : defender.getEffectDealers())
					ignore += ed.preventNegativeCondition("ptr");
				if(rng.nextFloat() < ignore) {
					if(battleTask != null)
						battleTask.sendB("|battle|"+defender.getNickname()+" doesn't become petrified!");
				} else {
					printEffectMsg("enemy ",defender,"petrified");
					defender.setPetrified(true);
				}
			}
		}

		// attacker
		if(!attacker.isKO()) {
			if(!attacker.hasNegativeCondition() && rng.nextFloat() < dealer.getUserParalysis()) {
				printEffectMsg("",attacker,"paralyzed");
				attacker.setParalyzed(true);
			}
			if(!attacker.hasNegativeCondition() && rng.nextFloat() < dealer.getUserPoison()) {
				printEffectMsg("",attacker,"poisoned");
				attacker.setPoisoned(true);
			}
			if(!attacker.hasNegativeCondition() && rng.nextFloat() < dealer.getUserToxic()) {
				printEffectMsg("",attacker,"badly poisoned");
				attacker.setIntoxicated(true);
			}
			if(!attacker.hasNegativeCondition() && rng.nextFloat() < dealer.getUserBurn()) {
				printEffectMsg("",attacker,"burned");
				attacker.setBurned(true);
			}
			if(!attacker.hasNegativeCondition() && rng.nextFloat() < dealer.getUserSleep()) {
				printEffectMsg("",attacker,"asleep");
				attacker.setAsleep(true);
			}
			if(!attacker.hasNegativeCondition() && rng.nextFloat() < dealer.getUserPetrify()) {
				printEffectMsg("",attacker,"petrified");
				attacker.setPetrified(true);
			}
			if(rng.nextFloat() < dealer.getUserConfusion() && !attacker.isConfused()) {
				printEffectMsg("",attacker,"confused");
				attacker.setConfused(true);
				attacker.confusionCounter = rng.nextInt(Battle.MAX_CONFUSION_DURATION+1);
				if(Debug.on) printDebug("Confusion count = "+defender.confusionCounter);
			}
			if(rng.nextFloat() < dealer.getUserFlinch()) {
				printEffectMsg("",attacker,"flinched");
				attacker.setFlinched(true);
			}
		
			// Healing Effects 
			if(dealer.healUser() > 0) {
				attacker.setHp((int)(attacker.hp()+(attacker.maxhp()*dealer.healUser())));
				if(battleTask != null) {
					battleTask.sendB(ally,"|damage|ally|" + (-attacker.maxhp()*dealer.healUser()));
					battleTask.sendB(opp,"|damage|opp|" + (-attacker.maxhp()*dealer.healUser()));
				}
			}
			if(rng.nextFloat() < dealer.healUserStatus() && attacker.hasNegativeCondition()) {
				attacker.healStatus();
				if(battleTask != null) {
					battleTask.sendB(ally,"|rmstatus|ally");
					battleTask.sendB(opp,"|rmstatus|opp");
				}
			}
			if(rng.nextFloat() < dealer.removeUserNegativeStatModifiers()) {
				for(String mod : Pony.statNames()) {
					int currentMod = attacker.boost(mod,0);
					if(currentMod < 0) {
						if(battleTask != null) {
							battleTask.sendB(ally,"|boost|ally|"+mod+"|"+ (-currentMod));
							battleTask.sendB(opp,"|boost|opp|"+mod+"|"+ (-currentMod));
						}
						attacker.boost(mod,-currentMod);
					}
				}
			}
			if(rng.nextFloat() < dealer.removeUserPositiveStatModifiers()) {
				for(String mod : Pony.statNames()) {
					int currentMod = attacker.boost(mod,0);
					if(currentMod > 0) {
						if(battleTask != null) {
							battleTask.sendB(ally,"|boost|ally|"+mod+"|"+ (-currentMod));
							battleTask.sendB(opp,"|boost|opp|"+mod+"|"+ (-currentMod));
						}
						attacker.boost(mod,-currentMod);
					}
				}

			}
		}

		if(rng.nextFloat() < dealer.healAllTeamStatus()) {
			teamAttacker.healTeamStatus();
			if(battleTask != null) {
				battleTask.sendB(ally,"|healteam|ally");
				battleTask.sendB(opp,"|healteam|opp");
			}
		}

		if(!defender.isKO()) {
			if(rng.nextFloat() < dealer.removeTargetNegativeStatModifiers()) {
				for(String mod : Pony.statNames()) {
					int currentMod = defender.boost(mod,0);
					if(currentMod < 0) {
						if(battleTask != null) {
							battleTask.sendB(ally,"|boost|opp|"+mod+"|"+ (-currentMod));
							battleTask.sendB(opp,"|boost|ally|"+mod+"|"+ (-currentMod));
						}
						defender.boost(mod,-currentMod);
					}
				}
			}
			if(rng.nextFloat() < dealer.removeTargetPositiveStatModifiers()) {
				for(String mod : Pony.statNames()) {
					int currentMod = defender.boost(mod,0);
					if(currentMod > 0) {
						if(battleTask != null) {
							battleTask.sendB(ally,"|boost|opp|"+mod+"|"+ (-currentMod));
							battleTask.sendB(opp,"|boost|ally|"+mod+"|"+ (-currentMod));
						}
						defender.boost(mod,-currentMod);
					}
				}
			}
			if(dealer.tauntTarget() && checkProtect(dealer)) {
				defender.setTaunted(true);
				defender.tauntCounter = rng.nextInt(5) + 2;
				if(battleTask != null) {
					battleTask.sendB(ally,"|taunt|opp");
					battleTask.sendB(opp,"|taunt|ally");
				}
			}
		}
		
		// Weather Effects 
		if(dealer.changeWeather() != null) {
			if(weather == null) weather = new WeatherHolder(dealer.changeWeather());
			else weather.set(dealer.changeWeather());
			if(echoBattle) printMsgnb("Weather changed to "+weather+"!");
			if(Debug.on) if(echoBattle) printMsg(" (count: "+weather.count+")");
			else if(echoBattle) printMsg("");
			if(battleTask != null) {
				battleTask.sendB("|setweather|"+weather.get());
			}
		}

		// Move-Specific Effects 
		if(dealer instanceof Move) {
			/* Hazard Effects */
			Hazard hazard = ((Move)dealer).getHazard();
			if(hazard != null) {
				// check if hazard is already present and, if layerable, add layer
				boolean found = false;
				if(teamAttacker == team1) {
					if(Debug.on) printDebug("[BE] Searching for hazard "+
							hazard.getName()+" in p2's side...\nhazards2="+hazards2);
					for(Hazard h : hazards2) {
						if(h.getName().equals(hazard.getName())) {
							found = true;
							if(Debug.on) printDebug("[BE] Found it. Layers: "+
									h.getLayers()+" / "+h.getMaxLayers());
							if(h.getLayers() < h.getMaxLayers()) {
								h.addLayer();
								if(echoBattle) printMsg(hazard.getSetupPhrase()[0]);
								if(battleTask != null) {
									battleTask.sendB(ally,"|addhazard|opp|"+
											hazard.getClass().getSimpleName());
									battleTask.sendB(opp,"|addhazard|ally|"+
											hazard.getClass().getSimpleName());
									battleTask.sendB(ally,"|battle|"+hazard.getSetupPhrase()[0]);
									battleTask.sendB(opp,"|battle|"+hazard.getSetupPhrase()[1]);
								}
							} else {
								if(echoBattle) printMsg("Cannot stack more layers of "+hazard.getName());
								if(battleTask != null)
									battleTask.sendB("|battle|Cannot stack more than "+
										hazard.getMaxLayers()+" layers of "+hazard.getName()+"!");
							}
							break;
						}
					}
					// else, if hazard was not present, just add it.
					if(!found) {
						if(Debug.on) printDebug("[BE] Not found. Adding hazard.");
						hazard.setSide(2);
						hazard.setLayers(1);
						hazards2.add(hazard);
						if(echoBattle) printMsg(hazard.getSetupPhrase()[0]);
						if(battleTask != null) {
							battleTask.sendB(ally,"|addhazard|opp|"+hazard.getClass().getSimpleName());
							battleTask.sendB(opp,"|addhazard|ally|"+hazard.getClass().getSimpleName());
							battleTask.sendB(ally,"|battle|"+hazard.getSetupPhrase()[0]);
							battleTask.sendB(opp,"|battle|"+hazard.getSetupPhrase()[1]);
						}
					}
				} else if(teamAttacker == team2) {
					if(Debug.on) printDebug("[BE] Searching for hazard "+
							hazard.getName()+" in p1's side...\nhazards1="+hazards1);
					for(Hazard h : hazards1) {
						if(h.getName().equals(hazard.getName())) {
							found = true;
							if(Debug.on) printDebug("[BE] Found it. Layers: "+
									h.getLayers()+" / "+h.getMaxLayers());
							if(h.getLayers() < h.getMaxLayers()) {
								h.addLayer();
								if(echoBattle) printMsg(hazard.getSetupPhrase()[0]);
								if(battleTask != null) {
									battleTask.sendB(ally,"|addhazard|opp|"+
											hazard.getClass().getSimpleName());
									battleTask.sendB(opp,"|addhazard|ally|"+
											hazard.getClass().getSimpleName());
									battleTask.sendB(ally,"|battle|"+hazard.getSetupPhrase()[0]);
									battleTask.sendB(opp,"|battle|"+hazard.getSetupPhrase()[1]);
								}
							} else {
								if(echoBattle) printMsg("Cannot stack more layers of "+hazard.getName());
								if(battleTask != null)
									battleTask.sendB("|battle|Cannot stack more than "+
										hazard.getMaxLayers()+" layers of "+hazard.getName()+"!");
							}
							break;
						}
					}
					// else, if hazard was not present, just add it.
					if(!found) {
						if(Debug.on) printDebug("[BE] Not found. Adding hazard.");
						hazard.setSide(1);
						hazard.setLayers(1);
						hazards1.add(hazard);
						if(echoBattle) printMsg(hazard.getSetupPhrase()[0]);
						if(battleTask != null) {
							battleTask.sendB(ally,"|addhazard|opp|"+hazard.getClass().getSimpleName());
							battleTask.sendB(opp,"|addhazard|ally|"+hazard.getClass().getSimpleName());
							battleTask.sendB(ally,"|battle|"+hazard.getSetupPhrase()[0]);
							battleTask.sendB(opp,"|battle|"+hazard.getSetupPhrase()[1]);
						}
					}
				} else {
					throw new RuntimeException("Error in BattleEngine.applyAdditionalEffects: teamAttacker is neither team1 nor team2!");
				}
			} //~ end hazard effects

			// Locking effects
			Move mv = (Move)dealer;
			if(mv.locksTargetOn() != null && checkProtect(dealer) && !defender.isLockedOnMove()) {
				if(mv.locksTargetOn().equals("last")) {
					if(defender.getLastMoveUsed() == null) {
						if(battleTask != null) {
							battleTask.sendB(ally,"|fail|ally");
							battleTask.sendB(opp,"|fail|opp");
						}
						return;
					}
					if(defender.getLastMoveUsed().getPP() == 0) {
						if(battleTask != null)
							battleTask.sendB(opp,"|lockon|Struggle");
					} else {
						if(battleTask != null)
							battleTask.sendB(opp,"|lockon|"+defender.getLastMoveUsed());
					}
				} else if(mv.locksTargetOn().equals("random")) {
					List<Integer> li = new ArrayList<>();
					for(int i = 0; i < defender.getMoves().size(); ++i)
						li.add(i);
					int sel = -1;
					do {
						sel = rng.nextInt(li.size());
						if(defender.getMove(sel).getPP() == 0) {
							li.remove(sel);
							sel = -1;
						}
					} while(sel == -1 && li.size() > 0);
					if(sel == -1) {
						if(battleTask != null) {
							battleTask.sendB(ally,"|fail|ally");
							battleTask.sendB(opp,"|fail|opp");
						}
						return;
					} else {
						if(battleTask != null) 
							battleTask.sendB("|lockon|"+defender.getMove(sel));
					}
				} else {
					// check if move exists before trying locking defender
					try {
						Move tmp = MoveCreator.create(mv.locksTargetOn());
					} catch(ClassNotFoundException e) {
						if(battleTask != null)
							battleTask.sendB("|error|But it failed (move "+mv.locksTargetOn()+" not found)");
						return;
					} catch(ReflectiveOperationException e) {
						printDebug("[BE] ReflectiveOperationException:");
						e.printStackTrace();
						return;
					}
					if(battleTask != null)
						battleTask.sendB("|lockon|"+mv.locksTargetOn());
				}						
				defender.setLockedOnMove(true);
				defender.setUnlockPhrase(mv.getPhrase().replaceAll("\\[pony\\]",defender.getNickname()));
				if(battleTask != null) {
					battleTask.sendB(ally,"|resultanim|opp|neutral|Locked!");
					battleTask.sendB(opp,"|resultanim|ally|neutral|Locked!");
				}
				lockingTurns[currentPlayer() == 1 ? 1 : 0] = mv.getLockingTurns();
			} //~ end locking effects

			// Death scheduling effects
			if(mv.getSelfKODelay() > -1 && checkProtect(dealer) && !attacker.isDeathScheduled()) {
				if(battleTask != null && mv.getPhrase() != null && mv.getPhrase().length() > 0)
					battleTask.sendB("|battle|"+mv.getPhrase().replaceAll("\\[pony\\]",attacker.getNickname()));
				attacker.scheduleDeath(mv.getSelfKODelay());
			}
			if(mv.getTargetKODelay() > -1 && checkProtect(dealer) && !defender.isDeathScheduled()) {
				if(battleTask != null && mv.getPhrase() != null && mv.getPhrase().length() > 0)
					battleTask.sendB("|battle|"+mv.getPhrase().replaceAll("\\[pony\\]",defender.getNickname()));
				defender.scheduleDeath(mv.getTargetKODelay());
			}

		} //~ end move-specific effects

		if(dealer.removesAllyHazards()) {
			if(teamAttacker == team1)
				hazards1.clear();
			else
				hazards2.clear();
			for(Hazard h : hazards1) {
				if(echoBattle) printMsg(h.getName()+" disappeared from the attacker's field!");
			}
			if(battleTask != null) {
				battleTask.sendB(ally,"|rmhazard|ally");
				battleTask.sendB(opp,"|rmhazard|opp");
			}
		}
		if(dealer.removesEnemyHazards()) {
			if(teamDefender == team2)
				hazards2.clear();
			else
				hazards1.clear();
			for(Hazard h : hazards2) {
				if(echoBattle) printMsg(h.getName()+" disappeared from the opponent's field!");
			}
			if(battleTask != null) {
				battleTask.sendB(ally,"|rmhazard|opp");
				battleTask.sendB(opp,"|rmhazard|ally");
			}
		}
	}

	public void tryStatChange(Pony pony,String stat,int boost) {
		boolean immune = false;
		for(EffectDealer ed : pony.getEffectDealers()) {
			if(ed.ignoreBoosts(Pony.toBriefStat(stat)) || (ed.ignoreNegativeBoosts(Pony.toBriefStat(stat)) && boost < 0)) {
				immune = true;
				break;
			}
		}
		if(!immune) {
			boostStat(pony == attacker, pony, stat, boost);
		} else {
			if(battleTask != null)
				battleTask.sendB("|battle|"+pony.getNickname()+" ignores the stat change!");
		}
	}

	/** Checks if defender is protected; if it is, prints a message and returns False, else returns True 
	 * @param dealer The EffectDealer that affects the target
	 * @param considerSubstitute If true, target is considered protected also when behind Substitute.
	 * @return True if attack can hit defender, False if defender is protected.
	 */
	private boolean checkProtect(final EffectDealer dealer, boolean considerSubstitute) {
		if(Debug.on) printDebug("[BE] Called checkProtect("+dealer+")");
		if(defender.isProtected() && !dealer.ignoreProtection()) {
			if(!sentProtectedMsg[currentPlayer() - 1]) {
				if(battleTask != null) {
					battleTask.sendB(ally,"|protected|opp");
					battleTask.sendB(opp,"|protected|ally");

				}
				if(echoBattle) printMsg(defender.getName() + " is protected!");
				sentProtectedMsg[currentPlayer() - 1] = true;
			}
			defender.protectCounter++;
			if(Debug.on) printDebug("[BE.checkProtect("+dealer+")] protectCounter = "+defender.protectCounter);
			return false;
		} else {
			defender.protectCounter = 0;
			return !(considerSubstitute && defender.hasSubstitute());
		}
	}

	private boolean checkProtect(final EffectDealer dealer) {
		return checkProtect(dealer, true);
	}

	/** Inflicts recoil damage equal to dealer.getRecoil() * inflictedDamage */
	private void applyRecoilDamage(EffectDealer dealer,int inflictedDamage) {
		if(dealer.getRecoil() == 0 || inflictedDamage == 0) return;

		int recoilDamage = (int)(dealer.getRecoil() * inflictedDamage);
		attacker.damage(recoilDamage);
		if(Debug.pedantic) printDebug("Recoil damage = "+recoilDamage);
		if(battleTask != null) {
			battleTask.sendB(ally,"|recoil|ally|"+recoilDamage);
			battleTask.sendB(opp,"|recoil|opp|"+recoilDamage);
		}
		if(echoBattle) printMsg(attacker.getName()+" got " + (Debug.on ? recoilDamage + " ("+dealer.getRecoil()*100+"%) " : "") + "damage from the recoil!");
	}

	/** If attacker or defender is null, throws exception */
	private void checkActivePony() throws NoActivePonyException {
		if(attacker == null || defender == null) {
			StringBuilder msg = new StringBuilder("");
			if(attacker == null) {
				msg.append("Attacker is null! ");
			}
			if(defender == null) {
				msg.append("Defender is null!");
			}
			throw new NoActivePonyException(msg.toString());
		}
	}

	private final Team team1;
	private final Team team2;
	private Team teamAttacker;
	private Team teamDefender;
	private Pony attacker;
	private Pony defender;
	private WeatherHolder weather = WeatherHolder.getClearWeather();
	private Set<Hazard> hazards1;
	private Set<Hazard> hazards2;
	private DamageCalculator dc;
	private Connection ally;
	private Connection opp;
	private Random rng;
	private BattleTask battleTask;
	private int moveStack;
	private List<BattleEvent> battleEvents = new LinkedList<>();
	/** List of persistent effects on p1/2's field */
	//private List<PersistentEffect> persistentEffects = new LinkedList<>();
	/** Remaining HP of substitutes */
	private int[] substitute = { 0, 0 };
	/** 0: false, 1: true - can decide pony to switch in, 2: true - switch to random. */
	private byte[] forcedToSwitch = { 0, 0 };
	/** Move being used 'right now' */
	private Move currentMove;
	private int inflictedDamage, latestInflictedDamage;
	/** This is used for SuckerPunch-like moves et sim. */
	private Move[] chosenMove = new Move[2];
	private boolean[] sentFaintedMsg = { false, false };
	private boolean[] sentProtectedMsg = { false, false };
	/** List of delayed moves { (move, player), (move, player), ... } */
	//private List<Map.Entry<Move,Integer>> delayedMoves = new ArrayList<Map.Entry<Move,Integer>>();
	private int[] lockingTurns = { 0, 0 };
	private boolean hadSubstitute;
	private boolean breakCycle;
	boolean echoBattle = true;
}
