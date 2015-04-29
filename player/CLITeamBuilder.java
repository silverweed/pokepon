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
 * @author silverweed
 */
public class CLITeamBuilder extends TeamBuilder {

	public CLITeamBuilder() {
		super();
		scan = new Scanner(System.in);
	}
	
	public void buildTeam() {
		
		consoleMsg("@----------------------------------------------------------------@");
		consoleMsg("| Pokepon Command Line Interface Team Builder 1.0  by silverweed |");
		consoleMsg("@----------------------------------------------------------------@");
		consoleMsg("\n*** Welcome to the Team Builder! ***\n");
	
		printCommands(parser);
		do {
			parser.clear();
			switch((MainCommand)promptPlayer(parser)) {
				case LIST:
					listPonies();	
					consoleMsg("");
					break;
				case SELECT:
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
							consoleMsg("Added "+pony+" to your team. Current team:\n"+team);
							editPony(pony);
						}
					}
					break;
				case EDIT: {
					boolean found = false;
					String input = null;
					if((input = parser.getPonyName()) == null) {
						consoleMsg(team.toString());
						input = Saner.sane(getInput("Select a pony > "),Meta.complete(PONY_DIR),Pony.class);						
					}
					for(Pony p : team) {
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
				case TEAM:
					consoleMsg(team+"");
					break;
				case SAVE: {
					String input = null;
					if((input = ConcatenateArrays.merge(parser.getArgs().toArray(new String[0]))) == null) {
						input = getInput("Give filename > ");
					}
					if(team.getName().equals("Untitled Team")) 
						team.setName(input.endsWith(TeamDealer.SAVE_EXT)
								? input.replaceAll("\\."+TeamDealer.SAVE_EXT,"") 
								: input);
					String savePath = (input.startsWith("/") ? input : Meta.getSaveURL().getPath()
						+ Meta.DIRSEP + input) + (input.endsWith(TeamDealer.SAVE_EXT) 
							? "" 
							: TeamDealer.SAVE_EXT);
					if(teamDealer.save(team, savePath))
						consoleMsg("Team successfully saved.");
					else
						consoleMsg("Error: could not save team.");
					break;
				}
				case LOAD: {
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
						+ Meta.DIRSEP + input) + (input.endsWith(TeamDealer.SAVE_EXT) 
							? ""
							: TeamDealer.SAVE_EXT);
					if(teamDealer.load(team,loadPath))
						consoleMsg("\n"+team+"\n");
					else
						consoleMsg("Error: could not load team.");
					break;
				}
				case HELP:
					printCommands(parser);
					break;
				case EXIT:
				case QUIT:
					return;
				default:
					consoleMsg("Invalid option. ('?' for help)");
					break;
			}
		} while(true);
	}
	
	/** Lists all classes derived from Pony in PONY_DIR */
	private void listPonies() {
		List<String> lc = ClassFinder.findSubclassesNames(Meta.complete(PONY_DIR),Pony.class);
		consoleMsg("Found "+lc.size()+" ponies:");
		int i = 0;
		consoleFixedTable(lc, 5);
	}		

	private void editPony(Pony pony) {
		if(!team.contains(pony)) {
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
			switch((PonyCommand)promptPlayer(editParser, "[editing "+pony.getName()+"]")) {
				case NAME: {
					StringBuilder sb = new StringBuilder("");
					for(String s : editParser.getArgs()) {
						sb.append(s);
					}
					pony.setNickname(sb.toString());	//note that also accepts whitespaces 
					consoleMsg(pony.getName()+" is now nicknamed "+pony.getNickname()+".");
					break;
				}
				case LEVEL:
					try {
						pony.setLevel(Integer.parseInt(editParser.popFirstArg()));
					} catch(Exception e) {
						printDebug("Caught exception: "+e);
					}
					consoleMsg(pony.getNickname() + " is now at level "+pony.getLevel()+".");
					break;
				case MOVE: {
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
				case NATURE: {
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
				case ABILITY: {
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
				case ITEM: {
					consoleMsg("Select item among these:");
					int j = 0;
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
				case IV: {
					String iv = editParser.popFirstArg();
					Pony.Stat stat = Pony.Stat.forName(iv);
					if(stat == null) {
						consoleMsg("Invalid argument: "+iv);
						return;
					}
					int num = 0;
					try {
						num = Integer.parseInt(editParser.popFirstArg());
					} catch(Exception e) {
						printDebug("Caught exception while parsing IV args: "+e);
					}
					pony.setIV(stat, num);
					consoleMsg(pony.getNickname()+"'s "+iv+" IV are now "+pony.getIV(stat));
					pony.printStats();
					break;
				}
				case EV: {
					String ev = editParser.popFirstArg();
					Pony.Stat stat = Pony.Stat.forName(ev);
					if(stat == null) {
						consoleMsg("Invalid argument: "+ev);
						return;
					}
					int num = 0;
					try {
						num = Integer.parseInt(editParser.popFirstArg());
					} catch(Exception e) {
						printDebug("Caught exception while parsing EV args: "+e);
					}
					pony.setEV(stat, num);
					consoleMsg(pony.getNickname()+"'s "+ev+" EV are now "+pony.getEV(stat));
					pony.resetHp();
					pony.printStats();
					break;	
				}
				case HAPPINESS:
					try {
						pony.setHappiness(Integer.parseInt(editParser.popFirstArg()));
					} catch(Exception e) {
						printDebug("Caught exception: "+e);
					}
					consoleMsg(pony.getNickname() + "'s happiness is now "+pony.getHappiness()+".");
					break;		
				case INFO:
					pony.printInfo(true);
					break;
				case HELP:
					consoleMsg("\n------------------------------------------\nCommands:");
					for(Parser.Command cmd : PonyCommand.values())
						consoleMsg(cmd.getDescription());
					break;

				case DONE:
					return;
			}
		} while(true);
	}

	/** Prints a given Map in a certain fashion. */
	private void printCommands(Parser parser) {
		for(Parser.Command cmd : parser.getCommands()) {
			System.out.format("%-10s : %s\n", cmd, cmd.getDescription()); 
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

	/** Get command from player and return int corresponding to that command. */
	private Parser.Command promptPlayer(Parser parser, String... str) {

		readInput(parser, str);

		return parser.getCommand();
	}
	
	/** Continue to prompt player until an acceptable command is passed or EOF is received. */
	private void readInput(Parser p, String... str) {
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
			super(mainCommands);
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
		
	private Scanner scan;
	private Set<? extends Parser.Command> mainCommands = EnumSet.allOf(MainCommand.class);
	private Set<? extends Parser.Command> ponyCommands = EnumSet.allOf(PonyCommand.class);
	private CLITBParser parser = new CLITBParser();
	private TeamDealer teamDealer = new TeamDealer();
	private Parser editParser = new SanedParser(ponyCommands);
	private List<String> allItems = ClassFinder.findSubclassesNames(Meta.complete(ITEM_DIR),Item.class);

	private static enum MainCommand implements Parser.Command {
		LIST, SELECT, EDIT, TEAM, SAVE, LOAD, HELP, EXIT, QUIT;

		public String getDescription() {
			switch(this) {
				case LIST: return "list available ponies.";
				case SELECT: return "select <pony> for your team.";
				case EDIT: return "edit <pony> in your team (must have that pony already in team).";
				case TEAM: return "list ponies in your team.";
				case SAVE: return "save your team to file <file>";
				case LOAD: return "load a team from file <file>";
				case HELP: return "get this help";
				case EXIT: return "quit the program";
				case QUIT: return "quit the program";
			}
			return null;
		}

		@Override
		public String toString() {
			if(this == HELP) return "?";
			return super.toString().toLowerCase();
		}

		public int getNArgs() {
			switch(this) {
				case SELECT: return 1;
				default: return 0;
			}
		}
	}

	private static enum PonyCommand implements Parser.Command {
		NAME, LEVEL, MOVE, NATURE, ABILITY, ITEM, IV, EV,
		HAPPINESS, INFO, HELP, DONE;

		public String getDescription() {
			switch(this) {
				case NAME: return String.format("  %-10s < string >", "name");
				case LEVEL: return String.format("  %-10s < 0 - %d >", "level", Pony.MAX_LEVEL);
				case MOVE: return String.format("  %-10s < 1 - %d >", "move", Pony.MOVES_PER_PONY);
				case NATURE: return String.format("  %-10s", "nature");
				case ABILITY: return String.format("  %-10s", "ability");
				case IV: return String.format("  %-10s <hp|atk|def|spatk|spdef|speed> < 0 - %d >", "iv", Pony.MAX_IV);
				case EV: return String.format("  %-10s <hp|atk|def|spatk|spdef|speed> < 0 - %d >", "ev", Pony.MAX_EV);
				case HAPPINESS: return String.format("  %-10s < 0 - %d >", "happiness", Pony.MAX_HAPPINESS);
				case INFO: return String.format("  %-10s", "info");
				case HELP: return String.format("  %-10s - get this help", "?");
				case DONE: return String.format("  %-10s------------------------------------------", "done");
			}
			return "";
		}

		@Override
		public String toString() {
			if(this == HELP) return "?";
			return super.toString().toLowerCase();
		}

		public int getNArgs() {
			switch(this) {
				case NAME: return 1;
				case LEVEL: return 1;
				case MOVE: return 1;
				case NATURE: return 0;
				case ABILITY: return 0;
				case ITEM: return 0;
				case IV: return 2;
				case EV: return 2;
				case HAPPINESS: return 1;
				case INFO: return 0;
				case HELP: return 0;
				case DONE: return -1;
			}
			return -1;
		}
	}
}
