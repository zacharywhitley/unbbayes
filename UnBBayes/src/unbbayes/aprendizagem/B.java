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
 *   Essa classe implementa os métodos necessários para que
 *   o algoritmo B funcione .O algoritmo k2 é um
 *   algoritmo de aprendizagem que utiliza a busca em pontuaçao. *
 *   @author     Danilo Custódio
 *   @version    1.0
 *   @see TAprendizagemTollKit
 */

public class B extends BToolkit{     	
	


    /**
    * Método que representa a funçao principal do algoritmo B.
    * Esse método recebe um lista de variáveis, uma matriz com
    * os dados do arquivo, uma matriz arranjo que possui a pontu
    * acao de cada para de elemento e a partir disso monta a rede
    * bayseana correspondente aquele arquivo.
    *
    * @param variaveis Lista de variáveis(<code>List</code>)
    * @param BaseDados Representaçào do arquivo em memória(<code>byte[][]<code>)
    * @param vetor     Vetor que indica quantas vezes uma linha do arquivo se repete
    * (<code>int[]<code>)
    * @see LearningNode
    * @see Tnij
    * @see TAprendizagemTollKit
    */
  public B(NodeList variables, byte[][] dataBase, int[] vector, long caseNumber,
                String metric, String param, boolean compacted){  
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