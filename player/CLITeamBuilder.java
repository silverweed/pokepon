//: player/CLITeamBuilder.java

package pokepon.player;

import pokepon.pony.*;
import pokepon.ability.*;
import pokepon.move.*;
import pokepon.item.*;
import pokepon.util.*;
import static pokepon.util.MessageManager.*;
import static pokepon.util.Meta.*;
import java.util.*;
import java.io.*;

/** TeamBuilder which implements buildTeam via the Command Line Interface.
 *
 * @author Giacomo Parolini
 */
public class CLITeamBuilder extends TeamBuilder {

	public CLITeamBuilder() {
		super();
		scan = new Scanner(System.in);
		initializeCommands();
	}
	
	public void buildTeam() {
		
		consoleMsg("@----------------------------------------------------------------@");
		consoleMsg("| Pokepon Command Line Interface Team Builder 1.0  by silverweed |");
		consoleMsg("@----------------------------------------------------------------@");
		consoleMsg("\n*** Welcome to the Team Builder! ***\n");
	
		printCommands(cmd);
		int c = 42;
		
		do {
			parser.clear();
			switch((c = promptPlayer(parser,cmd))) {
				case 0:	//list
					listPonies();	
					consoleMsg("");
					break;
				case 1:	//select
					if(team.members() == Team.MAX_TEAM_SIZE) {
						consoleMsg("Cannot add more ponies to your team.");
						break;
					}
					Pony pony = null;
					if(parser.getPonyName() == null) {
						consoleMsg("--pony not found: couldn't find valid pony within arguments given.");
						break;
					} else {
						if(Debug.on) printDebug("Selected seemingly valid pony: "+parser.getPonyName());
						try {
							pony = PonyCreator.create(parser.getPonyName());
							
						} catch(java.lang.reflect.InvocationTargetException e) {
							printDebug("Error: InvocationTargetException (wrapping "+e.getTargetException()+")");
							e.printStackTrace();
						} catch(Exception e) {
							printDebug("Error: couldn't find specified class in "+POKEPON_ROOTDIR+"."+PONY_DIR+": "+e);
						}
						
						if(pony != null) {
							team.add(pony);
							consoleMsg("Added "+pony+" to your team. Current team:\n"+team.getAllPonies());
							editPony(pony);
						}
					}
					break;
				case 2:	{ //edit:
					boolean found = false;
					String input = null;
					if((input = parser.getPonyName()) == null) {
						consoleMsg(team.toString());
						input = Saner.sane(getInput("Select a pony > "),Meta.complete(PONY_DIR),Pony.class);						
					}
					for(Pony p : team.getAllPonies()) {
						if(p.getClass().getSimpleName().equals(input)) {
							found = true;
							parser.popFirstArg();
							editPony(p);
							break;
						}
					}
					if(!found) consoleMsg("--pony not found: couldn't find valid pony within arguments given.");
					break;
				}
				case 3:	//team
					consoleMsg(team+"");
					break;
				case 4:	{ //save
					String input = null;
					if((input = ConcatenateArrays.merge(parser.getArgs().toArray(new String[0]))) == null) {
						input = getInput("Give filename > ");
					}
					if(team.getName().equals("Untitled Team")) 
						team.setName(input.endsWith(teamDealer.SAVE_EXT)
								? input.replaceAll("\\."+teamDealer.SAVE_EXT,"") 
								: input);
					String savePath = (input.startsWith("/") ? input : Meta.getSaveURL().getPath()
						+ Meta.DIRSEP + input) + (input.endsWith(teamDealer.SAVE_EXT) ? "" :
						teamDealer.SAVE_EXT);
					if(teamDealer.save(team, savePath))
						consoleMsg("Team successfully saved.");
					else
						consoleMsg("Error: could not save team.");
					break;
				}
				case 5:	{ //load
					String input = null;
					if((input = ConcatenateArrays.merge(parser.getArgs().toArray(new String[0]))) == null) {
						List<File> saveFiles = teamDealer.listSaveFiles();
						if(saveFiles.isEmpty()) {
							consoleMsg("No save files in "+Meta.getSaveURL().getPath());
							break;
						} 
						Collections.sort(saveFiles);
						int i = 1;
						for(File f : saveFiles) {
							consoleMsg(i++ + "- " + hideExtension(f.toString()));
						}
						// accept both the number of file in list or a filename.
						// Note that a file named as a number won't be loaded this way,
						// but must be loaded with `CLITB > load 1` or with an explicit
						// `1.pkp`.
						do {
							input = getInput("Select a file > ");
							try {
								int j = Integer.parseInt(input);
								if(j >= i || j < 1) {
									consoleMsg("Invalid number. Insert a number between 1 and " + (i - 1) +
										" or the name of a save file.");
								} else {
									input = saveFiles.get(j - 1).getPath();
									break;
								}
							} catch(IllegalArgumentException ee) {
								break;
							}
						} while(true);
					}
					String loadPath = (input.startsWith("/") ? input : Meta.getSaveURL().getPath()
						+ Meta.DIRSEP + input) + (input.endsWith(teamDealer.SAVE_EXT) ? "" :
						teamDealer.SAVE_EXT);
					if(teamDealer.load(team,loadPath))
						consoleMsg("\n"+team+"\n");
					else
						consoleMsg("Error: could not load team.");
					break;
				}
				case 6:	//?
					printCommands(cmd);
					break;
				case 7: //exit
					return;
				case 8: //quit
					return;
				case 9:	//args (debugging option)
					if(Debug.on) consoleDebug(parser.toString());
					else consoleMsg("Invalid option. ('?' for help)");
					break;
				default:
					consoleMsg("Invalid option. ('?' for help)");
					break;
			}
		} while(c != 7 && c != 8); // exit command
	}
	
	private void initializeCommands() {
		cmd.put("list","list available ponies.");
		cmd.put("select","select <pony> for your team.");
		cmd.put("edit","edit <pony> in your team (must have that pony already in team).");
		cmd.put("team","list ponies in your team.");
		cmd.put("save","save your team to file <file>");
		cmd.put("load","load a team from file <file>");
		cmd.put("?","get this help");
		cmd.put("exit","");
		cmd.put("quit","");
	}

	/** Lists all classes derived from Pony in PONY_DIR */
	private void listPonies() {
		List<String> lc = ClassFinder.findSubclassesNames(Meta.complete(PONY_DIR),Pony.class);
		consoleMsg("Found "+lc.size()+" ponies:");
		int i = 0;
		consoleFixedTable(lc, 5);
	}		

	private void editPony(Pony pony) {
		if(!team.getAllPonies().contains(pony)) {
			printDebug("Error: pony "+pony+" not found in team.");
			return;
		}

		consoleMsg("\n------------------------------------------");
		consoleMsg("Now editing pony >> "+pony.getFullName()+" <<");
		consoleMsg("------------------------------------------");
		pony.printInfo(true);
		consoleMsg("\n------------------------------------------\nCommands:");
		consoleFormat("  %-10s < string >\n","name");
		consoleFormat("  %-10s < 0 - %d >\n","level",Pony.MAX_LEVEL);
		consoleFormat("  %-10s < 1 - %d >\n","move",Pony.MOVES_PER_PONY);
		consoleFormat("  %-10s\n","nature");
		consoleFormat("  %-10s\n","ability");
		consoleFormat("  %-10s <hp|atk|def|spatk|spdef|speed> < 0 - %d >\n","iv",Pony.MAX_IV);
		consoleFormat("  %-10s <hp|atk|def|spatk|spdef|speed> < 0 - %d >\n","ev",Pony.MAX_EV);
		consoleFormat("  %-10s < 0 - %d >\n","happiness",Pony.MAX_HAPPINESS);
		consoleFormat("  %-10s\n","info");
		consoleFormat("  %-10s - get this help\n","?");
		consoleFormat("  %-10s\n------------------------------------------\n","done");
		
		do {
			editParser.clear();
			switch(promptPlayer(editParser,commands,"[editing "+pony.getName()+"]")) {
				case 0:	{ //name
					StringBuilder sb = new StringBuilder("");
					for(String s : editParser.getArgs()) {
						sb.append(s);
					}
					pony.setNickname(sb.toString());	//note that also accepts whitespaces 
					consoleMsg(pony.getName()+" is now nicknamed "+pony.getNickname()+".");
					break;
				}
				case 1:	//level
					try {
						pony.setLevel(Integer.parseInt(editParser.popFirstArg()));
					} catch(Exception e) {
						printDebug("Caught exception: "+e);
					}
					consoleMsg(pony.getNickname() + " is now at level "+pony.getLevel()+".");
					break;
				case 2:	{ //move
					int num = -1;
					try {
						num = Integer.parseInt(editParser.popFirstArg()) - 1;
					} catch(Exception e) {
						printDebug("Caught exception: "+e);
					}
					if(num == -1) {
						consoleMsg("Invalid argument for command 'move'");
						return;
					}
					consoleMsg("Select move among these:");
					consoleTable(pony.getLearnableMoves().keySet(),5);
					
					String input = getInput("\n"+"CLITB[editing "+pony.getName()+"#move"+Integer.toString(num+1)+"] > ");
					
					if(input == null) break;

					String selectedMove = Saner.sane(input,Meta.complete(MOVE_DIR),Move.class);

					if(selectedMove == null) {
						printMsg("Move not selected.");
						break;
					}

					for(String s : pony.getLearnableMoves().keySet()) {
						if(Debug.on) printDebug("Confronting selected move "+selectedMove+" with "+s);
						if(selectedMove.equals(s.replaceAll("\\ ",""))) {
							try {
								Move newmove = MoveCreator.create(selectedMove,pony);
								pony.setMove(num,newmove);
								consoleMsg("Set move "+(num+1)+" to: "+s);
								newmove.printInfo();
								break;
							} catch(Exception e) {
								printDebug("Caught exception while selecting move: "+e);
								return;
							}
						}
					}
					break;
				}
				case 3:	{ //nature
					consoleMsg("Select nature among these:");
					int j = 0;
					for(Pony.Nature n : Pony.Nature.values()) {
						consoleFormat("%-13s %-27s ",n,Pony.printNatureInfo(n));
						if(++j % 3 == 0) consoleMsg("");
					}
				
					String in = getInput("\n"+"CLITB[editing "+pony.getName()+"#nature] > ");
					
					if(in == null) break;

					String selectedNature = Saner.sane(in,Pony.Nature.nameSet(),true);

					if(selectedNature == null) {
						consoleMsg("Nature not selected.");
						break;
					}
					
					for(Pony.Nature n : Pony.Nature.values()) {
						if(Debug.pedantic) printDebug("Confronting selected nature "+selectedNature+" with "+n);
						if(selectedNature.equalsIgnoreCase(n.toString())) {
							pony.setNature(n);
							consoleMsg("Set "+pony.getNickname()+"'s nature to "+n);
							break;
						}
					}
					break;
				}
				case 4:	{ //ability
					consoleMsg("Select ability among these:");
					int j= 0;
					// use this instead of consoleTable because we only expect at most 3 entries
					for(String ab : pony.getPossibleAbilities()) {
						consoleFormat("%-25s ",ab);
					}
				
					String in = getInput("\n"+"CLITB[editing "+pony.getName()+"#ability] > ");
					
					if(in == null) break;

					String selectedAbility = Saner.sane(in,pony.getPossibleAbilities(),true);

					if(selectedAbility == null) {
						consoleMsg("Ability not selected.");
						break;
					}
					
					try {
						Ability ability = AbilityCreator.create(selectedAbility);
						if(ability == null) break;
						pony.setAbility(ability);
						if(Debug.on) printDebug("Set pony's ability to "+ability);
					} catch(ReflectiveOperationException e) {
						printDebug("Error creating ability "+selectedAbility+": "+e);
					}
					break;
				}
				case 5: { // item
					consoleMsg("Select item among these:");
					int j= 0;
					consoleTable(allItems, 5);
				
					String in = getInput("\n"+"CLITB[editing "+pony.getName()+"#item] > ");
					
					if(in == null) break;

					String selectedItem = Saner.sane(in,allItems,true);

					if(selectedItem == null) {
						consoleMsg("Item not selected.");
						break;
					}
					
					try {
						Item item = ItemCreator.create(selectedItem);
						if(item == null) break;
						pony.setItem(item);
						if(Debug.on) printDebug("Set pony's item to "+item);
					} catch(ReflectiveOperationException e) {
						printDebug("Error creating item "+selectedItem+": "+e);
					}
					break;
				}
				case 6:	{ //IV
					String iv = editParser.popFirstArg();
					if(!Arrays.asList(Pony.statNames()).contains(iv)) {
						consoleMsg("Invalid argument: "+iv);
						return;
					}
					int num2 = 0;
					try {
						num2 = Integer.parseInt(editParser.popFirstArg());
					} catch(Exception e) {
						printDebug("Caught exception while parsing IV args: "+e);
					}
					pony.setIV(iv,num2);
					consoleMsg(pony.getNickname()+"'s "+iv+" IV are now "+pony.getIV(iv));
					pony.printStats();
					break;
				}
				case 7: { //EV
					String ev = editParser.popFirstArg();
					if(!Arrays.asList(Pony.statNames()).contains(ev)) {
						consoleMsg("Invalid argument: "+ev);
						return;
					}
					int num3 = 0;
					try {
						num3 = Integer.parseInt(editParser.popFirstArg());
					} catch(Exception e) {
						printDebug("Caught exception while parsing EV args: "+e);
					}
					printDebug("ev: "+ev+", num: "+num3);
					pony.setEV(ev,num3);
					consoleMsg(pony.getNickname()+"'s "+ev+" EV are now "+pony.getEV(ev));
					pony.resetHp();
					pony.printStats();
					break;	
				}
				case 8:	//happiness
					try {
						pony.setHappiness(Integer.parseInt(editParser.popFirstArg()));
					} catch(Exception e) {
						printDebug("Caught exception: "+e);
					}
					consoleMsg(pony.getNickname() + "'s happiness is now "+pony.getHappiness()+".");
					break;		
				case 9:	//info
					pony.printInfo(true);
					break;
				case 10: //help
					consoleMsg("\n------------------------------------------\nCommands:");
					consoleFormat("  %-10s < string >\n","name");
					consoleFormat("  %-10s < 0 - %d >\n","level",Pony.MAX_LEVEL);
					consoleFormat("  %-10s < 1 - %d >\n","move",Pony.MOVES_PER_PONY);
					consoleFormat("  %-10s\n","nature");
					consoleFormat("  %-10s\n","ability");
					consoleFormat("  %-10s <hp|atk|def|spatk|spdef|speed> < 0 - %d >\n","iv",Pony.MAX_IV);
					consoleFormat("  %-10s <hp|atk|def|spatk|spdef|speed> < 0 - %d >\n","ev",Pony.MAX_EV);
					consoleFormat("  %-10s < 0 - %d >\n","happiness",Pony.MAX_HAPPINESS);
					consoleFormat("  %-10s\n","info");
					consoleFormat("  %-10s - get this help\n","?");
					consoleFormat("  %-10s\n------------------------------------------\n","done");
					break;

				case 11: //done
					return;
			}
		} while(true);
	}

	/** Prints a given Map in a certain fashion. */
	private void printCommands(Map<String,String> cmds) {
		for(Map.Entry<String,String> entry : cmds.entrySet()) {
			System.out.format("%-10s : %s\n",entry.getKey(),entry.getValue());
		}
	}
	
	/** Get a single input from console (with no restriction) */
	private String getInput(String prompt) {
		consoleMsgnb(prompt);
		String in = "";
		try {
			in = scan.nextLine();
		} catch(NoSuchElementException e) {
			consoleMsg("quitting...");
			System.exit(-1);
		} catch(Exception e) {
			printDebug("Caught exception: "+e);
		}
		return in;
	}

	/** get command from player and return int corresponding to that command. */
	private <T> int promptPlayer(Parser prs,Map<String,T> cmds,String... str) {

		readInput(prs,str);
		
		int i = 0;	
		for(Map.Entry<String,T> entry : cmds.entrySet()) {
			if(prs.getCommand().equals(entry.getKey())) return i;
			++i;
		}
		return -1;		
	}
	
	/** Continue to prompt player until an acceptable command is passed or EOF is received. */
	private void readInput(Parser p,String... str) {
		String postprompt = "";
		for(String s : str) postprompt = postprompt + s;
		do {
			consoleMsgnb("CLITB"+postprompt+" > ");
			try {
				p.parse(scan.nextLine());
			} catch(NoSuchElementException e) {	//EOF received (probably)
				consoleDebug("quitting...");
				System.exit(-1);
			} catch(Exception e) {
				consoleDebug("Caught exception while parsing: "+e);
				e.printStackTrace();
			}
			if(!p.ok()) consoleMsg(p.getParsingError()+" ('?' for help)");
		} while(!p.ok());
	}	

	/** Inner class used to parse the first input; Basically a Parser with an useful "getPonyName" method. */
	private class CLITBParser extends SanedParser {
		
		public CLITBParser() {
			type.put("list",0);
			type.put("select",1);
			type.put("edit",0);
			type.put("?",0);
			type.put("team",0);
			type.put("save",0);
			type.put("load",0);
			type.put("exit",0);
			type.put("quit",0);
			if(Debug.on) type.put("args",0);
		}
		
		public String getPonyName() {
			StringBuilder sb2 = new StringBuilder("");
			for(String s : args) sb2.append(s);
			return Saner.sane(sb2.toString(),Meta.complete(PONY_DIR),Pony.class);
		}

		/** Capitalizes first character in a string */
		private String capitalize(String s) {
			if(s == null) return null;
			char first = Character.toUpperCase(s.charAt(0));
			return first+s.substring(1); 
		}
	}
		
	private static Map<String,Integer> commands = new LinkedHashMap<String,Integer>();
	static {
		commands.put("name",1);
		commands.put("level",1);
		commands.put("move",1);
		commands.put("nature",0);
		commands.put("ability",0);
		commands.put("item",0);
		commands.put("iv",2);
		commands.put("ev",2);
		commands.put("happiness",1);
		commands.put("info",0);
		commands.put("?",0);
		commands.put("done",-1);
	}
	private Scanner scan;
	private Map<String,String> cmd = new LinkedHashMap<String,String>();
	private CLITBParser parser = new CLITBParser();
	private TeamDealer teamDealer = new TeamDealer();
	private Parser editParser = new SanedParser(commands);
	private List<String> allItems = ClassFinder.findSubclassesNames(Meta.complete(ITEM_DIR),Item.class);
}
