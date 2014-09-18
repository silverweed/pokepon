//: pony/Pony.java

package pokepon.pony;

import pokepon.move.*;
import pokepon.move.hazard.*;
import pokepon.enums.*;
import pokepon.util.*;
import pokepon.battle.*;
import pokepon.player.*;
import pokepon.ability.*;
import pokepon.item.*;
import pokepon.enums.Type;
import pokepon.battle.*;
import static pokepon.util.MessageManager.*;
import static pokepon.util.Meta.*;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import java.net.*;

/** The base class for Ponies;
 * The class is abstract so it cannot be instantiated directly, creating an
 * anonymous pony with 0 stats.
 * @author Giacomo Parolini
 */
public abstract class Pony implements Comparable<Pony>, Serializable {
	
	//////////////// CONSTANT VALUES ////////////////////
	
	/** How many moves a pony can have. */
	public static final int MOVES_PER_PONY = 4;
	/** How many ability slots a pony can have (the ability is always unique, though). */
	public static final int ABILITIES_PER_PONY = 3;
	/** Max length of the list of all learnable moves of a single pony */
	public static final int MAX_LEARNABLE_MOVES = 255;
	/** Max amount of types a pony can have */
	public static final int MAX_TYPES = 2;
	/** Maximum level reachable. */
	public static final int MAX_LEVEL = 100;
	/** Maximum amount of happiness reachable. */
	public static final int MAX_HAPPINESS = 255;
	/** Mmaximum number of IV per stat. */
	public static final int MAX_IV = 31;
	/** Maximum number of EV per stat. */
	public static final int MAX_EV = 252;
	/** Max number of TOTAL EVs. */
	public static final int TOT_EV = 510;
	/** Max length of pony name/nickname */
	public static final int MAX_NAME_LENGTH = 20;
	/** Stats short names */
	private static final String[] STAT_NAMES = { "hp","atk","def","spatk","spdef","speed" };

	////////////// ENUMS ////////////////

	public static enum Nature { 
		// neutral
		FRIENDLY("Friendly"), 
		SENSITIVE("Sensitive"),
		DILIGENT("Diligent"),
		SMILEY("Smiley"), 
		CHAOTIC("Chaotic"),
		// atk
		PROUD("Proud"),
		PRAGMATIC("Pragmatic"),
		SELFISH("Selfish"),
		BLACKHEARTED("Blackhearted"),		
		// def
		SHY("Shy"), 
		PATIENT("Patient"),
		DEPENDABLE("Dependable"), 
		TACITURN("Taciturn"),
		// spatk
		EGGHEAD("Egghead"),
		SOLITARY("Solitary"),
		RANDOM("Random"),
		BOOKWORM("Bookworm"),	
		// spdef
		FABULOUS("Fabulous"),
		SILLY("Silly"),
		RADIANT("Radiant"), 
		MYSTERIOUS("Mysterious"),	
		// speed
		STYLISH("Stylish"),
		AWESOME("Awesome"), 
		COOL("Cool"),
		RADICAL("Radical");		
		
		private final String name;

		Nature(final String name) {
			this.name = name;
		}

		/* Utility methods */
		@Override
		public String toString() {
			return name;
		}

		public static Set<String> nameSet() {
			Set<String> set = new HashSet<String>();
			for(Nature n : values()) {
				set.add(n.toString());
			}
			return set;
		}

		public String increasedStat() {
			for(String s : statNames()) 
				if(natureModifier(s,this) > 1) 
					return s;
			return null;
		}

		public String decreasedStat() {
			for(String s : statNames()) 
				if(natureModifier(s,this) < 1) 
					return s;
			return null;
		}

		public static Nature forName(String name) {
			for(Nature n : values()) 
				if(n.toString().equalsIgnoreCase(name))
					return n;
			return null;
		}
	}
	
	public enum Race { 
		EARTHPONY("Earth Pony"), 
		PEGASUS("Pegasus"),
		UNICORN("Unicorn"),
		ALICORN("Alicorn"), 
		ZEBRA("Zebra"),
		MYTHICBEAST("Mythic Beast"), 
		GRYPHON("Gryphon"),
		BUFFALO("Buffalo"),
		DIAMONDDOG("Diamond Dog");

		private final String name;

		Race(final String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	};

	public enum Status { 
		KO("KO"),
		PARALYZED("Paralyzed"),
		BURNED("Burned"), 
		ASLEEP("Asleep"),
		PETRIFIED("Petrified"), 
		POISONED("Poisoned"), 
		INTOXICATED("Intoxicated"),
		CONFUSED("Confused");

		private final String name;

		Status(final String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}

		public static Status forName(final String name) {
			if(name.length() == 3) {
				if(name.equalsIgnoreCase("par")) return Status.PARALYZED;
				else if(name.equalsIgnoreCase("cnf")) return Status.CONFUSED;
				else if(name.equalsIgnoreCase("psn")) return Status.POISONED;
				else if(name.equalsIgnoreCase("tox")) return Status.INTOXICATED;
				else if(name.equalsIgnoreCase("brn")) return Status.BURNED;
				else if(name.equalsIgnoreCase("slp")) return Status.ASLEEP;
				else if(name.equalsIgnoreCase("ptr")) return Status.PETRIFIED;
			}

			for(Status s : values()) 
				if(s.toString().equalsIgnoreCase(name))
					return s;
			return null;
		}
	};

	public class Volatiles {
		public int atkModifier;
		public int defModifier;
		public int spatkModifier;
		public int spdefModifier;
		public int speedModifier;
		public int evasionModifier;
		public int accuracyModifier;
		public boolean confused;
		public boolean taunted;
		public boolean substitute;
		public EnumMap<Type,Float> volatileEffectiveness = new EnumMap<>(Type.class);
	}

	////////////// PUBLIC METHODS / FIELDS /////////////////
	
	/** Default constructor - should not be called directly */
	private Pony() { 
		name = "Unnamed_Pony"; 
		level = 1;
		nature = generateNature();
		generateIV();
	}
	
	/** Constructor with level - the one you should use */
	public Pony(int _level) {
		this();
		if(_level < 1) _level = 1;
		else if(_level > MAX_LEVEL) _level = MAX_LEVEL;
		level = _level;
		if(Debug.pedantic) 
			printDebug("Generated pony:\n\tlevel="+level+"\n\tnature="+nature);
	}
	
	/** "Clones" this pony (copies base stats, name, level, IV, EV, Nature, moves, etc;
	 * basically what is needed to create a pony from the team builder);
	 * Note that this "clone" method is *NOT* meant to implement the Cloneable interface
	 * because it takes arguments, and does not ensure a field-per-field copy.
	 *
	 * @param cloneIVsAndEVs (optional) can give up to 2 booleans: first is cloneIVs, second is cloneEVs (default: true,true)
	 */
	public Pony clone(boolean... cloneIVsAndEVs) throws ReflectiveOperationException {
		Pony clonedPony = PonyCreator.create(getClass());
	
		boolean cloneIVs = true;
		boolean cloneEVs = true;
		if(cloneIVsAndEVs.length > 0) {
			if(cloneIVsAndEVs.length > 1) {
				cloneIVs = cloneIVsAndEVs[0];
				cloneEVs = cloneIVsAndEVs[1];
			} else {
				cloneIVs = cloneIVsAndEVs[0];
			}
		}

		clonedPony.baseHp = baseHp;
		clonedPony.baseAtk = baseAtk;
		clonedPony.baseDef = baseDef;
		clonedPony.baseSpatk = baseSpatk;
		clonedPony.baseSpdef = baseSpdef;
		clonedPony.baseSpeed = baseSpeed;

		for(String s : statNames()) {
			if(cloneIVs)
				clonedPony.setIV(s,getIV(s));
			if(cloneEVs)
				clonedPony.setEV(s,getEV(s));
		}

		clonedPony.level = level;
		clonedPony.name = name;
		clonedPony.nature = nature;

		for(int i = 0; i < MOVES_PER_PONY; ++i) {
			clonedPony.move[i] = move[i];
		}

		return clonedPony;
	}

	/** Given a pony, transforms this pony into that one, keeping a backup of
	 * the original one; the transformation can be reverted with transformBack().
	 */
	public void transformInto(Pony other) {
		if(other == null) {
			if(Debug.on) printDebug("[Pony.transformInto] other is null: not transforming.");
			return;
		}
		try {
			original = this.clone(true,true);
			baseHp = other.baseHp;
			baseAtk = other.baseAtk;
			baseDef = other.baseDef;
			baseSpatk = other.baseSpatk;
			baseSpdef = other.baseSpdef;
			baseSpeed = other.baseSpeed;
			ability = other.ability;
			name = other.name;
			frontSprite = other.frontSprite;
			backSprite = other.backSprite;
			type = other.type;
			race = other.race;
			// FIXME: y u no work??
			atkModifier = other.atkModifier;
			defModifier = other.defModifier;
			spatkModifier = other.spatkModifier;
			spdefModifier = other.spdefModifier;
			speedModifier = other.speedModifier;
			for(int i = 0; i < MOVES_PER_PONY; ++i)
				move[i] = other.move[i];
			transformed = true;
			if(Debug.on) printDebug("Pony "+original.getName()+" transformed into "+name+". Original = "+original);
		} catch(ReflectiveOperationException e) {
			printDebug("[Pony] Couldn't transform into "+other);
			e.printStackTrace();
		}
	}

	public void transformBack() {
		if(!transformed) {
			if(Debug.on) printDebug("[Pony["+name+"].transformBack()] "+name+" was not transformed.");
			return;
		}
		transformInto(original);
		transformed = false;
		original = null;
		if(Debug.on) printDebug("Successfully transformed back into "+name);
	}

	public boolean isTransformed() {
		return transformed;
	}

	public Pony getOriginal() {
		return original;
	}

	// GET METHODS //
	/** @return True name of pony */
	public String getName() { return (name.length() > MAX_NAME_LENGTH ? name.substring(0,MAX_NAME_LENGTH) : name); }
	/** @return Nickname of pony, if given; else, true name */
	public String getNickname() { return (nickname == null ? getName() : nickname); }
	/** @return True if pony is nicknamed, false otherwise. */
	public boolean hasNickname() { return nickname != null; }
	/** @return True name of pony and, if given, nickname */
	public String getFullName() { return (nickname == null || nickname.equals(name) ? getName() : nickname+" {"+name+"}"); }
	public Type getType(int i) { return type[i]; }
	public boolean isCanon() { return canon; }
	/** @return Reference of the team whose this pony is member (if assigned) */
	public Team getTeam() { return team; }
	/** @return sprite; if sprite is not set, return the default URL 
	 * of the front sprite (i.e. resources/sprites/PonyName/stand_left.gif) 
	 */
	public URL getFrontSprite() { 
		if(frontSprite != null)
			return frontSprite;
		else {
			if(getClass().getResource(Meta.complete2(Meta.SPRITE_DIR)+DIRSEP+
				name.replaceAll("[\\s']","")+DIRSEP+"stand_left.gif") != null
			) {
				return getClass().getResource(Meta.complete2(Meta.SPRITE_DIR)+DIRSEP+
					name.replaceAll("[\\s']","")+DIRSEP+"stand_left.gif");
			} else if(getClass().getResource(Meta.complete2(Meta.SPRITE_DIR)+DIRSEP+
				name.replaceAll("[\\s']","")+DIRSEP+"stand_left.png") != null
			) {
				return getClass().getResource(Meta.complete2(Meta.SPRITE_DIR)+DIRSEP+
					name.replaceAll("[\\s']","")+DIRSEP+"stand_left.png");
			} else {
				return null;
			}
		}
	}
	/** @return sprite; if sprite is not set, return the default URL 
	 * of the front sprite (i.e. resources/sprites/PonyName/stand_right.gif) 
	 */
	public URL getBackSprite() { 
		if(backSprite != null)
			return backSprite;
		else {
			if(getClass().getResource(Meta.complete2(Meta.SPRITE_DIR)+DIRSEP+
				name.replaceAll("[\\s']","")+DIRSEP+"stand_right.gif") != null
			) {
				return getClass().getResource(Meta.complete2(Meta.SPRITE_DIR)+DIRSEP+
					name.replaceAll("[\\s']","")+DIRSEP+"stand_right.gif");
			} else if(getClass().getResource(Meta.complete2(Meta.SPRITE_DIR)+DIRSEP+
				name.replaceAll("[\\s']","")+DIRSEP+"stand_right.png") != null
			) {
				return getClass().getResource(Meta.complete2(Meta.SPRITE_DIR)+DIRSEP+
					name.replaceAll("[\\s']","")+DIRSEP+"stand_right.png");
			} else {
				return null;
			}
		}
	}
	/// UNUSED 
	/*public URL getToken() { 
		if(token != null)
			return token;
		else {
			try {
				return new URL("file://"+getTokensURL().getPath()+DIRSEP+name+".png");
			} catch(MalformedURLException e) {
				printDebug("getSprite(): Malformed URL: "+e);
				return null;
			}
		}
			
	}*/
	public Item getItem() { return item; }
	public Ability getAbility() { return ability; }
	public List<Type> getTypes() { 
		List<Type> types = new ArrayList<Type>();
		for(int i = 0; i < MAX_TYPES; ++i) {
			if(type[i] != null) types.add(type[i]);
		}
		return types;
	}
	public int getLevel() { return level; }
	public Nature getNature() { return nature; }
	public Race getRace() { return race; }
	public Move getMove(int i) { return move[i]; }
	public Move getMove(final String name) {
		for(Move m : move) {
			if(m.getName().equals(name)) { return m; }
		}
		return null;
	}
	public List<Move> getMoves() { 
		List<Move> moves = new ArrayList<Move>();
		for(int i = 0; i < MOVES_PER_PONY; ++i) {
			if(move[i] != null) moves.add(move[i]);
		}
		return moves;
	}
	public List<String> getMovesNames() {
		List<String> movesNames = new ArrayList<String>();
		for(Move m : getMoves()) {
			movesNames.add(m.getName());
		}
		return movesNames;
	}
	public Move getLastMoveUsed() {
		return lastMoveUsed;
	}

	/** @return Map (name,level) of learnable moves */
	public Map<String,Integer> getLearnableMoves() { return learnableMoves; }
	public boolean canLearn(final Move move) {
		 return learnableMoves.keySet().contains(move.getName());
	}
	public boolean canHaveAbility(Ability ab) {
		for(int i = 0; i < ABILITIES_PER_PONY; ++i)
			if(possibleAbilities[i] != null && possibleAbilities[i].equals(ab.getName()))
				return true;
		return false;
	}

	/** @return Array of possible abilities' names. */
	public List<String> getPossibleAbilities() { 
		List<String> abilities = new ArrayList<String>();
		for(int i = 0; i < ABILITIES_PER_PONY; ++i)
			if(possibleAbilities[i] != null) abilities.add(possibleAbilities[i]);
		return abilities;
	}

	/** Utility method to retreive a stat by name */
	public int getStat(String name) {
		if(name.equalsIgnoreCase("hp")) return maxhp();
		if(name.equalsIgnoreCase("atk")) return atk();
		if(name.equalsIgnoreCase("def")) return def();
		if(name.equalsIgnoreCase("spatk")) return spatk();
		if(name.equalsIgnoreCase("spdef")) return spdef();
		if(name.equalsIgnoreCase("speed")) return speed();
		else return -1;
	}

	/** Utility method to retreive a stat by number (starting from 0) */
	public int getStat(final int num) {
		switch(num) {
			case 0: return maxhp();
			case 1: return atk();
			case 2: return def();
			case 3: return spatk();
			case 4: return spdef();
			case 5: return speed();
			default: return -1;
		}
	}

	/** Utility method to retreive a base stat by name */
	public int getBaseStat(final String name) {
		if(name.equalsIgnoreCase("hp")) return baseHp;
		if(name.equalsIgnoreCase("atk")) return baseAtk;
		if(name.equalsIgnoreCase("def")) return baseDef;
		if(name.equalsIgnoreCase("spatk")) return baseSpatk;
		if(name.equalsIgnoreCase("spdef")) return baseSpdef;
		if(name.equalsIgnoreCase("speed")) return baseSpeed;
		if(name.equalsIgnoreCase("bst")) return bst();
		else return -1;
	}

	/** Utility method to retreive a base stat by number */
	public int getBaseStat(final int num) {
		switch(num) {
			case 0: return baseHp;
			case 1: return baseAtk;
			case 2: return baseDef;
			case 3: return baseSpatk;
			case 4: return baseSpdef;
			case 5: return baseSpeed;
			default: return -1;
		}
	}

	public Set<Status> getStatus() { 
		Set<Status> status = (Set<Status>)EnumSet.noneOf(Status.class);
		if(paralyzed) status.add(Status.PARALYZED);
		if(burned) status.add(Status.BURNED);
		if(petrified) status.add(Status.PETRIFIED);
		if(confused) status.add(Status.CONFUSED);
		if(poisoned) status.add(Status.POISONED);
		if(intoxicated) status.add(Status.INTOXICATED);
		if(asleep) status.add(Status.ASLEEP);
		if(isKO()) status.add(Status.KO);

		return status;
	}
	
	public int getHappiness() { 
		return happiness; 
	}
	
	public boolean isActive() {
		return active;
	}

	public Volatiles getVolatiles() {
		Volatiles v = new Volatiles();
		v.atkModifier = atkModifier;
		v.defModifier = defModifier;
		v.spatkModifier = spatkModifier;
		v.spdefModifier = spdefModifier;
		v.speedModifier = speedModifier;
		v.evasionModifier = evasionModifier;
		v.accuracyModifier = accuracyModifier;
		v.confused = confused;
		v.substitute = substitute;
		v.volatileEffectiveness = new EnumMap<Type,Float>(volatileEffectiveness);
		return v;
	}

	// Temporary statuses

	public boolean hasNegativeCondition() {
		return paralyzed || poisoned || petrified || intoxicated || burned || asleep;
	}

	public boolean isParalyzed() {
		return paralyzed;
	}
	
	public boolean isConfused() {
		return confused;
	}
	
	public boolean isPoisoned() {
		return poisoned;
	}
	
	public boolean isPetrified() {
		return petrified;
	}
	
	public boolean isIntoxicated() {
		return intoxicated;
	}
	
	public boolean isBurned() {
		return burned;
	}
	
	public boolean isAsleep() {
		return asleep;
	}
	
	public boolean isKO() {
		return hp <= 0;
	}

	public boolean isFainted() {
		return isKO();
	}
	
	public boolean isFlinched() {
		return flinched;
	}
	
	public boolean isProtected() {
		return isProtected;
	}

	public boolean isTrapped() {
		return trapped;
	}

	public boolean isLockedOnMove() {
		return lockedOnMove;
	}

	public String getUnlockPhrase() {
		return unlockPhrase;
	}

	public boolean isTaunted() {
		return taunted;
	}

	public boolean hasSubstitute() {
		return substitute;
	}

	public boolean finishedPP() {
		for(Move m : move) {
			if(m != null && m.getPP() > 0) return false;
		}
		return true;
	}
	
	// Stats
	
	public String getHp() {
		return hp+" / "+maxhp();
	}

	public float getHpPerc() {
		return (float)hp/maxhp();
	}

	public int hp() {
		return hp;
	}

	public int maxhp() {
		if(manualMaxHp) return maxHp;
		return (int)(((hpIV+2*baseHp+hpEV/4)*level/100)+10+level);
	}

	public int atk() {
		return (int)(((atkIV+2*baseAtk+atkEV/4)*level/100+5)*natureModifier("atk")*getStatMod(atkModifier));
	}

	public int spatk() {
		return (int)(((spatkIV+2*baseSpatk+spatkEV/4)*level/100+5)*natureModifier("spatk")*getStatMod(spatkModifier));
	}

	public int def() {
		return (int)(((defIV+2*baseDef+defEV/4)*level/100+5)*natureModifier("def")*getStatMod(defModifier));
	}

	public int spdef() {
		return (int)(((spdefIV+2*baseSpdef+spdefEV/4)*level/100+5)*natureModifier("spdef")*getStatMod(spdefModifier));
	}

	public int speed() {
		return (int)(((speedIV+2*baseSpeed+speedEV/4)*level/100+5)*natureModifier("speed")*getStatMod(speedModifier));
	}

	public int getBaseHp() {
		return baseHp;
	}

	public int getBaseAtk() {
		return baseAtk;
	}

	public int getBaseDef() {
		return baseDef;
	}

	public int getBaseSpatk() {
		return baseSpatk;
	}

	public int getBaseSpdef() {
		return baseSpdef;
	}

	public int getBaseSpeed() {
		return baseSpeed;
	}

	public int bst() {
		return baseHp+baseAtk+baseDef+baseSpatk+baseSpdef+baseSpeed;
	}

	// IVs and EVs
	public int getIV(final String stat) {
		if(stat.equals("hp")) return hpIV;
		else if(stat.equals("atk")) return atkIV;
		else if(stat.equals("def")) return defIV;
		else if(stat.equals("spatk")) return spatkIV;
		else if(stat.equals("spdef")) return spdefIV;
		else if(stat.equals("speed")) return speedIV;
		else return 0;
	}

	public int[] getIVs() {
		return new int[] { hpIV, atkIV, defIV, spatkIV, spdefIV, speedIV };
	}

	public int getTotalIVs() {
		int ivs = 0;
		for(String s : statNames())
			ivs += getIV(s);
		return ivs;
	}

	public int[] getEVs() {
		return new int[] { hpEV, atkEV, defEV, spatkEV, spdefEV, speedEV };
	}

	public int getEV(final String stat) {
		if(stat.equals("hp")) return hpEV;
		else if(stat.equals("atk")) return atkEV;
		else if(stat.equals("def")) return defEV;
		else if(stat.equals("spatk")) return spatkEV;
		else if(stat.equals("spdef")) return spdefEV;
		else if(stat.equals("speed")) return speedEV;
		else return 0;
	}

	public int getTotalEVs() {
		int evs = 0;
		for(String s : statNames())
			evs += getEV(s);
		return evs;
	}

	public int remainingEVs() {
		return TOT_EV - getTotalEVs();
	}

	// Stats modifiers
	public String getBoosts() {
		StringBuilder sb = new StringBuilder("[");
		if(atkModifier != 0) sb.append("atk:"+atkModifier+",");
		if(defModifier != 0) sb.append("def:"+defModifier+",");
		if(spatkModifier != 0) sb.append("spa:"+spatkModifier+",");
		if(spdefModifier != 0) sb.append("spd:"+spdefModifier+",");
		if(speedModifier != 0) sb.append("spe:"+speedModifier+",");
		if(evasionModifier != 0) sb.append("eva:"+evasionModifier+",");
		if(accuracyModifier != 0) sb.append("acc:"+accuracyModifier+",");
		if(sb.length() > 1) 
			sb.delete(sb.length()-1,sb.length());
		sb.append("]");
		return sb.toString();
	}

	public int getBoost(final String s) {
		if(Debug.on) printDebug("Called getBoost("+s+"). Modifiers = "+getBoosts());
		if(s.equals("atk")) return atkModifier;
		else if(s.equals("def")) return defModifier;
		else if(s.equals("spatk")) return spatkModifier;
		else if(s.equals("spdef")) return spdefModifier;
		else if(s.equals("speed")) return speedModifier;
		else if(s.equals("evasion")) return evasionModifier;
		else if(s.equals("accuracy")) return accuracyModifier;
		else return 0;
	}

	public static String toBriefStat(final String stat) {
		if(stat.equalsIgnoreCase("hp")) return "HP";
		else if(stat.equalsIgnoreCase("attack") || stat.equalsIgnoreCase("atk")) return "Atk";
		else if(stat.equalsIgnoreCase("defense") || stat.equalsIgnoreCase("def")) return "Def";
		else if(stat.equalsIgnoreCase("spdef") || 
			stat.equalsIgnoreCase("specialdefense") || 
			stat.equalsIgnoreCase("special defense") ||
			stat.equalsIgnoreCase("spd")) return "SpD";
		else if(stat.equalsIgnoreCase("spatk") || 
			stat.equalsIgnoreCase("specialattack") ||
			stat.equalsIgnoreCase("special attack") ||
			stat.equalsIgnoreCase("spa")) return "SpA";
		else if(stat.equalsIgnoreCase("speed") || stat.equalsIgnoreCase("spe")) return "Spe";
		else return "";
	}

	public int atkMod() { return atkModifier; }
	public int defMod() { return defModifier; }
	public int spatkMod() { return spatkModifier; }
	public int spdefMod() { return spdefModifier; }
	public int speedMod() { return speedModifier; }
	
	/** @return Modification of stats */
	public static float getStatMod(final int mod) {
		switch(mod) {
			case -6: return 0.25f;
			case -5: return 0.29f;
			case -4: return 0.33f;
			case -3: return 0.40f;
			case -2: return 0.50f;
			case -1: return 0.67f;
			case 0: return 1.00f;
			case 1: return 1.50f;
			case 2: return 2.00f;
			case 3: return 2.50f;
			case 4: return 3.00f;
			case 5: return 3.50f;
			case 6: return 4.00f;
			default: return 0f;
		}
	}

	// "Special" stats: accuracy / evasion
	public int accuracyMod() { return accuracyModifier; }
	public int evasionMod() { return evasionModifier; }

	public float getAccuracy() {
		return getSpecialStatMod(accuracyModifier);
	}

	public float getEvasion() {
		return getSpecialStatMod(evasionModifier);
	}

	/** @return Modifier for accuracy/evasion */
	public static float getSpecialStatMod(int mod) {
		if(mod > 6) mod = 6;
		else if(mod < -6) mod = -6;
		switch(mod) {
			case -6: return 0.33f;
			case -5: return 0.38f;
			case -4: return 0.43f;
			case -3: return 0.50f;
			case -2: return 0.60f;
			case -1: return 0.75f;
			case 0: return 1.33f;
			case 1: return 1.67f;
			case 2: return 2.00f;
			case 3: return 2.33f;
			case 4: return 2.67f;
			case 5: return 3.00f;
			case 6: return 4.00f;
			default: return 0f;
		}
	}

	public void removeNegativeStatModifiers() {
		if(atkModifier < 0) atkModifier = 0;
		if(defModifier < 0) defModifier = 0;
		if(spatkModifier < 0) spatkModifier = 0;
		if(spdefModifier < 0) spdefModifier = 0;
		if(speedModifier < 0) speedModifier = 0;
		if(evasionModifier < 0) evasionModifier = 0;
		if(accuracyModifier < 0) accuracyModifier = 0;
	}
	
	public void removePositiveStatModifiers() {
		if(atkModifier > 0) atkModifier = 0;
		if(defModifier > 0) defModifier = 0;
		if(spatkModifier > 0) spatkModifier = 0;
		if(spdefModifier > 0) spdefModifier = 0;
		if(speedModifier > 0) speedModifier = 0;
		if(evasionModifier > 0) evasionModifier = 0;
		if(accuracyModifier > 0) accuracyModifier = 0;
	}

	public void removeVolatiles() {
		confused = false;
		removePositiveStatModifiers();
		removeNegativeStatModifiers();
		volatileEffectiveness.clear();
		toxicCounter = 0;
		protectCounter = 0;
		deathCounter = 0;
		tauntCounter = 0;
		deathScheduled = false;
		isProtected = false;
		taunted = false;
		trapped = false;
		lockedOnMove = false;
		for(Move m : move)
			if(m != null)
				m.reset();
	}

	public void setVolatiles(final Volatiles vol) {
		atkModifier = vol.atkModifier;	
		defModifier = vol.defModifier;	
		spatkModifier = vol.spatkModifier;	
		spdefModifier = vol.spdefModifier;	
		speedModifier = vol.speedModifier;	
		evasionModifier = vol.evasionModifier;	
		accuracyModifier = vol.accuracyModifier;	
		confused = vol.confused;
		substitute = vol.substitute;
		volatileEffectiveness = new EnumMap<Type,Float>(vol.volatileEffectiveness);
	}

	// Weaknesses/Resistances/Immunities: for these methods, delegate to TypeDealer.
	/** @return map of { type: damage modifier } */
	public Map<Type,Integer> getWeaknesses() {
		Map<Type,Integer> wks = TypeDealer.getWeaknesses(type);
		// TODO
		for(Type t : volatileEffectiveness.keySet()) {
			if(volatileEffectiveness.get(t) > 1f) {
				if(wks.keySet().contains(t)) 
					wks.put(t,wks.get(t)*volatileEffectiveness.get(t).intValue());
				else
					wks.put(t,volatileEffectiveness.get(t).intValue());
			} else if(volatileEffectiveness.get(t) != 0) {
				if(!wks.keySet().contains(t)) continue;
				wks.put(t,(int)(wks.get(t)/volatileEffectiveness.get(t)));
			}
		}
		return wks;
	}

	/** @return map of { type: 1 / damage modifier } */
	public Map<Type,Integer> getResistances() {
		Map<Type,Integer> res = TypeDealer.getResistances(type);

		for(Type t : volatileEffectiveness.keySet()) {
			if(volatileEffectiveness.get(t) < 1f) {
				if(res.keySet().contains(t)) 
					res.put(t,(int)(res.get(t)/volatileEffectiveness.get(t)));
				else
					res.put(t,(int)(1/volatileEffectiveness.get(t)));
			} else if(volatileEffectiveness.get(t) != 0) {
				if(!res.keySet().contains(t)) continue;
				res.put(t,(int)(res.get(t)*volatileEffectiveness.get(t)));
			}
		}
		return res;
	}

	public Set<Type> getImmunities() {
		Set<Type> imm = TypeDealer.getImmunities(type);

		for(Type t : volatileEffectiveness.keySet()) 
			imm.add(t);

		return imm;
	}

	public boolean isDeathScheduled() {
		return deathScheduled;
	}

	// COUNTERS //
	public int deathCounter;
	public int toxicCounter;
	public int protectCounter;
	public int sleepCounter;
	public int confusionCounter;
	public int tauntCounter;
	public int activeTurns;
	
	
	// SET METHODS //
	
	public void setLevel(final int _level) {
		level = _level;
		if(level < 1) level = 1;
		else if(level > MAX_LEVEL) level = MAX_LEVEL;
	}

	public void setNickname(final String nickname) {
		this.nickname = nickname;
		if(nickname.length() > MAX_NAME_LENGTH) this.nickname = nickname.substring(0,MAX_NAME_LENGTH);
	}

	public void setTeam(final Team team) {
		this.team = team;
	}

	/** @param path - either the complete URL of the sprite or the relative path
	 * from the sprites directory is accepted.
	 */
	public void setFrontSprite(final String path) {
		try {
			frontSprite = new URL(path);
		} catch(MalformedURLException e) {
			if(Debug.on) printDebug("[Pony.setFrontSprite] Malformed URL: "+path+";\n trying with relative path file://"+getSpritesURL()+DIRSEP+path+" ...");
			try {
				frontSprite = new URL("file://"+getSpritesURL()+DIRSEP+path);
			} catch(MalformedURLException ee) {
				printDebug("[Pony.setFrontSprite] Malformed URL: "+e);
			}
		}
	}

	/** @param path - either the complete URL of the sprite or the relative path from the sprites directory is accepted.
	 */
	public void setBackSprite(final String path) {
		try {
			backSprite = new URL(path);
		} catch(MalformedURLException e) {
			if(Debug.on) printDebug("[Pony.setBackSprite] Malformed URL: "+path+";\n trying with relative path file://"+getSpritesURL()+DIRSEP+path+" ...");
			try {
				backSprite = new URL("file://"+getSpritesURL()+DIRSEP+path);
			} catch(MalformedURLException ee) {
				printDebug("[Pony.setBackSprite] Malformed URL: "+e);
			}
		}
	}

	/** @param path - either the complete URL of the token or the relative path from the tokens directory is accepted.
	 */
	public void setToken(final String path) {
		try {
			token = new URL(path);
		} catch(MalformedURLException e) {
			if(Debug.on) printDebug("[Pony.setToken] Malformed URL: "+path+";\n trying with relative path file://"+getResourcesURL().getPath()+"/tokens/"+path+" ...");
			try {
				token = new URL("file://"+getResourcesURL().getPath()+"/tokens/"+path);
			} catch(MalformedURLException ee) {
				printDebug("[Pony.setToken] Malformed URL: "+e);
			}
		}
	}

	public void setBaseStat(final String stat, final int val) {
		if(toBriefStat(stat).equals("Atk")) baseAtk = Math.max(1,val);
		else if(toBriefStat(stat).equals("Def")) baseDef = Math.max(1,val);
		else if(toBriefStat(stat).equals("SpA")) baseSpatk = Math.max(1,val);
		else if(toBriefStat(stat).equals("SpD")) baseSpdef = Math.max(1,val);
		else if(toBriefStat(stat).equals("Spe")) baseSpeed = Math.max(1,val);
		else if(stat.equalsIgnoreCase("hp")) baseHp = Math.max(1,val);
	}

	/** Sets hp = maxhp. */
	public void resetHp() {
		hp = maxhp();
	}

	public void setActive(final boolean bool) {
		active = bool;
		if(!active) {
			inBattle = false;
			removeVolatiles();
		} else {
			activeTurns = 0;
		}
	}

	public void setMove(final int num, final Move move) {
		if(num >= 0 && num < MOVES_PER_PONY) {
			this.move[num] = move;
		} else {
			printDebug("Error: index "+num+" is out of bounds. Not setting move.");
		}
	}

	/** Adds a move in the first free slot.
	 * @return The slot in which the move was added, or -1 if move was not added.
	 */
	public int addMove(final Move move) {
		for(int i = 0; i < MOVES_PER_PONY; ++i) {
			if(this.move[i] == null) {
				this.move[i] = move;
				return i;
			}
		}
		return -1;
	}
	
	public void setLastMoveUsed(final Move m) {
		lastMoveUsed = m;
	}

	public void setLastMoveUsed(final int i) {
		if(i >= 0 && i < MOVES_PER_PONY)
			lastMoveUsed = move[i];
		else if(Debug.on) printDebug("Error: index "+i+" is out of bounds. Not setting lastMoveUsed.");
	}

	public void setNature(final Nature n) {
		nature = n;
	}
	
	public void setHappiness(final int happ) {
		happiness = happ;
		if(happiness < 0) happiness = 0;
		else if(happiness > MAX_HAPPINESS) happiness = MAX_HAPPINESS;
	}

	public void setItem(final Item item) {
		this.item = item;
		if(item != null)
			this.item.setPony(this);
	}

	public void setAbility(final Ability ability) {
		this.ability = ability;
		if(ability != null) 
			this.ability.setPony(this);
	}

	public void setIV(final String stat, int num) {
		if(num < 0) num = 0;
		else if(num > MAX_IV) num = MAX_IV;

		if(stat.equals("atk")) atkIV = num;
		else if(stat.equals("def")) defIV = num;
		else if(stat.equals("spatk")) spatkIV = num;
		else if(stat.equals("spdef")) spdefIV = num;
		else if(stat.equals("speed")) speedIV = num;
		else if(stat.equals("hp")) hpIV = num;
	}

	public void setEV(final String stat,int num) {
		if(num < 0) num = 0;
		int otherEVs = 0;
		for(String s : statNames()) {
			if(s.equals(stat)) continue;
			otherEVs += getEV(s);
		}
		if(Debug.pedantic) printDebug("otherEvs: "+otherEVs);
		if(num > MAX_EV) num = MAX_EV;
		if(num > TOT_EV-otherEVs) num = TOT_EV-otherEVs;	//limit total EVs to TOT_EV

		if(stat.equals("atk")) atkEV = num; 
		else if(stat.equals("def")) defEV = num;
		else if(stat.equals("spatk")) spatkEV = num;
		else if(stat.equals("spdef")) spdefEV = num;
		else if(stat.equals("speed")) speedEV = num;
		else if(stat.equals("hp")) hpEV = num;
	}

	/** @return The amount of HP regained */
	public int increaseHp(final int value) {
		int val = maxhp() - hp;
		val = Math.min(val,value);
		hp += val;
		if(hp < 0) hp = 0;
		return val;
	}

	/** @param perc (0-100) percentage of maxhp to increase */
	public int increaseHpPerc(float perc) {
		return increaseHp((int)(maxhp()*perc/100f));
	}
	
	public void setHp(int value) {
		hp = value;
		if(hp < 0) hp = 0;
		else if(hp > maxhp()) hp = maxhp();
	}

	/** This method is used by the BattlePanel to display the correct amount of HP
	 * without knowing the actual pony stats; beware that, once called, maxhp() will
	 * return this specific manual value.
	 */
	public void setMaxHp(int value) {
		manualMaxHp = true;
		maxHp = value;
	}

	/** @return The amount of HP lost */
	public int damage(int value) {
		int val = hp;
		if(Debug.pedantic) printDebugnb("[damage] Pony's hp="+getHp()+";");
		val = Math.min(hp,value);
		if(Debug.pedantic) printDebug(" val = min("+hp+","+value+") = "+val);
		hp -= val;
		if(Debug.pedantic) printDebug("[damage] hp are now "+hp);
		if(hp > maxhp()) hp = maxhp();
		return val;
	}

	/** Damage the pony in percentage.
	 * @param perc (0-100) percentage of maxHp to detract.
	 * @return The amount of damage inflicted.
	 */
	public int damagePerc(float perc) {
		if(Debug.pedantic) printDebug("damagePerc: "+perc+"; amount = "+(int)(perc/100f*maxhp()));
		return damage((int)(perc/100f*maxhp()));
	}

	/** Calculates the percentage of damage corresponding to
	 * given argument; returned float is rounded at the 1st decimal.
	 */
	public float calculateDamagePerc(int damage) {
		return (float)((int)1000f*damage/maxhp())/10f;
	}

	public void setParalyzed(boolean bool) {
		paralyzed = bool;
	}

	public void setPetrified(boolean bool) {
		petrified = bool;
	}
	
	public void setConfused(boolean bool) {
		confused = bool;
	}
	
	public void setBurned(boolean bool) {
		burned = bool;
	}
	
	public void setAsleep(boolean bool) {
		asleep = bool;
	}
	
	public void setPoisoned(boolean bool) {
		poisoned = bool;
		// resetting poison also resets intoxication
		if(bool == false) {
			intoxicated = false;
		}
	}
	
	public void setIntoxicated(boolean bool) {
		intoxicated = bool;
		// setting intoxication also sets poison
		if(bool == true) {
			poisoned = true;
		}
	}
	
	public void setFlinched(boolean bool) {
		flinched = bool;
	}

	public void setBlocked(boolean bool) {
		blocked = bool;
	}

	public void setTrapped(boolean bool) {
		trapped = bool;
	}

	public void setLockedOnMove(boolean bool) {
		lockedOnMove = bool;
	}

	public void setUnlockPhrase(String phrase) {
		unlockPhrase = phrase;
	}

	public void setTaunted(boolean bool) {
		taunted = bool;
	}

	public int setSubstitute(boolean bool) {
		substitute = bool;
		return maxhp() / 4;
	}

	public void setKO(boolean bool) {
		hp = 0;
	}

	public void setFainted(boolean bool) {
		setKO(bool);
	}

	public void resetAllStatus() {
		petrified = false;
		confused = false;
		burned = false;
		asleep = false;
		poisoned = false;
		intoxicated = false;
	}
	
	public void setProtected(boolean bool) {
		isProtected = bool;
	}

	
	// Accuracy / Evasion modifiers
	
	public int boostAccuracy(int i) {
		accuracyModifier += i;
		if(accuracyModifier > 6) accuracyModifier = 6;
		else if(accuracyModifier < -6) accuracyModifier = -6;
		return accuracyModifier;
	}
	
	public int boostEvasion(int i) {
		evasionModifier += i;
		if(evasionModifier > 6) evasionModifier = 6;
		else if(evasionModifier < -6) evasionModifier = -6;
		return evasionModifier;
	}
	
	public void resetAccuracyModifier() {
		accuracyModifier = 0;
	}
	
	public void resetEvasionModifier() {
		evasionModifier = 0;
	}
	
	// Stats modifiers
	public int boost(String stat,int value) {
		if(stat.equalsIgnoreCase("atk") || stat.equalsIgnoreCase("attack")) 
			return boostAtk(value);
		else if(stat.equalsIgnoreCase("def") || stat.equalsIgnoreCase("defense"))
			return boostDef(value);
		else if(stat.equalsIgnoreCase("spa") || stat.equalsIgnoreCase("spatk") || stat.equalsIgnoreCase("special attack"))
			return boostSpatk(value);
		else if(stat.equalsIgnoreCase("spd") || stat.equalsIgnoreCase("spdef") || stat.equalsIgnoreCase("special defense"))
			return boostSpdef(value);
		else if(stat.equalsIgnoreCase("spe") || stat.equalsIgnoreCase("speed"))
			return boostSpeed(value);
		else if(stat.equalsIgnoreCase("eva") || stat.equalsIgnoreCase("evasion"))
			return boostAccuracy(value);
		else if(stat.equalsIgnoreCase("acc") || stat.equalsIgnoreCase("accuracy"))
			return boostEvasion(value);
		else
			return 0;
	}

	public int boostAtk(int value) {
		atkModifier += value;
		if(atkModifier > 6) atkModifier = 6;
		else if(atkModifier < -6) atkModifier = -6;
		return atkModifier;
	}
	
	public int boostDef(int value) {
		defModifier += value;
		if(defModifier > 6) defModifier = 6;
		else if(defModifier < -6) defModifier = -6;
		return defModifier;
	}
	
	public int boostSpatk(int value) {
		spatkModifier += value;
		if(spatkModifier > 6) spatkModifier = 6;
		else if(spatkModifier < -6) spatkModifier = -6;
		return spatkModifier;
	}
	
	public int boostSpdef(int value) {
		spdefModifier += value;
		if(spdefModifier > 6) spdefModifier = 6;
		else if(spdefModifier < -6) spdefModifier = -6;
		return spdefModifier;
	}
	
	public int boostSpeed(int value) {
		speedModifier += value;
		if(speedModifier > 6) speedModifier = 6;
		else if(speedModifier < -6) speedModifier = -6;
		return speedModifier;
	}

	public void scheduleDeath(int delay) {
		if(Debug.on) printDebug("[Pony] Scheduling death for "+this+" in "+delay+" turns.");
		deathScheduled = true;
		deathCounter= delay;
	}

	/** Like learnMove(Move,boolean) but with move name. */
	public boolean learnMove(String moveName,boolean... verbose) {
		try {
			return learnMove(MoveCreator.create(moveName,this),verbose);
		} catch(InvocationTargetException e) {
			printDebug("InvocationTargetException in learnMove("+moveName+")");
			printDebug("Caused by: "+e.getCause());
			e.printStackTrace();
			return false;
		} catch(Exception e) {
			printDebug("Caught exception in "+name+".learnMove("+moveName+"): "+e);
			e.printStackTrace();
			return false;
		}
	}

	/** Method used to teach new moves to the pony 
	 * @return true: learned / false: not learned new move.
	 */
	public boolean learnMove(Move _move,boolean... verbose) throws IOException {
	
		boolean vb = true;
		boolean learnable = learnableMoves.containsKey(_move.getName());
		
		for(boolean b : verbose) {
			if(b == false) {
				vb = false;
				break;
			}
		}

		if(learnable) {
			/* Check level */
			if(learnableMoves.get(_move.getName()) > level) {
				if(vb) printMsg(name+" cannot learn "+_move+" yet.");
				if(Debug.on) printDebug("(required level "+learnableMoves.get(_move.getName())+")");
				return false;
			}
			if(knownMoves() < MOVES_PER_PONY) {
				_move.setPony(this);
				move[knownMoves()] = _move;
				if(vb) printMsg(name + " learned " + _move.getName() + "!");
				return true;
			} else {
				// if pony already has all moves, ask to forget one (only in verbose mode)
				//if(vb) return forgetMoveToLearn(_move);
				//else 
				return false;
			}
		} else {
			if(vb) printMsg(name+" cannot learn "+_move+"!");
			return false;
		}
	}
	
	public void printInfo() {
		printInfo(false);
	}

	/** Prints info about internal fields. */
	public void printInfo(boolean all) {
		printMsgnb("*** Pony Info ***\nName: "+name+(item == null ? "" : " @ "+item)+"\nAbility: "+ability+"\nLevel: "+level);
		printMsg("\nNature: "+nature+" "+printNatureInfo(nature)+"\nRace: "+race);
		printTypingInfo();
		printMsg(getIVMsg());
		printStats(all);
		StringBuilder sb = new StringBuilder("Known moves: ");
		boolean noMoves = true;
		for(int i = 0; i < MOVES_PER_PONY; ++i) 
			if(move[i] != null) {
				noMoves = false;
				sb.append(move[i]+", ");
			}
		sb.delete(sb.length()-2,sb.length());
		if(!noMoves)
			printMsg(sb.toString());
		else
			printMsg(sb.toString()+": none");
		if(Debug.on) {
			printMsg("- IVs -\nhp: "+hpIV+"\natk: "+atkIV+"\ndef: "+defIV+"\nspatk: "+spatkIV+"\nspdef: "+spdefIV+"\nspeed: "+speedIV);
			printMsg("- EVs -\nhp: "+hpEV+"\natk: "+atkEV+"\ndef: "+defEV+"\nspatk: "+spatkEV+"\nspdef: "+spdefEV+"\nspeed: "+speedEV);
		}
		
	}
	
	public void printStats() {
		printStats(false);
	}

	/** Prints stats at screen */
	public void printStats(boolean printBase) {
		StringBuilder sb = new StringBuilder("- STATS -");
		if(printBase) {
			for(int i = 0; i < 6; ++i) 
				sb.append("\n%-6s %-3s  (%s)");
			printMsg(String.format(sb.toString(),
				"hp:",maxhp(),baseHp,
				"atk:",atk(),baseAtk,
				"def:",def(),baseDef,
				"spatk:",spatk(),baseSpatk,
				"spdef:",spdef(),baseSpdef,
				"speed:",speed(),baseSpeed)
			);
		} else {
			sb.append("\n%-6s %-9s");
			for(int i = 0; i < 5; ++i)
				sb.append("\n%-6s %-3s");
			printMsg(String.format(sb.toString(),
				"hp:",getHp(),
				"atk:",atk(),
				"def:",def(),
				"spatk:",spatk(),
				"spdef:",spdef(),
				"speed:",speed())
			);
		}

	}

	public String getTyping() {
		StringBuilder sb = new StringBuilder(type[0].toString());
		for(int i = 1; i < MAX_TYPES; ++i) {
			if(type[i] != null) {
				sb.append(" / ");
				sb.append(type[i]);
			}
		}
		return sb.toString();
	}

	public String getTypingHTMLTokens() {
		StringBuilder sb = new StringBuilder("");
		for(Type tp : type)
			if(tp != null)
				sb.append("<img src=\""+tp.getToken()+"\"/>&nbsp;");
		return sb.toString();
	}

	public String getTypingHTMLSpecialTags() {
		StringBuilder sb = new StringBuilder("");
		for(Type tp : type)
			if(tp != null)
				sb.append("<img src=\"[type: "+tp+"]\"/>&nbsp;");
		return sb.toString();
	}

	public List<EffectDealer> getEffectDealers() {
		List<EffectDealer> eds = new ArrayList<>();
		if(ability != null)
			eds.add(ability);
		if(item != null)
			eds.add(item);
		return eds;
	}

	public List<TriggeredEffectDealer> getTriggeredEffectDealers() {
		List<TriggeredEffectDealer> eds = new ArrayList<>();
		if(ability != null)
			eds.add(ability);
		if(item != null)
			eds.add(item);
		return eds;
	}

	/** Prints info about typings */
	public void printTypingInfo() {
		printMsgnb("Type: "+type[0]);
		for(int i = 1;i < MAX_TYPES;++i) {
			if(type[i] == null) 
				break;
			printMsgnb(" / "+type[i]);
		}			
		printMsgnb("\nWeak to: ");
		printMsgnb(TypeDealer.printWeaknesses(type)); 
		printMsg("");
		printMsgnb("Resists to: ");
		printMsgnb(TypeDealer.printResistances(type)); 
		printMsg("");
		printMsgnb("Immune to: ");
		printMsgnb(TypeDealer.printImmunities(type));
		printMsg("");
	}

	/** @return A nature's bonuses/maluses */
	public static String printNatureInfo(final Nature n) {
		StringBuilder sb = new StringBuilder("");
		for(String s : statNames()) {
			if(natureModifier(s,n) > 1) {
				sb.append("("+s+" "+(int)((natureModifier(s,n)-1)*100)+"%),");
			}
		}
		for(String s : statNames()) {
			if(natureModifier(s,n) < 1) {
				sb.append("("+s+" "+(int)((natureModifier(s,n)-1)*100)+"%),");
			}
		}
		if(sb.length() < 2) {
			return new String("(--)");
		} else sb.delete(sb.length()-1,sb.length());
		return sb.toString();
	}

	/** Overriding of toString method */
	public String toString() {
		if(nickname == null) return name + " (Lv "+level+")";
		else return (nickname.equals(name) ? name+" (Lv "+level+")" : nickname+" {"+name+"} (Lv "+level+")");
	}
	
	/** This is public for convenience */
	public volatile boolean inBattle = false;
	
	/** Utility function that returns all stats names */
	public static final String[] statNames() { return STAT_NAMES; }
	
	/** @return Number of known moves */
	public int knownMoves() {
		int known = 0;
		for(int i = 0; i < MOVES_PER_PONY; ++i) {
			if(move[i] != null) {
				++known;
			}
		}

		return known;
	}

	public Map<Type,Float> getVolatileEffectiveness() { return volatileEffectiveness; }

	/** Returns a string with all relevant internal data in a format adapt to be
	 * written directly to a save file (in a PokemonShowdown-like fashion)
	 */
	public String getSaveData() {
		StringBuilder sb = new StringBuilder("");
		
		/* First line: name [~ nickname] [@ item] */
		sb.append(name);
		if(nickname != null)
			if(!nickname.equals(name))
				sb.append(" ~ "+nickname);
		if(item != null)
			sb.append(" @ "+item);
		sb.append("\n");
		
		/* Second line: ability */
		if(ability != null)
			sb.append("Ability: "+ability+"\n");
		
		/* Third line: level */
		if(level != MAX_LEVEL) 
			sb.append("Level: "+level+"\n");
			
		/* Fourth line: happiness */
		if(happiness != MAX_HAPPINESS)
			sb.append("Happiness: "+happiness+"\n");
			
		/* Fifth line: EVs */
		boolean first = true;
		for(String s : statNames()) {
			if(getEV(s) != 0) {
				if(first) { 
					sb.append("EVs:");
					first = false;
				} else sb.append(" /");
				sb.append(" "+getEV(s)+" "+s);
			}
		}
		if(!first) sb.append("\n");	//append newline only if any EVs is != 0
		
		/* Sixth line: IVs */
		first = true;
		for(String s : statNames()) {
			if(getIV(s) != MAX_IV) {
				if(first) { 
					sb.append("IVs:");
					first = false;
				} else sb.append(" /");
				sb.append(" "+getIV(s)+" "+s);
			}
		}
		if(!first) sb.append("\n");	//append newline only if any IVs is != 0
		
		/* Seventh line: Nature */
		sb.append("Nature: "+nature+"\n");
		
		/* Eighth/Ninth/Tenth/Eleventh lines: moves */
		for(Move m : move) 
			if(m != null) sb.append("- "+m.getName()+"\n");
			
		/* Final line: append a symbol to signal EOF (not really necessary) */
		//sb.append("--END\n");
		
		return sb.toString();			
	}
	
	/** Comparator implementation: sort by level and, within each level, by name */
	public int compareTo(final Pony p) {
		if(level > p.level) return 1;
		else if(level < p.level) return -1;
		else return name.compareTo(p.name);
	}
		
	/** @return String with max IV (random if tie) */
	public String getMaxIV() {
		TreeMap<Integer,String> mapIV = new TreeMap<Integer,String>();
		
		mapIV.put(hpIV,"hp");
		mapIV.put(atkIV,"atk");
		mapIV.put(defIV,"def");
		mapIV.put(spatkIV,"spatk");
		mapIV.put(spdefIV,"spdef");
		mapIV.put(speedIV,"speed");
		
		return mapIV.get(mapIV.lastKey()).toString();
	
	}

	public void addVolatileEffectiveness(final Type t, final float mul) {
		volatileEffectiveness.put(t, mul);
	}

	public Float removeVolatileEffectiveness(final Type t) {
		return volatileEffectiveness.remove(t);
	}

	public void addStatus(final Status status) {
		switch(status) {
			case PARALYZED:
				paralyzed = true;
				break;
			case ASLEEP:
				asleep = true;
				break;
			case PETRIFIED:
				petrified = true;
				break;
			case INTOXICATED:
				intoxicated = true;
				poisoned = true;
				break;
			case POISONED:
				poisoned = true;
				break;
			case BURNED:
				burned = true;
				break;
			case CONFUSED:
				confused = true;
				break;
		}
	}

	public void healStatus() {
		paralyzed = false;
		asleep = false;
		sleepCounter = 0;
		petrified = false;
		intoxicated = false;
		toxicCounter = 0;
		poisoned = false;
		burned = false;
		confused = false;
		confusionCounter = 0;
	}

	public void healStatus(final Status status) {
		switch(status) {
			case PARALYZED:
				paralyzed = false;
				break;
			case ASLEEP:
				asleep = false;
				break;
			case PETRIFIED:
				petrified = false;
				break;
			case INTOXICATED:
				intoxicated = false;
				poisoned = false;
				break;
			case POISONED:
				poisoned = false;
				break;
			case BURNED:
				burned = false;
				break;
			case CONFUSED:
				confused = false;
				break;
		}
	}

	/** This method uses reflection to invoke methods of item, ability and hazards (all of which
	 * are TriggeredEffectDealers); it quietly catches NoSuchMethodExceptions and throws
	 * all other kinds of exceptions.
	 * @param what The name of the TriggeredEffectDealers' method to call.
	 * @param be The BattleEngine managing the current battle turn.
	 */
	public void trigger(String what,final BattleEngine be) {
		if(Debug.on) printDebug("["+name+"] Called trigger("+what+")");
		// discover if we're defending or attacking
		int side = -1;
		if(team == be.getTeam1()) side = 1;
		else if(team == be.getTeam2()) side = 2;

		// we must consider all the possible TriggeredEffectDealers
		for(TriggeredEffectDealer ed : getTriggeredEffectDealers()) {
			try {
				Method called = ed.getClass().getMethod(what,BattleEngine.class);
				try {
					if(Debug.on) printDebug("["+name+"] triggering "+ed.getName()+"::"+what);
					called.invoke(ed,be);
				} catch(InvocationTargetException e) {
					printDebug("InvocationTargetException in trigger("+what+")");
					printDebug("Caused by: "+e.getCause());
					e.printStackTrace();
				} catch(Exception ee) {
					throw new RuntimeException(ee);
				}
			} catch(NoSuchMethodException e) {
				//ok, means effect dealer doesn't activate with that trigger
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
		}
		if(side != 1 && side != 2) {
			printDebug("[Pony.trigger("+what+")] Error: pony belongs to neither team!");
			return;
		}
		if(!be.getHazards(side).isEmpty()) {
			for(Hazard h : be.getHazards(side)) {
				try {
					Method called = h.getClass().getMethod(what,BattleEngine.class);
					try {
						if(Debug.on) printDebug("["+name+"] triggering "+h.getName()+"::"+what);
						called.invoke(h,be);
					} catch(InvocationTargetException e) {
						printDebug("InvocationTargetException in trigger("+what+")");
						printDebug("Caused by: "+e.getCause());
						e.printStackTrace();
					} catch(Exception ee) {
						throw new RuntimeException(ee);
					}
				} catch(NoSuchMethodException e) {
					//ok, means hazard doesn't activate with that trigger
				} catch(Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}		

	/** Given a stat, returns the modifier due to the pony's nature (1, 0.9 or 1.1) */
	public float natureModifier(String stat) {
		return natureModifier(stat,nature);
	}

	/** @return Float modifier depending on nature. */
	public static float natureModifier(String stat,Nature n) {
		
		switch(n) {
			//neutral
			case FRIENDLY:	
			case SENSITIVE:
			case DILIGENT:
			case SMILEY:
			case CHAOTIC:
				return 1;
			//+Atk -Def
			case PROUD:
				if(stat.equals("atk")) return 1.1f;
				else if(stat.equals("def")) return 0.9f;
				else return 1;
			//+Atk -SpA
			case PRAGMATIC:
				if(stat.equals("atk")) return 1.1f;
				else if(stat.equals("spatk")) return 0.9f;
				else return 1;
			//+Atk -SpD
			case SELFISH:
				if(stat.equals("atk")) return 1.1f;
				else if(stat.equals("spdef")) return 0.9f;
				else return 1;
			//+Atk -Spe
			case BLACKHEARTED:
				if(stat.equals("atk")) return 1.1f;
				else if(stat.equals("speed")) return 0.9f;
				else return 1;
			//+Def -Atk
			case SHY:
				if(stat.equals("def")) return 1.1f;
				else if(stat.equals("atk")) return 0.9f;
				else return 1;
			//+Def -Spa
			case PATIENT:
				if(stat.equals("def")) return 1.1f;
				else if(stat.equals("spatk")) return 0.9f;
				else return 1;
			//+Def -SpD
			case DEPENDABLE:
				if(stat.equals("def")) return 1.1f;
				else if(stat.equals("spdef")) return 0.9f;
				else return 1;
			//+Def -Spe
			case TACITURN:
				if(stat.equals("def")) return 1.1f;
				else if(stat.equals("speed")) return 0.9f;
				else return 1;
			//+SpA -Atk
			case EGGHEAD:
				if(stat.equals("spatk")) return 1.1f;
				else if(stat.equals("atk")) return 0.9f;
				else return 1;
			//+SpA -Def
			case SOLITARY:
				if(stat.equals("spatk")) return 1.1f;
				else if(stat.equals("def")) return 0.9f;
				else return 1;
			//+SpA -SpD
			case RANDOM:
				if(stat.equals("spatk")) return 1.1f;
				else if(stat.equals("spdef")) return 0.9f;
				else return 1;			
			//+SpA -Spe
			case BOOKWORM:
				if(stat.equals("spatk")) return 1.1f;
				else if(stat.equals("speed")) return 0.9f;
				else return 1;			
			//+SpD -Atk
			case FABULOUS:
				if(stat.equals("spdef")) return 1.1f;
				else if(stat.equals("atk")) return 0.9f;
				else return 1;
			//+SpD -Def
			case SILLY:
				if(stat.equals("spdef")) return 1.1f;
				else if(stat.equals("def")) return 0.9f;
				else return 1;
			//+SpD -SpA
			case RADIANT:
				if(stat.equals("spdef")) return 1.1f;
				else if(stat.equals("spatk")) return 0.9f;
				else return 1;
			//+SpD -Spe
			case MYSTERIOUS:
				if(stat.equals("spdef")) return 1.1f;
				else if(stat.equals("speed")) return 0.9f;
				else return 1;
			//+Spe -Atk
			case STYLISH:
				if(stat.equals("speed")) return 1.1f;
				else if(stat.equals("atk")) return 0.9f;
				else return 1;
			//+Spe -Def
			case AWESOME:
				if(stat.equals("speed")) return 1.1f;
				else if(stat.equals("def")) return 0.9f;
				else return 1;
			//+Spe -SpA
			case COOL:
				if(stat.equals("speed")) return 1.1f;
				else if(stat.equals("spatk")) return 0.9f;
				else return 1;
			//+Spe -SpD
			case RADICAL:
				if(stat.equals("speed")) return 1.1f;
				else if(stat.equals("spdef")) return 0.9f;
				else return 1;
			default:
				return 1;	
		}
		
	}

	/////////////////////////////////////////////////////////// END PUBLIC
	
	//////////////// PROTECTED METHODS / FIELDS ////////////////////

	// parameters
	protected String name;
	protected Type[] type = new Type[MAX_TYPES];
	protected int level;
	protected Nature nature;
	protected Race race;
	protected Move[] move = new Move[MOVES_PER_PONY];
	protected HashMap<String,Integer> learnableMoves = new LinkedHashMap<String,Integer>(){};
	protected String[] possibleAbilities = new String[ABILITIES_PER_PONY];
	
	// stats
	protected int hp;
	protected int baseHp;
	protected int baseAtk;
	protected int baseSpatk;
	protected int baseDef;
	protected int baseSpdef;
	protected int baseSpeed;
	
	// temporary modifiers
	protected EnumMap<Type,Float> volatileEffectiveness = new EnumMap<Type,Float>(Type.class);

	// etc
	protected boolean canon = true;
	/** URL of the (possibly animated) sprite showed in battle (frontal view) */
	protected URL frontSprite;
	/** URL of the (possibly animated) sprite showed in battle (back view) */
	protected URL backSprite;
	/** URL of the small token image showed in teambuilder and similar; CURRENTLY UNUSED: we use a scaled instance of the frontSprite instead. */
	protected URL token;

	//////////////////////////////////////////////////////////// END PROTECTED
	
	/////////////// PRIVATE METHODS / FIELDS //////////////////
	/** Chooses at random a Nature among the ones in the Nature enum and returns it. */
	private Nature generateNature() {
		int rand = (int) (Nature.values().length * Math.random());
		if(Debug.pedantic) printDebug("Rand: "+rand);
		return Nature.values()[rand];
	}
	
	/** Gives random values [0-31] to IVs */
	private void generateIV() {
		Random rand = new Random();
		
		hpIV = rand.nextInt(MAX_IV+1);
		atkIV = rand.nextInt(MAX_IV+1);
		defIV = rand.nextInt(MAX_IV+1);
		spatkIV = rand.nextInt(MAX_IV+1);
		spdefIV = rand.nextInt(MAX_IV+1);
		speedIV = rand.nextInt(MAX_IV+1);
	}


	/** Prints a different message depending on the highest IV  */
	public String getIVMsg() {
		 return IVPhrases.get(getMaxIV())[getIV(getMaxIV()) % 5];
	}

	// miscellaneous
	private String nickname;
	private int happiness;
	private Item item;		
	private Ability ability;	
	/** Keep a reference to self team. */
	private Team team;
	private Move lastMoveUsed;
	private boolean deathScheduled;
	private boolean manualMaxHp;
	private int maxHp;	//used only if manualMaxHp is true.
	private boolean transformed;
	private Pony original;
	private String unlockPhrase;


	// status
	private boolean active;

	private boolean paralyzed;
	private boolean confused;
	private boolean burned;
	private boolean petrified;
	private boolean poisoned;
	private boolean intoxicated;
	private boolean asleep;
	
	private boolean flinched;
	private boolean isProtected;
	private boolean blocked;
	private boolean trapped;
	private boolean lockedOnMove;
	private boolean taunted;
	private boolean substitute;

	
	// IV	
	private int hpIV;
	private int atkIV;
	private int defIV;
	private int spatkIV;
	private int spdefIV;
	private int speedIV;

	// EV
	private int hpEV;
	private int atkEV;
	private int defEV;
	private int spatkEV;
	private int spdefEV;
	private int speedEV;
	
	// modifiers
	private int accuracyModifier;
	private int evasionModifier;
	private int atkModifier;
	private int defModifier;
	private int spatkModifier;
	private int spdefModifier;
	private int speedModifier;
	
	// these are more an easter egg than actually useful content
	private static HashMap<String,String[]> IVPhrases = new HashMap<String,String[]>();
	static {
		IVPhrases.put("hp",new String[] {
			"Loves sweets.",
			"Loves writing stories about adventures!",
			"Loves to have fancy parties!",
			"Loves berries!",
			"Loves sharing treats!"
		});
		IVPhrases.put("atk",new String[] {
			"Always watching clouds blow across the sky!",
			"Always dresses in style.",
			"Is always on time with the help of some magic!",
			"Is always smiling wherever she goes!",
			"Is always cheerful and trying new things!"
		});
		IVPhrases.put("def",new String[] {
			"Is friendly and sweet!",
			"Is so loyal.",
			"Is so helpful.",
			"Is always busy.",
			"Is so lucky."
		});
		IVPhrases.put("spatk",new String[] {
			"Loves tiaras.",
			"Reads all day.",
			"Is great at magic tricks!",
			"Loves learning with her friends!",
			"Keeps her friends laughing!"
		});
		IVPhrases.put("spdef",new String[] {
			"Lets her dreams soar!",
			"Has so many smart ideas to share!",
			"Gives her friends great advice!",
			"Is very gentle",
			"Sparkles with fun!"
		});
		IVPhrases.put("speed",new String[] {
			"Loves jumping.",
			"Loves windy days.",
			"Loves to skip, gallop and run everywhere!",
			"Loves sports.",
			"Loves dancing - on the ground and in the air!"
		});	
	}	
}
