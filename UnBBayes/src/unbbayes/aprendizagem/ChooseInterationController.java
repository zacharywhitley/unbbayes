/*
 *  UnbBayes
 *  Copyright (C) 2002 Universidade de Brasília
 *
 *  This file is part of UnbBayes.
 *
 *  UnbBayes is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  UnbBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnbBayes; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package unbbayes.aprendizagem;

import javax.swing.JPanel;
import javax.swing.JCheckBox;

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
		TVariavel variable;
		for(int i = 0 ;i < length; i++){
		    variable = frame.getVariable(i);
		    variable.setParticipa(((JCheckBox)choosePanel.getComponent(i)).isSelected());		    
		}				
	}

}
