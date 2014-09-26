//: move/SkyDive.java

package pokepon.move;

import pokepon.enums.*;
import pokepon.pony.Pony;

/**
 * Inflicts good physical damage.
 *
 * @author silverweed
 */

public class SkyDive extends Move {

	public SkyDive() {
		super("Sky Dive");
		type = Type.LOYALTY;
		moveType = Move.MoveType.PHYSICAL;
		maxpp = pp = 30;
		baseDamage = 90;
		accuracy = 100;
		priority = 0;
		description = "Swoop towards your enemy from the sky to inflict massive damage.";
		briefDesc = "Inflicts regular damage.";

		animation.put("name","Compound");
		animation.put("anims", java.util.Arrays.asList("Fade","Fade"));
		animation.put("sprite","user");
		animation.put("transparent",true);
		animation.put("fadeOut",true);
		animation.put("persistent",true);
		animation.put("1:iterations",40f);
		animation.put("1:delay",50);
		animation.put("1:accelerated",true);
		animation.put("1:initialPoint","ally");
		animation.put("1:finalPoint","opp b450X b450Y");
		animation.put("1:rewind",true);
		animation.put("2:initialPoint","opp -300X -300Y");
		animation.put("2:finalPoint","opp +300X +250Y");
		animation.put("2:rewindTo","ally");
		//animation.put("name","Direct");
		//animation.put("sprite","user");
		//animation.put("passThrough",true);
	}

	public SkyDive(Pony p) {
		this();
		pony = p;
	}
}
