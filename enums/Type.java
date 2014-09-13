//: enums/Type.java

package pokepon.enums;

import java.util.EnumMap;
import java.awt.Color;
import java.net.URL;
import pokepon.util.Meta;

/**
 * Contains enum with types of Ponies and Moves
 * @author silverweed
 */

public enum Type { 
	// canon elements of Harmony
	MAGIC("Magic"),
	LOYALTY("Loyalty"),
	HONESTY("Honesty"),
	LAUGHTER("Laughter"),
	KINDNESS("Kindness"),
	GENEROSITY("Generosity"),
	// 'villains types'
	CHAOS("Chaos"),
	NIGHT("Night"),
	SHADOW("Shadow"),
	// secondary characters' types
	SPIRIT("Spirit"), // Zecora's typing
	LOVE("Love"),	// Cadance's
	PASSION("Passion"),	// Scoot's,...
	MUSIC("Music"),	//Lyra,Vinyl,Octavia,...
	LIGHT("Light");	//Celestia's

	private String name;

	Type(final String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public Color getBGColor() {
		return new Color(color.get(this)[0]);
	}

	public Color getFGColor() {
		return new Color(color.get(this)[1]);
	}

	public URL getToken() {
		return getClass().getResource(Meta.complete2(Meta.TOKEN_DIR)+"/types/"+name.toLowerCase()+".png");
	}
	
	public static Type forName(String name) {
		for(Type t : values()) {
			if(t.name.equalsIgnoreCase(name))
				return t;
		}
		return null;
	}

	/** Map (type,Hex.{background color,foreground color}) */
	private static EnumMap<Type,int[]> color = new EnumMap<>(Type.class);
	static {
		color.put(MAGIC,	new int[] { 0x990066, 0x000000 });
		color.put(LOYALTY, 	new int[] { 0x0000FF, 0xFFFFFF });
		color.put(HONESTY, 	new int[] { 0xFF6600, 0x000000 });
		color.put(LAUGHTER, 	new int[] { 0xFF0099, 0x000000 });
		color.put(KINDNESS, 	new int[] { 0xFFFF66, 0x000000 });
		color.put(GENEROSITY, 	new int[] { 0x9900CC, 0x000000 });
		color.put(CHAOS, 	new int[] { 0x663300, 0x000000 });
		color.put(NIGHT, 	new int[] { 0x000055, 0xFFFFFF });
		color.put(SHADOW, 	new int[] { 0x111111, 0xFFFFFF });
		color.put(SPIRIT, 	new int[] { 0x666666, 0x000000 });
		color.put(PASSION, 	new int[] { 0xDD0000, 0x000000 });
		color.put(MUSIC, 	new int[] { 0x00FF99, 0x000000 });
		color.put(LOVE, 	new int[] { 0xFF00FF, 0x000000 });
		color.put(LIGHT, 	new int[] { 0xFFFF00, 0x000000 });
	}
	// TODO: add weather modificators
};
