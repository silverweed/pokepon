package pokepon.battle;

import pokepon.player.*;
import pokepon.pony.*;
import pokepon.util.*;
import pokepon.net.jack.*;
import pokepon.move.*;
import pokepon.item.*;
import static pokepon.util.MessageManager.*;
import java.util.*;
import java.io.*;
import java.util.concurrent.*;

/** Class implementing 1 vs 1 battle.
 *
 * @author silverweed
 */
public class Battle1v1 extends Battle {

	/** Constructor used for tests */
	public Battle1v1(Player p1, Player p2) {
		this.p1 = p1;
		this.p2 = p2;
	}

	/** Constructor used by the Pokepon Server: initialize() must be called before starting battle. */
	public Battle1v1(Connection c1,Connection c2) {
		this.c1 = c1;
		this.c2 = c2;
		p1 = new Player(c1.getName());
		p2 = new Player(c2.getName());
	}

	/** Makes pre-battle preparations: retrieves and validates teams and so on 
	 * @return true - if initialization succeeded; false - otherwise.
	 */
	@Override
	public boolean initialize(boolean randomBattle) throws InterruptedException {
		if(randomBattle) {
			// generate random teams
			p1.setTeam(randomTeam());
			p2.setTeam(randomTeam());
		} else {
			// concurrently retreive teams
			if(Debug.on) printDebug("[BATTLE "+p1+","+p2+"]: Retrieving teams...");
			List<Callable<Boolean>> tr = new ArrayList<Callable<Boolean>>();
			tr.add(new TeamRetreiver(c1,p1));
			tr.add(new TeamRetreiver(c2,p2));
			List<Future<Boolean>> results = executor.invokeAll(tr);
			
			try {
				if(Debug.pedantic) printDebug("Future.get(1)...");
				if(!results.get(0).get(15,TimeUnit.SECONDS)) {
					printDebug(p1.getName()+"'s team not retreived correctly.");
					return false;
				}
				if(Debug.pedantic) printDebug("Future.get(2)...");
				if(!results.get(1).get(15,TimeUnit.SECONDS)) {
					printDebug(p2.getName()+"'s team not retreived correctly");
					return false;
				}
			} catch(TimeoutException e) {
				printDebug("Timeout: "+e);
				return false;
			} catch(ExecutionException e) {
				printDebug("Caught exception in result.get(): "+e);
				e.printStackTrace();
				return false;
			}
			if(Debug.on) printDebug("[BATTLE "+c1.getName()+" ~ "+c2.getName()+"] Teams retreived correctly.");
		}

		return true;
	}

	public Player getPlayer(int num) {
		if(num == 1) return p1;
		else if(num == 2) return p2;
		else throw new IllegalArgumentException("[Battle.getPlayer()]: num is "+num);
	}

	public Team getTeam(int num) {
		if(num == 1) return p1.getTeam();
		else if(num == 2) return p2.getTeam();
		else throw new IllegalArgumentException("[Battle.getTeam()]: num is "+num);
	}
	
	@Override
	protected Team randomTeam() {
		// This algorithm is very naive, especially when compared to PS's one.
		// I'll probably improve this in time, but for now should be better than
		// a flat logic-less random algorithm.
		Team team = new Team();
		int count = 0, wimps = 0, ubers = 0;
		Set<String> uberPonies = RuleSet.Predefined.NOUBER.getBannedPonies();
		ponies:
		while(count < Team.MAX_TEAM_SIZE) {
			try {
				Pony pony = PonyCreator.createRandom();
				if(Debug.on) printDebug("[randomTeam] created pony "+pony.getName());

				// reject if team already has one
				if(team.getPony(pony.getName()) != null) continue;

				// for now we judge uberness by BST; in future we may introduce tiers.
				if(uberPonies.contains(pony.getName())) ++ubers;
				else if(pony.bst() <= 420) ++wimps;
				
				// to keep things balanced, prevent too many ubers or wimps in a team.
				if(wimps > 2) continue;
				if(ubers > 2) continue;

				/* Choose level. 
				 * We follow a PokemonShowdown-like algorithm:
				 * level is based on BST. Min level is 70, max is 99.
				 * 600+ BST is 70, 300 is 99, and intermediate between those values.
				 * More specifically, every 10.35 BST adds a level from 70 to 99.
				 */
				// Special cases
				if(pony.getName().equals("Tirek")) {
					pony.setLevel(70);
				} else {
					int bst = Math.min(600, Math.max(300, pony.bst()));
					pony.setLevel(70 + (int)Math.floor((600 - bst) / 10.35));
				}

				/* Choose moves. 
				 * Try not choosing moves of the same type, and try to
				 * have at least 1 damaging move. 
				 * Tend not to give MirrorPond to low-HP ponies 
				 */
				List<String> moves = new LinkedList<>(pony.getLearnableMoves().keySet());
				Collections.shuffle(moves, rng);
				if(Debug.on) printDebug("possible moves: "+moves);

				int damagingMoves = 0;
				if(moves.size() < Pony.MOVES_PER_PONY) {
					for(String m : moves) {
						try {
							Move mv = MoveCreator.create(m, pony);
							pony.learnMove(mv);
							if(mv.getMoveType() != Move.MoveType.STATUS)
								++damagingMoves;
						} catch(ReflectiveOperationException ee) {
							printDebug("[Battle1v1.randomTeam()] Failed to create move:");
							ee.printStackTrace();
						}
					}
				} else {
					boolean possiblyMixed = Math.abs(pony.getBaseAtk() - pony.getBaseSpatk()) < 50;
					boolean isOffensive = pony.getBaseAtk() + pony.getBaseSpatk() > 200;
					boolean isDefensive = pony.getBaseDef() + pony.getBaseSpdef() > 200;
					int cycles = 0, i = -1;
					boolean hasDuplicateType = false;
					moves:
					while(pony.knownMoves() < Pony.MOVES_PER_PONY) {
						i = (i + 1) % moves.size();
						if(Debug.on) printDebug(pony.getName()+"'s moves are now "+pony.getMoves());
						// avoid infinite loops
						if(++cycles > 100) {
							if(Debug.on) printDebug("[Battle1v1.randomTeam()] exceeded cycles: giving up.");
							break;
						}
						try {
							Move m = MoveCreator.create(moves.get(i), pony);
							if(Debug.on) printDebug("chosen move "+m);

							// insert first move with no checks
							if(pony.knownMoves() == 0) {
								if(m.getMoveType() != Move.MoveType.STATUS)
									++damagingMoves;
								pony.learnMove(m);
								continue;
							}

							// Reject move if we already have learned it
							if(pony.getMove(m.getName()) != null)
								continue;

							// Mirror Pond is not really viable for a low-HP pony
							if(m.getName().equals("Mirror Pond") && pony.getBaseHp() < 65 && rng.nextFloat() < 0.9)
								continue;

							// check for damaging moves with the same type
							if(m.getMoveType() != Move.MoveType.STATUS) {
								for(Move mv : pony.getMoves()) {
									if(mv.getMoveType() != Move.MoveType.STATUS && mv.getType() == m.getType()) {
										if(possiblyMixed && mv.getMoveType() != m.getMoveType()) {
											if(hasDuplicateType) {
												// reject
												continue moves;
											} else {
												// accept this move too
												++damagingMoves;
												pony.learnMove(m);
												hasDuplicateType = true;
												continue moves;
											}
										}
									}
								}
								++damagingMoves;
								pony.learnMove(m);

							} else {
								// if move is STATUS, check there aren't already too many
								int sm = pony.knownMoves() - damagingMoves;
								switch(sm) {
									case 0: // accept move
										pony.learnMove(m);
										continue moves;
									case 1: // accept unless this pony is really offensive
										if(isOffensive)
											continue moves;
										pony.learnMove(m);
										continue moves;
									case 2: // reject unless this pony is really defensive
										if(!isDefensive)
											continue moves;
										pony.learnMove(m);
										continue moves;
									default: // always reject
										continue moves;
								}
							}
						} catch(ReflectiveOperationException ee) {
							printDebug("[Battle1v1.randomTeam()] Failed to create move:");
							ee.printStackTrace();
						}
					}
				} // end moves
				
				/* IVs are always maxed */
				for(Pony.Stat s : Pony.Stat.core())
					pony.setIV(s, 31);

				/* EVs are given according to the moveset and the base stats.
				 * Give high atk/spa to ponies with high offensive stats,
				 * high speed to fast ones, etc.
				 * For now, do things stupidly: 
				 *   calculate a counter for each stat, based on baseStat+#damaging moves
				 *     (phys moves increase physCounter, spec specCounter)
				 * give EVs to the highest one (Atk,SpA,Def or SpD). If phys and spec counters
				 * are very similar, give a mixed setup.
				 * If pony is speedy and has first 252 EVs on offensive stats, give EVs on speed,
				 * else on HP.
				 * We don't care (for now) about boosting moves, etc, so setups may
				 * be quite (very) flawed in some cases.
				 */
				int physCounter = pony.getBaseAtk(), specCounter = pony.getBaseSpatk();
				boolean physViable = false, specViable = false;
				for(Move m : pony.getMoves()) {
					switch(m.getMoveType()) {
						case PHYSICAL:
							physCounter += 25;
							physViable = true;
							break;
						case SPECIAL:
							specCounter += 25;
							specViable = true;
							break;
					}
				}
				if(Debug.on) printDebug("physCounter = "+physCounter+", specCounter = "+specCounter);

				int maxDef = Math.max(pony.getBaseDef(), pony.getBaseSpdef());
				if(physCounter > maxDef || specCounter > maxDef) {
					// offensive setup
					if(Debug.on) printDebug("offensive setup selected");
					if(Math.abs(physCounter - specCounter) < 25 && specViable && physViable) {
						// mixed
						if(Debug.on) printDebug("mixed setup selected");
						float rand = rng.nextFloat();
						if(rand > 0.8) {
							pony.setEV(Pony.Stat.ATK, 126);
							pony.setEV(Pony.Stat.SPATK, 126);
						} else if(rand > 0.4) {
							pony.setEV(Pony.Stat.ATK, 252);
						} else {
							pony.setEV(Pony.Stat.SPATK, 252);
						}
					} else if(physCounter > specCounter && physViable) {
						// physical
						pony.setEV(Pony.Stat.ATK, 252);
					} else if(specViable) {
						// special
						pony.setEV(Pony.Stat.SPATK, 252);
					} else {
						// should never happen (no damaging moves), but in case
						// pick the first damaging moves from viable and use the
						// setup relative to that moveType.
						boolean ok = false;
						outerfor:
						for(String s : moves) {
							try {
								Move move = MoveCreator.create(s, pony);
								switch(move.getMoveType()) {
									case PHYSICAL:
										pony.setMove(3, move);
										pony.setEV(Pony.Stat.ATK, 252);
										ok = true;
										break outerfor;
									case SPECIAL:
										pony.setMove(3, move);
										pony.setEV(Pony.Stat.SPATK, 252);
										ok = true;
										break outerfor;
								}
							} catch(ReflectiveOperationException ee) {
								printDebug("[Battle1v1.randomTeam()] Failed to create move:");
								ee.printStackTrace();
							}
						}
						if(!ok) {
							// uh, shit. Let's retry altogether:
							continue ponies;
						}
						++damagingMoves;
					}
					// choose whether to give EVs to Speed or HP:
					// we use the same speed cutoff as PS, 80.
					if(pony.getBaseSpeed() > 80)
						pony.setEV(Pony.Stat.SPEED, 252);
					else
						pony.setEV(Pony.Stat.HP, 252);
					
					// last 6 EVs on strong defense:
					if(pony.getBaseDef() > pony.getBaseSpdef())
						pony.setEV(Pony.Stat.DEF, 6);
					else
						pony.setEV(Pony.Stat.SPDEF, 6);

				} else {
					if(Debug.on) printDebug("defensive setup selected");
					// defensive setup. It's quite hard that both physCounter and specCounter
					// are less than maxDef: in that case, the pony has at least 1 very unbalanced
					// defensive stat. We choose at random which def to boost, then give remaining
					// EVs to HP.
					if(rng.nextFloat() < 0.5) {
						pony.setEV(Pony.Stat.DEF, 252);
						pony.setEV(Pony.Stat.SPDEF, 6);
					} else {
						pony.setEV(Pony.Stat.SPDEF, 252);
						pony.setEV(Pony.Stat.DEF, 6);
					}
					pony.setEV(Pony.Stat.HP, 252);
				}
				if(Debug.on) printDebug("EVs are now "+pony.dumpEVs());
				/* Assign ability: for now, we just choose at random */
				if(pony.getPossibleAbilities().size() > 0) {
					try {
						pony.setAbility(AbilityCreator.create(pony.getPossibleAbilities().get(
							rng.nextInt(pony.getPossibleAbilities().size()))));
						if(Debug.on) printDebug(pony.getName()+": set ability to "+pony.getAbility());
					} catch(ReflectiveOperationException ee) {
						printDebug("[randomTeam]: Exception while creating ability: ");
						ee.printStackTrace();
					}
				}
				/* Assign item:
				 * for an offensive setup, if Speed is boosted, choose between
				 * Alicorn Amulet, a Choice item or a move type-boosting item;
				 * if HP are boosted, or for a defensive setup, choose SpareSnacks 
				 * or a type-boosting item.
				 * If HP are low, there's a chance to choose ZapAppleJuice
				 * (This till we have more items)
				 */
				List<String> pool = new LinkedList<>();
				int statusMoves = pony.knownMoves() - damagingMoves;
				if(pony.getEV(Pony.Stat.ATK) > 0) {
					if(damagingMoves > 1)
						pool.add("Alicorn Amulet");
					if(pony.getEV(Pony.Stat.SPATK) > 0) {
						// mixed
						pool.add("Alicorn Amulet");
					} else if(statusMoves == 0) {
						pool.add("Choice Saddle");
					}
				} else if(pony.getEV(Pony.Stat.SPATK) > 0) {
					if(damagingMoves > 1)
						pool.add("Alicorn Amulet");
					if(statusMoves == 0)
						pool.add("Choice Bridle");
				}
				if(pony.getBaseSpeed() >= 75 && statusMoves == 0) {
					pool.add("Choice Boots");
				}
				for(Move m : pony.getMoves()) {
					if(m.getMoveType() == Move.MoveType.STATUS) continue;
					switch(m.getType()) {
						case MAGIC:
							pool.add("Magic Tiara");
							break;
						case LOYALTY:
							pool.add("Flight Goggles");
							break;
						case HONESTY:
							pool.add("Cowboy Hat");
							break;
						case LAUGHTER:
							pool.add("Party Hat");
							break;
						case KINDNESS:
							// TODO
							break;
						case GENEROSITY:
							pool.add("Fashion Scarf");
							break;
						case CHAOS:
							// TODO
							break;
						case NIGHT:
							// TODO
							break;
						case SHADOW:
							pool.add("Dark Cloak");
							break;
						case SPIRIT: 
							pool.add("Mystic Mask");
							break;
						case LOVE:	
							pool.add("Diamond Ring");
							break;
						case PASSION:	
							pool.add("Scooter");
							break;
						case MUSIC:	
							pool.add("Golden Lyre");
							break;
						case LIGHT:
							// TODO
							break;
					}
				}
				if(pony.getEV(Pony.Stat.HP) > 0)
					pool.add("Spare Snacks");
				if(pony.getEV(Pony.Stat.DEF) > 0 || pony.getEV(Pony.Stat.SPDEF) > 0)
					pool.add("Spare Snacks");
				if(pony.getBaseHp() < 60)
					pool.add("Zap Apple Juice");
				
				try {
					Item item = ItemCreator.create(pool.get(rng.nextInt(pool.size())));
					if(Debug.on) printDebug("Chosen item "+item);
					pony.setItem(item);
				} catch(ReflectiveOperationException ee) {
					printDebug("[Battle1v1.randomTeam()] Failed to create item:");
					ee.printStackTrace();
				}
				// pony is ready to roll
				++count;
				team.add(pony);

			} catch(ReflectiveOperationException e) {
				printDebug("[Battle1v1.randomTeam()] Failed to create pony:");
				e.printStackTrace();
			}
		} // end of ponies

		return team;
	}

	private Player p1;
	private Player p2;
	private Connection c1;
	private Connection c2;
}
