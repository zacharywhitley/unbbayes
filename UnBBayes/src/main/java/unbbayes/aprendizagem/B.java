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


import unbbayes.prs.bn.LearningNode;
import unbbayes.util.NodeList;
import unbbayes.util.SetToolkit;

/**
 * 	 This class implements methods which are necessary to make the algorithm B to work.
 *   The k2 algorithm is a learning algorithm which uses scoring search (busca em pontuacao)
 *   @author     Danilo Custódio
 *   @version    1.0
 *   @see TAprendizagemTollKit
 */

public class B extends BToolkit{     	
	


   /**
    * Method which represents the main function of B algorithm.
    * This method accepts a list of variables, an array
    * with data from the archive, another array containing the scores
    * of each elements. Based on those informations, this method
    * builds the bayesian network of that archive.
    *
    * @param variaveis List of variables(<code>List</code>)
    * @param BaseDados On-memory representation of the archive(<code>int[][]<code>)
    * @param vetor     Vector representing how many times a line withing the archive is repeating(<code>int[]<code>)
    * @see LearningNode
    * @see Tnij
    * @see TAprendizagemTollKit
    */
  public B(NodeList variables, int[][] dataBase, int[] vector, long caseNumber,
                String metric, String param, boolean compacted){  
    //LearningNode variable;
	LearningNode variable;
    NodeList parentsAux;
    double gi;
    double gj;
//    double variation;
    this.compacted = compacted;
    this.dataBase = dataBase;
    this.vector = vector;
    this.caseNumber = caseNumber; 
    this.variablesVector = variables;
    descendants = new boolean[variables.size()];
    forefathers = new boolean[variables.size()];
    int IJVector[] = new int[2];    
    try{
        setMetric(metric);
//        variation = Math.pow(10,Integer.parseInt(param));       
        gMatrix = getGMatrix();
        constructGMatrix();                
        IJVector = maxMatrix();
    	while(gMatrix[IJVector[0]][IJVector[1]] > 0){ 
        	//variable = (LearningNode)variablesVector.get(IJVector[0]);
    		variable = (LearningNode)variablesVector.get(IJVector[0]);
           	parentsAux = variable.getPais();
           	parentsAux.add(variablesVector.get(IJVector[1]));
           	gi = getG(variable,parentsAux);
       		setForefathers(variable);
           	setDescendants(variable);
           	for(int i = 0 ; i < forefathers.length; i++){
            	for(int j = 0 ; j < descendants.length; j++){
               		if(forefathers[i] && descendants[j]){
                   		gMatrix[i][j] = Double.NEGATIVE_INFINITY;
               		}
            	}
           	}
           	for(int i = 0; i < variables.size(); i++){
                if(gMatrix[IJVector[0]][i] > Double.NEGATIVE_INFINITY){
                  	//if(isMember((LearningNode)variables.get(i),variable.getPais())){
                	if(isMember((LearningNode)variables.get(i),variable.getPais())){
                      	gMatrix[IJVector[0]][i] = 0;
                   	} else{
                       	parentsAux = SetToolkit.clone(variable.getPais());
                       	parentsAux.add(variables.get(i));
                       	gj = getG(variable,parentsAux);
                       	gMatrix[IJVector[0]][i] = gj - gi;
                   	}
            	}
           	}           	        	
           	for(int i = 0 ; i < variables.size(); i++){
           		forefathers[i] = false;
           		descendants[i] = false;           		
           	}           	
       		IJVector = maxMatrix();       		
    	}    
    }catch (NumberFormatException e){
    	System.err.println(e.getMessage());    	
    }        
  }        
}