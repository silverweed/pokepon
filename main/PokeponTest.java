//: main/PokeponTest

package pokepon.main;

import java.io.*;
import pokepon.battle.*;
import pokepon.pony.*;
import pokepon.player.*;
import pokepon.move.*;
import pokepon.util.*;
import pokepon.ability.*;
import pokepon.item.*;
import pokepon.main.TestingClass;
import static pokepon.util.MessageManager.*;

/** Pok&#233Pon - by Silverweed &#169 2013-2014
 * A testing class (old, useless)
 *
 * @author Giacomo Parolini
 */
public class PokeponTest implements TestingClass {
	
	public static void main(String[] args) throws Exception {
		consoleHeader("   Launching Pokepon Test...   ");
		/* ***************************************************** */
		Player p1 = new Player();
		printDebug("Created player: "+p1);
		Player p2 = new Player();
		printDebug("Created player: "+p2);
		
		//td.printTypesTable();
		
		/*p1.getTeam().addPony(new RainbowDash(60),new PrincessLuna(80),new ShiningArmor(40),new TwilightSparkle(10),new Discord(100),new InkyPie(40));
		
		PrincessLuna luna = (PrincessLuna)(p1.getTeam().getPony("Princess Luna"));
		
		luna.learnMove(new FriendshipCannon());
		luna.learnMove(new EternalNight());
		luna.learnMove(new MagicShield());
		luna.learnMove(new CanterlotVoice());

		p1.getTeam().getPony("Inky Pie").learnMove(new RockThrow());
		p1.getTeam().getPony("Rainbow Dash").learnMove(new SonicRainboom());		
				
		p2.getTeam().addPony(new PinkiePie(100),new PrincessCelestia(100),new Chrysalis(70));
		
		p2.getTeam().getPony(0).learnMove(new PartyCannon());
		
		Pony disc = p1.getTeam().getPony("Discord");

		p1.getTeam().getPony("Discord").printInfo();
		p2.getTeam().getPony("Princess Celestia").printInfo();

		p1.getTeam().setActivePony("Discord");
		p2.getTeam().setActivePony("Princess Celestia");
		
		p1.getTeam().getPony("Discord").learnMove(new Rampage());
		
		BattleEngine be = new BattleEngine(p1.getTeam(),p2.getTeam(),null);
		
		be.ponyUseMove("Rampage");

		printMsg("Generating random pony: ");
		(new PonyCreator().createRandom()).printInfo();
		*/

		p1.addPony(PonyCreator.create("Discord"));
		p2.addPony(PonyCreator.create("PrincessCelestia"));

		
		Battle battle = new Battle(p1,p2);
		battle.start();
		
	}
}

