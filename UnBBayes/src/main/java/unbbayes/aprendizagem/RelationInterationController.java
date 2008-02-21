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

import javax.swing.DefaultListModel;
import javax.swing.JList;

import unbbayes.prs.bn.LearningNode;
import unbbayes.util.NodeList;


public class RelationInterationController {
	
	private RelationsWindow frame;
	private NodeList variables;
	
	public RelationInterationController(RelationsWindow frame, NodeList variables){
		this.frame = frame;
		this.variables = variables;
	}
	
	public void addEvent(Object index1, Object index2){
		DefaultListModel relationListModel = frame.getRelationModel();		        
        String relationIndex  = index1 +  "-->" + index2;
        boolean flag = false;
        if (!(relationIndex.equals("null-->null"))){
            for(int i = 0; i < variables.size(); i++){
                LearningNode aux = (LearningNode)variables.get(i);
                if (aux.getName().equals(""+index2)){
                    for(int j = 0; j < variables.size(); j++){
                        if (i != j){
                          aux = (LearningNode)variables.get(j);
                          if (aux.getName().equals(""+index1)){
                              aux = (LearningNode)variables.get(j);
                              if (!(aux.getPai(""+index2).equals(""+index2))){
                                  aux = (LearningNode)variables.get(i);
                                  NodeList auxVector = aux.getPais();
                                  for (int k = 0; k < auxVector.size() ; k++ ){
                                      LearningNode aux1 = (LearningNode)auxVector.get(k);
                                      if (aux1.getName().equals(""+index1)){
                                          flag = true;
                                          break;      
                                      }                                
                                  }
                                  if (!flag){ 
                                   	  aux.adicionaPai((LearningNode)variables.get(j));
                                      relationListModel.addElement(relationIndex);
                                  }
                              }
                          }
                        }
                    }
                }
            }
        }		
	}
	
	public void removeEvent(Object relation){	    
        String relationName = (String)relation;
        NodeList auxVector  = new NodeList();
        JList relationList  = frame.getRelationList();
        DefaultListModel relationListModel = (DefaultListModel)relationList.getModel();
        if (!relationList.isSelectionEmpty() && relation != null){
            int index     = relationName.indexOf('-');
            int length    = relationName.length();
            boolean flag  = false;
            String parentName = relationName.substring(0,index);
            String sunName     = relationName.substring(index +3,length);
            for (int i = 0; i < variables.size() ; i++ ){
                LearningNode aux = (LearningNode)variables.get(i);
                if (aux.getName().equals(sunName)){
                    auxVector =  aux.getPais();
                    for (int j = 0 ; j < auxVector.size() ;j++ ){
                        LearningNode aux1 = (LearningNode)auxVector.get(j);
                        if (aux1.getName().equals(parentName)){
                            auxVector.remove(j);
                            relationListModel.removeElement(relation);
                            flag = true;
                            break;
                        }
                    }
                    if(flag){
                        break;
                    }
                }
            }
        }	 		
	}
	
	public void continueEvent(){
		frame.dispose();		
	}
	

}
