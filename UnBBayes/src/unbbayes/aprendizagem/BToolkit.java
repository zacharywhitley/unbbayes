
package unbbayes.aprendizagem;

import unbbayes.util.NodeList;
import unbbayes.util.SetToolkit;
import java.util.List;

public abstract class BToolkit extends PonctuationToolkit{
	
	protected double[][] gMatrix;
    protected boolean[] forefathers;
    protected boolean[] descendants;
    protected NodeList variablesVector;
    
	protected double[][] getGMatrix(){	
      return new double[variablesVector.size()][variablesVector.size()];
    }
    
    protected void constructGMatrix(){
    	double gi = 0;
    	double gk = 0;
    	TVariavel variable;
    	TVariavel aux;
    	NodeList parentsAux;
    	for(int i = 0; i < variablesVector.size(); i++){
            variable   = (TVariavel)variablesVector.get(i);            
       		gi = getG(variable,variable.getPais());
       		for(int j = 0; j < variablesVector.size(); j++){
         	    if(i != j){
            		aux = (TVariavel)variablesVector.get(j);
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
    
    protected boolean isMember(TVariavel variable, NodeList list){
        for(int i = 0 ; i < list.size(); i++){
            if(variable.getName().equals(((TVariavel)list.get(i)).getName())){
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
  	
  	protected void setForefathers(TVariavel variable){
        TVariavel aux;
        NodeList list = variable.getPais();
        for(int j = 0 ; j < variablesVector.size(); j++)
           if(variable.getName().equals(((TVariavel)variablesVector.get(j)).getName())){
                forefathers[j] = true;
                break;
        }
        for(int i = 0 ; i < list.size();i++){
            aux = (TVariavel)list.get(i);
            setForefathers(aux);
        }
    }
    
    protected void setDescendants(TVariavel variable){
        TVariavel aux;	
     	TVariavel aux2;
     	NodeList list;
     	for(int j = 0 ; j < variablesVector.size(); j++){
            if(variable.getName().equals(((TVariavel)variablesVector.get(j)).getName())){
                descendants[j] = true;
                break;
     		}
        }
     	for(int i = 0 ; i < variablesVector.size();i++){
         	aux  = (TVariavel)variablesVector.get(i);
         	list =  aux.getPais();
         	for(int j = 0 ; j < list.size(); j++){
            	aux2 = (TVariavel)list.get(j);
            	if(variable.getName().equals(aux2.getName())){
                	setDescendants(aux);
            	}
         	}
     	}
  	}
    
    			
	
}
