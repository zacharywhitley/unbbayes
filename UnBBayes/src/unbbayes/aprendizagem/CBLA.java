package unbbayes.aprendizagem;

import sun.awt.VariableGridLayout;
import sun.beans.editors.IntEditor;
import sun.misc.Queue;
import unbbayes.util.*;
import unbbayes.gui.TJanelaEdicao;
import unbbayes.prs.Node;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;


import java.awt.SystemColor;
import java.util.*;
import java.util.Stack;


public class CBLA extends CBLToolkit{
	
	
	
	public CBLA(NodeList variables, byte[][] dataBase, int[] vector,
	             long caseNumber, String param, boolean compacted){
	    this.variablesVector = variables;
	    double epsilon;
	    TVariavel variable;
        this.separators = new ArrayList();	    
	    this.es = new ArrayList();	    	    
	    this.variablesVector = variables;
	    this.dataBase = dataBase;
	    this.vector   = vector;
	    this.vector = vector;	
	    this.caseNumber = caseNumber;    
	    try{ 
	    	this.epsilon = Double.parseDouble(param);	    		    	
	    	expand(scketch());
	        refine();	    	    
            mapStructure();					        
	    } catch(NumberFormatException e){
	    	System.err.println(e.getMessage());	    	
	    }   
	}
	
	private void refine(){
		int[] peace;
		ArrayList esx;
		for(int i = 0 ; i < es.size(); i++){
			peace = (int[])es.get(i);
			esx = (ArrayList)es.clone();
			esx.remove(i);			
			if(findWays(peace[0],peace[1],esx).size() == 0
			    && ! needConnect( peace[0], peace[1],esx, 0)){
			    	es.remove(i);			    									
			    	i--;
			}			
		}
		
	}	
	
	private void mapStructure(){
		int[] peace;
		TVariavel var1;
		TVariavel var2;
		for(int i = 0 ; i < es.size(); i++){
			peace = (int[])es.get(i);
			var1 = (TVariavel)variablesVector.get(peace[1]);
			var2 = (TVariavel)variablesVector.get(peace[0]);
			var1.adicionaPai(var2);						
		}	
	}
	
	
	/*Coloca os primeiros arcos da estrutura*/
	private ArrayList scketch(){
		System.out.println("Inicia o esboçar");
		int n = this.variablesVector.size();
		/* imAux recebe as informações mutuas auxiliares*/
		double imAux;
		ArrayList ls = new ArrayList(); 		
		/*Seta as informações mutuas de cada par, a informcao mutua de ab é 
		 * a mesma de ba*/		
		for(int i = 0 ; i < n; i++){
			for(int k = i+1; k < n ; k++){
			    imAux = mutualInformation((TVariavel)variablesVector.get(i), 
			                        (TVariavel)variablesVector.get(k));   					    
			    if( imAux > epsilon){
			    	ls.add(new double[]{imAux,i,k});			    				    				    	
			    }						
			}				
		}
		/*Ordena a lista em ordem decrescente de informacao mutua*/				
		sort(ls);						        						
		double[] peace;
		/*Verifica se há caminhos abertos entre as variaveis, caso não 
		 * haja é adionado um novo caminho entre essas variaveis*/		
		for(int i  = 0 ; i < ls.size(); i++){
			peace =(double[])ls.get(i);
			if(!(isOpenWays((int)peace[1],(int)peace[2],es))){
				es.add(new int[]{(int)peace[1],(int)peace[2]});	
                System.out.println("Arco inserido = " + (int)peace[1]+ ", "+ (int)peace[2]);
				ls.remove(i);							
				i--;
			}			
		}   	
      	System.out.println("Acaba o esboçar");
       	return ls;					       	
	}
	
	
	private void expand(ArrayList ls){		
		double[] peace;        		
		System.out.println("Inicia Alargar");
		for(int i = 0 ; i < ls.size(); i++){
			peace = (double[])ls.get(i);
			System.out.println("Tentativa = " + (int)peace[1]+ ", "+ (int)peace[2]);
			if(needConnect((int)peace[1],(int)peace[2],es,0)){				
			    es.add(new int[]{(int)peace[1],(int)peace[2]});		
			    System.out.println("Arco inserido = " + (int)peace[1]+ ", "+ (int)peace[2]);			    
			}			
		}			
		System.out.println("Inicia Alargar");
	}				    	      		
			
}