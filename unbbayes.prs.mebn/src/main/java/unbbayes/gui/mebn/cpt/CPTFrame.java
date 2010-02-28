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

package unbbayes.gui.mebn.cpt;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import unbbayes.controller.mebn.MEBNController;
import unbbayes.gui.GUIUtils;
import unbbayes.prs.mebn.ResidentNode;

public class CPTFrame extends JFrame{

	private final MEBNController mebnController; 
	private final ResidentNode residentNode; 
	
	public CPTFrame(MEBNController mebnController_, ResidentNode residentNode_){
		super(residentNode_.getName());
		mebnController = mebnController_; 
		residentNode = residentNode_; 
    	CPTEditionPane cptEditionPane = new CPTEditionPane(mebnController, residentNode);
    	setContentPane(cptEditionPane);
    	setLocation(GUIUtils.getCenterPositionForComponent(750,300));
    	pack(); 
    	setVisible(true); 
    	
    	setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); 
    	
    	addWindowListener(new WindowAdapter() {
    		    public void windowClosing(WindowEvent we) {
    		        mebnController.closeCPTDialog(residentNode); 
    		    }
    	});
	}
	
}
