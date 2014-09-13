//: move/SharpNails.java

package pokepon.move;

import pokepon.move.*;
import pokepon.enums.*;
import pokepon.move.hazard.*;
import pokepon.pony.*;

/** Hazard move which spreads SharpNailsHazard through enemy field;
 * Sharp Nails damage any non-PEGASUS/ALICORN/GRYPHON on switch-in
 * by 1/8, 1/6 and 1/4 of maximum hp.
 *
 * @author silverweed
 */
public class SharpNails extends Move {

	public SharpNails() {
		super("Sharp Nails");

		type = Type.SHADOW;
		moveType = Move.MoveType.STATUS;
		maxpp = pp = 10;
		accuracy = -1;
		priority = 0;
		description = "Scatter sharp nails on the enemy field which damage any non-flying opponent on switch-in.";
		briefDesc = "Enemies are damaged on switch-in.<br>Flying races are immune.";

		animation.put("name","Ballistic");
		animation.put("sprite","nail.png");
		animation.put("bounceBack",false);

		hazard = new SharpNailsHazard();				
	}

	public SharpNails(Pony p) {
		this();
		pony = p;
	}
	
	@Override
	public String getPhrase() {
		return hazard.getPhrase();
	}
}


