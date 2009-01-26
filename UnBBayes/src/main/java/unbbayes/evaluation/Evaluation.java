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

	private int[] positionTargetNodeList;

	private int[] positionEvidenceNodeList;

	// -------- OUTPUT VALUES - EVALUATION --------//

	private float[][] evidenceSetLCM;

	private float evidenceSetPCC = Evaluation.UNSET_VALUE;

	private List<EvidenceEvaluation> evidenceEvaluationList;
	
	public List<EvidenceEvaluation> getEvidenceEvaluationList() {
		return evidenceEvaluationList;
	}

	public float[][] getEvidenceSetLCM() {
		return evidenceSetLCM;
	}

	public float getEvidenceSetPCC() throws EvaluationException {
		if (evidenceSetPCC == Evaluation.UNSET_VALUE) {
			if (evidenceSetLCM == null) {
				throw new EvaluationException(
						"Must calculate evidence set LCM before computing evidence set PCC.");
			}
			evidenceSetPCC = 0;
			for (int i = 0; i < evidenceSetLCM.length; i++) {
				evidenceSetPCC += evidenceSetLCM[i][i];
			}
			evidenceSetPCC /= evidenceSetLCM.length;
		}
		return evidenceSetPCC;
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
	
	public String evaluate(String netFileName, List<String> targetNodeNameList,
			List<String> evidenceNodeNameList, int sampleSize) throws Exception {
		loadNetwork(netFileName);
		return evaluate(targetNodeNameList, evidenceNodeNameList, sampleSize);
	}

	public String evaluate(ProbabilisticNetwork net,
			List<String> targetNodeNameList, List<String> evidenceNodeNameList,
			int sampleSize) throws Exception {
		this.net = net;
		return evaluate(targetNodeNameList, evidenceNodeNameList, sampleSize);
	}

	private String evaluate(List<String> targetNodeNameList,
			List<String> evidenceNodeNameList, int sampleSize) throws Exception {

		evidenceSetLCM = computeLCM(targetNodeNameList, evidenceNodeNameList, sampleSize);
		
		evidenceEvaluationList = new ArrayList<EvidenceEvaluation>();
		for (String evidenceName : evidenceNodeNameList) {
			EvidenceEvaluation evidenceEvaluation = new EvidenceEvaluation(evidenceName);
			
			// Compute individual LCM
			List<String> tempList = new ArrayList<String>();
			tempList.add(evidenceName);
			evidenceEvaluation.setIndividualLCM(computeLCM(targetNodeNameList, tempList, sampleSize));
			
			// Compute marginal LCM
			tempList.clear();
			tempList.addAll(evidenceNodeNameList);
			tempList.remove(evidenceName);
			evidenceEvaluation.setMarginalLCM(computeLCM(targetNodeNameList, tempList, sampleSize));
			
			evidenceEvaluationList.add(evidenceEvaluation);
		}

		return "";

	}

	private float[][] computeLCM(List<String> targetNodeNameList,
			List<String> evidenceNodeNameList, int sampleSize) throws Exception {

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

		// Get the position in the network of target and evidence nodes
		positionTargetNodeList = new int[targetNodeList.length];
		positionEvidenceNodeList = new int[evidenceNodeList.length];

		// Position of the nodes in the sampled matrix.
		List<Node> positionNodeList = mc.getSamplingNodeOrderQueue();

		for (int i = 0; i < positionTargetNodeList.length; i++) {
			// positionTargetNodeList[i] =
			// net.getNodeIndex(targetNodeList[i].getName());
			positionTargetNodeList[i] = positionNodeList.indexOf(net
					.getNode(targetNodeList[i].getName()));
		}

		for (int i = 0; i < positionEvidenceNodeList.length; i++) {
			// positionEvidenceNodeList[i] =
			// net.getNodeIndex(evidenceNodeList[i].getName());
			positionEvidenceNodeList[i] = positionNodeList.indexOf(net
					.getNode(evidenceNodeList[i].getName()));
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
				currentStatesProduct *= net
						.getNodeAt(positionTargetNodeList[j]).getStatesSize();
			}

			// Add to total frequency for evidence nodes independent of state
			frequencyEvidenceList[(int) (row / evidenceStatesProduct)]++;

			currentStatesProduct = evidenceStatesProduct;
			for (int j = 0; j < positionEvidenceNodeList.length; j++) {
				currentStatesProduct /= net.getNodeAt(
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
		for (int i = 0; i < statesProduct; i++) {
			for (int j = 0; j < statesSize; j++) {
				row = ((int) (i / evidenceStatesProduct)) * statesSize + j;
				index = (i % evidenceStatesProduct) + j * evidenceStatesProduct;
				postProbTargetGivenTarget[row] += postProbTargetGivenEvidence[i]
						* postProbEvidenceGivenTarget[index];
			}
		}

		// 6. Set LCM
		float[][] LCM = new float[statesSize][statesSize];
		for (int i = 0; i < statesSize; i++) {
			for (int j = 0; j < statesSize; j++) {
				LCM[i][j] = postProbTargetGivenTarget[i * statesSize + j];
			}
		}

//		System.out.println(getLCMLog(postProbTargetGivenEvidence,
//				postProbEvidenceGivenTarget, postProbTargetGivenTarget));

		return LCM;
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
		private float[][] individualLCM;

		// Probability of correct classification of the evidence set without
		// this evidence
		private float marginalPCC = Evaluation.UNSET_VALUE;

		// Local confusion matrix of the evidence set without this evidence
		private float[][] marginalLCM;

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
				if (individualLCM == null) {
					throw new EvaluationException(
							"Must calculate individual LCM before computing individual PCC.");
				}
				individualPCC = 0;
				for (int i = 0; i < individualLCM.length; i++) {
					individualPCC += individualLCM[i][i];
				}
				individualPCC /= individualLCM.length;
			}
			return individualPCC;
		}

		public float getMarginalPCC() throws EvaluationException {
			if (marginalPCC == Evaluation.UNSET_VALUE) {
				if (marginalLCM == null) {
					throw new EvaluationException(
							"Must calculate marginal LCM before computing marginal PCC.");
				}
				marginalPCC = 0;
				for (int i = 0; i < marginalLCM.length; i++) {
					marginalPCC += marginalLCM[i][i];
				}
				marginalPCC /= marginalLCM.length;
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
			return individualLCM;
		}

		public void setIndividualLCM(float[][] individualLCM) {
			this.individualLCM = individualLCM;
		}

		public float[][] getMarginalLCM() {
			return marginalLCM;
		}

		public void setMarginalLCM(float[][] marginalLCM) {
			this.marginalLCM = marginalLCM;
		}

		public String getName() {
			return name;
		}

	}

	private float[] computePostProbTargetGivenEvidenceUsingMC() {
		// 1. Count # of occurrences of target nodes given evidence nodes
		int[] frequencyTargetGivenEvidenceList = new int[statesProduct];
		int[] frequencyTargetList = new int[evidenceStatesProduct];

		// Iterate over all cases in the MC sample
		for (int i = 0; i < sampleMatrix.length; i++) {
			// Row to compute the frequency
			int row = 0;
			int currentStatesProduct = 1;
			for (int j = positionEvidenceNodeList.length - 1; j >= 0; j--) {
				byte state = sampleMatrix[i][positionEvidenceNodeList[j]];
				row += state * currentStatesProduct;
				currentStatesProduct *= net.getNodeAt(
						positionEvidenceNodeList[j]).getStatesSize();
			}

			// Add to total frequency for target nodes independent of state
			frequencyTargetList[row]++;

			for (int j = 0; j < positionTargetNodeList.length; j++) {
				byte state = sampleMatrix[i][positionTargetNodeList[j]];
				row += state * currentStatesProduct;
				currentStatesProduct *= net
						.getNodeAt(positionTargetNodeList[j]).getStatesSize();
			}

			// Add to total frequency for specific targets
			frequencyTargetGivenEvidenceList[row]++;
		}

		// 2. Compute probabilities for target nodes given evidence nodes
		float[] postProbTargetGivenEvidence = new float[statesProduct];
		for (int i = 0; i < postProbTargetGivenEvidence.length; i++) {
			float n = (float) frequencyTargetList[i % (evidenceStatesProduct)];
			if (n != 0) {
				postProbTargetGivenEvidence[i] = (float) frequencyTargetGivenEvidenceList[i]
						/ n;
			}
			formatter.format("%2.2f\n", postProbTargetGivenEvidence[i] * 100);
		}

		formatter.format("\n\n");

		return postProbTargetGivenEvidence;
	}

	private void getExatProbTargetGivenEvidence() throws Exception {
		// TODO for now I am just considering there is one target node!
		TreeVariable targetNode = targetNodeList[0];

		net.compile();

		float[] postProbList = new float[statesProduct];

		int sProd = targetNode.getStatesSize();

		byte[][] stateCombinationMatrix = new byte[statesProduct][net
				.getNodes().size()];
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

	}

	private void getExatProbEvidenceGivenTarget() throws Exception {

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
						"The network must be in XMLBIF 0.4 or NET format!");
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

		Evaluation evaluation = new Evaluation();
		evaluation.evaluate(netFileName, targetNodeNameList,
				evidenceNodeNameList, sampleSize);
		
		StringBuilder sb = new StringBuilder();
		// Send all output to the appendable object sb
		Formatter formatter = new Formatter(sb, Locale.US);
		
		formatter.format("----TOTAL------");
		formatter.format("\n\n");
		
		formatter.format("LCM:\n");
		printMatrix(evaluation.getEvidenceSetLCM(), formatter);
		
		formatter.format("\n");
		
		formatter.format("PCC: ");
		formatter.format("%2.2f\n", evaluation.getEvidenceSetPCC() * 100);
		
		formatter.format("\n\n\n");
		formatter.format("----MARGINAL------");
		formatter.format("\n\n");
		
		List<EvidenceEvaluation> list = evaluation.getBestMarginalImprovement();
		
		for (EvidenceEvaluation evidenceEvaluation : list) {
			
			formatter.format("-" + evidenceEvaluation.getName() + "-");
			formatter.format("\n\n");
			
			formatter.format("LCM:\n");
			printMatrix(evidenceEvaluation.getMarginalLCM(), formatter);
			
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
		
		list = evaluation.getBestIndividualPCC();
		
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
		
		list = evaluation.getBestIndividualCostRate();
		
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
		
		System.out.println(sb.toString());

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
