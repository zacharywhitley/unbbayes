package unbbayes.monteCarlo.simulacao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import unbbayes.prs.Node;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.util.NodeList;

/**
 * @author Danilo
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class SimulacaoMonteCarlo {
	
	private ProbabilisticNetwork pn;
	private int nCasos;
	private NodeList fila;
	
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
			System.out.println("Estado "+ n.getDescription() + " = " + estado[i]); 						 						
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
		System.out.println("Numero Randomico = "+ numero);
		faixa = criarFaixasIntervalo(coluna);			
		for(int i = 0; i< coluna.length; i++){
			if(i ==0){
				if (numero <= faixa[i][1] && numero >= faixa[i][0]){
					return i;					
				}  				
			}else{
				if(numero <= faixa[i][1] && numero > faixa[i][0]){
					return i;	
				}
			}			
		}
		return -1;				
	}
	
	private double[][] criarFaixasIntervalo(double[] coluna){
		double[][] faixa = new double[coluna.length][2];		
		double[] colunaOrdenada = ordenar(coluna);
		double atual = 0.0d;
		for(int i = 0 ; i < coluna.length; i++){
			faixa[i][0] = atual;
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
		for(int i = 0; i < n.getStatesSize(); i++){
			int[] coordenadas = new int[indicesAnteriores.size()+1];	
			coordenadas[0] = i;
			for(int j = 0 ; j < indicesAnteriores.size(); j++){				
				indice = ((Integer)indicesAnteriores.get(j)).intValue();
				coordenadas[j+1] = estado[indice];								
			}
			for(int k = 0 ; k < coordenadas.length ; k++){
				System.out.print("Coordenada "+ i +" = "+ coordenadas[k]);								
			}
			System.out.println();	
			coluna[i] = pt.getValue(coordenadas);
			System.out.println("Coluna "+ i + " = " + coluna[i]);			
		}
		return coluna;
	}
}
