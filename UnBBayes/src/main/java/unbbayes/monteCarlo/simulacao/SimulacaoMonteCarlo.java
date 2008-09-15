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
package unbbayes.monteCarlo.simulacao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import unbbayes.prs.Node;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;

/**
 * 
 * Classe que implementa o metodo de simulacao de Monte Carlo
 * Por este metodo sao gerados numeros aleatorios entre 0 e 1. Cada valor sorteado vai estar associada a uma
 * instancia dentro do universo representado pela rede bayseana. Esta associacao e feita com base em uma funcao
 * de densidade acumulada que representa a rede.
 * 
 * @author Danilo Custodio
 *
 */
public class SimulacaoMonteCarlo {
	
	private ProbabilisticNetwork pn;
	private int nCasos;
	private ArrayList<Node> fila;
	
	
	/**
	 * Metodo que gera a simulacao de Monte Carlo
	 * @param pn Rede a partir da qual serao gerados os casos
	 * @param nCasos Numero de casos que serao gerados
	 */
	public SimulacaoMonteCarlo(ProbabilisticNetwork pn , int nCasos){		
		this.pn = pn;
		this.nCasos = nCasos;	
		iniciar();				
	}
	
	private void iniciar(){
		fila = new ArrayList<Node>();		
		criarFila();
		byte [][] matrizFila = new byte[nCasos][pn.getNodeCount()];		
		for(int i = 0; i < nCasos; i++){						
			simular(matrizFila, i);
		}
		
		System.out.print("CASE");
		for (int i = 0; i < pn.getNodeCount(); i++) {
			System.out.print("	" + pn.getNodeAt(i).getName() /*+ " - " + pn.getNodeAt(i).getDescription()*/);
		}
		System.out.println();
		for (int i = 0; i < matrizFila.length; i++) {
			System.out.print(i + " :" );
			for (int j = 0; j < matrizFila[0].length; j++) {
				System.out.print("	" + pn.getNodeAt(j).getStateAt(matrizFila[i][j]));
			}
			System.out.println();
		}
		
	}
	
	/**
	 * Creates the queue of the nodes that are going to be analyzed.
	 */
	private void criarFila(){				
		boolean[] visitados = new boolean[pn.getNodeCount()];
		inicializaFila(visitados);											
		for(int i = 0; i < fila.size(); i++){
			Node node = fila.get(i);
			adicionaFila(node.getChildren(), visitados);			
		}		
	}
	
	/**
	 * Initializes the queue with the nodes that are root. In other words. 
	 * It will put in the queue the nodes that do not have parents.
	 * @param visitados Contains the nodes that were already added to the queue.
	 */
	private void inicializaFila(boolean[] visitados){
		for(int i = 0 ; i < pn.getNodeCount(); i++){
			if(pn.getNodeAt(i).getParents().size() == 0 ){
				visitados[i]= true;					
				fila.add(pn.getNodeAt(i));
			}
		}			
	}
	
	/**
	 * Take the children of a node that have already been added to the queue. Analyze them
	 * one by one and add the child that is not in the queue yet. 
	 * @param filhos Children of a node that is already in the queue.
	 * @param visitados Nodes that have already been added to the queue.
	 */
	private void adicionaFila(ArrayList<Node> filhos, boolean[] visitados){
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
		List<Integer> parentsIndexes = new ArrayList<Integer>();
		double[] column;
		int[] estado = new int[fila.size()];
		for(int i = 0 ; i < fila.size(); i++){			
			ProbabilisticNode node = (ProbabilisticNode)fila.get(i);									
			parentsIndexes = getParentsIndicesInQueue(node);
			// It seems we can optimize this. It always give the same result.
			column = getColumn(estado, parentsIndexes, node);													
			estado[i] = getState(column);
			matrizFila[caso][i] = (byte)estado[i];
		}				
	}
	
	/**
	 * Return the indexes in the queue for the parents of a given node. 
	 * @param node The node to retrieve the parents for finding the indexes.
	 * @return List of indexes of a node's parents in the queue.
	 */
	private List<Integer> getParentsIndicesInQueue(ProbabilisticNode node){
		List<Integer> indices = new ArrayList<Integer>();
		ArrayList<Node> parents = node.getParents();		
		for(int i = 0 ; i < parents.size();i++){
			Node node1 = parents.get(i);
			indices.add(getIndexInQueue(node1));						
		}	
		return indices;		
	}
	
	/**
	 * Retrieves the node's index in the queue.  
	 * @param node
	 * @return
	 */
	private Integer getIndexInQueue(Node node){
		for(int i = 0 ; i <fila.size();i++){
			if(node.getName().equals(fila.get(i).getName())){				
				return i;				
			}			
		}	
		return null;	
	}
	
	private int getState(double[] column){
		// Cumulative distribution function
		double[][] cdf;
		double numero = Math.random();		
		cdf = createCumulativeDistributionFunction(column);
		for(int i = 0; i< cdf.length; i++){
			if(i == 0){				
				if (numero <= cdf[i][1] || cdf[i][1] == 0.0 ){
					return i;										
				}
				continue;  				
			}else{				
				if(numero <= cdf[i][1] && numero > cdf[i][0]){
					return i;	
				}				
			}			
		}
		return -1;				
	}
	
	private double[][] createCumulativeDistributionFunction(double[] coluna){
		// Instead of using [statesSize][2] we could only use [statesSize]
		// and the upper value for the interval would be the lower value of 
		// the following state. In the last state the upper value would be 1.
		double[][] cdf = new double[coluna.length][2];		
		double atual = 0.0d;
		for(int i = 0 ; i < coluna.length; i++){
			// Lower value
			cdf[i][0] = atual;
			// Upper value
			cdf[i][1] = coluna[i] + atual;
			
			// Next lower value is equal to the previous upper value
			atual = cdf[i][1];
		}
		return cdf;
	}
	
	private double[]  getColumn(int[] estado, List<Integer> parentsIndexes, ProbabilisticNode node){
		PotentialTable pt = node.getPotentialTable();
		int statesSize = node.getStatesSize();
		int index;
		double[] column = new double[statesSize];
		int[] coordinates = new int[parentsIndexes.size() + 1];
		ArrayList<Node> parents = new ArrayList<Node>();		
		for(int i = 0; i < node.getStatesSize(); i++){				
			coordinates[0] = i;
			if(i == 0){
				for(int j = 0 ; j < parentsIndexes.size(); j++){				
					index = parentsIndexes.get(j);
					parents.add(fila.get(index));
					coordinates[j + 1] = estado[index];								
				}
			}
			column[i] = pt.getValue(coordinates);
		}
		/*System.out.println("Node " + node.getName());
		for (int i = 0; i < column.length; i++) {
			System.out.print(node.getStateAt(i) + " = " + column[i] + " ");
		}
		System.out.println();*/
		return column;
	}	
	
}
