package unbbayes.aprendizagem;

import java.util.Date;
import java.util.List;
import unbbayes.fronteira.TJanelaEdicao;
import unbbayes.jprs.jbn.Node;
import unbbayes.jprs.jbn.PotentialTable;
import unbbayes.jprs.jbn.ProbabilisticNetwork;
import unbbayes.util.NodeList;
import unbbayes.util.SetToolkit;

/**
 *   Essa classe implementa os métodos necessários para que
 *   o algoritmo k2 funcione .O algoritmo k2 é um
 *   algoritmo de aprendizagem que utiliza a busca em pontuaçao.
 *   @author     Danilo Custódio
 *   @version    1.0
 *   @see TAprendizagemTollKit
 */

public class K2 extends K2Toolkit{

    private NodeList variablesVector;  
    
    
    public K2(NodeList variables, byte[][] dataBase, int vector[], long 
            caseNumber, String metric, String param, boolean compacted){    	
    	Object[]  zMax;
    	TVariavel z;
        TVariavel variable;                
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
            	variable          = (TVariavel)variablesVector.get(i);
            	pOld              = getG(variable,null);
            	parentsLength     = variable.getNumeroMaximoPais();                         
            	while (continueFlag && variable.getTamanhoPais() < parentsLength){
                	zMax = getZMax((TVariavel)variable.clone());
                	z    = (TVariavel)zMax[0];
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