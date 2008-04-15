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

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import unbbayes.controller.IconController;
import unbbayes.controller.MEBNController;
import unbbayes.gui.mebn.auxiliary.MebnToolkit;
import unbbayes.prs.mebn.ResidentNode;

/**
 * Pane for edition and view of the properties of a Resident Node
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 *
 */

public class ResidentNodePane extends JPanel{

	private PossibleValuesEditionPane possibleValuesEditPane; 
	private JSplitPane splitStateTable; 
	private MEBNController controller; 
	private IconController iconController = IconController.getInstance(); 

	/**
	 * Create a empty pane. 
	 */
	public ResidentNodePane(){

	}
	
	/**
	 * Create a resident node pane. 
	 * @param _controller
	 * @param _residentNode
	 */
	public ResidentNodePane(MEBNController _controller, ResidentNode _residentNode){
		super(); 
		
		this.setBorder(MebnToolkit.getBorderForTabPanel("Resident Node")); 
		controller = _controller; 
		
		possibleValuesEditPane = new PossibleValuesEditionPane(_controller, _residentNode); 
	    
		this.setLayout(new BorderLayout()); 
	    
		this.add(new ResidentPaneOptions(_controller), BorderLayout.NORTH); 
	    this.add(possibleValuesEditPane, BorderLayout.CENTER);
	    
	}
	

	
}
