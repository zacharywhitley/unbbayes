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

import javax.swing.JComboBox;

import unbbayes.prs.bn.LearningNode;
import unbbayes.util.NodeList;
public class CompactChooseInterationController {
	
	private CompactChooseWindow frame;
	
	public CompactChooseInterationController(CompactChooseWindow frame){
		this.frame = frame;		
	}
	
	public void actionOk(JComboBox variablesCombo, NodeList variablesVector){
		frame.dispose();
		String name = (String)variablesCombo.getSelectedItem();
            LearningNode aux;
            for (int i = 0 ; i < variablesVector.size() ;i++ ){
                aux = (LearningNode)variablesVector.get(i);
                if (aux.getName().equals(name)){
                    System.out.println("Sigla = " + aux.getName());
                    aux.isRep(true);
                    break;
                }
            }            
	}
	
	public void actionCancel(){
	    frame.dispose();	
	}	

}
