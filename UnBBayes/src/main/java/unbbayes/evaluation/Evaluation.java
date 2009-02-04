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
import java.util.List;
import java.util.Locale;
import java.util.Random;

import unbbayes.evaluation.exception.EvaluationException;
import unbbayes.io.BaseIO;
import unbbayes.io.NetIO;
import unbbayes.io.XMLBIFIO;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.simulation.montecarlo.sampling.MonteCarloSampling;

public class Evaluation {

	// ------- VALUES THAT DO NOT CHANGE --------//

	private ProbabilisticNetwork net;
	
	private int sampleSize;

	// It will change when more than one target node is allowed
	private TreeVariable targetNode;

	// It will change when more than one target node is allowed
	private int targetStatesProduct;

	public static final float UNSET_VALUE = Float.NEGATIVE_INFINITY;

	private Formatter formatter;

	// ------- VALUES THAT DO CHANGE FOR EACH SUB-EVALUATION --------//

	private TreeVariable[] targetNodeList;

	private TreeVariable[] evidenceNodeList;

	private int statesProduct;

	private int evidenceStatesProduct;

	private byte[][] sampleMatrix;
	
	private List<Node> positionNodeList;

	private int[] positionTargetNodeList;

	private int[] positionEvidenceNodeList;

	// -------- OUTPUT VALUES - EVALUATION --------//

	private float[][] evidenceSetCM;

	private float evidenceSetPCC = Evaluation.UNSET_VALUE;

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
		if (evidenceSetPCC == Evaluation.UNSET_VALUE) {
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
			List<String> evidenceNodeNameList, int sampleSize) throws Exception {
		loadNetwork(netFileName);
		evaluate(targetNodeNameList, evidenceNodeNameList, sampleSize);
	}

	public void evaluate(ProbabilisticNetwork net,
			List<String> targetNodeNameList, List<String> evidenceNodeNameList,
			int sampleSize) throws Exception {
		this.net = net;
		evaluate(targetNodeNameList, evidenceNodeNameList, sampleSize);
	}

	private void evaluate(List<String> targetNodeNameList,
			List<String> evidenceNodeNameList, int sampleSize) throws Exception {
		
		this.sampleSize = sampleSize;

		evidenceSetCM = computeApproximateCM(targetNodeNameList, evidenceNodeNameList);
		// As the set LCM was computed, its PCC is now unset (lazy computing)
		evidenceSetPCC = Evaluation.UNSET_VALUE;
		
		evidenceEvaluationList = new ArrayList<EvidenceEvaluation>();
		for (String evidenceName : evidenceNodeNameList) {
			EvidenceEvaluation evidenceEvaluation = new EvidenceEvaluation(evidenceName);
			
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
	
	public void evaluate(String netFileName, List<String> targetNodeNameList,
			List<String> evidenceNodeNameList) throws Exception {
		loadNetwork(netFileName);
		evaluate(targetNodeNameList, evidenceNodeNameList);
	}

	public void evaluate(ProbabilisticNetwork net,
			List<String> targetNodeNameList, List<String> evidenceNodeNameList) throws Exception {
		this.net = net;
		evaluate(targetNodeNameList, evidenceNodeNameList);
	}

	private void evaluate(List<String> targetNodeNameList,
			List<String> evidenceNodeNameList) throws Exception {
		
		evidenceSetCM = computeExactCM(targetNodeNameList, evidenceNodeNameList);
		// As the set LCM was computed, its PCC is now unset (lazy computing)
		evidenceSetPCC = Evaluation.UNSET_VALUE;
		
		evidenceEvaluationList = new ArrayList<EvidenceEvaluation>();
		for (String evidenceName : evidenceNodeNameList) {
			EvidenceEvaluation evidenceEvaluation = new EvidenceEvaluation(evidenceName);
			
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

	private float[][] computeApproximateCM(List<String> targetNodeNameList,
			List<String> evidenceNodeNameList) throws Exception {

		init(targetNodeNameList, evidenceNodeNameList);

		// 1. Generate the MC sample from the network file
		// Trial# StateIndexForNode1 StateIndexForNode2 StateIndexForNodeJ
		// 001 0 1 0
		// 002 2 0 1
		// ...
		// i x y z
		MonteCarloSampling mc = new MonteCarloSampling(net, sampleSize);
		mc.start();
		sampleMatrix = mc.getSampledStatesMatrix();

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
		int[] frequencyEvidenceGivenTargetList = new int[statesProduct];
		int[] frequencyEvidenceList = new int[targetStatesProduct];

		// Iterate over all cases in the MC sample
		for (int i = 0; i < sampleMatrix.length; i++) {
			// Row to compute the frequency
			int row = 0;
			int currentStatesProduct = evidenceStatesProduct;
			for (int j = positionTargetNodeList.length - 1; j >= 0; j--) {
				byte state = sampleMatrix[i][positionTargetNodeList[j]];
				row += state * currentStatesProduct;
				currentStatesProduct *= positionNodeList.get(positionTargetNodeList[j]).getStatesSize();
			}

			// Add to total frequency for evidence nodes independent of state
			frequencyEvidenceList[(int) (row / evidenceStatesProduct)]++;

			currentStatesProduct = evidenceStatesProduct;
			for (int j = 0; j < positionEvidenceNodeList.length; j++) {
				currentStatesProduct /= positionNodeList.get(
						positionEvidenceNodeList[j]).getStatesSize();
				byte state = sampleMatrix[i][positionEvidenceNodeList[j]];
				row += state * currentStatesProduct;
			}

			// Add to total frequency for specific evidences
			frequencyEvidenceGivenTargetList[row]++;
		}

		// 3. Compute probabilities for evidence nodes given target nodes
		float[] postProbEvidenceGivenTarget = new float[statesProduct];
		for (int i = 0; i < postProbEvidenceGivenTarget.length; i++) {
			float n = (float) frequencyEvidenceList[(int) (i / evidenceStatesProduct)];
			if (n != 0) {
				postProbEvidenceGivenTarget[i] = (float) frequencyEvidenceGivenTargetList[i]
						/ n;
			}
		}

		// 4. Compute probabilities for target given evidence using evidence
		// given target
		// P(T|E) = P(E|T)P(T)
		float[] postProbTargetGivenEvidence = new float[statesProduct];
		int row = 0;
		float prob = 0.0f;
		float[] normalizationList = new float[evidenceStatesProduct];
		net.compile();
		for (int i = 0; i < targetNode.getStatesSize(); i++) {
			for (int j = 0; j < evidenceStatesProduct; j++) {
				row = j + i * evidenceStatesProduct;
				prob = postProbEvidenceGivenTarget[row]
						* targetNode.getMarginalAt(i);
				postProbTargetGivenEvidence[row] = prob;
				normalizationList[j] += prob;
			}
		}

		float norm = 0;
		for (int i = 0; i < postProbTargetGivenEvidence.length; i++) {
			norm = normalizationList[i % evidenceStatesProduct];
			if (norm != 0) {
				postProbTargetGivenEvidence[i] /= norm;
			}
		}

		// 5. Compute probabilities for target given target
		// P(T|T) = P(T|E)P(E|T)
		float[] postProbTargetGivenTarget = new float[(int) Math.pow(targetNode
				.getStatesSize(), 2)];
		int statesSize = targetNode.getStatesSize();
		row = 0;
		int index = 0;
		System.out.println("Approximate");
		for (int i = 0; i < statesProduct; i++) {
			System.out.println("P(T|E) " + printArray(getMultidimensionalCoord(i)) + postProbTargetGivenEvidence[i]);
			System.out.println("P(E|T) " + printArray(getMultidimensionalCoord(i)) + postProbEvidenceGivenTarget[i]);
			for (int j = 0; j < statesSize; j++) {
				row = ((int) (i / evidenceStatesProduct)) * statesSize + j;
				index = (i % evidenceStatesProduct) + j * evidenceStatesProduct;
				postProbTargetGivenTarget[row] += postProbTargetGivenEvidence[i]
						* postProbEvidenceGivenTarget[index];
			}
		}

		// 6. Set CM
		float[][] CM = new float[statesSize][statesSize];
		for (int i = 0; i < statesSize; i++) {
			for (int j = 0; j < statesSize; j++) {
				CM[i][j] = postProbTargetGivenTarget[i * statesSize + j];
			}
		}

//		System.out.println(getLCMLog(postProbTargetGivenEvidence,
//				postProbEvidenceGivenTarget, postProbTargetGivenTarget));

		return CM;
	}

	private String getLCMLog(float[] postProbTargetGivenEvidence,
			float[] postProbEvidenceGivenTarget,
			float[] postProbTargetGivenTarget) {
		StringBuilder sb = new StringBuilder();
		// Send all output to the appendable object sb
		formatter = new Formatter(sb, Locale.US);

		formatter.format("P(T|E) = N[ P(E|T)P(T) ]\n");
		for (int i = 0; i < targetStatesProduct; i++) {
			for (int j = 0; j < evidenceStatesProduct; j++) {
				formatter.format("%2.2f	", postProbTargetGivenEvidence[i
						* evidenceStatesProduct + j] * 100);
			}
			formatter.format("\n");
		}

		formatter.format("\n");

		formatter.format("P(E|T)\n");
		for (int i = 0; i < evidenceStatesProduct; i++) {
			for (int j = 0; j < targetStatesProduct; j++) {
				formatter.format("%2.2f	", postProbEvidenceGivenTarget[j
						* evidenceStatesProduct + i] * 100);
			}
			formatter.format("\n");
		}

		formatter.format("\n");

		formatter.format("P(T|T) = P(T|E)P(E|T)\n");
		int statesSize = targetNode.getStatesSize();
		for (int i = 0; i < statesSize; i++) {
			for (int j = 0; j < statesSize; j++) {
				formatter.format("%2.2f	", postProbTargetGivenTarget[i
						* statesSize + j] * 100);
			}
			formatter.format("\n");
		}

		return sb.toString();
	}

	public class EvidenceEvaluation {

		private String name;

		private float cost = Evaluation.UNSET_VALUE;

		// Individual probability of correct classification
		private float individualPCC = Evaluation.UNSET_VALUE;

		// Individual local confusion matrix
		private float[][] LCM;

		// Probability of correct classification of the evidence set without
		// this evidence
		private float marginalPCC = Evaluation.UNSET_VALUE;

		// Local confusion matrix of the evidence set without this evidence
		private float[][] marginalCM;

		// The evidence set PCC minus the setPCC (PCC of the set without this
		// evidence)
		private float marginalImprovement = Evaluation.UNSET_VALUE;

		// Individual PCC divided by its cost
		private float costRate = Evaluation.UNSET_VALUE;

		public EvidenceEvaluation(String name) {
			this.name = name;
		}

		public EvidenceEvaluation(String name, float cost) {
			this(name);
			this.cost = cost;
		}

		public float getIndividualPCC() throws EvaluationException {
			if (individualPCC == Evaluation.UNSET_VALUE) {
				if (LCM == null) {
					throw new EvaluationException(
							"Must calculate individual LCM before computing individual PCC.");
				}
				individualPCC = 0;
				for (int i = 0; i < LCM.length; i++) {
					individualPCC += LCM[i][i];
				}
				individualPCC /= LCM.length;
			}
			return individualPCC;
		}

		public float getMarginalPCC() throws EvaluationException {
			if (marginalPCC == Evaluation.UNSET_VALUE) {
				if (marginalCM == null) {
					throw new EvaluationException(
							"Must calculate marginal LCM before computing marginal PCC.");
				}
				marginalPCC = 0;
				for (int i = 0; i < marginalCM.length; i++) {
					marginalPCC += marginalCM[i][i];
				}
				marginalPCC /= marginalCM.length;
			}
			return marginalPCC;
		}

		public float getMarginalImprovement() throws EvaluationException {
			if (marginalImprovement == Evaluation.UNSET_VALUE) {
				marginalImprovement = getEvidenceSetPCC() - getMarginalPCC();
			}
			return marginalImprovement;
		}

		public float getCostRate() throws EvaluationException {
			if (costRate == Evaluation.UNSET_VALUE) {
				if (cost == Evaluation.UNSET_VALUE
						|| individualPCC == Evaluation.UNSET_VALUE) {
					throw new EvaluationException(
							"Must set cost and calculate individual PCC before computing cost rate.");
				}
				costRate = individualPCC / cost;
			}
			return costRate;
		}

		public float getCost() {
			return cost;
		}

		public void setCost(float cost) {
			this.cost = cost;
		}

		public float[][] getIndividualLCM() {
			return LCM;
		}

		public void setLCM(float[][] LCM) {
			this.LCM = LCM;
		}

		public float[][] getMarginalCM() {
			return marginalCM;
		}

		public void setMarginalCM(float[][] marginalCM) {
			this.marginalCM = marginalCM;
		}

		public String getName() {
			return name;
		}

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

		printProbMatrix(stateCombinationMatrix, postProbList);
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

		// First column is the P(T|E) and the second is P(E|T) 
		float[][] postProbList = new float[statesProduct][2];


		for (int row = 0; row < statesProduct; row++) {
			// It has the state of target/evidence node at index i (first nodes are target then evidence)
			int [] states = getMultidimensionalCoord(row);
			// P(T|E)
			float probTGivenE = getProbTargetGivenEvidence(states);
			postProbList[row][0] = probTGivenE;
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
			postProbList[row][1] = probEGivenT;
		}
		
		// Compute probabilities for target given target
		// P(T|T) = P(T|E)P(E|T)
		float[] postProbTargetGivenTarget = new float[(int) Math.pow(targetNode
				.getStatesSize(), 2)];
		int statesSize = targetNode.getStatesSize();
		int row = 0;
		int index = 0;
		System.out.println("Exact");
		for (int i = 0; i < statesProduct; i++) {
			System.out.println("P(T|E) " + printArray(getMultidimensionalCoord(i)) + postProbList[i][0]);
			System.out.println("P(E|T) " + printArray(getMultidimensionalCoord(i)) + postProbList[i][1]);
			for (int j = 0; j < statesSize; j++) {
				row = ((int) (i / evidenceStatesProduct)) * statesSize + j;
				index = (i % evidenceStatesProduct) + j * evidenceStatesProduct;
				postProbTargetGivenTarget[row] += postProbList[i][0]
						* postProbList[index][1];
			}
		}

		// Set CM
		float[][] CM = new float[statesSize][statesSize];
		for (int i = 0; i < statesSize; i++) {
			for (int j = 0; j < statesSize; j++) {
				CM[i][j] = postProbTargetGivenTarget[i * statesSize + j];
			}
		}
		
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

	private void printProbMatrix(byte[][] stateCombinationMatrix,
			float[] postProbList) {

		for (int i = 0; i < stateCombinationMatrix.length; i++) {
			for (int j = 0; j < stateCombinationMatrix[0].length; j++) {
				System.out.print(stateCombinationMatrix[i][j] + "    ");
			}
			System.out.println(postProbList[i]);
		}

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

		List<String> targetNodeNameList = new ArrayList<String>();
		targetNodeNameList.add("Rain");

		List<String> evidenceNodeNameList = new ArrayList<String>();
		evidenceNodeNameList.add("Springler");
		evidenceNodeNameList.add("Cloudy");
		evidenceNodeNameList.add("Wet");

		String netFileName = "../UnBBayes/examples/xml-bif/WetGrass_XMLBIF5.xml";

		int sampleSize = 100000;

		Evaluation evaluationApproximate = new Evaluation();
		evaluationApproximate.evaluate(netFileName, targetNodeNameList,
				evidenceNodeNameList, sampleSize);
		Evaluation evaluationExact = new Evaluation();
		evaluationExact.evaluate(netFileName, targetNodeNameList,
				evidenceNodeNameList);
		
		StringBuilder sb = new StringBuilder();
		// Send all output to the appendable object sb
		Formatter formatter = new Formatter(sb, Locale.US);
		
		formatter.format("----TOTAL------");
		formatter.format("\n\n");
		
		formatter.format("LCM:\n");
		printMatrix(evaluationApproximate.getEvidenceSetCM(), formatter);
		printMatrix(evaluationExact.getEvidenceSetCM(), formatter);
		
		formatter.format("\n");
		
		formatter.format("PCC: ");
		formatter.format("%2.2f\n", evaluationApproximate.getEvidenceSetPCC() * 100);
		formatter.format("%2.2f\n", evaluationExact.getEvidenceSetPCC() * 100);
		
		formatter.format("\n\n\n");
		formatter.format("----MARGINAL------");
		formatter.format("\n\n");
		
		// APPROXIMATE //
		
		List<EvidenceEvaluation> list = evaluationApproximate.getBestMarginalImprovement();
		
		for (EvidenceEvaluation evidenceEvaluation : list) {
			
			formatter.format("-" + evidenceEvaluation.getName() + "-");
			formatter.format("\n\n");
			
			formatter.format("LCM:\n");
			printMatrix(evidenceEvaluation.getMarginalCM(), formatter);
			
			formatter.format("\n");
			
			formatter.format("PCC: ");
			formatter.format("%2.2f\n", evidenceEvaluation.getMarginalPCC() * 100);
			
			formatter.format("\n");
			
			formatter.format("Marginal Improvement: ");
			formatter.format("%2.2f\n", evidenceEvaluation.getMarginalImprovement() * 100);
			
			formatter.format("\n\n");
		}
		
		formatter.format("\n");
		formatter.format("----INDIVIDUAL PCC------");
		formatter.format("\n\n");
		
		list = evaluationApproximate.getBestIndividualPCC();
		
		for (EvidenceEvaluation evidenceEvaluation : list) {
			
			formatter.format("-" + evidenceEvaluation.getName() + "-");
			formatter.format("\n\n");
			
			formatter.format("LCM:\n");
			printMatrix(evidenceEvaluation.getIndividualLCM(), formatter);
			
			formatter.format("\n");
			
			formatter.format("PCC: ");
			formatter.format("%2.2f\n", evidenceEvaluation.getIndividualPCC() * 100);
			
			formatter.format("\n\n");
			
			// Add random costs for each
			evidenceEvaluation.setCost((new Random()).nextFloat() * 1000);
		}
		
		formatter.format("\n");
		formatter.format("----INDIVIDUAL PCC------");
		formatter.format("\n\n");
		
		list = evaluationApproximate.getBestIndividualCostRate();
		
		for (EvidenceEvaluation evidenceEvaluation : list) {
			
			formatter.format("-" + evidenceEvaluation.getName() + "-");
			formatter.format("\n\n");
			
			formatter.format("PCC: ");
			formatter.format("%2.2f\n", evidenceEvaluation.getIndividualPCC() * 100);
			
			formatter.format("\n");
			
			formatter.format("Cost: ");
			formatter.format("%2.2f\n", evidenceEvaluation.getCost());
			
			formatter.format("\n");
			
			formatter.format("Cost Rate: ");
			formatter.format("%2.2f\n", evidenceEvaluation.getCostRate() * 100);
			
			formatter.format("\n\n");
		}
		
		// EXACT //
		
		list = evaluationExact.getBestMarginalImprovement();
		
		for (EvidenceEvaluation evidenceEvaluation : list) {
			
			formatter.format("-" + evidenceEvaluation.getName() + "-");
			formatter.format("\n\n");
			
			formatter.format("LCM:\n");
			printMatrix(evidenceEvaluation.getMarginalCM(), formatter);
			
			formatter.format("\n");
			
			formatter.format("PCC: ");
			formatter.format("%2.2f\n", evidenceEvaluation.getMarginalPCC() * 100);
			
			formatter.format("\n");
			
			formatter.format("Marginal Improvement: ");
			formatter.format("%2.2f\n", evidenceEvaluation.getMarginalImprovement() * 100);
			
			formatter.format("\n\n");
		}
		
		formatter.format("\n");
		formatter.format("----INDIVIDUAL PCC------");
		formatter.format("\n\n");
		
		list = evaluationExact.getBestIndividualPCC();
		
		for (EvidenceEvaluation evidenceEvaluation : list) {
			
			formatter.format("-" + evidenceEvaluation.getName() + "-");
			formatter.format("\n\n");
			
			formatter.format("LCM:\n");
			printMatrix(evidenceEvaluation.getIndividualLCM(), formatter);
			
			formatter.format("\n");
			
			formatter.format("PCC: ");
			formatter.format("%2.2f\n", evidenceEvaluation.getIndividualPCC() * 100);
			
			formatter.format("\n\n");
			
			// Add random costs for each
			evidenceEvaluation.setCost((new Random()).nextFloat() * 1000);
		}
		
		formatter.format("\n");
		formatter.format("----INDIVIDUAL PCC------");
		formatter.format("\n\n");
		
		list = evaluationExact.getBestIndividualCostRate();
		
		for (EvidenceEvaluation evidenceEvaluation : list) {
			
			formatter.format("-" + evidenceEvaluation.getName() + "-");
			formatter.format("\n\n");
			
			formatter.format("PCC: ");
			formatter.format("%2.2f\n", evidenceEvaluation.getIndividualPCC() * 100);
			
			formatter.format("\n");
			
			formatter.format("Cost: ");
			formatter.format("%2.2f\n", evidenceEvaluation.getCost());
			
			formatter.format("\n");
			
			formatter.format("Cost Rate: ");
			formatter.format("%2.2f\n", evidenceEvaluation.getCostRate() * 100);
			
			formatter.format("\n\n");
		}
		
		
		//System.out.println(sb.toString());

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
				formatter.format("%2.2f ", matrix[i][j] * 100);
			}
			formatter.format("\n");
		}
	}
}
