//: battle/Format.java

package pokepon.battle;

import java.util.*;

public interface Format {
	public String getName();
	public Set<String> getBannedPonies();
	public Set<String> getBannedMoves();
	public Set<String> getBannedAbilities(); 
	public Set<String> getBannedItems();
	public Set<String[]> getBannedCombos();
	public Set<String> getSpecialFormats();
}
