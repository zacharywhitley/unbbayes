
package unbbayes.aprendizagem;

import unbbayes.util.NodeList;
import java.util.ArrayList;
import java.util.Stack;

public class CBLB extends CBLToolkit{
	
	private ArrayList esFinal; 
	
	public CBLB(NodeList variables, byte[][] dataBase, int[] vector,
	             long caseNumber, String param, boolean compacted){
	    this.variablesVector = variables;
	    double epsilon;
	    TVariavel variable;
	    esFinal = new ArrayList();
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
	        findVStructures();
	        ruleCBL1();
	        ruleCBL2();
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
			if(findWays(peace[0],peace[1],esx).size() >0
			    && ! needConnect( peace[0], peace[1],esx, 1)){			    	
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
			var2.adicionaPai(var1);
		}        
		for(int i = 0 ; i < esFinal.size();i++){
			peace = (int[])esFinal.get(i);
			var1 = (TVariavel)variablesVector.get(peace[1]);
			var2 = (TVariavel)variablesVector.get(peace[0]);			
			var1.adicionaPai(var2);			
		}
		
	}
	
	
	/*Coloca os primeiros arcos da estrutura*/
	private ArrayList scketch(){
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
			    	System.out.println(""+i+ ", "+ k+ " = " +imAux);
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
			if(findWays((int)peace[1],(int)peace[2],es).size() == 0 ){
				es.add(new int[]{(int)peace[1],(int)peace[2]});					
				ls.remove(i);					
				i--;
			}						
		}		
       	return ls;					
	}
	
	
	private void expand(ArrayList ls){
		double[] peace;        
		for(int i = 0 ; i < ls.size(); i++){
			peace = (double[])ls.get(i);
			if(needConnect((int)peace[1],(int)peace[2],es,1)){				
			    es.add(new int[]{(int)peace[1],(int)peace[2]});		
			}			
		}				
	}
	
	private void findVStructures(){
		boolean flag = false;
		ArrayList sep;
		Object[] aux;
		int[]vars1;
		int[] vars;
		int[] indexes;
		for(int i = 0 ;i < variablesVector.size(); i++){
			for(int j = 0 ; j < es.size(); j++){
				vars = (int[])es.get(j);
				if(vars[0] == i){
					for(int k = 0; k < es.size(); k++){
						if(k != j){
    						vars1 = (int[])es.get(k);
    						if(vars1[1] == vars[1]){
    							for(int w = 0 ; !flag && w < separators.size(); w++){
    								aux     = (Object[])separators.get(w);
                                    indexes = (int[])aux[0];
                                    sep     = (ArrayList)aux[1];
                                    if(indexes.equals(new int[]{vars[0],vars1[0]})){
                                        if(notContain(sep,vars[1] )){
                                            esFinal.add(new int[]{vars[0],vars[1]});
                                            esFinal.add(new int[]{vars1[0],vars1[1]});
                                            remove(new int[]{vars1[0], vars1[1]});
                                            remove(new int[]{vars[0], vars[1]});
                                            flag = true;
                                        }
                                    }
                           	    }
                           	    if(!flag){
                                    esFinal.add(new int[]{vars[0],vars[1]});
                                    esFinal.add(new int[]{vars1[0],vars1[1]});
                                    remove(new int[]{vars1[0], vars1[1]});
                                    remove(new int[]{vars[0], vars[1]});
                             	    flag = false;
                           	    }
    						}
						}
					}
				}
			}
		}
	}
	
	private boolean notContain(ArrayList sep, int k){
		for(int i = 0 ; i < sep.size(); i++){
			if(((Integer)sep.get(i)).intValue() == k){
			    return false;	
			}		
		}
		return true;
	}
	
	private void ruleCBL1(){
		boolean flag;
		ArrayList sep;
		ArrayList aux;
		int[] vars;
		int[] vars1;
		for(int i = 0 ;i < variablesVector.size(); i++){
			for(int j = 0 ; j < esFinal.size(); j++){
				vars = (int[])esFinal.get(j);
				if(vars[0] == i){
					for(int k = 0; k < es.size(); k++){
						if(k != j){
    						vars1 = (int[])es.get(k);
    						if(vars1[0] == vars[1]){		    							
    							if(notHave(new int[]{vars[0], vars1[1]})){
    								remove(vars1);
                  	               	esFinal.add(vars1);                  	               	
                  	                break;    								
    							}
    						}
						}
					}
				}
			}
		}  
	}
	
	private boolean notHave(int[] peace){
		for(int i = 0; i < es.size(); i++){
			if(((int[])es.get(i)).equals(peace)){
				return false;				
			}			
		}			
		return true;		
	}
	private void remove(int[] peace){
		int[] aux;
		for(int i = 0 ; i< es.size(); i++){			
			aux = (int[]) es.get(i);			
     		if(aux[0] == peace[0] && aux[1] == peace[1]){
     			es.remove(i);     			
     			return;
     		}
		}		
	}
	
	private void ruleCBL2(){
		int[] peace; 
		for(int i = 0 ; i < es.size(); i++){
			peace = (int[])es.get(i);
			if(orientedWay(peace)){
				remove(peace);
			}			
		}						
	}
	
	private boolean orientedWay(int[] peace){
		for(int i = 0 ; i < esFinal.size(); i++){
			if(((int[])esFinal.get(i)).equals(peace)){
			    return true;	
			}			
		}
		return false;
		
	}

}
