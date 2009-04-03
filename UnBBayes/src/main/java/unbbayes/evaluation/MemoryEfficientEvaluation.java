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
package unbbayes.evaluation;

import java.io.File;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import unbbayes.evaluation.exception.EvaluationException;
import unbbayes.io.BaseIO;
import unbbayes.io.NetIO;
import unbbayes.io.XMLBIFIO;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.simulation.montecarlo.sampling.IMonteCarloSampling;
import unbbayes.simulation.montecarlo.sampling.MapMonteCarloSampling;

public class MemoryEfficientEvaluation {

	// ------- VALUES THAT DO NOT CHANGE --------//

	private ProbabilisticNetwork net;
	
	private int sampleSize;

	// It will change when more than one target node is allowed
	private TreeVariable targetNode;

	// It will change when more than one target node is allowed
	private int targetStatesProduct;

	public static final float UNSET_VALUE = Float.NEGATIVE_INFINITY;

	// ------- VALUES THAT DO CHANGE FOR EACH SUB-EVALUATION --------//

	private TreeVariable[] targetNodeList;

	private TreeVariable[] evidenceNodeList;

	private int statesProduct;

	private int evidenceStatesProduct;

	private List<Node> positionNodeList;

	private int[] positionTargetNodeList;

	private int[] positionEvidenceNodeList;

	// -------- OUTPUT VALUES - EVALUATION --------//

	private float[][] evidenceSetCM;

	private float evidenceSetPCC = MemoryEfficientEvaluation.UNSET_VALUE;

	private List<EvidenceEvaluation> evidenceEvaluationList;
	
	public List<EvidenceEvaluation> getEvidenceEvaluationList() {
		return this.evidenceEvaluationList;
	}

	public float[][] getEvidenceSetCM() {
		return this.evidenceSetCM;
	}
	
	public int getSampleSize() {
		return this.sampleSize;
	}
	
	public float getError() {
		return (float) (1/Math.sqrt(this.sampleSize));
	}

	public float getEvidenceSetPCC() throws EvaluationException {
		if (evidenceSetPCC == MemoryEfficientEvaluation.UNSET_VALUE) {
			if (evidenceSetCM == null) {
				throw new EvaluationException(
						"Must calculate evidence set LCM before computing evidence set PCC.");
			}
			evidenceSetPCC = 0;
			for (int i = 0; i < evidenceSetCM.length; i++) {
				evidenceSetPCC += evidenceSetCM[i][i];
			}
			evidenceSetPCC /= evidenceSetCM.length;
		}
		return this.evidenceSetPCC;
	}
	
	public List<EvidenceEvaluation> getBestIndividualPCC() throws EvaluationException {
		List<EvidenceEvaluation> sortedList = new ArrayList<EvidenceEvaluation>();
		sortedList.addAll(evidenceEvaluationList);
		boolean change = true;
		while (change) {
			change = false;
			for (int i = 0; i < sortedList.size() - 1; i++) {
				EvidenceEvaluation ev1 = sortedList.get(i);
				EvidenceEvaluation ev2 = sortedList.get(i + 1);
				if (ev1.getIndividualPCC() - ev2.getIndividualPCC() < 0) {
					sortedList.set(i + 1, ev1);
					sortedList.set(i, ev2);
					change = true;
				}
			}
		}
		return sortedList;
	}
	
	public List<EvidenceEvaluation> getBestIndividualCostRate() throws EvaluationException {
		List<EvidenceEvaluation> sortedList = new ArrayList<EvidenceEvaluation>();
		sortedList.addAll(evidenceEvaluationList);
		boolean change = true;
		while (change) {
			change = false;
			for (int i = 0; i < sortedList.size() - 1; i++) {
				EvidenceEvaluation ev1 = sortedList.get(i);
				EvidenceEvaluation ev2 = sortedList.get(i + 1);
				if (ev1.getCostRate() - ev2.getCostRate() < 0) {
					sortedList.set(i + 1, ev1);
					sortedList.set(i, ev2);
					change = true;
				}
			}
		}
		return sortedList;
	}
	
	public List<EvidenceEvaluation> getBestMarginalImprovement() throws EvaluationException {
		List<EvidenceEvaluation> sortedList = new ArrayList<EvidenceEvaluation>();
		sortedList.addAll(evidenceEvaluationList);
		boolean change = true;
		while (change) {
			change = false;
			for (int i = 0; i < sortedList.size() - 1; i++) {
				EvidenceEvaluation ev1 = sortedList.get(i);
				EvidenceEvaluation ev2 = sortedList.get(i + 1);
				if (ev1.getMarginalImprovement() - ev2.getMarginalImprovement() < 0) {
					sortedList.set(i + 1, ev1);
					sortedList.set(i, ev2);
					change = true;
				}
			}
		}
		return sortedList;
	}
	
	public void evaluate(String netFileName, List<String> targetNodeNameList,
			List<String> evidenceNodeNameList, int sampleSize, boolean onlyGCM) throws Exception {
		loadNetwork(netFileName);
		evaluate(targetNodeNameList, evidenceNodeNameList, sampleSize, onlyGCM);
	}

	public void evaluate(ProbabilisticNetwork net,
			List<String> targetNodeNameList, List<String> evidenceNodeNameList,
			int sampleSize, boolean onlyGCM) throws Exception {
		this.net = net;
		evaluate(targetNodeNameList, evidenceNodeNameList, sampleSize, onlyGCM);
	}

	private void evaluate(List<String> targetNodeNameList,
			List<String> evidenceNodeNameList, int sampleSize, boolean onlyGCM) throws Exception {
		
		this.sampleSize = sampleSize;

		evidenceSetCM = computeApproximateCM(targetNodeNameList, evidenceNodeNameList);
		// As the set LCM was computed, its PCC is now unset (lazy computing)
		evidenceSetPCC = MemoryEfficientEvaluation.UNSET_VALUE;
		
		if (!onlyGCM) {
			evidenceEvaluationList = new ArrayList<EvidenceEvaluation>();
			for (String evidenceName : evidenceNodeNameList) {
				EvidenceEvaluation evidenceEvaluation = new EvidenceEvaluation(evidenceName, getEvidenceSetPCC());
				
				// Compute individual LCM
				List<String> tempList = new ArrayList<String>();
				tempList.add(evidenceName);
				evidenceEvaluation.setLCM(computeApproximateCM(targetNodeNameList, tempList));
				
				// Compute marginal LCM
				tempList.clear();
				tempList.addAll(evidenceNodeNameList);
				tempList.remove(evidenceName);
				evidenceEvaluation.setMarginalCM(computeApproximateCM(targetNodeNameList, tempList));
				
				evidenceEvaluationList.add(evidenceEvaluation);
			}
		}

	}
	
	public void evaluate(String netFileName, List<String> targetNodeNameList,
			List<String> evidenceNodeNameList, boolean onlyGCM) throws Exception {
		loadNetwork(netFileName);
		evaluate(targetNodeNameList, evidenceNodeNameList, onlyGCM);
	}

	public void evaluate(ProbabilisticNetwork net,
			List<String> targetNodeNameList, List<String> evidenceNodeNameList, boolean onlyGCM) throws Exception {
		this.net = net;
		evaluate(targetNodeNameList, evidenceNodeNameList, onlyGCM);
	}

	private void evaluate(List<String> targetNodeNameList,
			List<String> evidenceNodeNameList, boolean onlyGCM) throws Exception {
		
		evidenceSetCM = computeExactCM(targetNodeNameList, evidenceNodeNameList);
		// As the set LCM was computed, its PCC is now unset (lazy computing)
		evidenceSetPCC = MemoryEfficientEvaluation.UNSET_VALUE;
		
		if (!onlyGCM) {
			evidenceEvaluationList = new ArrayList<EvidenceEvaluation>();
			for (String evidenceName : evidenceNodeNameList) {
				EvidenceEvaluation evidenceEvaluation = new EvidenceEvaluation(evidenceName, getEvidenceSetPCC());
				
				// Compute individual LCM
				List<String> tempList = new ArrayList<String>();
				tempList.add(evidenceName);
				evidenceEvaluation.setLCM(computeExactCM(targetNodeNameList, tempList));
				
				// Compute marginal LCM
				tempList.clear();
				tempList.addAll(evidenceNodeNameList);
				tempList.remove(evidenceName);
				evidenceEvaluation.setMarginalCM(computeExactCM(targetNodeNameList, tempList));
				
				evidenceEvaluationList.add(evidenceEvaluation);
			}
		}

	}

	private float[][] computeApproximateCM(List<String> targetNodeNameList,
			List<String> evidenceNodeNameList) throws Exception {

		init(targetNodeNameList, evidenceNodeNameList);

		// 1. Generate the MC sample from the network file
		// Trial# StateIndexForNode1 StateIndexForNode2 StateIndexForNodeJ NumerTimesSampled
		// 001 0 1 0 10
		// 002 2 0 1 35
		// ...
		// i x y z nTimes
		IMonteCarloSampling mc = new MapMonteCarloSampling();
		long init = System.currentTimeMillis();
		mc.start(net, sampleSize);
		long end = System.currentTimeMillis();
		System.out.println("Time elapsed for sampling: " + (float)(end-init)/1000);
		Set<Map.Entry<Integer,Integer>> sampledSet = mc.getSampledStatesMap().entrySet();
		System.out.println("Sample size: " + sampleSize);
		System.out.println("Sample map size: " + sampledSet.size());
//		show(sampleMatrix);
		
		init = System.currentTimeMillis();

		// FIXME For now let's just consider the simple case of having just one
		// target node!
		targetNode = targetNodeList[0];
		if (targetNodeList.length != 1) {
			throw new Exception("For now, just one target node is accepted!");
		}

		// Get the position in the sampled matrix of target and evidence nodes
		positionTargetNodeList = new int[targetNodeList.length];
		positionEvidenceNodeList = new int[evidenceNodeList.length];

		// Position of the nodes in the sampled matrix.
		positionNodeList = mc.getSamplingNodeOrderQueue();

		for (int i = 0; i < positionTargetNodeList.length; i++) {
			positionTargetNodeList[i] = positionNodeList.indexOf((Node)targetNodeList[i]);
		}

		for (int i = 0; i < positionEvidenceNodeList.length; i++) {
			positionEvidenceNodeList[i] = positionNodeList.indexOf((Node)evidenceNodeList[i]);
		}
		
		// 2. Count # of occurrences of evidence nodes given target nodes
		Map<Integer, Integer> frequencyEvidenceGivenTargetMap = new HashMap<Integer, Integer>();
		Map<Integer, Integer> frequencyTargetMap = new HashMap<Integer, Integer>();
		
		for (Entry<Integer, Integer> entry : sampledSet) {
			int value = entry.getValue(); 
			int[] dim = getMultidimensionalCoord(mc.getMultidimensionalCoord(entry.getKey()));
			Integer previousValue = frequencyTargetMap.get(dim[0]);
			if (previousValue != null) {
				frequencyTargetMap.put(dim[0], previousValue + value);
			} else {
				frequencyTargetMap.put(dim[0], value);
			}
			int key = getLinearCoord(dim);
			previousValue = frequencyEvidenceGivenTargetMap.get(key);
			if (previousValue != null) {
				frequencyEvidenceGivenTargetMap.put(key, previousValue + value);
			} else {
				frequencyEvidenceGivenTargetMap.put(key, value);
			}
			
		}

		// 3. Compute probabilities for evidence nodes given target nodes
		Map<Integer, Float> postProbEvidenceGivenTargetMap = new HashMap<Integer, Float>();
		Set<Map.Entry<Integer, Integer>> frequencyEvidenceGivenTargetSet = frequencyEvidenceGivenTargetMap.entrySet();
		float[][] postProbEvidenceGivenTarget = new float[evidenceStatesProduct][targetNode.getStatesSize()];
		for (Entry<Integer, Integer> entry : frequencyEvidenceGivenTargetSet) {
			float value = entry.getValue(); 
			int key = entry.getKey();
			int[] dim = getMultidimensionalCoord(key);
			float prob = value/frequencyTargetMap.get(dim[0]);
			postProbEvidenceGivenTargetMap.put(key, prob);
			postProbEvidenceGivenTarget[getEvidenceLinearCoord(dim)][dim[0]] = prob;
		}
//		show(postProbEvidenceGivenTarget);
		
		// 4. Compute probabilities for target given evidence using evidence
		// given target
		// P(T|E) = P(E|T)P(T)
		Map<Integer, Float> postProbTargetGivenEvidenceMap = new HashMap<Integer, Float>();
		Map<Integer, Float> normalizationMap = new HashMap<Integer, Float>();
		Set<Map.Entry<Integer, Float>> postProbEvidenceGivenTargetSet = postProbEvidenceGivenTargetMap.entrySet();
		net.compile();
		for (Entry<Integer, Float> entry : postProbEvidenceGivenTargetSet) {
			int key = entry.getKey();
			int[] dim = getMultidimensionalCoord(key);
			float prob = entry.getValue() * targetNode.getMarginalAt(dim[0]);
			postProbTargetGivenEvidenceMap.put(getLinearCoord(dim), prob);
			Float previousProb = normalizationMap.get(getEvidenceLinearCoord(dim));
			if (previousProb != null) {
				normalizationMap.put(getEvidenceLinearCoord(dim), previousProb + prob);
			} else {
				normalizationMap.put(getEvidenceLinearCoord(dim), prob);
			}
		}
		
		Set<Map.Entry<Integer, Float>> postProbTargetGivenEvidenceSet = postProbTargetGivenEvidenceMap.entrySet();
		float[][] postProbTargetGivenEvidence = new float[targetNode.getStatesSize()][evidenceStatesProduct];
		for (Entry<Integer, Float> entry : postProbTargetGivenEvidenceSet) {
			int key = entry.getKey();
			int[] dim = getMultidimensionalCoord(key);
			float prob = entry.getValue() / normalizationMap.get(getEvidenceLinearCoord(dim));
			entry.setValue(prob);
			postProbTargetGivenEvidence[dim[0]][getEvidenceLinearCoord(dim)] = prob;
		}
//		show(postProbTargetGivenEvidence);
		
		// 5. Compute probabilities for target given target and set as CM
		// P(T|T) = P(T|E)P(E|T)
		int N = targetNode.getStatesSize();
		float[][] CM = new float[N][N];
		// ikj pure row
        //long start = System.currentTimeMillis(); 
        for (int i = 0; i < N; i++) {
            float[] arowi = postProbTargetGivenEvidence[i];
            float[] crowi = CM[i];
            for (int k = 0; k < evidenceStatesProduct; k++) {
                float[] browk = postProbEvidenceGivenTarget[k];
                float aik = arowi[k];
                for (int j = 0; j < N; j++) {
                    crowi[j] += aik * browk[j];
                }
            }
        }

        end = System.currentTimeMillis();
		System.out.println("Time elapsed for computing CM: " + (float)(end-init)/1000);

		return CM;
	}

	private float[] getExatProbTargetGivenEvidence() throws Exception {
		// TODO for now I am just considering there is one target node!
		TreeVariable targetNode = targetNodeList[0];

		net.compile();

		float[] postProbList = new float[statesProduct];

		int sProd = targetNode.getStatesSize();

		byte[][] stateCombinationMatrix = new byte[statesProduct][1 + evidenceNodeList.length];
		int state = 0;
		for (int row = 0; row < statesProduct; row++) {
			stateCombinationMatrix[row][0] = (byte) (row / (statesProduct / sProd));
			for (int j = 0; j < evidenceNodeList.length; j++) {
				sProd *= evidenceNodeList[j].getStatesSize();
				state = (row / (statesProduct / sProd))
						% evidenceNodeList[j].getStatesSize();
				evidenceNodeList[j].addFinding(state);
				stateCombinationMatrix[row][j + 1] = (byte) state;
			}
			sProd = targetNode.getStatesSize();
			try {
				net.updateEvidences();
				postProbList[row] = targetNode
						.getMarginalAt(stateCombinationMatrix[row][0]);
			} catch (Exception e) {
				postProbList[row] = 0;
			}
			net.compile();
		}

		return postProbList;
	}

	private float[][] computeExactCM(List<String> targetNodeNameList, List<String> evidenceNodeNameList) throws Exception {
		
		init(targetNodeNameList, evidenceNodeNameList);

		// FIXME For now let's just consider the simple case of having just one
		// target node!
		targetNode = targetNodeList[0];
		if (targetNodeList.length != 1) {
			throw new Exception("For now, just one target node is accepted!");
		}

		float [][] postTGivenE = new float[targetNode.getStatesSize()][evidenceStatesProduct];
		float [][] postEGivenT = new float[evidenceStatesProduct][targetNode.getStatesSize()];
		for (int row = 0; row < statesProduct; row++) {
			// It has the state of target/evidence node at index i (first nodes are target then evidence)
			int [] states = getMultidimensionalCoord(row);
			int indexTarget = states[0];
			int indexEvidence = getEvidenceLinearCoord(states); //row - (indexTarget * evidenceStatesProduct);
			// P(T|E)
			float probTGivenE = getProbTargetGivenEvidence(states);
			postTGivenE[indexTarget][indexEvidence] = probTGivenE;
			// P(E)
			int [] evidencesStates = new int[states.length - 1];
			for (int i = 0; i < evidencesStates.length; i++) {
				evidencesStates[i] = states[i+1];
			}
			float probE = getEvidencesJointProbability(evidencesStates);
			// P(T)
			float probT = getTargetPriorProbability(states[0]);
			// P(E|T) = P(T|E)P(E)/P(T)
			float probEGivenT = probTGivenE * probE / probT;
			postEGivenT[indexEvidence][indexTarget] = probEGivenT;
			
		}
		
		// Compute probabilities for target given target and set as CM
		// P(T|T) = P(T|E)P(E|T)
		int N = targetNode.getStatesSize();
		float[][] CM = new float[N][N];
		// ikj pure row
        //long start = System.currentTimeMillis(); 
        for (int i = 0; i < N; i++) {
            float[] arowi = postTGivenE[i];
            float[] crowi = CM[i];
            for (int k = 0; k < evidenceStatesProduct; k++) {
                float[] browk = postEGivenT[k];
                float aik = arowi[k];
                for (int j = 0; j < N; j++) {
                    crowi[j] += aik * browk[j];
                }
            }
        }
        //long stop = System.currentTimeMillis();
        //float elapsed = (stop - start) / 1000.0f;
        //System.out.println("Order ikj pure row:   " + elapsed + " seconds");
        //show(postTGivenE);
        //show(postEGivenT);
        //show(CM);
		
		return CM;
	}
	
	private float getProbTargetGivenEvidence(int[] states) throws Exception {
		
		net.compile();
		// Add findings
		for (int i = 0; i < evidenceNodeList.length; i++) {
			// The evidence state index is its index plus one, because the target node is the first index.
			evidenceNodeList[i].addFinding(states[1 + i]);
		}
		
		try {
			net.updateEvidences();
			return targetNode.getMarginalAt(states[0]);
		} catch (Exception e) {
			return 0;
		}
		
	}
	
	private float getEvidencesJointProbability(int states[]) throws Exception {
		net.compile();

		float prob = 1;

		for (int j = 0; j < evidenceNodeList.length; j++) {
			
			if (j > 0) {
				evidenceNodeList[j-1].addFinding(states[j-1]);
			}
			
			try {
				net.updateEvidences();
				prob *= evidenceNodeList[j].getMarginalAt(states[j]);
			} catch (Exception e) {
				return 0;
			}
			
		}

		return prob;
		
	}
	
	private float[] getEvidencesJointProbability() throws Exception {
		net.compile();

		float[] jointProbability = new float[evidenceStatesProduct];

		int sProd = 1;

		// TODO - Rommel - Remove the matrix afterwards!!
		//byte[][] stateCombinationMatrix = new byte[evidenceStatesProduct][evidenceNodeList.length];
		int stateCurrentNode = 0;
		int statePreviousNode = 0;
		for (int row = 0; row < evidenceStatesProduct; row++) {
			jointProbability[row] = 1;
			for (int j = 0; j < evidenceNodeList.length; j++) {
				sProd *= evidenceNodeList[j].getStatesSize();
				stateCurrentNode = (row / (evidenceStatesProduct / sProd))
						% evidenceNodeList[j].getStatesSize();
				
				//stateCombinationMatrix[row][j] = (byte) state;
				//for (int k = j - 1; k < 0; k--) {
				if (j > 0) {
					evidenceNodeList[j-1].addFinding(statePreviousNode);
				}
				
				try {
					net.updateEvidences();
					jointProbability[row] *= evidenceNodeList[j]
							.getMarginalAt(stateCurrentNode);
				} catch (Exception e) {
					jointProbability[row] = 0;
				}
				
				statePreviousNode = stateCurrentNode;
			}
			sProd = 1;
			net.compile();
		}

		//printProbMatrix(stateCombinationMatrix, jointProbability);
		return jointProbability;
		
	}
	
	private float getTargetPriorProbability(int state) throws Exception {
		net.compile();

		return targetNode.getMarginalAt(state);
	}
	
	private float[] getTargetPriorProbability() throws Exception {
		net.compile();

		float[] priorProb = new float[targetNode.getStatesSize()];
		for (int i = 0; i < targetNode.getStatesSize(); i++) {
			priorProb[i] = targetNode.getMarginalAt(i);
		}
		
		return priorProb;
	}

	protected int[] factors;
	
	/**
	 * Calculate the factors necessary to transform the linear coordinate into a multidimensional 
	 * one (which is the the state for each possible node - target and evidence).
	 * FactorForNode[i + 1] = ProductOf(NumberOfStates[i]), for all previous nodes (i).
	 */
	protected void computeFactors() {
		int size = targetNodeList.length + evidenceNodeList.length;
		if (factors == null || factors.length != size) {
		   factors = new int[size];
		}
		
		// Create one list of all nodes
		TreeVariable[] nodes = new TreeVariable[size];
		int nodeIndex = 0;
		for (int i = 0; i < targetNodeList.length; i++) {
			nodes[nodeIndex++] = targetNodeList[i];
		}
		for (int i = 0; i < evidenceNodeList.length; i++) {
			nodes[nodeIndex++] = evidenceNodeList[i];
		}
		
		factors[0] = 1;
		Node node;
		for (int i = 1; i < size; i++) {
			node = nodes[i-1];
			factors[i] = factors[i-1] * node.getStatesSize();
		}
	}
	
	/**
	 * Get the linear coordinate from the multidimensional one.
	 * LinearCoord = SumOf(StateOf[i] * FactorOf[i]), for all 
	 * possible nodes (i).
	 * 
	 * @param multidimensionalCoord Multidimensional coordinate (represented by the state for
	 * each node).
	 * @return The corresponding linear coordinate.
	 */
	protected final int getLinearCoord(int multidimensionalCoord[]) {
		computeFactors();
		int coordLinear = 0;
		int size = targetNodeList.length + evidenceNodeList.length;
		for (int v = 0; v < size; v++) {
			coordLinear += multidimensionalCoord[v] * factors[v];
		}
		return coordLinear;
	}
	
	protected final int getEvidenceLinearCoord(int multidimensionalCoord[]) {
		computeFactors();
		int coordLinear = 0;
		int size = targetNodeList.length + evidenceNodeList.length;
		for (int v = 1; v < size; v++) {
			coordLinear += multidimensionalCoord[v] * factors[v]/factors[1];
		}
		return coordLinear;
	}

	/**
	 * Get the multidimensional coordinate from the linear one.
	 * 
	 * @param linearCoord The linear coordinate.
	 * @return The corresponding multidimensional coordinate.
	 */
	protected final int[] getMultidimensionalCoord(int linearCoord) {
		computeFactors();
		int factorI;
		int size = targetNodeList.length + evidenceNodeList.length;
		int multidimensionalCoord[] = new int[size];
		int i = size - 1;
		while (linearCoord != 0) {
			factorI = factors[i];
			multidimensionalCoord[i--] = linearCoord / factorI;
			linearCoord %= factorI;
		}
		return multidimensionalCoord;
	}
	
	/**
	 * Get the multidimensional evaluation coordinate from the monte carlo
	 * multidimensional coordinate.
	 * 
	 * @param dim Monte carlo multidimensional coordinate.
	 * @return The corresponding multidimensional coordinate.
	 */
	protected final int[] getMultidimensionalCoord(byte[] dim) {
		int size = targetNodeList.length + evidenceNodeList.length;
		int multidimensionalCoord[] = new int[size];
		for (int i = 0; i < positionTargetNodeList.length; i++) {
			multidimensionalCoord[i] = dim[positionTargetNodeList[i]];
		}
		for (int i = 0; i < positionEvidenceNodeList.length; i++) {
			multidimensionalCoord[i + positionTargetNodeList.length] = dim[positionEvidenceNodeList[i]];
		}
		return multidimensionalCoord;
	}

	private void init(List<String> targetNodeNameList,
			List<String> evidenceNodeNameList) {
		targetNodeList = new TreeVariable[targetNodeNameList.size()];
		evidenceNodeList = new TreeVariable[evidenceNodeNameList.size()];
		statesProduct = 1;
		targetStatesProduct = 1;
		evidenceStatesProduct = 1;

		// Create list of target TreeVariable
		int count = 0;
		for (String targetNodeName : targetNodeNameList) {
			Node targetNode = net.getNode(targetNodeName);

			targetNodeList[count] = (TreeVariable) targetNode;

			targetStatesProduct *= targetNode.getStatesSize();
			count++;
		}

		// Create list of evidence TreeVariable
		count = 0;
		for (String evidenceNodeName : evidenceNodeNameList) {
			Node evidenceNode = net.getNode(evidenceNodeName);

			evidenceNodeList[count] = (TreeVariable) evidenceNode;

			evidenceStatesProduct *= evidenceNode.getStatesSize();
			count++;
		}

		statesProduct = targetStatesProduct * evidenceStatesProduct;
	}

	private void loadNetwork(String netFileName) {
		File netFile = new File(netFileName);
		String fileExt = netFileName.substring(netFileName.length() - 3);

		try {
			BaseIO io = null;
			if (fileExt.equalsIgnoreCase("xml")) {
				io = new XMLBIFIO();
			} else if (fileExt.equalsIgnoreCase("net")) {
				io = new NetIO();
			} else {
				throw new Exception(
						"The network must be in XMLBIF 0.5 or NET format!");
			}
			net = io.load(netFile);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void main(String[] args) throws Exception {
		
		boolean runSmallTest = false;
		boolean runApproximate = true;
		boolean runExact = false;
		boolean onlyGCM = true;
		
		List<String> targetNodeNameList = new ArrayList<String>();
		List<String> evidenceNodeNameList = new ArrayList<String>();
		String netFileName = "";
		if (runSmallTest) {
			targetNodeNameList = new ArrayList<String>();
			targetNodeNameList.add("Springler");
	
			evidenceNodeNameList = new ArrayList<String>();
			evidenceNodeNameList.add("Cloudy");
			evidenceNodeNameList.add("Rain");
			evidenceNodeNameList.add("Wet");
	
			netFileName = "src/test/resources/testCases/evaluation/WetGrass.xml";
		} else {
			targetNodeNameList = new ArrayList<String>();
			targetNodeNameList.add("TargetType");
	
			evidenceNodeNameList = new ArrayList<String>();
			evidenceNodeNameList.add("UHRR_Confusion");
			evidenceNodeNameList.add("ModulationFrequency");
			evidenceNodeNameList.add("CenterFrequency");
			evidenceNodeNameList.add("PRI");
			evidenceNodeNameList.add("PRF");
	
			netFileName = "src/test/resources/testCases/evaluation/AirID.xml";
		}

		// APPROXIMATE //
		if (runApproximate) {
			
			int sampleSize = 0;
			if (runSmallTest) {
				sampleSize = 100000;
			} else {
				sampleSize = 50000000;
			}
			
			MemoryEfficientEvaluation evaluationApproximate = new MemoryEfficientEvaluation();
			evaluationApproximate.evaluate(netFileName, targetNodeNameList,
					evidenceNodeNameList, sampleSize, onlyGCM);
			
			System.out.println("----TOTAL------");
			
			System.out.println("LCM:\n");
			show(evaluationApproximate.getEvidenceSetCM());
			System.out.println("\n");
			
			System.out.println("PCC: ");
			System.out.printf("%2.2f\n", evaluationApproximate.getEvidenceSetPCC() * 100);
			
			if (!onlyGCM) {
				System.out.println("\n\n\n");
				System.out.println("----MARGINAL------");
				System.out.println("\n\n");
				
				
				List<EvidenceEvaluation> list = evaluationApproximate.getBestMarginalImprovement();
				
				for (EvidenceEvaluation evidenceEvaluation : list) {
					
					System.out.println("-" + evidenceEvaluation.getName() + "-");
					System.out.println("\n\n");
					
					System.out.println("LCM:\n");
					show(evidenceEvaluation.getMarginalCM());
					
					System.out.println("\n");
					
					System.out.println("PCC: ");
					System.out.printf("%2.2f\n", evidenceEvaluation.getMarginalPCC() * 100);
					
					System.out.println("\n");
					
					System.out.println("Marginal Improvement: ");
					System.out.printf("%2.2f\n", evidenceEvaluation.getMarginalImprovement() * 100);
					
					System.out.println("\n\n");
				}
				
				System.out.println("\n");
				System.out.println("----INDIVIDUAL PCC------");
				System.out.println("\n\n");
				
				list = evaluationApproximate.getBestIndividualPCC();
				
				for (EvidenceEvaluation evidenceEvaluation : list) {
					
					System.out.println("-" + evidenceEvaluation.getName() + "-");
					System.out.println("\n\n");
					
					System.out.println("LCM:\n");
					show(evidenceEvaluation.getIndividualLCM());
					
					System.out.println("\n");
					
					System.out.println("PCC: ");
					System.out.printf("%2.2f\n", evidenceEvaluation.getIndividualPCC() * 100);
					
					System.out.println("\n\n");
					
					// Add random costs for each
					evidenceEvaluation.setCost((new Random()).nextFloat() * 1000);
				}
				
				System.out.println("\n");
				System.out.println("----INDIVIDUAL PCC------");
				System.out.println("\n\n");
				
				list = evaluationApproximate.getBestIndividualCostRate();
				
				for (EvidenceEvaluation evidenceEvaluation : list) {
					
					System.out.println("-" + evidenceEvaluation.getName() + "-");
					System.out.println("\n\n");
					
					System.out.println("PCC: ");
					System.out.printf("%2.2f\n", evidenceEvaluation.getIndividualPCC() * 100);
					
					System.out.println("\n");
					
					System.out.println("Cost: ");
					System.out.printf("%2.2f\n", evidenceEvaluation.getCost());
					
					System.out.println("\n");
					
					System.out.println("Cost Rate: ");
					System.out.printf("%2.2f\n", evidenceEvaluation.getCostRate() * 100);
					
					System.out.println("\n\n");
				}
			}

			// EXACT //
			if (runExact) {
				MemoryEfficientEvaluation evaluationExact = new MemoryEfficientEvaluation();
				evaluationExact.evaluate(netFileName, targetNodeNameList,
						evidenceNodeNameList, onlyGCM);
				
				System.out.println("----TOTAL------");
				
				System.out.println("LCM:\n");
				show(evaluationExact.getEvidenceSetCM());
				
				System.out.println("\n");
				
				System.out.println("PCC: ");
				System.out.printf("%2.2f\n", evaluationExact.getEvidenceSetPCC() * 100);
				
				if (!onlyGCM) {
					System.out.println("\n\n\n");
					System.out.println("----MARGINAL------");
					System.out.println("\n\n");
					
					List<EvidenceEvaluation> list = evaluationExact.getBestMarginalImprovement();
					
					for (EvidenceEvaluation evidenceEvaluation : list) {
						
						System.out.println("-" + evidenceEvaluation.getName() + "-");
						System.out.println("\n\n");
						
						System.out.println("LCM:\n");
						show(evidenceEvaluation.getMarginalCM());
						
						System.out.println("\n");
						
						System.out.println("PCC: ");
						System.out.printf("%2.2f\n", evidenceEvaluation.getMarginalPCC() * 100);
						
						System.out.println("\n");
						
						System.out.println("Marginal Improvement: ");
						System.out.printf("%2.2f\n", evidenceEvaluation.getMarginalImprovement() * 100);
						
						System.out.println("\n\n");
					}
					
					System.out.println("\n");
					System.out.println("----INDIVIDUAL PCC------");
					System.out.println("\n\n");
					
					list = evaluationExact.getBestIndividualPCC();
					
					for (EvidenceEvaluation evidenceEvaluation : list) {
						
						System.out.println("-" + evidenceEvaluation.getName() + "-");
						System.out.println("\n\n");
						
						System.out.println("LCM:\n");
						show(evidenceEvaluation.getIndividualLCM());
						
						System.out.println("\n");
						
						System.out.println("PCC: ");
						System.out.printf("%2.2f\n", evidenceEvaluation.getIndividualPCC() * 100);
						
						System.out.println("\n\n");
						
						// Add random costs for each
						evidenceEvaluation.setCost((new Random()).nextFloat() * 1000);
					}
					
					System.out.println("\n");
					System.out.println("----INDIVIDUAL PCC------");
					System.out.println("\n\n");
					
					list = evaluationExact.getBestIndividualCostRate();
					
					for (EvidenceEvaluation evidenceEvaluation : list) {
						
						System.out.println("-" + evidenceEvaluation.getName() + "-");
						System.out.println("\n\n");
						
						System.out.println("PCC: ");
						System.out.printf("%2.2f\n", evidenceEvaluation.getIndividualPCC() * 100);
						
						System.out.println("\n");
						
						System.out.println("Cost: ");
						System.out.printf("%2.2f\n", evidenceEvaluation.getCost());
						
						System.out.println("\n");
						
						System.out.println("Cost Rate: ");
						System.out.printf("%2.2f\n", evidenceEvaluation.getCostRate() * 100);
						
						System.out.println("\n\n");
					}
				}
			}
		}
		
		

	}
	
	public static void show(float[][] a) {
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                System.out.printf("%6.4f ", a[i][j]);
            }
            System.out.println();
        }
        System.out.println();
    }
	
	public static void show(int[][] a) {
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                System.out.print(a[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }
	
	private String printArray(int[] array) {
		String str = "";
		for (int i = 0; i < array.length; i++) {
			str += " " + array[i];
		}
		return str;
	}
	
	public static void printMatrix(float[][] matrix, Formatter formatter) {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				System.out.printf("%2.2f ", matrix[i][j] * 100);
			}
			System.out.println("\n");
		}
	}
}
