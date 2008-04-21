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

import java.util.ArrayList;

import unbbayes.prs.Node;
import unbbayes.prs.bn.LearningNode;


public class CBLA extends CBLToolkit{
	
	
	
	public CBLA( ArrayList<Node> variables, int[][] dataBase, int[] vector,
	             long caseNumber, String param, boolean compacted){
	    this.variablesVector = variables;
        this.separators = new ArrayList<Object[]>();	    
	    this.es = new ArrayList<int[]>();	    	    
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
		ArrayList<int[]> esx;
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
		LearningNode var1;
		LearningNode var2;
		for(int i = 0 ; i < es.size(); i++){
			peace = (int[])es.get(i);
			var1 = (LearningNode)variablesVector.get(peace[1]);
			var2 = (LearningNode)variablesVector.get(peace[0]);
			var1.adicionaPai(var2);						
		}	
	}
	
	
	/*Coloca os primeiros arcos da estrutura*/
	private ArrayList<double[]> scketch(){
		System.out.println("Inicia o esboçar");
		int n = this.variablesVector.size();
		/* imAux recebe as informações mutuas auxiliares*/
		double imAux;
		ArrayList<double[]> ls = new ArrayList<double[]>(); 		
		/*Seta as informações mutuas de cada par, a informcao mutua de ab é 
		 * a mesma de ba*/		
		for(int i = 0 ; i < n; i++){
			for(int k = i+1; k < n ; k++){
			    imAux = mutualInformation((LearningNode)variablesVector.get(i), 
			                        (LearningNode)variablesVector.get(k));   					    
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