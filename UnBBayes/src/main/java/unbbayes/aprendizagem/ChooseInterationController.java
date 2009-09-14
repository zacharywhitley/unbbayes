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
package unbbayes.aprendizagem;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import unbbayes.prs.bn.LearningNode;

public class ChooseInterationController {
	
	
	private ChooseVariablesWindow frame;	
	
	
	public ChooseInterationController(ChooseVariablesWindow frame){		
		this.frame = frame;		
	}
	
	/**
	 * This method a state on the variable, this state represents
	 * if the variable will participate of the network learning 
	 * process
	 **/
	public void setVariablesState(){
		frame.dispose();				
		JPanel choosePanel = frame.getChoosePanel();
		int length = choosePanel.getComponentCount();
		LearningNode variable;
		for(int i = 0 ;i < length; i++){
		    variable = frame.getVariable(i);
		    variable.setParticipa(((JCheckBox)choosePanel.getComponent(i)).isSelected());		    
		}				
	}
	
	public int setVariablesState(int inutil){
		JPanel choosePanel = frame.getChoosePanel();
		int length = choosePanel.getComponentCount();
		LearningNode variable;
		int resultado=-1;
		for(int i = 0 ;i < length; i++){
		    variable = frame.getVariable(i);
		    variable.setParticipa(!((JRadioButton)choosePanel.getComponent(i)).isSelected());
		    if(((JRadioButton)choosePanel.getComponent(i)).isSelected()){
		    	resultado=i;		    	
		    }
		}
		return resultado;
	}

}
