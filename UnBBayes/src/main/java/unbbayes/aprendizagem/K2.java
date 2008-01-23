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

/**
 * 	 This class implements those methods needed for the k2 algorithm to work.
 * 	 The k2 algorithm is a learning algorithm which uses scoring search.
 *   @author     Danilo Custódio
 *   @version    1.0
 *   @see TAprendizagemTollKit
 */

public class K2 extends K2Toolkit{

    private NodeList variablesVector;  
    
    
    public K2(NodeList variables, int[][] dataBase, int vector[], long 
            caseNumber, String metric, String param, boolean compacted){    	
    	Object[]  zMax;
    	LearningNode z;
        LearningNode variable;                
        this.compacted = compacted;
    	this.variablesVector = variables;
        this.dataBase        = dataBase;
        this.vector          = vector;
        this.caseNumber      = caseNumber;        
        double pOld;
        double pNew;        
        double variation;
        int parentsLength; 
        try{
        	setMetric(metric);
            variation = Math.pow(10,Integer.parseInt(param));                                     	        
	        boolean continueFlag = false;
    	    int length = variables.size();        
            constructPredecessors(variablesVector);                        
        	for(int i = 0; i < length;i++){
            	continueFlag      = true;
            	variable          = (LearningNode)variablesVector.get(i);
            	pOld              = getG(variable,null);
            	parentsLength     = variable.getNumeroMaximoPais();                         
            	while (continueFlag && variable.getTamanhoPais() < parentsLength){
                	zMax = getZMax((LearningNode)variable.clone());
                	z    = (LearningNode)zMax[0];
                	pNew = ((Double)zMax[1]).doubleValue();
                	if (pNew - pOld > variation){
                    	pOld = pNew;
                    	union(variable.getPais(), z);
                	} else{
                    	continueFlag = false;
                	}
            	}            	
        	}
        } catch(NumberFormatException e){
        	System.err.println(e.getMessage());        	
        }                   
    }
}