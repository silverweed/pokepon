//: main/BattleTest.java

package pokepon.main;

import java.io.*;
import java.util.*;
import pokepon.battle.*;
import pokepon.pony.*;
import pokepon.player.*;
import pokepon.move.*;
import pokepon.util.*;
import pokepon.ability.*;
import static pokepon.util.MessageManager.*;

/** Pok&#233Pon - by jp &#169 2013
 * A battle testing class (old, useless)
 *
 * @author Giacomo Parolini
 */
public class BattleTest implements TestingClass {
	
	public static void main(String[] args) throws Exception {
		consoleHeader("   Launching BattleTest...   ");

		Player[] p = { new Player(),new Player() };

		/*p1.addPony(PonyCreator.create("Chrysalis","Fluttershy","RainbowDash","Rarity","Trixie"));
		p2.addPony(PonyCreator.create("Discord","PrincessCadance","PrincessCelestia"));

		p2.getPony("Discord").setAbility(new ChaosMagic());
		p1.getPony("Trixie").setAbility(new Boasting());

		p1.getPony("Chrysalis").learnMove(new Mutate());
		p1.getPony("Chrysalis").learnMove(new NastyPlot());
		p1.getPony("Rarity").learnMove(new MartialArts());
		p1.getPony("Rarity").learnMove(new GemStorm());
		p1.getPony("Rarity").learnMove(new HornBeam());
		p1.getPony("Fluttershy").learnMove(new Lullaby());
		p1.getPony("Fluttershy").learnMove(new Stare());
		p2.getPony("Discord").learnMove(new ChaosBurst());
		p2.getPony("Princess Celestia").learnMove(new RaiseSun());
		p2.getPony("Princess Celestia").learnMove(new FriendshipCannon());
		p2.getPony("Princess Cadance").learnMove(new CrystalShield());
		p1.getPony("Rainbow Dash").learnMove(new SonicRainboom());
		p1.getPony("Rainbow Dash").learnMove(new SpeedUp());

		for(Pony p : p1.getTeam().getAllPonies())
			p.learnMove(new Tackle(),false);

		for(Pony p : p2.getTeam().getAllPonies())
			p.learnMove(new Tackle(),false);
		*/

		p[0].setTeam(Team.randomTeam(6));
		//p[0].addPony(PonyCreator.create("Princess Cadance","Shining Armor","Discord","Chief Thunderhooves","Snowflake"));
		p[1].setTeam(Team.randomTeam(6));
		
		/*p[0].getPony("Princess Cadance").learnMove(new LoveBurst(p[0].getPony("Princess Cadance")));
		p[0].getPony("Shining Armor").learnMove(new LoveBurst(p[0].getPony("Shining Armor")));
		p[0].getPony("Discord").learnMove(new Rampage(p[0].getPony("Discord")));
*/
		try {
			for(int i = 0; i < 2; ++i) {
				for(int j = 0; j < 6; ++j) {
					Pony pony = p[i].getTeam().getPony(j);
					for(String m : pony.getLearnableMoves().keySet()) {
						pony.learnMove(m);
					}
					if(pony.getPossibleAbilities().size() != 0) {
						pony.setAbility(AbilityCreator.create(pony.getPossibleAbilities().get((new Random()).nextInt(pony.getPossibleAbilities().size()))));
						printDebug(pony.getNickname()+": set ability to "+pony.getAbility());
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}

		Battle battle = new Battle(p[0],p[1]);
		
		//battle.start();

		System.exit(0);
		//p1.getPony(0).printInfo();

	}
}
