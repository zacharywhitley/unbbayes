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
package unbbayes.learning;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.Node;
import unbbayes.prs.bn.LearningNode;
import unbbayes.util.SetToolkit;

public abstract class BToolkit extends PonctuationToolkit{
	
	protected double[][] gMatrix;
    protected boolean[] forefathers;
    protected boolean[] descendants;
    protected List<Node> variablesVector;
    
	protected double[][] getGMatrix(){	
      return new double[variablesVector.size()][variablesVector.size()];
    }
    
    protected void constructGMatrix(){
    	double gi = 0;
    	double gk = 0;
    	LearningNode variable;
    	LearningNode aux;
    	ArrayList<Node> parentsAux;
    	for(int i = 0; i < variablesVector.size(); i++){
            variable   = (LearningNode)variablesVector.get(i);            
       		gi = getG(variable,variable.getPais());
       		for(int j = 0; j < variablesVector.size(); j++){
         	    if(i != j){
            		aux = (LearningNode)variablesVector.get(j);
            		if(isMember(aux, variable.getPais())){
               		    gMatrix[i][j]  = 0;
            		}else{
                 		parentsAux = SetToolkit.clone(variable.getPais());
                 		parentsAux.add(aux);
                 		gk = getG(variable,parentsAux);
                 		gMatrix[i][j] = gk - gi;
            		}
         		}else{
                    gMatrix[i][j] = Double.NEGATIVE_INFINITY;
                }      
       		}
    	}
    }
    
    protected boolean isMember(LearningNode variable, ArrayList<Node> list){
        for(int i = 0 ; i < list.size(); i++){
            if(variable.getName().equals(((LearningNode)list.get(i)).getName())){
                return true;
        	}
      	}
      	return false;
    }	
    
    protected int[] maxMatrix(){
        double max;
     	double maxAux;
     	int vector[] = new int[2];
     	max = Double.NEGATIVE_INFINITY;
     	for(int i = 0 ; i < variablesVector.size(); i++){
        	for(int j = 0 ; j < variablesVector.size(); j++){
            	maxAux = gMatrix[i][j];
             	if(maxAux >= max){
                	vector[0] = i;
                 	vector[1] = j;
            	    max = maxAux;
             	}
        	}
     	}
     	return vector;
  	}
  	
  	protected void setForefathers(LearningNode variable){
        LearningNode aux;
        ArrayList<Node> list = variable.getPais();
        for(int j = 0 ; j < variablesVector.size(); j++)
           if(variable.getName().equals(((LearningNode)variablesVector.get(j)).getName())){
                forefathers[j] = true;
                break;
        }
        for(int i = 0 ; i < list.size();i++){
            aux = (LearningNode)list.get(i);
            setForefathers(aux);
        }
    }
    
    protected void setDescendants(LearningNode variable){
        LearningNode aux;	
     	LearningNode aux2;
     	ArrayList<Node> list;
     	for(int j = 0 ; j < variablesVector.size(); j++){
            if(variable.getName().equals(((LearningNode)variablesVector.get(j)).getName())){
                descendants[j] = true;
                break;
     		}
        }
     	for(int i = 0 ; i < variablesVector.size();i++){
         	aux  = (LearningNode)variablesVector.get(i);
         	list =  aux.getPais();
         	for(int j = 0 ; j < list.size(); j++){
            	aux2 = (LearningNode)list.get(j);
            	if(variable.getName().equals(aux2.getName())){
                	setDescendants(aux);
            	}
         	}
     	}
  	}
    
    			
	
}
