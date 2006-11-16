package unbbayes.gui.mebn;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import unbbayes.controller.MEBNController;
import unbbayes.controller.NetworkController;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.ResidentNode;

/**
 * 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com) 
 * @version 0.1 (11/15/2006)
 * 
 */

public class EditArgumentsTab extends JPanel{
	
	OVariableTreeMFrag treeMFrag; 
	OVariableTreeResident treeResident; 
	JToolBar jtbInformation; 
	
	JScrollPane jspTreeMFrag; 
	JScrollPane jspTreeResident; 
	
	MEBNController mebnController; 
	MFrag mFrag; 
	ResidentNode residentNode; 
	
	public EditArgumentsTab(NetworkController _controller, ResidentNode resident){
		
		super(); 
		
		GridBagLayout gridbag = new GridBagLayout(); 
		GridBagConstraints constraints = new GridBagConstraints(); 
		
		setLayout(gridbag);
		
		mebnController = _controller.getMebnController(); 
	    mFrag = mebnController.getCurrentMFrag(); 
	    residentNode = resident;
	    
	    
	    treeMFrag = new OVariableTreeMFrag(_controller);
	    jspTreeMFrag = new JScrollPane(treeMFrag);
	    
	    treeResident = new OVariableTreeResident(_controller, resident); 	    
	    jspTreeResident = new JScrollPane(treeResident); 
	    
	    jtbInformation = new JToolBar(); 
	    
	    
	    constraints.gridx = 0; 
	    constraints.gridy = 0; 
	    constraints.gridwidth = 1; 
	    constraints.gridheight = 1; 
	    constraints.weightx = 100; 
	    constraints.weighty = 60; 
	    constraints.fill = GridBagConstraints.BOTH; 
	    constraints.anchor = GridBagConstraints.NORTH; 
	    gridbag.setConstraints(jspTreeMFrag, constraints); 
	    this.add(jspTreeMFrag);
	    
	    constraints.gridx = 0; 
	    constraints.gridy = 1; 
	    constraints.gridwidth = 1; 
	    constraints.gridheight = 1; 
	    constraints.weightx = 0; 
	    constraints.weighty = 30; 
	    constraints.fill = GridBagConstraints.BOTH; 
	    constraints.anchor = GridBagConstraints.NORTH; 	
	    gridbag.setConstraints(jspTreeResident, constraints); 	    
	    this.add(jspTreeResident);
	    
	    constraints.gridx = 0; 
	    constraints.gridy = 2; 
	    constraints.gridwidth = 1; 
	    constraints.gridheight = 1; 
	    constraints.weightx = 0; 
	    constraints.weighty = 10; 
	    constraints.fill = GridBagConstraints.BOTH; 
	    constraints.anchor = GridBagConstraints.NORTH; 
	    gridbag.setConstraints(jtbInformation, constraints); 	    
	    this.add(jtbInformation);	    
	}

	/**
	 *  Create a empty painel 
	 *  */
	
	public EditArgumentsTab(){
		
	}
		
	public void update(){
	  	treeMFrag.updateTree(); 
	  	treeResident.updateTree(); 
	}
	
}