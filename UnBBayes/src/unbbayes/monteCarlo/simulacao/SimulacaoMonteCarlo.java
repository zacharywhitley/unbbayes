package unbbayes.monteCarlo.simulacao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import unbbayes.prs.Node;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.util.NodeList;

/**
 * 
 * Classe que implementa o método de simulação de Monte Carlo
 * Por este método são gerados números aleatórios entre 0 e 1. Cada valor sorteado vai estar associada a uma
 * instancia dentro do universo representado pela rede bayseana. Esta associação é feito com base em um função
 * de densidade acumulada que representa a rede.
 * 
 * @author Danilo
 *
 */
public class SimulacaoMonteCarlo {
	
	private ProbabilisticNetwork pn;
	private int nCasos;
	private NodeList fila;
	
	
	/**
	 * Método que gera a simulação de Monte Carlo
	 * @param pn Rede a partir da qual serão gerados os casos
	 * @param nCasos numero de casos que seráo gerados
	 */
	public SimulacaoMonteCarlo(ProbabilisticNetwork pn , int nCasos){		
		this.pn = pn;
		this.nCasos = nCasos;	
		iniciar();				
	}
	
	private void iniciar(){
		fila = new NodeList();		
		criarFila();
		byte [][] matrizFila = new byte[nCasos][pn.getNodeCount()];		
		for(int i = 0; i < nCasos; i++){						
			simular(matrizFila, i);
		}
	}
	
	private void criarFila(){				
		boolean[] visitados = new boolean[pn.getNodeCount()];
		inicializaFila(visitados);											
		for(int i = 0; i < fila.size(); i++){
			Node n = fila.get(i);
			adicionaFila(n.getChildren(),visitados);			
		}		
	}
	
	private void inicializaFila(boolean[] visitados){
		for(int i = 0 ; i < pn.getNodeCount(); i++){
			if(pn.getNodeAt(i).getParents().size() == 0 ){
				visitados[i]= true ;					
				fila.add(pn.getNodeAt(i));
				
			}
		}			
	}
	
	private void adicionaFila(NodeList filhos,boolean[] visitados){
		for(int i = 0 ; i < filhos.size(); i++){
			Node n1 = filhos.get(i);
			for(int j = 0 ; j < pn.getNodeCount(); j++){
				Node n2 = pn.getNodeAt(j);
				if(n1.getName().equals(n2.getName())){
					if(!visitados[j]){
						fila.add(n1);						
						visitados[j] = true;						
						break;						
					}										
				}				
			}	
		}
		System.out.println();		
	}
	
	private void simular(byte[][] matrizFila, int caso){
		List indicesAnteriores = new ArrayList();
		double numero = Math.random();		
		double[] coluna;
		int[] estado = new int[fila.size()];
		for(int i = 0 ; i < fila.size(); i++){			
			ProbabilisticNode n = (ProbabilisticNode)fila.get(i);									
			indicesAnteriores = getIndices(n);
			coluna = getColuna(estado,indicesAnteriores,n);													
			estado[i] = getEstado(coluna);
			matrizFila[caso][i] = (byte)estado[i];
			//System.out.println("Estado "+ n.getDescription() + " = " + estado[i]); 						 						
		}				
	}
	
	private List getIndices(ProbabilisticNode n){
		List indices = new ArrayList();
		NodeList pais = n.getParents();		
		for(int i = 0 ; i < pais.size();i++){
			Node n1 = pais.get(i);
			indices.add(getIndiceFila(n1));						
		}	
		return indices;		
	}
	
	private Integer getIndiceFila(Node n){
		for(int i = 0 ; i <fila.size();i++){
			if(n.getName().equals(fila.get(i).getName())){				
				return new Integer(i);				
			}			
		}	
		return null;	
	}
	
	private int getEstado(double[] coluna){		
		double[][] faixa;
		double numero = Math.random();		
		faixa = criarFaixasIntervalo(coluna);			
		for(int i = 0; i< faixa.length; i++){
			if(i == 0){				
				if (numero <= faixa[i][1] || faixa[i][1] == 0.0 ){
					return i;										
				}
				continue;  				
			}else{				
				if(numero <= faixa[i][1] && numero > faixa[i][0]){
					return i;	
				}				
			}			
		}
		System.out.println("AKI  = " + numero);
		return -1;				
	}
	
	private double[][] criarFaixasIntervalo(double[] coluna){
		double[][] faixa = new double[coluna.length][2];		
		//double[] colunaOrdenada = ordenar(coluna);
		double atual = 0.0d;
		for(int i = 0 ; i < coluna.length; i++){
			faixa[i][0] = atual;
			//
			faixa[i][1] = coluna[i] + atual;			
			atual = faixa[i][1]; 
		}
		return faixa;
	}
	
	private double[] ordenar(double[] coluna){		
		List lista = new ArrayList();
		double[] colunaOrdenada = new double[coluna.length];	
		for(int i = 0 ; i < coluna.length; i++){
			lista.add(new Double(coluna[i]));				
		}
		Collections.sort(lista);
		for(int i = 0 ; i < lista.size(); i++){
			colunaOrdenada[i] = ((Double)lista.get(i)).doubleValue();				
		}				
		return colunaOrdenada;		
	}
	
	private double[]  getColuna(int[] estado,List indicesAnteriores, ProbabilisticNode n){
		PotentialTable pt = n.getPotentialTable();
		//System.out.println("Nomde Nó = "+ n.getDescription());
		int numeroEstados = n.getStatesSize();
		int indice;
		double[] coluna = new double[numeroEstados];
		int[] coordenadas = new int[indicesAnteriores.size()+1];
		NodeList parents = new NodeList();		
		for(int i = 0; i < n.getStatesSize(); i++){				
			coordenadas[0] = i;
			if(i == 0){
				for(int j = 0 ; j < indicesAnteriores.size(); j++){				
					indice = ((Integer)indicesAnteriores.get(j)).intValue();
					parents.add(fila.get(indice));
					coordenadas[j+1] = estado[indice];								
				}
			}			
			/*for(int k = 0 ; k < coordenadas.length ; k++){
				System.out.print("Coordenada "+ k +" = "+ coordenadas[k]);								
			}*/
			//System.out.println();
			/*try{
				
				if( i == 1 && coluna[i] == 0.0){
				  //System.out.println("Coluna "+ i + " = " + coluna[i]);				  
				}*s/coluna[i] = pt.getValue(getLinearCoord(coordenadas,parents));/* 	
			}catch(Exception e){
				for(int k = 0 ; k < coordenadas.length ; k++){
					  System.out.println(" Coordenada  "+ k +"  =  "+ coordenadas[k]);								
				}
				e.printStackTrace();
			}*/
			
			//			
		}		
		return coluna;
	}
	
	public  final int getLinearCoord(int coord[], NodeList parents) {
        int fatores[] = calcularFatores(parents);
        int coordLinear = 0;
        int sizeVariaveis = parents.size();
        for (int v = 0; v < sizeVariaveis; v++) {
            coordLinear += coord[v] * fatores[v];
            System.out.print("Coord = " + coord[v] + " Fator  = " + fatores[v]);
        }        
        System.out.println();
        return coordLinear;        
    }
    
	 protected int[] calcularFatores(NodeList variaveis) {		
		int sizeVariaveis = variaveis.size();
		int fatores[] = null;
		if (fatores == null || fatores.length < sizeVariaveis) {
			fatores = new int[sizeVariaveis];
		}
		if(fatores.length > 0 ){
			fatores[0] = 1;
		}
		Node auxNo;
		for (int i = 1; i < sizeVariaveis; i++) {
			 auxNo = variaveis.get(i-1);
			 fatores[i] = fatores[i-1] * auxNo.getStatesSize();
		}
		return fatores;
  }
	
	
}
