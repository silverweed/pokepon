//: move/hazard/Hazard.java

package pokepon.move.hazard;

import pokepon.battle.*;
import pokepon.enums.*;
import pokepon.util.*;
import static pokepon.util.MessageManager.*;
import java.net.*;

public abstract class Hazard extends TriggeredEffectDealer {

	public Hazard() {
		super();
	}

	public Hazard(String name) {
		super(name);
	}

	/** [0]: phrase for the user; [1]: phrase for the target */
	public abstract String[] getSetupPhrase();
	
	public int getLayers() { return layers; }
	public int getMaxLayers() { return maxLayers; }
	public void addLayer() { 
		if(layers < maxLayers)
			++layers;
	}
	public void removeLayer() {
		if(layers > 0)
			--layers;
	}
	public void setLayers(final int layers) {
		this.layers = layers;
	}
	public URL getToken() {
		if(token == null)
			return null;

		return getClass().getResource(Meta.complete2(Meta.TOKEN_DIR)+"/moves/fx/"+token);
		//new URL("file://"+getTokensURL().getPath()+DIRSEP+"moves"+DIRSEP+"fx"+DIRSEP+token);
	}

	/** This must be called manually each time a hazard is set. */
	public void setSide(final int s) { 
		side = s;
	}

	/** Current layers */
	protected int layers = 1;
	/** Maximum amount of stackable layers */
	protected int maxLayers = 1;
	/** Can be 1 or 2 for player 1 or player 2 (see BattleEngine) */
	protected int side;
	/** Filename of the token image (path relative to Tokens directory) */
	protected String token;
}	
