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


import java.awt.event.MouseEvent;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JList;

import unbbayes.prs.bn.LearningNode;
import unbbayes.util.NodeList;

public class OrdenationInterationController {
	
	private NodeList variables;
	private OrdenationWindow frame;
	/*Paradigm algorithm metric parameter*/
	private String[] pamp;
	
	public OrdenationInterationController(NodeList variables, OrdenationWindow frame){
		this.variables = variables;
		this.frame = frame;				
		pamp = new String[4];			
	}
	
	public void upEvent(){
		JList ordenationJL = frame.getOrdenationJL();		
		DefaultListModel listModel   =  (DefaultListModel)ordenationJL.getModel();
		NodeList auxVector = new NodeList();
        String auxName = (String)ordenationJL.getSelectedValue();
        LearningNode aux = null;
        int index = ordenationJL.getSelectedIndex();
        if (index != 0){
            listModel.remove(index);
            index--;
            listModel.add(index, auxName);
            for (int i = 0 ; i < variables.size() ; i++ ){
                 aux = (LearningNode)variables.get(i);
                 if (aux.getName().equals(auxName)){
                     variables.remove(i);
                     for(int j = 0; j < i -1 ; j++){
                         auxVector.add(variables.get(j));
                     }
                     auxVector.add(aux);
                     for (int k = i -1 ; k < variables.size() ; k++){
                          auxVector.add(variables.get(k));
                     }
                     variables = auxVector;            
                     ordenationJL.setSelectedIndex(index);
                     break;
                 }
            }
        }			        
	}
	
	public void downEvent(){
		JList ordenationJL = frame.getOrdenationJL();		
		DefaultListModel listModel   =  (DefaultListModel)ordenationJL.getModel();
		NodeList auxVector = new NodeList();
        String auxName = (String)ordenationJL.getSelectedValue();
        LearningNode aux = null;
        int index = ordenationJL.getSelectedIndex();
        if (index < listModel.getSize()-1){
            listModel.remove(index);
            index++;
            listModel.add(index, auxName);
            for (int i = 0 ; i < variables.size() ; i++ ){
                 aux = (LearningNode)variables.get(i);
                 if (aux.getName().equals(auxName)){
                     variables.remove(i);
                     for(int j = 0; j < i +1 ; j++){
                         auxVector.add(variables.get(j));
                     }
                     auxVector.add(aux);
                     for (int k = i +1 ; k < variables.size() ; k++){
                          auxVector.add(variables.get(k));
                     }
                     variables = auxVector;    
                     ordenationJL.setSelectedIndex(index);
                     break;
                 }
            }
        }				
	}
	
	public String[] getPamp(){
		return pamp;		
	}	
	
	public void continueEvent(String p, String a, String m, String param){
		/*Paradigm Algorithm Metric Parameter*/
		pamp[0] = p;
		pamp[1] = a;
		pamp[2] = m;
		pamp[3] = param; 	
		frame.dispose();       
	}
	
	public void relationsEvent(){
		new RelationsWindow(variables);		
	}
	

	public void doubleClickEvent(MouseEvent e){
		int nClick = e.getClickCount();
        if (nClick == 2){
             JList list = (JList)e.getSource();
             if (!list.isSelectionEmpty()){
                 LearningNode aux = null;
                 Object object = list.getSelectedValue();
                 String name = object.toString();
                 for (int i = 0 ; i < variables.size(); i++ ){
                     aux = (LearningNode)variables.get(i);
                     if (aux.getName().equals(name)){
                         break;
                     }
                 }
                 new OptionsWindow(aux);
             }
        }				
	}
	
	public void paradigmEvent(int index){			
    	JComboBox algorithmsList = frame.getAlgorithmList(); 		
        algorithmsList.removeAllItems();	    	
		JComboBox metricList = frame.getMetricList();
   	    metricList.removeAllItems();    
        if(index ==0 ){            	        	
          	for(int i = 0; i < frame.getPonctuationSize(); i++){			    
           		algorithmsList.addItem(frame.getPonctuatioAlgorithms(i));            		
           	}
           	for(int i = 0; i < frame.getMetricsSize(); i++){			    
           		metricList.addItem(frame.getMetrics(i));            		
           	}
           	metricList.setEnabled(true);
	    } else if(index == 1) {	    	
	 	    for(int i = 0; i < frame.getIcSize(); i++){			    
           		algorithmsList.addItem(frame.getIcAlgorithms(i));            		
		    }            	            	
		    metricList.setEnabled(false);
        }		
	}	
	
	public NodeList getVariables(){
		return this.variables;	
	}

}
