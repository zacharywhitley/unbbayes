package unbbayes.gui.mebn;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.TitledBorder;

import unbbayes.controller.NetworkController;
import unbbayes.prs.mebn.DomainResidentNode;

/**
 * Pane for edition and view of the properties of a Resident Node
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 *
 */

public class ResidentNodePane extends JPanel{

	private PossibleValuesEditionPane possibleValuesEditPane; 
	private TableViewPane tableViewPane; 
	private JSplitPane splitStateTable; 
	
	public ResidentNodePane(){
		
	}
	
	public ResidentNodePane(NetworkController _controller, DomainResidentNode _residentNode){
		super(); 
		
		this.setBorder(ToolKitForGuiMebn.getBorderForTabPanel("Resident Node")); 
		
		possibleValuesEditPane = new PossibleValuesEditionPane(_controller, _residentNode); 
		tableViewPane = new TableViewPane(_controller, _residentNode); 
		
		GridBagLayout gridbag = new GridBagLayout(); 
		GridBagConstraints constraints = new GridBagConstraints(); 
		
		this.setLayout(gridbag); 
	    
		constraints.gridx = 0; 
	    constraints.gridy = 0; 
	    constraints.gridwidth = 1; 
	    constraints.gridheight = 1; 
	    constraints.weightx = 100;
	    constraints.weighty = 50; 
	    constraints.fill = GridBagConstraints.BOTH; 
	    constraints.anchor = GridBagConstraints.NORTH; 
	    gridbag.setConstraints(possibleValuesEditPane, constraints); 
	    this.add(possibleValuesEditPane);
	    
		constraints.gridx = 0; 
	    constraints.gridy = 1; 
	    constraints.gridwidth = 1; 
	    constraints.gridheight = 1; 
	    constraints.weightx = 0; 
	    constraints.weighty = 50; 
	    constraints.fill = GridBagConstraints.BOTH; 
	    constraints.anchor = GridBagConstraints.NORTH; 
	    gridbag.setConstraints(tableViewPane, constraints); 
	    this.add(tableViewPane);
		
		
	    
	}
	
}
