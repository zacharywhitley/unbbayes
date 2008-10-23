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
package unbbayes.simulation.montecarlo.sampling;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.Node;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;

/**
 * 
 * Class that implements the Monte Carlo simulation.
 * It uses forward sampling to calculate a RV's probability mass function. Based on its pmf, it then 
 * calculates the cumulative density function. Finally, a random number between 0 and 1 is generated 
 * and the sampled state is defined by the state the random number relates based on its cdf. 
 * 
 * @author Danilo Custodio
 * @author Rommel Carvalho
 *
 */
public class MonteCarloSampling {
	
	private ProbabilisticNetwork pn;
	private int nTrials;
	private ArrayList<Node> samplingNodeOrderQueue;
	
	
	/**
	 * Responsible for setting the initial variables for Monte Carlo simulation.
	 * @param pn Probabilistic network that will be used for sampling.
	 * @param nTrials Number of trials to generate.
	 */
	public MonteCarloSampling(ProbabilisticNetwork pn , int nTrials){		
		this.pn = pn;
		this.nTrials = nTrials;	
		//start();				
	}
	
	/**
	 * Generates the MC sample and return this sample as a matrix[i][j]. Row represents the sample case, 
	 * column represents the node, and the value at matrix[i][j] represents the state's node for this case.
	 * @return A matrix with the state for each node for each case of the sample.
	 */
	public byte[][] start(){
		samplingNodeOrderQueue = new ArrayList<Node>();		
		createSamplingOrderQueue();
		byte [][] sampledStatesMatrix = new byte[nTrials][pn.getNodeCount()];		
		for(int i = 0; i < nTrials; i++){						
			simulate(sampledStatesMatrix, i);
		}
		/*
		System.out.print("TRIAL");
		for (int i = 0; i < pn.getNodeCount(); i++) {
			System.out.print("	" + pn.getNodeAt(i).getName() + " - " + pn.getNodeAt(i).getDescription());
		}
		System.out.println();
		for (int i = 0; i < sampledStatesMatrix.length; i++) {
			System.out.print(i + " :" );
			for (int j = 0; j < sampledStatesMatrix[0].length; j++) {
				System.out.print("	" + pn.getNodeAt(j).getStateAt(sampledStatesMatrix[i][j]));
			}
			System.out.println();
		}
		*/
		return sampledStatesMatrix;
	}
	
	/**
	 * Creates the queue of the nodes that are going to be analyzed.
	 */
	private void createSamplingOrderQueue(){
		// Keeps track of the nodes that have already been added to the queue (nodeAddedList[nodeIndex]=true). 
		boolean[] nodeAddedList = new boolean[pn.getNodeCount()];
		initSamplingOrderQueue(nodeAddedList);											
		for(int i = 0; i < samplingNodeOrderQueue.size(); i++){
			Node node = samplingNodeOrderQueue.get(i);
			addToSamplingOrderQueue(node.getChildren(), nodeAddedList);			
		}		
	}
	
	/**
	 * Initializes the queue with the nodes that are root. In other words. 
	 * It will put in the queue the nodes that do not have parents.
	 * @param nodeAddedList Keeps track of the nodes that have already been added to the queue (nodeAddedList[nodeIndex]=true).
	 */
	private void initSamplingOrderQueue(boolean[] nodeAddedList){
		for(int i = 0 ; i < pn.getNodeCount(); i++){
			if(pn.getNodeAt(i).getParents().size() == 0 ){
				nodeAddedList[i]= true;					
				samplingNodeOrderQueue.add(pn.getNodeAt(i));
			}
		}			
	}
	
	/**
	 * Take the children of a node that have already been added to the queue. Analyze them
	 * one by one and add the child that is not in the queue yet. 
	 * @param children Children of a node that is already in the queue.
	 * @param nodeAddedList Nodes that have already been added to the queue.
	 */
	private void addToSamplingOrderQueue(ArrayList<Node> children, boolean[] nodeAddedList){
		for(int i = 0 ; i < children.size(); i++){
			Node n1 = children.get(i);
			for(int j = 0 ; j < pn.getNodeCount(); j++){
				Node n2 = pn.getNodeAt(j);
				if(n1.getName().equals(n2.getName())){
					if(!nodeAddedList[j]){
						samplingNodeOrderQueue.add(n1);						
						nodeAddedList[j] = true;						
						break;						
					}										
				}				
			}	
		}	
	}
	
	/**
	 * Responsible for simulating MC for sampling.
	 * @param sampledStatesMatrix The matrix containing the sampled states for every trial. 
	 * @param nTrial The trial number to simulate.
	 */
	private void simulate(byte[][] sampledStatesMatrix, int nTrial){
		List<Integer> parentsIndexes = new ArrayList<Integer>();
		double[] pmf;
		int[] sampledStates = new int[samplingNodeOrderQueue.size()];
		for(int i = 0 ; i < samplingNodeOrderQueue.size(); i++){			
			ProbabilisticNode node = (ProbabilisticNode)samplingNodeOrderQueue.get(i);									
			parentsIndexes = getParentsIndexesInQueue(node);
			pmf = getProbabilityMassFunction(sampledStates, parentsIndexes, node);													
			sampledStates[i] = getState(pmf);
			sampledStatesMatrix[nTrial][i] = (byte)sampledStates[i];
		}				
	}
	
	/**
	 * Return the indexes (sampling order) in the queue for the parents of a given node. 
	 * @param node The node to retrieve the parents for finding the indexes.
	 * @return List of indexes (sampling order) of a node's parents in the queue.
	 */
	private List<Integer> getParentsIndexesInQueue(ProbabilisticNode node){
		List<Integer> indexes = new ArrayList<Integer>();
		ArrayList<Node> parents = node.getParents();		
		for(int i = 0 ; i < parents.size();i++){
			Node parentNode = parents.get(i);
			indexes.add(getIndexInQueue(parentNode));						
		}	
		return indexes;		
	}
	
	/**
	 * Retrieves the node's index in the queue.  
	 * @param node
	 * @return
	 */
	private Integer getIndexInQueue(Node node){
		for(int i = 0 ; i <samplingNodeOrderQueue.size();i++){
			if(node.getName().equals(samplingNodeOrderQueue.get(i).getName())){				
				return i;				
			}			
		}	
		return null;	
	}
	
	/**
	 * Uses the pmf to retrieve the cdf to choose a state from a random generated number (between 0 and 1).
	 * @param pmf The probability mass function for the node RV that we want to sample the state for.
	 * @return The sampled state for a given RV (based on its pmf).
	 */
	private int getState(double[] pmf){
		// Cumulative distribution function
		double[][] cdf;
		double numero = Math.random();		
		cdf = getCumulativeDistributionFunction(pmf);
		for(int i = 0; i< cdf.length; i++){
			if(i == 0){				
				if (numero <= cdf[i][1]){
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
	
	/**
	 * Creates the cumulative distribution function (cdf) based on the node RV's pmf.
	 * @param pmf The probability mass function of the RV to calculate the cdf.
	 * @return The cumulative distribution function (cdf) for the given pmf.
	 */
	private double[][] getCumulativeDistributionFunction(double[] pmf){
		// Instead of using [statesSize][2] we could only use [statesSize]
		// and the upper value for the interval would be the lower value of 
		// the following state. In the last state the upper value would be 1.
		double[][] cdf = new double[pmf.length][2];		
		double atual = 0.0d;
		for(int i = 0 ; i < pmf.length; i++){
			// Lower value
			cdf[i][0] = atual;
			// Upper value
			cdf[i][1] = pmf[i] + atual;
			
			// Next lower value is equal to the previous upper value
			atual = cdf[i][1];
		}
		return cdf;
	}
	
	/**
	 * Creates the probability mass function based on the states sampled for the parents.
	 * @param sampledStates The states (sampledStates[nodeIndex]) sampled for the nodes (nodeIndex).
	 * @param parentsIndexes The nodeIndex for each parent.
	 * @param node The node/RV to calculate the pmf.
	 * @return The probability mass function (pmf) of the node RV.
	 */
	private double[]  getProbabilityMassFunction(int[] sampledStates, List<Integer> parentsIndexes, ProbabilisticNode node){
		PotentialTable pt = node.getPotentialTable();
		int statesSize = node.getStatesSize();
		int nodeIndex;
		double[] pmf = new double[statesSize];
		int[] coordinates = new int[parentsIndexes.size() + 1];
		ArrayList<Node> parents = new ArrayList<Node>();		
		for(int i = 0; i < node.getStatesSize(); i++){				
			coordinates[0] = i;
			if(i == 0){
				for(int j = 0 ; j < parentsIndexes.size(); j++){				
					nodeIndex = parentsIndexes.get(j);
					parents.add(samplingNodeOrderQueue.get(nodeIndex));
					coordinates[j + 1] = sampledStates[nodeIndex];								
				}
			}
			pmf[i] = pt.getValue(coordinates);
		}
		/*System.out.println("Node " + node.getName());
		for (int i = 0; i < pmf.length; i++) {
			System.out.print(node.getStateAt(i) + " = " + pmf[i] + " ");
		}
		System.out.println();*/
		return pmf;
	}	
	
}
