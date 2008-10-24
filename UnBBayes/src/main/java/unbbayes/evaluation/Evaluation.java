package unbbayes.evaluation;

import java.io.File;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import unbbayes.io.BaseIO;
import unbbayes.io.NetIO;
import unbbayes.io.XMLIO;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.simulation.montecarlo.sampling.MonteCarloSampling;

public class Evaluation {

	private ProbabilisticNetwork net;

	private TreeVariable[] targetNodeList;

	private TreeVariable[] evidenceNodeList;

	private int statesProduct;

	private int targetStatesProduct;

	private int evidenceStatesProduct;
	
	byte[][] sampleMatrix;
	
	int[] positionTargetNodeList;
	int[] positionEvidenceNodeList;
	
	private Formatter formatter;
	
	private TreeVariable targetNode;

	public String evaluate(String netFileName, List<String> targetNodeNameList, List<String> evidenceNodeNameList, int sampleSize) throws Exception {
		loadNetwork(netFileName);
		return evaluate(targetNodeNameList, evidenceNodeNameList, sampleSize);
	}
	
	public String evaluate(ProbabilisticNetwork net, List<String> targetNodeNameList, List<String> evidenceNodeNameList, int sampleSize) throws Exception {
		this.net = net;
		return evaluate(targetNodeNameList, evidenceNodeNameList, sampleSize);
	}

	private String evaluate(List<String> targetNodeNameList,
			List<String> evidenceNodeNameList, int sampleSize) throws Exception {
		
		StringBuilder sb = new StringBuilder();
		// Send all output to the appendable object sb
		formatter = new Formatter(sb, Locale.US);
		
		init(targetNodeNameList, evidenceNodeNameList);
		
		// 1. Generate the MC sample from the network file
		// Trial# StateIndexForNode1	StateIndexForNode2	StateIndexForNodeJ
		// 001	  0						1					0
		// 002	  2						0					1
		// ...
		// i	  x						y					z
		MonteCarloSampling mc = new MonteCarloSampling(net, sampleSize);
		mc.start();
		sampleMatrix = mc.getSampledStatesMatrix(); 
		
		// FIXME For now let's just consider the simple case of having just one target node!
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
			//positionTargetNodeList[i] = net.getNodeIndex(targetNodeList[i].getName());
			positionTargetNodeList[i] = positionNodeList.indexOf(net.getNode(targetNodeList[i].getName()));
		}
		
		for (int i = 0; i < positionEvidenceNodeList.length; i++) {
			//positionEvidenceNodeList[i] = net.getNodeIndex(evidenceNodeList[i].getName());
			positionEvidenceNodeList[i] = positionNodeList.indexOf(net.getNode(evidenceNodeList[i].getName()));
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
				currentStatesProduct *= net.getNodeAt(positionTargetNodeList[j]).getStatesSize();
			}
			
			// Add to total frequency for evidence nodes independent of state
			frequencyEvidenceList[(int)(row/evidenceStatesProduct)]++;
			
			currentStatesProduct = evidenceStatesProduct;
			for (int j = 0; j < positionEvidenceNodeList.length; j++) {
				currentStatesProduct /= net.getNodeAt(positionEvidenceNodeList[j]).getStatesSize();
				byte state = sampleMatrix[i][positionEvidenceNodeList[j]];
				row += state * currentStatesProduct;
			}
			
			// Add to total frequency for specific evidences
			frequencyEvidenceGivenTargetList[row]++;
		}
		
		// 3. Compute probabilities for evidence nodes given target nodes
		float[] postProbEvidenceGivenTarget = new float[statesProduct];
		for (int i = 0; i < postProbEvidenceGivenTarget.length; i++) {
			float n = (float)frequencyEvidenceList[(int)(i / evidenceStatesProduct)];
			if (n != 0) {
				postProbEvidenceGivenTarget[i] = (float)frequencyEvidenceGivenTargetList[i] / n;
			}
		}
		
		// 4. Compute probabilities for target given evidence using evidence given target
		// P(T|E) = P(E|T)P(T)
		float[] postProbTargetGivenEvidence = new float[statesProduct];
		int row = 0;
		float prob = 0.0f;
		float[] normalizationList = new float[evidenceStatesProduct];
		net.compile();
		for (int i = 0; i < targetNode.getStatesSize(); i++) {
			for (int j = 0; j < evidenceStatesProduct; j++) {
				row = j + i * evidenceStatesProduct;
				prob = postProbEvidenceGivenTarget[row] * targetNode.getMarginalAt(i);
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
		float[] postProbTargetGivenTarget = new float[(int)Math.pow(targetNode.getStatesSize(), 2)];
		int statesSize = targetNode.getStatesSize();
		row = 0;
		int index = 0;
//		System.out.println();
		for (int i = 0; i < statesProduct; i++) {
			for (int j = 0; j < statesSize; j++) {
				row = ((int)(i / evidenceStatesProduct)) * statesSize + j;
				index = (i % evidenceStatesProduct) + j * evidenceStatesProduct;
//				System.out.println("P(T|T)[" + row + "] = P(T|T)[" + row + "] + P(T|E)[" + i + "] * P(E|T)[" + index + "]");
//				System.out.println("P(T|T)[" + row + "] = " + postProbTargetGivenTarget[row] + " + " + postProbTargetGivenEvidence[i] + " * " + postProbEvidenceGivenTarget[index]);
				postProbTargetGivenTarget[row] += postProbTargetGivenEvidence[i] * postProbEvidenceGivenTarget[index];
//				System.out.println("P(T|T)[" + row + "] = " + postProbTargetGivenTarget[row]);
			}
		}
//		System.out.println();
		
		formatter.format("P(T|E) = N[ P(E|T)P(T) ]\n");
		for (int i = 0; i < targetStatesProduct; i++) {
			for (int j = 0; j < evidenceStatesProduct; j++) {
				formatter.format("%2.2f	", postProbTargetGivenEvidence[i * evidenceStatesProduct + j] * 100);
			}
			formatter.format("\n");
		}
		
		formatter.format("\n");
		
		formatter.format("P(E|T)\n");
		for (int i = 0; i < evidenceStatesProduct; i++) {
			for (int j = 0; j < targetStatesProduct; j++) {
				formatter.format("%2.2f	", postProbEvidenceGivenTarget[j * evidenceStatesProduct + i] * 100);
			}
			formatter.format("\n");
		}
		
		formatter.format("\n");
		
		formatter.format("P(T|T) = P(T|E)P(E|T)\n");
		for (int i = 0; i < statesSize; i++) {
			for (int j = 0; j < statesSize; j++) {
				formatter.format("%2.2f	", postProbTargetGivenTarget[i * statesSize + j] * 100);
			}
			formatter.format("\n");
		}
		
//		System.out.println(sb);
		
		return sb.toString();
		
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
				currentStatesProduct *= net.getNodeAt(positionEvidenceNodeList[j]).getStatesSize();
			}
			
			// Add to total frequency for target nodes independent of state
			frequencyTargetList[row]++;
			
			for (int j = 0; j < positionTargetNodeList.length; j++) {
				byte state = sampleMatrix[i][positionTargetNodeList[j]];
				row += state * currentStatesProduct;
				currentStatesProduct *= net.getNodeAt(positionTargetNodeList[j]).getStatesSize();
			}
			
			// Add to total frequency for specific targets
			frequencyTargetGivenEvidenceList[row]++;
		}
		
		// 2. Compute probabilities for target nodes given evidence nodes
		float[] postProbTargetGivenEvidence = new float[statesProduct];
		for (int i = 0; i < postProbTargetGivenEvidence.length; i++) {
			float n = (float)frequencyTargetList[i % (evidenceStatesProduct)];
			if (n != 0) {
				postProbTargetGivenEvidence[i] = (float)frequencyTargetGivenEvidenceList[i] / n;
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
				io = new XMLIO();
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

		String netFileName = "../UnBBayes/examples/xml-bif/WetGrass.xml";
		
		int sampleSize = 100000;

		Evaluation evaluation = new Evaluation();
		evaluation.evaluate(netFileName, targetNodeNameList, evidenceNodeNameList, sampleSize);

	}
}
