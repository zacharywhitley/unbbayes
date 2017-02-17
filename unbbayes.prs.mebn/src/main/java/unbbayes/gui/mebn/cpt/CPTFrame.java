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

import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.controller.mebn.MEBNController;
import unbbayes.gui.GUIUtils;
import unbbayes.prs.mebn.ResidentNode;

public class CPTFrame extends JFrame{

	private static final long serialVersionUID = 8161666753399577111L;
	
	private IMEBNMediator mediator; 
	private ResidentNode residentNode; 
	
	public CPTFrame(IMEBNMediator mebnController_, ResidentNode residentNode_){
		super(residentNode_.getName());
		setMebnController(mebnController_); 
		setResidentNode(residentNode_); 
    	this.initComponents();
    	this.initListeners();
	}
	
	/**
	 * Initializes the internal components of this frame.
	 * Subclasses may overwrite this method in order to customize the components in this frame.
	 * @see #initListeners()
	 */
	protected void initComponents() {
		CPTEditionPane cptEditionPane = new CPTEditionPane((MEBNController) getMebnController(), getResidentNode());
    	setContentPane(cptEditionPane);
    	setLocation(GUIUtils.getCenterPositionForComponent(750,300));
    	pack(); 
    	setVisible(true); 
    	
    	setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); 
    	
	}
	
	/**
	 * Initializes the action listeners related to this frame.
	 * Subclasses may overwrite this method in order to customize the behavior of this frame and
	 * components initialized in {@link #initComponents()}
	 * @see #initComponents()
	 */
	protected void initListeners() {
    	addWindowListener(new WindowAdapter() {
    		    public void windowClosing(WindowEvent we) {
    		        getMebnController().closeCPTDialog(getResidentNode()); 
    		    }
    	});
	}

	/**
	 * @return the mebnController
	 */
	public IMEBNMediator getMebnController() {
		return  mediator;
	}

	/**
	 * @return the residentNode
	 */
	public ResidentNode getResidentNode() {
		return residentNode;
	}

	/**
	 * @param mediator the mebnController to set
	 */
	protected void setMebnController(IMEBNMediator mediator) {
		this.mediator = mediator;
	}

	/**
	 * @param residentNode the residentNode to set
	 */
	protected void setResidentNode(ResidentNode residentNode) {
		this.residentNode = residentNode;
	}
}
