//: move/HiddenTalent.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.battle.*;
import pokepon.pony.Pony;
import pokepon.util.*;
import static pokepon.util.MessageManager.*;
import java.util.*;

/**
 * Its type depends on the pony's IVs with the following formula:
 * Type = [(a+2b+4c+8d+16e+32f) * 13] % 14
 * where a = HP IV, b = Atk IV etc
 * Types are numbered in the order of the Type enum.
 *
 * @author silverweed
 */

public class HiddenTalent extends Move {
	
	/** Note that constructing this move without a pony will produce 
	 * a HONESTY move.
	 */
	public HiddenTalent() {
		super("Hidden Talent");
		
		type = Type.HONESTY;
		moveType = Move.MoveType.SPECIAL;
		maxpp = pp = 15;
		baseDamage = 60;
		accuracy = 100;
		priority = 0;
		description = "";
		briefDesc = "Its type depends on the user's IV.";

		animation.put("name","Direct");
		animation.put("sprite","shadowball.png");
		animation.put("bounceBack",false);
	}

	public HiddenTalent(Pony p) {
		this();
		pony = p;

		type = getTypeByIVs(pony);
		if(Debug.pedantic) printDebug("[HiddenTalent] Type is "+type);
	}

	public Type getTypeByIVs(Pony pony) {
		int sum = 0;
		for(int i = 0; i < Pony.statNames().length; ++i)
			sum += (int)(Math.pow(2, i) * (pony.getIV(Pony.statNames()[i]) % 2));
		
		return Type.values()[(sum*13) % 14];
	}

	/** Given a pony and a type, adjusts the pony's IVs in order to be
	 * the best set compatible with that type.
	 */
	public static void adjustIVs(Pony p, Type t) {
		List<byte[]> tolower = new ArrayList<>();
		switch(t) {
			case MAGIC:
				tolower.add(new byte[] { 1, 0, 0, 0, 1, 1 });
				tolower.add(new byte[] { 1, 1, 1, 0, 0, 0 });
				tolower.add(new byte[] { 1, 1, 0, 0, 0, 1 });
				tolower.add(new byte[] { 1, 0, 1, 0, 1, 0 });
				break;
			case LOYALTY:
				tolower.add(new byte[] { 0, 0, 0, 1, 0, 0 });
				break;
			case HONESTY:
				tolower.add(new byte[] { 1, 0, 0, 1, 0, 0 });
				break;
			case LAUGHTER:
				tolower.add(new byte[] { 0, 1, 0, 1, 0, 0 });
				tolower.add(new byte[] { 0, 0, 0, 1, 1, 0 });
				break;
			case KINDNESS:
				tolower.add(new byte[] { 1, 1, 0, 1, 0, 0 });
				tolower.add(new byte[] { 1, 0, 0, 1, 1, 0 });
				break;
			case GENEROSITY:
				tolower.add(new byte[] { 0, 0, 1, 1, 0, 0 });
				tolower.add(new byte[] { 0, 0, 0, 1, 0, 1 });
				break;
			case CHAOS:
				tolower.add(new byte[] { 1, 0, 1, 1, 0, 0 });
				tolower.add(new byte[] { 1, 0, 0, 1, 0, 1 });
				break;
			case NIGHT:
				tolower.add(new byte[] { 0, 0, 0, 0, 0, 0 });
				break;
			case SHADOW:
				tolower.add(new byte[] { 1, 0, 0, 0, 0, 0 });
				break;
			case SPIRIT:
				tolower.add(new byte[] { 0, 1, 0, 0, 0, 0 });
				tolower.add(new byte[] { 0, 0, 0, 0, 1, 0 });
				break;
			case LOVE:
				tolower.add(new byte[] { 1, 1, 0, 0, 0, 0 });
				tolower.add(new byte[] { 1, 0, 0, 0, 1, 0 });
				break;
			case PASSION:
				tolower.add(new byte[] { 0, 0, 0, 0, 0, 1 });
				break;
			case MUSIC:
				tolower.add(new byte[] { 1, 0, 1, 0, 0, 0 });
				tolower.add(new byte[] { 1, 0, 0, 0, 0, 1 });
				break;
			case LIGHT:
				tolower.add(new byte[] { 0, 0, 1, 0, 1, 0 });
				tolower.add(new byte[] { 0, 1, 1, 0, 0, 0 });
				tolower.add(new byte[] { 0, 1, 0, 0, 0, 1 });
				tolower.add(new byte[] { 0, 0, 0, 0, 1, 1 });
				break;
		}
		int best = Pony.TOT_EV;
		int[] evs = p.getEVs();
		byte[] bestcombo = null;

		/* Calculate the best combination of IVs which penalizes the least the
		 * given EV spread of the pony.
		 */
		for(byte[] lower : tolower) {
			int tot = 0;
			for(int i = 0; i < 6; ++i)
				tot += (int)lower[i] * evs[i];
			if(tot < best) {
				best = tot;
				bestcombo = lower;
			}
		}
		for(int i = 0; i < 6; ++i) 
			p.setIV(p.statNames()[i], 31 - (int)bestcombo[i]);
	}
}
