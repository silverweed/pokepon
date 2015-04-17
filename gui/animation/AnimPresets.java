package pokepon.gui.animation;

import java.util.*;

/** A collection of preset Compound animations;
 * use like this:
 * <pre>
 * animation = new HashMap<>(AnimPresets.get(name);
 * animation.put("sprite", "sprite.png");
 * </pre>
 * @author silverweed
 */
public class AnimPresets {

	private static Map<String, Map<String,Object>> presets = new HashMap<>();
	
	public static Map<String,Object> get(final String name) {
		return presets.get(name);
	}

	static {
		Map<String,Object> tmp = new HashMap<>();
		// Daredevilry
		tmp.put("name","Compound");
		tmp.put("anims", Arrays.asList("Ballistic2", "Direct"));
		tmp.put("delay", 30);
		tmp.put("2:passThrough", true);
		tmp.put("2:accelerated", true);
		presets.put("ballistic-direct", new HashMap<>(tmp));
		tmp.clear();

		// Dissonance
		tmp.put("name", "Compound");
		tmp.put("anims", Arrays.asList("Fade", "Fade", "Fade"));
		tmp.put("transparent", true);
		tmp.put("accelerated", true);
		tmp.put("delay", 50);
		tmp.put("initialOpacity", 0.2f);
		tmp.put("finalOpacity", 1f);
		tmp.put("1:initialPoint", "opp +150Y");
		tmp.put("1:finalPoint", "opp -150Y");
		tmp.put("2:initialPoint", "opp +75X +150Y");
		tmp.put("2:finalPoint", "opp +75X -150Y");
		tmp.put("3:initialPoint", "opp +150Y");
		tmp.put("3:finalPoint", "opp -150Y");
		presets.put("rise-from-below", new HashMap<>(tmp));
		tmp.clear();

		// Whirling Hoof
		tmp.put("name","Compound");
		tmp.put("anims",Arrays.asList("Whirl","Ballistic2"));
		tmp.put("persistent",true);
		presets.put("whirl-ballistic", new HashMap<>(tmp));
		tmp.clear();

		// Sky Dive
		tmp.put("name","Compound");
		tmp.put("anims", Arrays.asList("Fade","Fade"));
		tmp.put("transparent",true);
		tmp.put("fadeOut",true);
		tmp.put("persistent",true);
		tmp.put("1:iterations",40f);
		tmp.put("1:delay",50);
		tmp.put("1:accelerated",true);
		tmp.put("1:initialPoint","ally");
		tmp.put("1:finalPoint","opp b450X b450Y");
		tmp.put("1:rewind",true);
		tmp.put("2:initialPoint","opp -300X -300Y");
		tmp.put("2:finalPoint","opp +300X +250Y");
		tmp.put("2:rewindTo","ally");
		presets.put("cross", new HashMap<>(tmp));
		tmp.clear();

		// Cutie Unmark
		tmp.put("name", "Compound");
		tmp.put("anims", Arrays.asList("Whirl", "Fade"));
		tmp.put("initialPoint", "opp");
		tmp.put("1:center","opp +30X +30Y");
		tmp.put("1:shakes", 1);
		tmp.put("2:transparent", true);
		tmp.put("2:finalPoint", "ally");
		tmp.put("2:fadeOut", false);
		presets.put("whirl-absorb", new HashMap<>(tmp));
		tmp.clear();

		// Canterlot Voice
		tmp.put("name", "Compound");
		tmp.put("anims", Arrays.asList("Direct", "Direct", "Direct"));
		tmp.put("bounceBack", false);
		tmp.put("passThrough", true);
		tmp.put("delay", 10);
		presets.put("gatling", new HashMap<>(tmp));
		tmp.clear();

		// Bizaam
		tmp.put("name", "Compound");
		tmp.put("anims", Arrays.asList("Shake","Direct"));
		tmp.put("1:shakes", 6);
		tmp.put("1:delay", 10);
		tmp.put("2:initialPoint","ally");
		tmp.put("2:finalPoint","opp");
		tmp.put("2:bounceBack", false);
		presets.put("shake-direct", new HashMap<>(tmp));
		tmp.clear();

		// Eerie Sonata
		tmp.put("name", "Compound");
		tmp.put("anims", Arrays.asList("Direct","Whirl"));
		tmp.put("1:initialPoint", "ally");
		tmp.put("1:finalPoint", "opp");
		tmp.put("1:bounceBack", false);
		tmp.put("1:persistent", false);
		tmp.put("2:center","opp +50X +50Y");
		tmp.put("2:radius",50);
		presets.put("direct-whirl", new HashMap<>(tmp));
		tmp.clear();

		// Dodge
		tmp.put("name", "Compound");
		tmp.put("anims", Arrays.asList("Shift", "Shift"));
		tmp.put("delay", 10);
		tmp.put("1:initialPoint", "ally");
		tmp.put("1:finalPoint", "ally b50X");
		tmp.put("1:postWait", 300);
		tmp.put("2:initialPoint", "ally");
		tmp.put("2:finalPoint", "ally f50X");
		presets.put("dodge", new HashMap<>(tmp));
		tmp.clear();
	}
}
