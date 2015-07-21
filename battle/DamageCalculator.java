//: battle/DamageCalculator.java
package pokepon.battle;


import pokepon.enums.*;
import pokepon.util.*;
import pokepon.pony.*;
import pokepon.move.*;
import pokepon.net.jack.server.*;
import static pokepon.util.MessageManager.*;
import java.util.*;

/** The calculator of battle damages (cannot be called directly, but should
 * only be accessed by BattleEngine); Damage is calculated using Pok&#233mon formulas.
 *
 * @author silverweed
 *
 */

class DamageCalculator {
	
	////////////////// PUBLIC METHODS / FIELDS ////////////////////
	
	/** Default constructor: if no RNG is passed, create a new one. */
	public DamageCalculator() {
		if(Debug.on) printDebug("[DC] called default constructor");
		setRNG();
	}
	
	public DamageCalculator(long seed) {
		if(Debug.on) printDebug("[DC] called constructor with seed "+seed);
		setRNG(seed);
	}
	
	public DamageCalculator(Random rng) {
		if(Debug.on) printDebug("[DC] called constructor with rng");
		this.rng = rng;
		if(rng == null) throw new RuntimeException("[DC] ERROR! rng is null!");
	}
	
	/** Overloaded method used to default the value of selfHit */
	int calculateBattleDamage(Move move, final BattleEngine be) {
		return calculateBattleDamage(move,be,false);
	}

	/** Method to calculate battle damage
	 * @param move Move used
	 * @param be The BattleEngine handling this battle
	 * @param selfHit Whether the attack is a self-hit or not (default: false)
	 * @return Damage inflicted
	 */
	public int calculateBattleDamage(Move move, final BattleEngine be, boolean selfHit) {
		if(move == null) printDebug("[DC]: MOVE IS NULL!");
		
		//uses the same formula as Pokemon

		/* First: calculate Modifier */

		Pony attacker = be.getAttacker();
		Pony defender = selfHit ? be.getAttacker() : be.getDefender();
		WeatherHolder weather = be.getWeather();
		BattleTask bt = be.getBattleTask();
		
		if(Debug.on) {
			printDebug("[DC] "+attacker.getName()+" => "+defender.getName()+
					" with "+move.getName()+" {weather="+weather+"}");
		}
		if(Debug.pedantic) {
			printDebug("[DC] Attacker: boosts="+attacker.getBoosts()+",status="+attacker.getStatus());
			printDebug("[DC] Defender: boosts="+defender.getBoosts()+",status="+defender.getStatus());
		}

		/* Same Type Attack Bonus */
		boolean STAB = false;
		for(int i = 0; i < Pony.MAX_TYPES; ++i) {
			if(move.getType() == attacker.getType(i) && !move.isTypeless()) {
				STAB = true;
				break;
			}
		}
		/* Type modifier */
		float typeModifier = 1;
		boolean sturdy = false;
		String sturdyMsg  = "";
		String sturdyFallback = "";
		if(!move.ignoreImmunities() && !move.isTypeless()) {
			/* Immunities */
			if(defender.getImmunities().contains(move.getType())) {
				// chaotic weather changes immunity to 4x damage (also inversion of wks/res)
				if(	move.invertWeaknessAndResistance() ^
					(weather != null && 
					weather.get() != null && 
					!weather.get().equals(Weather.CHAOTIC))
				) 
					typeModifier = 0;
				else 
					typeModifier *= 4;
			}
		}
		if(typeModifier == 0) {
			if(bt != null) {
				bt.sendB(be.getAlly(),"|immune|opp");
				bt.sendB(be.getOpp(),"|immune|ally");
			}
			if(be.echoBattle) printMsg("It does not affect "+defender.getName()+"...");
			return 0;	//don't waste time calculating stuff, since it's 0
		}
		if(!move.isTypeless()) {
			/* Weaknesses */
			if(!move.ignoreWeaknesses()) {
				Map<Type,Integer> wks = defender.getWeaknesses();
				if(wks.keySet().contains(move.getType())) {
					// effects of chaotic weather and intrinsic inversion of wks/res are cumulative.
					if(	move.invertWeaknessAndResistance() ^ 
						(weather != null && 
						weather.get() != null &&
						weather.get().equals(Weather.CHAOTIC))
					) 
						typeModifier /= (float)(wks.get(move.getType()));
					else 
						typeModifier *= (float)(wks.get(move.getType()));;
				}
			}
			/* Resistances */
			if(!move.ignoreResistances()) {
				Map<Type,Integer> res = defender.getResistances();
				if(res.keySet().contains(move.getType())) {
					if(	move.invertWeaknessAndResistance() ^ 
						(weather != null &&
						weather.get() != null && 
						weather.get().equals(Weather.CHAOTIC))
					) 
						typeModifier *= (float)(res.get(move.getType()));
					else 
						typeModifier /= (float)(res.get(move.getType()));;
				}
			}

		}

		/* Print effectiveness message */
		if(typeModifier != 1) {
			if(bt != null) {
				if(selfHit) {
					bt.sendB(be.getAlly(),"|effective|ally|"+typeModifier);
					bt.sendB(be.getOpp(),"|effective|opp|"+typeModifier);
				} else {
					bt.sendB(be.getAlly(),"|effective|opp|"+typeModifier);
					bt.sendB(be.getOpp(),"|effective|ally|"+typeModifier);
				}
			}
			if(typeModifier > 1) {
				if(typeModifier > 2) {
					if(be.echoBattle) printMsg("It's super-duper effective!");
				} else {
					if(be.echoBattle) printMsg("It's super effective!");
				}
			} else {
				if(be.echoBattle) printMsg("It's not very effective...");
			}
		}

		/* Other effectdealers' multipliers */
		for(EffectDealer d : defender.getEffectDealers()) {
			typeModifier *= d.changeDamageTakenFrom(move.getType());
			typeModifier *= d.changeDamageTakenFrom(move.getMoveType());
			if(typeModifier == 0) {
				if(bt != null) {
					bt.sendB(be.getAlly(),"|immune|opp");
					bt.sendB(be.getOpp(),"|immune|ally");
				}
				if(be.echoBattle) printMsg("It does not affect "+defender.getName()+"...");
				return 0;
			}
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
			typeModifier *= d.changeDamageDealtBy(move.getType());
			typeModifier *= d.changeDamageDealtBy(move.getMoveType());
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
		if(Debug.on) printDebug("[DC] typeModifier: "+typeModifier);

		boolean isCritical = (rng.nextFloat() < 0.0625 * move.getCritical());
		
		float modifier = typeModifier;
		if(STAB)
			modifier *= 1.5f;
			
		if(isCritical) {
			boolean prevents = false;
			for(EffectDealer ed : defender.getEffectDealers())
				if(ed.ignoreCriticalHits()) {
					prevents = true;
					break;
				}
			if(!prevents) {
				modifier *= 2;
				if(bt != null)
					bt.sendB("|battle|Critical hit!");
				if(be.echoBattle) System.out.println("Critical hit!");
			}
		}
		if(be.getAttacker().hasStatus(Pony.Status.BURNED) && move.getMoveType() == Move.MoveType.PHYSICAL)
			modifier /= 2;

		modifier *= (0.85 + rng.nextFloat()*0.15);	//float from 0.85 to 1
		
		if(Debug.on) printDebug("[DC] modifier: "+modifier+"; sturdy: "+sturdy);

		//Modifier = STAB x Type x Critical x other(items/abilities/...) x (random(0.85,1))

		/* Boring part: choose what atk and what def should be used */
		int atk = 0;
		int def = 0;
		if(move.useTargetAtk()) {
			atk = defender.atk();
			if(Debug.pedantic) printDebug("target atk = "+atk+" (mod: "+defender.atkMod()+", base atk = "+defender.getBaseStat(Pony.Stat.ATK)+")");
		} else if(move.useTargetSpatk()) {
			atk = defender.spatk();
			if(Debug.pedantic) printDebug("target spatk = "+atk+" (mod: "+defender.spatkMod()+", base spatk = "+defender.getBaseStat(Pony.Stat.SPATK)+")");
		} else if(move.getMoveType() == Move.MoveType.PHYSICAL) {
			atk = attacker.atk();
			if(Debug.pedantic) printDebug("user atk = "+atk+" (mod: "+attacker.atkMod()+", base atk = "+attacker.getBaseStat(Pony.Stat.ATK)+")");
		} else if(move.getMoveType() == Move.MoveType.SPECIAL) {
			atk = attacker.spatk(); 
			if(Debug.pedantic) printDebug("user spatk = "+atk+" (mod: "+attacker.spatkMod()+", base spatk = "+attacker.getBaseStat(Pony.Stat.SPATK)+")");
		}

		if(move.useTargetDef()) {
			def = defender.def();
			if(Debug.pedantic) printDebug("target def = "+def+" (mod: "+defender.defMod()+", base def = "+defender.getBaseStat(Pony.Stat.DEF)+")");
		} else if(move.useTargetSpdef()) {
			def = defender.spdef();
			if(Debug.pedantic) printDebug("target spdef = "+def+" (mod: "+defender.spdefMod()+", base spdef = "+defender.getBaseStat(Pony.Stat.SPDEF)+")");
		} else if(move.getMoveType() == Move.MoveType.PHYSICAL) {
			// for now, only selfHit case is confusion, which is physical
			def = selfHit ? attacker.def() : defender.def();
			if(Debug.pedantic) printDebug("target def = "+def+" (mod: "+defender.defMod()+", base def = "+defender.getBaseStat(Pony.Stat.DEF)+")");
		} else if(move.getMoveType() == Move.MoveType.SPECIAL) {
			def = defender.spdef();
			if(Debug.pedantic) printDebug("target spdef = "+def+" (mod: "+defender.spdefMod()+", base spdef = "+defender.getBaseStat(Pony.Stat.SPDEF)+")");
		}
	
		// Damage = ( (2*Level+10)/250 * Atk/Def * Base + 2) * Modifier
		int damage = (int)(((2f * attacker.getLevel() + 10f) / 250f * atk/def * move.getDamage() + 2f) * modifier);
		if(Debug.on) printDebug("[DC] damage = [(2 * "+attacker.getLevel()+" + 10) / 250 * "+atk+"/"+def+" * "+move.getDamage()+" + 2] * "+modifier+" = "+damage+" (from: "+move.getName()+")");
		if(sturdy && defender.hp() == defender.maxhp() && damage >= defender.maxhp()) {
			if(bt != null) {
				if(sturdyMsg.length() > 0) 
					bt.sendB("|battle|"+sturdyMsg);
				else
					bt.sendB("|battle|"+defender.getNickname()+" resisted because of "+sturdyFallback+"!");
			}
			damage = defender.maxhp() - 1;
		}
		if(Debug.on) printDebug("Dealt "+damage+" damage to "+defender.getName());
		
		return damage;
	}
	
	
	///////////////// PRIVATE METHODS / FIELD /////////////////////
	
	private void setRNG() {
		rng = new Random();
	}
	
	private void setRNG(long seed) {
		rng = new Random(seed);
	}
	
	/** Random Number Generator */
	private Random rng;
}
