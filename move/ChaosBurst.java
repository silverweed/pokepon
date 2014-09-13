//: move/ChaosBurst.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.move.*;
import pokepon.pony.*;
import pokepon.player.*;
import pokepon.battle.*;
import java.util.*;

/**
 * Activate a random damaging move from the team.
 *
 * @author Giacomo Parolini
 */
public class ChaosBurst extends Move {
	
	public ChaosBurst() {
		super("Chaos Burst");
		
		type = Type.CHAOS;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 15;
		accuracy = 80;
		priority = 0;
		description = "The user chooses one damaging move at random from the team and uses it.";
		briefDesc = "Uses a random damaging move from<br>a random member of the team.";
		startsSubMove = true;
	}

	public ChaosBurst(Pony p) {
		this();
		pony = p;
	}

	/** Move will fail if there are no other ponies in team or nopony has a damaging move. */
	@Override
	public boolean validConditions(final BattleEngine be) {
		if(be.getTeamAttacker().members() < 2) return false;
		for(Pony p : be.getTeamAttacker().getAllPonies()) {
			if(p == be.getAttacker()) continue;
			for(Move m : p.getMoves()) {
				if(m.getBaseDamage() != 0) return true;
			}
		}
		return false;
	}

	/** Select and return move to copy */
	@Override
	public Move spawnSubMove(final BattleEngine be) {
		List<Move> possibleMoves = new ArrayList<Move>();

		if(be.getTeamAttacker().members() < 2) {
			throw new RuntimeException("Not enough members in team! Why didn't validConditions prevent this?");
		}

		for(Pony p : be.getTeamAttacker().getAllPonies()) {
			if(p == be.getAttacker()) continue;
			for(Move m : p.getMoves()) {
				if(m.getBaseDamage() != 0) possibleMoves.add(m);
			}
		}
		
		if(possibleMoves.size() == 0) 
			throw new MoveNotFoundException("No valid move could be found in team! Why didn't validConditions prevent this?");

		Move newmove = possibleMoves.get((new Random()).nextInt(possibleMoves.size()));
		return newmove;
	}
		
}
