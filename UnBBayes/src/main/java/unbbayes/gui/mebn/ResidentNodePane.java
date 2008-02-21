/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.gui.mebn;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import unbbayes.controller.MEBNController;
import unbbayes.gui.mebn.auxiliary.ToolKitForGuiMebn;
import unbbayes.prs.mebn.DomainResidentNode;

/**
 * Pane for edition and view of the properties of a Resident Node
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 *
 */

public class ResidentNodePane extends JPanel{

	private PossibleValuesEditionPane possibleValuesEditPane; 
	private TablePreviewPane tableViewPane; 
	private JSplitPane splitStateTable; 
	
	public ResidentNodePane(){
		
	}
	
	public ResidentNodePane(MEBNController _controller, DomainResidentNode _residentNode){
		super(); 
		
		this.setBorder(ToolKitForGuiMebn.getBorderForTabPanel("Resident Node")); 
		
		possibleValuesEditPane = new PossibleValuesEditionPane(_controller, _residentNode); 
		tableViewPane = new TablePreviewPane(_controller, _residentNode); 
		
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
	    
	    /* Painel temporariamente retirado */
//		constraints.gridx = 0; 
//	    constraints.gridy = 1; 
//	    constraints.gridwidth = 1; 
//	    constraints.gridheight = 1; 
//	    constraints.weightx = 0; 
//	    constraints.weighty = 50; 
//	    constraints.fill = GridBagConstraints.BOTH; 
//	    constraints.anchor = GridBagConstraints.NORTH; 
//	    gridbag.setConstraints(tableViewPane, constraints); 
//	    this.add(tableViewPane);
		
		
	    
	}
	
}
