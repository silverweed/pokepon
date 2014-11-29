//: move/Move.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.move.hazard.*;
import pokepon.pony.*;
import pokepon.battle.*;
import pokepon.player.*;
import pokepon.util.Meta;
import static pokepon.util.MessageManager.*;
import java.net.*;
import java.util.*;

/** The abstract class for Moves.
 * @author silverweed
 */
public abstract class Move extends EffectDealer {

	public static final int MAX_NAME_LENGTH = 20;

	///////////////// CONSTANT VALUES ///////////////////////

	public static enum MoveType { 
		PHYSICAL("Physical"),
		SPECIAL("Special"),
		STATUS("Status");

		private String name;

		MoveType(final String name) {
			this.name = name;
		}

		public URL getToken() {
			return getClass().getResource(Meta.complete2(Meta.TOKEN_DIR)+"/moves/movetypes/"+name+".png");
		}

		public static MoveType forName(final String name) {
			for(MoveType m : values())
				if(name.equalsIgnoreCase(m.name))
					return m;
			return null;
		}

		@Override
		public String toString() {
			return name;
		}
	};

	///////////////// PUBLIC METHODS / FIELDS /////////////////

	/** Default constructor: creates a Move with no name nor pony associated */
	public Move() {
		super();
	}

	/** Creates a Move with a name, but with no pony associated */
	public Move(String name) {
		super(name);
	}

	/** This is the only constructor which the child classes should implement, as it
	 * guarantees that the move has an associated pony. */
	public Move(String name,Pony p) {
		super(name,p);
	}

	// GET METHODS //
	public int getId() { return id; }
	public Type getType() { return type; }
	public MoveType getMoveType() { return moveType; }
	public boolean isContactMove() { return contactMove; }
	public int getPP() { return pp; }
	public int getMaxPP() { return maxpp; }
	/** @return True if move has all PP, False otherwise. */
	public boolean fullPP() { return pp == maxpp; }
	public int getBaseDamage() { return baseDamage; }
	public int getDamage() { return baseDamage + damageBoost; }
	public int getAccuracy() { return accuracy; }
	public int getPriority() { return priority + bonusPriority; }
	public int getBasePriority() { return priority; }
	public int getHits() { return hits; }
	public List<Float> getHitsChance() { return hitsChance; }
	/** If a move (especially a STATUS move) has specific conditions to resolve, 
	 * it must override this function to provide them; this can also be used just
	 * to apply side effects to the move (for example, to set the baseDamage, if this
	 * depends on external conditions).
	 */
	public boolean validConditions(final BattleEngine be) { return true; }
	public boolean isOHKO() { return OHKO; }
	public boolean isTypeless() { return typeless; }

	// additional effects
	
	public int getTurnDelay() { return turnDelay; }
	public int getDuration() { return duration; }
	/** Spawns another move and replies its effects 
	 * (override with a "return new Move...") */
	public Move spawnSubMove() { return this; }
	public Move spawnSubMove(final BattleEngine be) { return this; }
	/** If spawnSubMove() is overridden, this must return true. */
	public boolean startsSubMove() { return startsSubMove; }
	/** If 0, then using this move kills
	 * the user the very same turn; else, schedules its death after selfKODelay turns;
	 * the selfKO effect applies at the end of the turn for both the user and the target;
	 * note that a SelfDestruct-like move won't use this method, because this only kills
	 * the user at the end of the whole turn; to kill the user immediately after, use
	 * a custom BattleEvent
	 */
	public int getSelfKODelay() { return selfKODelay; }
	/** This is like selfKO, but affects the target. */
	public int getTargetKODelay() { return targetKODelay; }
	public boolean copyVolatiles() { return copyVolatiles; }
	/** This gets called if the move doesn't hit successfully; by default, it does nothing. */
	public void onMoveFail(final BattleEngine be) {};

	// special-physical mods
	public boolean useTargetAtk() { return useTargetAtk; }
	public boolean useTargetSpatk() { return useTargetSpatk; }
	public boolean useTargetDef() { return useTargetDef; }
	public boolean useTargetSpdef() { return useTargetSpdef; }

	/// delayed effects
	public boolean isDelayed() { return delayed || blockingDelayed; }
	/** A 'blocking delayed' move doesn't let the user do anything until the delay is passed. */
	public boolean isBlockingDelayed() { return blockingDelayed; }
	/** This function is used to:
	 * 1- set turnDelay and countDelay for delayed effects
	 * 2- print preparing message
	 * NOTE that turnDelay must be set *HERE*, not in the constructor.
	 */
	public void prepareDelayEffects(final BattleEngine be) { return; }

	// counters (can be directly manipulated for ease of coding)
	public int countDelay;		//counter for turn delay 
	public int countDuration;	//counter for duration

	/** This function gets called when countDelay reaches 0 (only if move has delay greater than 0)
	 * and returns an EffectDealer whose effects are applied; if overridden, it is suggested to return
	 * an instance of an anonymous class implementing all the desired effects.
	 */
	public EffectDealer delayEffects() { return new EffectDealer(); }
	public BattleEvent[] getBattleEvents() { return new BattleEvent[0]; }
	public int getLockingTurns() { return lockingTurns; }
	public String locksTargetOn() { return locksTargetOn; }

	/// miscellaneous
	public boolean isBeamMove() { return beamMove; }

	@Override
	public void reset() {
		turnDelay = 0;
		countDelay = 0;
		countDuration = 0;
		damageBoost = 0;
		bonusPriority = 0;
	}
	
	/// hazard effects
	public Hazard getHazard() { return hazard; }

	/** Prints information of all fields.
	 */
	public void printInfo() {
		printMsg("id: "+id
			+"\nname: "+name
			+"\ntype: "+(typeless ? "<typeless>" : type)
			+"\nmovetype: "+moveType
			+"\npp: "+pp+" / "+maxpp
			+"\nbase_damage: "+baseDamage
			+"\naccuracy: "+(accuracy > 0 ? accuracy : "infinite")
			+"\npriority: "+priority
			+"\ndescription: "+description
			+"\nbrief desc: "+briefDesc
		);
		if(OHKO) printMsg("OHKO move");
		if(selfKODelay > -1) printMsg("Self-KO move (after "+selfKODelay+" turns)");
		if(targetKODelay > -1) printMsg("Target-KO move (after "+targetKODelay+" turns)");
		if(delayed) printMsg("Delayed move.");
		if(blockingDelayed) printMsg("Blocking delayed move.");
		if(hits > 1) printMsg("maxHits: "+hits);
		if(targetParalysis > 0) printMsg("% Paralize: " + targetParalysis*100 + "%");
		if(targetBurn > 0) printMsg("% Burn: " + targetBurn*100 + "%");
		if(targetPetrify > 0) printMsg("% Petrify: " + targetPetrify*100 + "%");
		if(targetSleep > 0) printMsg("% Sleep: " + targetSleep*100 + "%");
		if(targetConfusion > 0) printMsg("% Confuse: " + targetConfusion*100 + "%");
		if(targetPoison > 0) printMsg("% Poison: " + targetPoison*100 + "%");
	}
	
	public String toString() {
		return name;
	}


	// SET METHODS //
	public void setAccuracy(int acc) {
		accuracy = acc;
	}

	public void setBaseDamage(int dam) {
		baseDamage = dam;
	}
	
	public void setType(Type type) {
		this.type = type;
	}
	
	public void setDamageBoost(int boost) {
		damageBoost = boost;
	}

	public void setBonusPriority(byte tmpP) {
		bonusPriority = tmpP;
	}
	
	public void deductPP() {
		if(pp > 0) --pp;
	}

	public void setPP(int newpp) {
		pp = newpp;
		if(pp < 0) pp = 0;
		if(pp > maxpp) pp = maxpp;
	}
	
	
	/////////////////////////////////////////// END PUBLIC
	
	//////////////// PROTECTED METHODS / FIELDS ///////////////////
	
	protected int id; // currently unused
	protected Type type;
	protected MoveType moveType;	
	protected int pp;
	protected int maxpp;
	protected int baseDamage;
	protected int damageBoost;
	protected int accuracy;	//-1 means 'cannot fail'.
	protected byte priority;	// from -8 to 8 (?)
	protected byte bonusPriority;
	/** This means either the number of turns the user is locked, if
	 * this move locks user, or the number of turns the target is locked,
	 * if locks target (i.e locksTargetOn != null) 
	 */
	protected int lockingTurns;
	
	/* Hits */
	/** Max number of hits */
	protected int hits = 1;	
	/** List with relative probabilities to do 1,2,...hits */
	protected List<Float> hitsChance; 

	/* Additional Effects */
	protected int turnDelay;
	protected int duration;
	protected boolean delayed;
	protected boolean blockingDelayed;
	protected boolean OHKO;
	protected boolean startsSubMove;
	protected boolean typeless;
	protected int selfKODelay = -1;
	protected int targetKODelay = -1;
	protected Hazard hazard;
	protected boolean copyVolatiles;
	protected String locksTargetOn;

	/* Special-physical modifications */
	protected boolean useTargetAtk;
	protected boolean useTargetSpatk;
	protected boolean useTargetDef;
	protected boolean useTargetSpdef;

	/* Miscellaneous  */
	protected boolean contactMove;
	protected boolean beamMove;
}
