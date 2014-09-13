//: player/GUITeamBuilder.java

package pokepon.player;

//import org.imgscalr.Scalr;
import pokepon.pony.*;
import pokepon.move.*;
import pokepon.enums.*;
import pokepon.util.*;
import pokepon.gui.*;
import pokepon.net.jack.client.*;
import static pokepon.util.Meta.*;
import static pokepon.util.MessageManager.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.imageio.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.List;
import java.net.*;
import java.io.*;

/** This class implements a graphical TeamBuilder.
 *
 * @author Giacomo Parolini
 */
public class GUITeamBuilder extends TeamBuilder {

	private JFrame frame = new JFrame();
	private TeamBuilderPanel tbPanel = new TeamBuilderPanel();
	private JPanel buttonPanel = new JPanel(new GridLayout(1,5));
	private JTextField teamName = new RoundJTextField(30);
	private JButton exitButton = new JButton("Close");
	private JButton savePonyButton = new JButton("Save pony to team");
	private JButton deletePonyButton = new JButton ("Delete pony");
	private JButton saveTeamButton = new JButton("Save team");
	private JButton loadTeamButton = new JButton("Load team");
	private JFileChooser fileChooser; 
	private TeamDealer teamDealer = new TeamDealer();
	private PokeponClient pClient;

	public GUITeamBuilder() {
		super();
		fileChooser = new JFileChooser(Meta.getSaveURL().getPath()) {
						/*Meta.LAUNCHED_FROM_JAR 
						? Meta.getCwd().getPath()
						: Meta.getSaveURL().getPath()) { */
			@Override
			public void approveSelection() {
				File f = getSelectedFile();
				if(Debug.on) printDebug("approveSelection: file = "+f);
				if(f.exists() && getDialogType() == SAVE_DIALOG) {
					int result = JOptionPane.showConfirmDialog(this,
										"The file exists, overwrite?",
										"Existing file",
										JOptionPane.YES_NO_CANCEL_OPTION);
					switch(result) {
						case JOptionPane.YES_OPTION:
							super.approveSelection();
							return;
						case JOptionPane.NO_OPTION:
							return;
						case JOptionPane.CLOSED_OPTION:
							return;
						case JOptionPane.CANCEL_OPTION:
							cancelSelection();
							return;
					}
				}
				super.approveSelection();
			}        
		};	
	
		exitButton.addActionListener(exitListener);
		buttonPanel.add(exitButton);
		savePonyButton.addActionListener(savePonyListener);
		buttonPanel.add(savePonyButton);
		deletePonyButton.addActionListener(deletePonyListener);
		buttonPanel.add(deletePonyButton);
		saveTeamButton.addActionListener(saveTeamListener);
		buttonPanel.add(saveTeamButton);
		loadTeamButton.addActionListener(loadTeamListener);
		buttonPanel.add(loadTeamButton);

		frame.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.anchor = GridBagConstraints.WEST;
		JLabel lab = new JLabel("Team Name:");
		c.gridx = 0;
		c.ipadx = 10;
		frame.add(lab,c);

		c.gridx = 1;
		c.anchor = GridBagConstraints.WEST;
		teamName.setText("Untitled Team");
		teamName.setCaretPosition(teamName.getText().length());
		teamName.getDocument().addDocumentListener(teamNameListener);
		teamName.setMinimumSize(teamName.getPreferredSize());
		frame.add(teamName,c);

		c.gridx = 0;
		c.gridy = 1;
		c.gridheight = 4;
		c.gridwidth = 2;
		c.ipadx = 0;
		c.anchor = GridBagConstraints.CENTER;
		frame.add(tbPanel,c);

		c.gridy = 5;
		c.gridheight = 1;
		c.insets = new Insets(15,0,25,0);
		frame.add(buttonPanel,c);
	}

	public GUITeamBuilder(final PokeponClient pClient) {
		this();
		this.pClient = pClient;
	}

	public void buildTeam() {
		SwingConsole.run(frame,"Pokepon Teambuilder");
	}

	public static void main(String[] args) {
		SwingConsole.run(new GUITeamBuilder().frame,"Pokepon Teambuilder");
	}

	private ActionListener exitListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			consoleDebug("Disposing GUITeamBuilder.");
			frame.setVisible(false);
			frame.dispose();
		}
	};

	private ActionListener savePonyListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if(tbPanel.getSelectedTeamIndex() < 0 || tbPanel.getSelectedTeamIndex() > 5) {
				printDebug("[GUITB] Error: selected team index is "+tbPanel.getSelectedTeamIndex());
				return;
			}
			tbPanel.getPonyPanel().updatePhrase(tbPanel.getSelectedPony());
			// Set n-th pony to selectedpony
			team.setPony(tbPanel.getSelectedTeamIndex(),tbPanel.getSelectedPony());
			tbPanel.getTeamPanel().setPony(tbPanel.getSelectedTeamIndex(),tbPanel.getSelectedPony());
			// Increment selectedTeamIndex 
			if(tbPanel.getSelectedTeamIndex() < 5) 
				tbPanel.setSelectedTeamIndex(tbPanel.getSelectedTeamIndex()+1);

			frame.validate();
			if(Debug.on) printDebug("Added "+tbPanel.getSelectedPony()+" to team.");
			printMsg("Current team: "+team);
		}
	};

	private ActionListener deletePonyListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if(Debug.on) printDebug("Removed "+tbPanel.getTeamPanel().getPony(tbPanel.getSelectedTeamIndex()));
			team.remove(tbPanel.getSelectedTeamIndex());
			tbPanel.getTeamPanel().setPony(tbPanel.getSelectedTeamIndex(),null);
			tbPanel.setPony(null);
			printMsg("Current team: "+team);
		}
	};

	private ActionListener saveTeamListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			TeamDealer.ensureSaveDirExists();
			fileChooser.setSelectedFile(new File(Meta.getSaveURL().getPath()+Meta.DIRSEP+team.getName()+TeamDealer.SAVE_EXT));
			switch(fileChooser.showSaveDialog(frame)) {
				case JFileChooser.CANCEL_OPTION:
					printDebug("Team was not saved.");
					return;
				case JFileChooser.APPROVE_OPTION:
					printDebug("Selected file: "+fileChooser.getSelectedFile().getName());
					break;
				case JFileChooser.ERROR_OPTION:
					printDebug("An error occurred while saving team.");
					return;
			}
		
			String saveFile = fileChooser.getSelectedFile().getPath();
			if(!saveFile.endsWith(TeamDealer.SAVE_EXT)) {
				saveFile += TeamDealer.SAVE_EXT;
			}
			try(PrintWriter writer = new PrintWriter(new File(saveFile))) {
				if(!team.getName().equals("Untitled Team"))
					writer.write("$TEAM_NAME = "+team.getName()+"\n");
				for(String data : team.getTeamData())
					writer.write(data+"\n");
				printMsg("Team saved to " + saveFile); 
				// if TeamBuilder is bound to a PokeponClient, add the newly-created team to 
				// client's team list.
				if(pClient != null) {
					if(pClient.getTeams().size() >= PokeponClient.MAX_LOADABLE_TEAMS)
						pClient.getTeams().remove(0);

					pClient.getTeams().add(team);
				}

			} catch(IOException ee) {
				printDebug("Exception while saving team: "+ee);
			}
		}
	};

	private ActionListener loadTeamListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			TeamDealer.ensureSaveDirExists();
			switch(fileChooser.showOpenDialog(frame)) {
				case JFileChooser.CANCEL_OPTION:
					printDebug("Team was not loaded.");
					return;
				case JFileChooser.APPROVE_OPTION:
					printDebug("Selected file: "+fileChooser.getSelectedFile().getName());
					break;
				case JFileChooser.ERROR_OPTION:
					printDebug("An error occurred while loading team.");
					return;
			}
		
			team.clear();
			tbPanel.clear();

			if(teamDealer.load(team,fileChooser.getSelectedFile().getPath())) {
				// set team name
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						teamName.setText(team.getName());
					}
				});
				// update teamPanel
				for(int i = 0; i < team.members(); ++i) {
					tbPanel.getTeamPanel().setPony(i,team.getPony(i));
				}
				tbPanel.setPony(team.getPony(0));
				frame.repaint();
				printMsg("Team successfully loaded.\n"+team);
			}
		}
	};

	private DocumentListener teamNameListener = new DocumentListener() {
		public void changedUpdate(DocumentEvent e) {
			updateTeamName();
		}
		public void insertUpdate(DocumentEvent e) {
			updateTeamName();
		}
		public void removeUpdate(DocumentEvent e) {
			updateTeamName();
		}
		private void updateTeamName() {
			String name = teamName.getText(); 

			if(name == null || name.length() == 0) {
				return;
			}

			team.setName(name);
			if(Debug.on) printDebug("Setting team name to "+team.getName());
		}
	};


}

