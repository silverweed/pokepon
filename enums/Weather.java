//: enums/Weather.java

package pokepon.enums;

/** Contains enum with all possible Weathers
 * @author silverweed
 */

public enum Weather { 

	CLEAR("Clear"),		// no weather
	SUNNY("Sunny"),
	DARK("Dark"),		//reduces accuracy of everypony but NIGHT and SHADOW types by 1.
	CHAOTIC("Chaotic"),	//inverts weaknesses and resistances. Immunities become 4x weaknesses.
	STORMY("Stormy");	//damages everypony but alicorns (more damage to pegasi/gryphons) 

	private String name;

	Weather(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public static Weather forName(String name) {
		for(Weather w : values()) {
			if(w.name.equals(name)) return w;
		}
		return null;
	}

	public String getPhrase() {
		switch(this) {
			case STORMY:
				return "[pony] was damaged by the heavy storm!";
			default:
				return "";
		}
	}
};
