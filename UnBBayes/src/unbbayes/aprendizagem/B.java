package unbbayes.aprendizagem;

import java.awt.SystemColor;
import java.util.Date;
import java.util.List;

import unbbayes.gui.TJanelaEdicao;
import unbbayes.prs.bn.ProbabilisticNetwork;
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
    * @see TVariavel
    * @see TJanelaEdicao
    * @see Tnij
    * @see TAprendizagemTollKit
    */
  public B(NodeList variables, byte[][] dataBase, int[] vector, long caseNumber,
                String metric, String param, boolean compacted){  
    TVariavel variable;
    TVariavel aux;
    NodeList parentsAux;
    double gi;
    double gj;
    double variation;
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
        variation = Math.pow(10,Integer.parseInt(param));       
        gMatrix = getGMatrix();
        constructGMatrix();                
        IJVector = maxMatrix();
    	while(gMatrix[IJVector[0]][IJVector[1]] > 0){ 
        	variable = (TVariavel)variablesVector.get(IJVector[0]);
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
                  	if(isMember((TVariavel)variables.get(i),variable.getPais())){
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