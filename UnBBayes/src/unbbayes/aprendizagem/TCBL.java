package unbbayes.aprendizagem;

import sun.beans.editors.IntEditor;
import sun.misc.Queue;
import unbbayes.util.*;
import unbbayes.fronteira.TJanelaEdicao;
import unbbayes.jprs.jbn.Node;
import unbbayes.jprs.jbn.PotentialTable;
import unbbayes.jprs.jbn.ProbabilisticNetwork;


import java.awt.SystemColor;
import java.util.*;
import java.util.Stack;


public class TCBL extends TAprendizagemTollKit{
		
	
	private double epsilon;
	private ProbabilisticNetwork net;
	private ArrayList es;
	private ArrayList separadores;
    protected TJanelaEdicao janela;
	
	public TCBL(NodeList variaveis, byte[][] baseDados, int numeroCasos, int[]
	             vetor, double epsilon, ProbabilisticNetwork net  ){
       	int arrayNijk[][];
       	PotentialTable tabela;
        TVariavel variavel;
	    this.separadores = new ArrayList();
	    this.vetorVariaveis = variaveis;
	    this.BaseDados = baseDados;
	    this.numeroCaso = numeroCasos;
	    this.vetor = vetor;
	    this.net = net;
	    this.epsilon = epsilon;
	    this.es = new ArrayList();	    	    
	    alargar(esbocar());
	    refinar();	    
	    System.out.println("Aki3");
        mapeiaEstrutura();				
		janela = new TJanelaEdicao(variaveis, net);
        for(int i = 0; i < vetorVariaveis.size(); i++) {
            variavel = (TVariavel)variaveis.get(i);
            arrayNijk = calculaFrequencias(variavel,variavel.getPais());
            tabela = variavel.getProbabilidades();
            tabela.addVariable(variavel);
            int tamanhoPais = variavel.getTamanhoPais();
            for (int j = 0; j < tamanhoPais; j++) {
                tabela.addVariable(variavel.getPais().get(j));
            }
            calculaProbabilidade(arrayNijk, variavel);
        }
	        
	}
	
	private void refinar(){
		int[] gomo;
		ArrayList esx;
		for(int i = 0 ; i < es.size(); i++){
			gomo = (int[])es.get(i);
			esx = (ArrayList)es.clone();
			esx.remove(i);			
			if(haCaminhosAbertos(gomo[0],gomo[1],esx)
			    && ! precisaConectar(gomo[0], gomo[1],esx, 1)){
			    	es.remove(i);			    									
			    	i--;
			}			
		}
		
	}	
	
	private void mapeiaEstrutura(){
		int[] gomo;
		TVariavel var1;
		TVariavel var2;
		for(int i = 0 ; i < es.size(); i++){
			gomo = (int[])es.get(i);
			var1 = (TVariavel)vetorVariaveis.get(gomo[1]);
			var2 = (TVariavel)vetorVariaveis.get(gomo[0]);
			var1.adicionaPai(var2);						
		}	
	}
	
	
	public double g(TVariavel a ,NodeList b){
		return 0;
	}
	
	/*Coloca os primeiros arcos da estrutura*/
	private ArrayList esbocar(){
		int n = this.vetorVariaveis.size();
		/* imAux recebe as informações mutuas auxiliares*/
		double imAux;
		ArrayList ls = new ArrayList(); 
		
		/*Seta as informações mutuas de cada par, a informcao mutua de ab é 
		 * a mesma de ba*/		
		for(int i = 0 ; i < n; i++){
			for(int k = i+1; k < n ; k++){
			    imAux = informacaoMutua((TVariavel)vetorVariaveis.get(i), 
			                        (TVariavel)vetorVariaveis.get(k));   		
			    if( imAux > epsilon){
			    	ls.add(new double[]{imAux,i,k});			    				    	
			    	
			    }						
			}						
		}
		/*Ordena a lista em ordem decrescente de informacao mutua*/				
		ordena(ls);						        						
		double[] gomo;
		/*Verifica se há caminhos abertos entre as variaveis, caso não 
		 * haja é adionado um novo caminho entre essas variaveis*/
		 System.out.println("Size1 = " + ls.size());				
		for(int i  = 0 ; i < ls.size(); i++){
			gomo =(double[])ls.get(i);
			if(!(haCaminhosAbertos((int)gomo[1],(int)gomo[2],es))){
				es.add(new int[]{(int)gomo[1],(int)gomo[2]});	
				ls.remove(i);							
				i--;
			}			
		}
        System.out.println("Aki");
		return ls;					
		/*Esboçar está totalmente OKKK!!!!!!!!!!!!!!*/

	}
	
	
	private void alargar(ArrayList ls){
		double[] gomo;
        System.out.println("Size = " + ls.size());				
		for(int i = 0 ; i < ls.size(); i++){
			gomo = (double[])ls.get(i);
			System.out.println("I = " + i);
			if(precisaConectar((int)gomo[1],(int)gomo[2],es,0)){				
			    es.add(new int[]{(int)gomo[1],(int)gomo[2]});		
			}			
		}		
	}
	
	
	private boolean precisaConectar(int v1, int v2,ArrayList esx, int tipo){
		int n = this.vetorVariaveis.size();
		ArrayList Z;
		double m;		
		Z = separador(v1,v2,esx,n,tipo);
		m = informacaoMutuaCond(v1,v2,Z);		
		if( m < epsilon){			
			Object[] sep = new Object[2];
			sep[0] = new int[]{v1,v2};
			sep[1] = Z;
			separadores.add(sep);
			return false;						
		}				
		while( Z.size() > 1 ){
			int k = 0;
			double arrayM[] = new double[Z.size()];
			double min = Double.MAX_VALUE;			
			for(int i = 0 ; i < Z.size(); i++){	
				ArrayList ZAux = (ArrayList)Z.clone();
				ZAux.remove(i);				
			    m = informacaoMutuaCond(v1,v2,ZAux);			    			    
			    arrayM[i] = m;			    					
			    if(m < min){
			        min = m;
			        k = i;	
			    }			    
			}			
			if(min < epsilon){
				Object[] sep = new Object[2];
			    sep[0] = new int[]{v1,v2};
			    sep[1] = Z.remove(k);
			    separadores.add(sep);
			    return false;										
			}
			if(min > epsilon){				
				return true;				
			} else {
				m = arrayM[k];
				Z.remove(k);				
			}
		}		
		return true;		
	}
	
	private ArrayList separador(int v1, int v2,ArrayList esx, int n,int tipo){
		ArrayList esAnc;
		if(tipo == 0){			
	        esAnc =  acharSubgrafoAnc(v1,v2,esx);
		} else{ 
			esAnc = (ArrayList)esx.clone();			
		}				
	    ArrayList esAncMor =  moraliza(esAnc);
	    return achaSep(esAncMor, v1, v2);	
	}
	
	private ArrayList acharSubgrafoAnc(int v1,int v2, ArrayList esx){
		ArrayList esAnc = new ArrayList();
        boolean[] anc1 = new boolean[this.vetorVariaveis.size()]; 
        boolean[] anc2 = new boolean[this.vetorVariaveis.size()]; 
        int[] gomo;
		achaAncestrais(v1,anc1);
		achaAncestrais(v2,anc2);
		for(int i = 0 ; i < anc1.length; i++){
			if(anc1[i] || anc2[i]){
				anc1[i] = true;				
			}			
		}
		for(int j = 0 ; j < esx.size(); j++){
			gomo = (int[])esx.get(j);
			if(anc1[gomo[0]] &&  anc1[gomo[1]]){				
				esAnc.add(new int[]{gomo[0],gomo[1]});
			}
		}							
		return esAnc;
	}
	
	private ArrayList moraliza(ArrayList esAnc){
		ArrayList esAncMor = (ArrayList)esAnc.clone();
		int[] gomo;
		int[] gomo1;
		for(int i = 0 ; i < esAnc.size(); i++){
			gomo = (int[])esAnc.get(i);
			//esAncMor.add(new int[]{gomo[1],gomo[0]});
			for(int j = 0; j < esAnc.size() && j != i;j++){
			    gomo1 = (int[])esAnc.get(j);
			    if(gomo1[1] == gomo[1]){
    				esAncMor.add(new int[]{gomo1[0],gomo[0]});
			    }									
			}									
		}
		return esAncMor;				
	}
	
    private ArrayList achaSep(ArrayList esAncMor, int v1, int v2){
    	ArrayList sep = new ArrayList();   
    	/*É usada uma estrutura de Set para que não aja elementos
    	 * repetidos*/
    	TreeSet vv1 = new TreeSet();
    	TreeSet vv2 = new TreeSet();
    	ArrayList vvAux;
    	ArrayList caminhos  = achaCaminhos(v1,v2,esAncMor);  	    	   	    	
    	ArrayList vizinhos1 = achaVizinhos(v1, esAncMor);    	
    	ArrayList vizinhos2 = achaVizinhos(v2, esAncMor);
    	filtraVizinhos(vizinhos1, caminhos);    	
    	filtraVizinhos(vizinhos2, caminhos);     	 	    
 	    /*Acha os vizinhos dos vizinhos*/
    	for(int i = 0; i < vizinhos1.size(); i++){
    		vvAux = achaVizinhos(((Integer)vizinhos1.get(i)).intValue(),esAncMor);
    		for(int j = 0 ; j < vvAux.size(); j++){
    			if(((Integer)vvAux.get(j)).intValue() != v2 
    			    && ((Integer)vvAux.get(j)).intValue() != v1){    				
    				vv1.add(vvAux.get(j));
    			}
    		}
    		vvAux.clear();
    	}
    	/*Faz a uniao dos vizinhos, com os vizinhos dos vizinhos*/    	
    	for(int i = 0 ; i < vizinhos1.size(); i++){
    		vv1.add(vizinhos1.get(i));    		
    	}    	
    	for(int i = 0 ; i < vizinhos2.size(); i++){
    		vvAux = achaVizinhos(((Integer)vizinhos2.get(i)).intValue(), esAncMor);
    		for(int j = 0 ; j < vvAux.size(); j++){
    			if(((Integer)vvAux.get(j)).intValue() != v2
    			   && ((Integer)vvAux.get(j)).intValue() != v1){    				
    				vv2.add(vvAux.get(j));
    			}    			
    		}    		
    		vvAux.clear();
    	}    	
    	for(int i = 0 ; i < vizinhos2.size(); i++){
    		vv2.add(vizinhos2.get(i));    		
    	}    		
    	if(vv1.size() < vv2.size()){
    		Object[] a = vv1.toArray();
    		for(int i = 0 ; i < a.length; i++){    			
    			sep.add(a[i]);
    		}    		    		
    		filtraVizinhos(sep, caminhos);
    		return sep;    		
    	} else { 
    		Object[] a = vv2.toArray();
    		for(int i = 0 ; i < a.length; i++){    			
    			sep.add(a[i]);    			
    		}    		    
	   		filtraVizinhos(sep, caminhos);		
    		return sep;    		
    	}    	
    }	
    
    private void filtraVizinhos(ArrayList vizinhos, ArrayList caminhos){
    	ArrayList caminho; 
    	Integer passo;
    	Integer viz;
    	ArrayList vizinhoAux = new ArrayList();
    	boolean[] vizinho = new boolean[vizinhos.size()];
    	
    	for(int i = 0 ; i < vizinhos.size(); i++){
    		viz = (Integer)vizinhos.get(i);    		    		
    		for(int j = 0 ; j < caminhos.size() && !vizinho[i]; j++){
    			caminho = (ArrayList)caminhos.get(j);
           		for(int k = 0 ; k < caminho.size() && !vizinho[i]; k++){
    	    		passo = (Integer) caminho.get(k);
    	    		if( passo.intValue() == viz.intValue()){    	    			
                        vizinho[i] = true;
    	    		}    	    		    	    		
           		}           		    		    			    			
    		}    		
    	}
    	for(int i = 0 ; i < vizinhos.size(); i++){
    		if(! vizinho[i]){
    			vizinhos.remove(i);    			
    		}    		
    	}    		    	    	
    }
    
    private ArrayList achaVizinhos(int v1, ArrayList esAncMor){
    	ArrayList vizinhos = new ArrayList();
    	int[] gomo;
    	for(int i = 0 ; i < esAncMor.size(); i++){    		
    		gomo = (int[])esAncMor.get(i);
    		if(gomo[0] == v1){
    			vizinhos.add(new Integer(gomo[1]));    			  			
    		} else if(gomo[1] == v1){
    			vizinhos.add(new Integer(gomo[0]));    			
    		}   
    	} 
    	return vizinhos;    	   	    	
    }
    
    private ArrayList achaCaminhos(int v1, int v2, ArrayList esAncMor){
        ArrayList rs = new ArrayList();
    	ArrayList cs = new ArrayList();
    	ArrayList fila = new ArrayList();
    	ArrayList fs;
    	cs.add(new Integer(v2));
    	fila.add(cs.clone());
    	Integer ultimo;
    	while(fila.size() > 0){
    	    cs = (ArrayList)fila.get(0); 
    	    fila.remove(0);        		
    	    ultimo = (Integer)cs.get(cs.size() -1);
    	    if( ultimo.intValue() == v1){
    	    	rs.add(cs);    	    	
    	    } else{
    	    	fs = expande(cs, esAncMor);    	    	
    	    	for(int j = 0;  j < fs.size(); j++){
    	    		fila.add(fs.get(j));    	    		
    	    	}
    	    }
    	}    	
    	return rs;    	
    }
    
    private ArrayList expande(ArrayList cs, ArrayList esAncMor){
    	ArrayList fs = new ArrayList();
    	int[] gomo;
    	Integer x;
    	Integer y;
    	for(int i = 0 ; i < esAncMor.size(); i++){
    		gomo = (int[])esAncMor.get(i);
    		if( gomo[0] == ((Integer)cs.get(cs.size() -1)).intValue()
    		                  && ! membro(cs,gomo[1])){
    		                  	ArrayList csClone = (ArrayList)cs.clone();
    		                  	csClone.add(new Integer(gomo[1]));
    		                  	fs.add(csClone);
    		} else if( gomo[1] == ((Integer)cs.get(cs.size() -1)).intValue()
    		                  && ! membro(cs,gomo[0])){
                                ArrayList csClone = (ArrayList)cs.clone();
    		                  	csClone.add(new Integer(gomo[0]));
    		                  	fs.add(csClone);
    		}
    	}    	
    	return fs;    	
    }
    
    private boolean membro(ArrayList cs, int x){
    	Integer c; 
    	for(int i = 0 ; i < cs.size(); i++){
    		c = (Integer)cs.get(i);
    		if(c.intValue() == x){
    			return true;    			
    		}
    	}
    	return false;
    	
    }
	
		
	private void achaAncestrais(int v,boolean[] anc){		
		anc[v] = true;
		int[] gomo;
		for(int i = 0 ; i < es.size() ; i++){
			gomo = (int[])es.get(i);
			if(gomo[1] == v){				
				achaAncestrais(gomo[0],anc);											
			}						
		}		
	}
	
	private boolean haCaminhosAbertos(int v1, int v2, ArrayList esx){		
		Stack pilha = new Stack();
		ArrayList gomo = new ArrayList();
		/*Inicializa o primeiro elemento da pilha [-1,v1]*/
		gomo.add(new Integer(-1));
		gomo.add(new Integer(v1));
		pilha.push(gomo);
		ArrayList pai;
		ArrayList lista;
		int u;			
		while(! pilha.empty()){			
		    pai  = (ArrayList)pilha.pop();		    
		    u = ((Integer)cabeca(cauda(pai))).intValue();		    
		    if(u == v2){ 
		    	return true;		    	
		    } else {
	            lista = expandeAbertos(pai,esx);
	            for(int i = 0 ; i < lista.size(); i++){
	            	pilha.push(lista.get(i));	            	
	            }              	    			    			    	
		    }		    
		}	
		return false;	
	}
	
	private ArrayList expandeAbertos(ArrayList pai, ArrayList esx){
        Integer direcao = (Integer)pai.get(0);
        pai.remove(0);
        Integer u = (Integer)cabeca(pai);
        ArrayList as = (ArrayList)esx.clone();
        ArrayList fs = new ArrayList();
        ArrayList caminho = new ArrayList();
        while(as.size() > 0 ){
        	int[] ab = (int[])as.get(0); 
        	as.remove(0);
        	if(ab[0] == u.intValue()){
        		caminho.add(new Integer(1));
        		caminho.add(new Integer(ab[1]));
        		for(int i = 0 ; i < pai.size(); i++){
        			caminho.add(pai.get(i));        			
        		}
        		fs.add(caminho.clone());				
        	} else if(ab[1] == u.intValue() && direcao.intValue() < 0) {
        		caminho.add(new Integer(-1));
        		caminho.add(new Integer(ab[0]));
        		for(int i = 0 ; i < pai.size(); i++){
        			caminho.add(pai.get(i));        			
        		}
        		fs.add(caminho.clone());				
        	}      	        	
        	caminho.clear();
        }
        return fs;        
	}
	
	private Object cabeca(ArrayList lista) {
		return lista.get(0);				
	}
	
	private ArrayList cauda(ArrayList lista){
		ArrayList listaAux = (ArrayList)lista.clone();
		listaAux.remove(0);
		return listaAux;				
	}	
	
	
	private void ordena(ArrayList ls){		
		double[] gomo;
		double[] gomo1;   									
		for(int i = 0 ; i < ls.size(); i++){			
			gomo = (double[])ls.get(i);
			for(int j = i+1; j < ls.size(); j++){
				gomo1 = (double[])ls.get(j);
				if(gomo[0] < gomo1[0]){
					ls.add(i,gomo1);
					ls.remove(i+1);					
					ls.add(j,gomo);					
					ls.remove(j+1);
					i = i-1;
					break;
				}				
			}			
		}
	}
	
	private int min(double[] m){	
		return 0;    }
}
