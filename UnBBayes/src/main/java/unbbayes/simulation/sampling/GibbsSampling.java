/*
 *  UnBBayes
 *  Copyright (C) 2002, 2009 Universidade de Brasilia - http://www.unb.br
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
package unbbayes.simulation.sampling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import unbbayes.prs.Graph;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.simulation.likelihoodweighting.ILikelihoodWeightingSampling;
import unbbayes.simulation.likelihoodweighting.sampling.LikelihoodWeightingSampling;
import unbbayes.simulation.montecarlo.sampling.MatrixMonteCarloSampling;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;

/**
 * 
 * Likelihood Weighting sampling based on MC sampling. However, it does not 
 * sample for the evidence nodes, it just sets as the given state, and it 
 * calculates P(E|Par(E)) for each trial. 
 * 
 * @author Rommel Carvalho (rommel.carvalho@gmail.com)
 *
 */
// TODO ROMMEL - CREATE ONE SAMPLING THAT USES MAPMCSAMPLING
public class GibbsSampling extends MatrixMonteCarloSampling implements IInferenceAlgorithm {
	
	protected ProbabilisticNetwork pn;
	protected int sampleSize;
	
	protected List<Node> evidenceNodeList;
	
	protected Map<Node, Float[]> marginalMap;
	
	/** Load resource file from util */
  	private static ResourceBundle resource = ResourceBundle.getBundle(
  			unbbayes.util.resources.UtilResources.class.getName());
	
	public GibbsSampling() {
		this.sampleSize = 100000;
	}

	public GibbsSampling(ProbabilisticNetwork pn,
			int sampleSize) {
		this.setNetwork(pn);
		this.sampleSize = sampleSize;
	}

	@Override
	/**
	 * Responsible for setting the initial variables for Likelihood Weighting.
	 * Besides sampling (like MC), it calculates P(E|Par(E)) for each trial.
	 * @param pn Probabilistic network that will be used for sampling.
	 * @param nTrials Number of trials to generate.
	 */
	public void start(ProbabilisticNetwork pn , int nTrials){
		this.setNetwork(pn);
		this.sampleSize = nTrials;
		this.marginalMap = new HashMap<Node, Float[]>();
		Float[] marginal;
		for (Node node : pn.getNodes()) {
			marginal = new Float[node.getStatesSize()];
			for (int i = 0; i < marginal.length; i++) {
				marginal[i] = 0.0f;
			}
			marginalMap.put(node, marginal);
		}
		super.start(pn, nTrials);
		normalizeMarginals(nTrials);
	}
	
	private void normalizeMarginals(int nTrials) {
		float[] marginal;
		for (int i = 0; i < samplingNodeOrderQueue.size(); i++) {
			TreeVariable node = (TreeVariable)samplingNodeOrderQueue.get(i);
			if (!node.hasEvidence()) {
				marginal = new float[node.getStatesSize()];
				for (int j = 0; j < marginal.length; j++) {
					marginal[j] = marginalMap.get(node)[j] / nTrials;
				}
				node.initMarginalList();
				node.addLikeliHood(marginal);
			}
		}
	}

	@Override
	/**
	 * It does the same thing as the MC sampling, but now it calculates P(E|Par(E)) \
	 * for each trial. 
	 */
	protected void simulate(int nTrial) {
		double[] pmf;
		
		int nodeIndex = nTrial % samplingNodeOrderQueue.size();
		ProbabilisticNode node = (ProbabilisticNode)samplingNodeOrderQueue.get(nodeIndex);
		
		// Define sampledStates
		int[] sampledStates = new int[pn.getNodeCount()];
		// Use Likelihood Weighting for the first trial (guess)
		if (nTrial == 0) {
			ILikelihoodWeightingSampling lw = new LikelihoodWeightingSampling();
			lw.start(pn, 1);
			for (int i = 0; i < sampledStates.length; i++) {
				sampledStates[i] = lw.getSampledStatesMatrix()[0][i];
			}
		// Use previous sampledStates for every other trial
		} else {
			for (int i = 0; i < sampledStates.length; i++) {
				sampledStates[i] = sampledStatesMatrix[nTrial-1][i];
			}
			
			// Compute pmf and sample state of the nth node (one node sampled per trial - in order)
			if (!node.hasEvidence()) {
				pmf = getProbabilityMassFunction(sampledStates, getParentsIndexesInQueue(node), getChildrenIndexesInQueue(node), node);
				
				sampledStates[nodeIndex] = getState(pmf);
				
				for (int j = 0; j < pmf.length; j++) {
					marginalMap.get(node)[j] += (float)pmf[j];
				}
			// If it is an evidence node, then we do not need to sample it.
			} else {
				sampledStates[nodeIndex] = node.getEvidence();
			}
			
		}
		
		for(int i = 0 ; i < samplingNodeOrderQueue.size(); i++){
			// For every other non-evidence node, compute and 
			// store its pmf (to compute expectation) based on the new sampledStates
			node = (ProbabilisticNode)samplingNodeOrderQueue.get(i);
			if (nTrial == 0 || (i != nodeIndex && !node.hasEvidence())) {							
				pmf = getProbabilityMassFunction(sampledStates, getParentsIndexesInQueue(node), getChildrenIndexesInQueue(node), node);
				
				for (int j = 0; j < pmf.length; j++) {
					marginalMap.get(node)[j] += (float)pmf[j];
				}
			}
			// For every node, stores the sampled state
			sampledStatesMatrix[nTrial][i] = (byte)sampledStates[i];
		}	
		
	}
	
	/**
	 * Creates the probability mass function based on the markov blanket of the given node.
	 * @param sampledStates The states (sampledStates[nodeIndex]) sampled for the nodes (nodeIndex).
	 * @param parentsIndexes The index for each parent of node.
	 * @param childrenIndexes The index for each child of node.
	 * @param node The node/RV to calculate the pmf.
	 * @return The probability mass function (pmf) of the node RV, given its markov blanket.
	 */
	protected double[] getProbabilityMassFunction(int[] sampledStates,
			List<Integer> parentsIndexes, List<Integer> childrenIndexes, ProbabilisticNode node) {
		int[] sampledStatesCopy = new int[sampledStates.length];
		System.arraycopy(sampledStates, 0, sampledStatesCopy, 0, sampledStates.length);
		int statesSize = node.getStatesSize();
		int childIndex;
		int nodeIndex = getIndexInQueue(node);
		double[] pmf = new double[statesSize];
		double[] pmfNode = new double[statesSize];
		pmfNode = getProbabilityMassFunction(sampledStatesCopy, parentsIndexes, node);
		System.arraycopy(pmfNode, 0, pmf, 0, pmfNode.length);
		ProbabilisticNode child;
		double probChild;
		for (int j = 0; j < childrenIndexes.size(); j++) {
			childIndex = childrenIndexes.get(j);
			child = (ProbabilisticNode)samplingNodeOrderQueue.get(childIndex);
			for (int i = 0; i < node.getStatesSize(); i++) {
				sampledStatesCopy[nodeIndex] = i;
				probChild = getProbabilityMassFunction(sampledStatesCopy, getParentsIndexesInQueue(child), child)[sampledStatesCopy[childIndex]];
				pmf[i] *= probChild;
			}
		}
		normalize(pmf);
		/*
		 * System.out.println("Node " + node.getName()); for (int i = 0; i <
		 * pmf.length; i++) { System.out.print(node.getStateAt(i) + " = " +
		 * pmf[i] + " "); } System.out.println();
		 */
		return pmf;
	}
	
	protected void normalize(double[] doubleList) {
		
		double total = 0;
		for (int i = 0; i < doubleList.length; i++) {
			total += doubleList[i];
		}
		
		for (int i = 0; i < doubleList.length; i++) {
			doubleList[i] /= total;
		}
		
	}
	
	/**
	 * Return the indexes (sampling order) in the queue for the children of a given node. 
	 * @param node The node to retrieve the children for finding the indexes.
	 * @return List of indexes (sampling order) of a node's children in the queue.
	 */
	protected List<Integer> getChildrenIndexesInQueue(ProbabilisticNode node) {
		List<Integer> indexes = new ArrayList<Integer>();
		ArrayList<Node> children = node.getChildren();		
		for(int i = 0 ; i < children.size();i++){
			Node childNode = children.get(i);
			indexes.add(getIndexInQueue(childNode));						
		}	
		return indexes;		
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#run()
	 */
	public void run() {
		start(pn, sampleSize);
	}


	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#setNetwork(unbbayes.prs.Graph)
	 */
	public void setNetwork(Graph g) throws IllegalArgumentException {
		this.pn = (ProbabilisticNetwork)g;
	}
	
	
	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#getDescription()
	 */
	public String getDescription() {
		return this.resource.getString("gibbsAlgorithmDescription");
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#getName()
	 */
	public String getName() {
		return this.resource.getString("gibbsAlgorithmName");
	}

	/**
	 * @return the sampleSize
	 */
	public int getSampleSize() {
		return sampleSize;
	}

	/**
	 * @param sampleSize the sampleSize to set
	 */
	public void setSampleSize(int sampleSize) {
		this.sampleSize = sampleSize;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#reset()
	 */
	public void reset() {
		this.pn.resetEvidences();
		this.run();
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#propagate()
	 */
	public void propagate() {
		this.run();
	}
	
	
	

}
