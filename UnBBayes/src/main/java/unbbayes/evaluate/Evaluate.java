package unbbayes.evaluate;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import unbbayes.io.BaseIO;
import unbbayes.io.NetIO;
import unbbayes.io.XMLIO;
import unbbayes.monteCarlo.simulacao.SimulacaoMonteCarlo;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.TreeVariable;


public class Evaluate {
	
	ProbabilisticNetwork net;
	TreeVariable[] targetNodeList; 
	TreeVariable[] evidenceNodeList;
	int statesProduct;
	
	public Evaluate(String netFileName, List<String> targetNodeNameList, List<String> evidenceNodeNameList) throws Exception {
		
		laodNetwork(netFileName);
		
		init(targetNodeNameList, evidenceNodeNameList);
		
		// 1. Generate the MC sample from the network file
		// Case# StateIndexForNode1	StateIndexForNode2	StateIndexForNodeJ
		// 001	 0					1					0
		// 002	 2					0					1
		// ...
		// i	 x					y					z
		SimulacaoMonteCarlo mc = new SimulacaoMonteCarlo(net, 50000);
		byte[][] sampleMatrix = mc.start();
		
		// FIXME For now let's just consider the simple case of having just one target node!
		TreeVariable targetNode = targetNodeList[0];
		if (targetNodeList.length != 1) {
			throw new Exception("For now, just one target node is accepted!");
		}
		
		// 2. Count # of occurrences of target node given evidence nodes
		
		int nNodes = targetNodeList.length + evidenceNodeList.length;
		int[] frequencyTargetGivenEvidenceList = new int[statesProduct];
		int[][] frequencyTargetList = new int[targetNode.getStatesSize()][statesProduct / targetNode.getStatesSize()];
		int[] positionTargetNodeList = new int[targetNodeList.length];
		int[] positionEvidenceNodeList = new int[evidenceNodeList.length];
		
		for (int i = 0; i < positionTargetNodeList.length; i++) {
			positionTargetNodeList[i] = net.getNodeIndex(targetNodeList[i].getName());
		}
		
		for (int i = 0; i < positionEvidenceNodeList.length; i++) {
			positionEvidenceNodeList[i] = net.getNodeIndex(evidenceNodeList[i].getName());
		}
		
		// Iterate over all cases in the MC sample
		for (int i = 0; i < sampleMatrix.length; i++) {
			// Row to compute the frequency
			int row = 0;
			int evidenceStatesProduct = 1;
			for (int j = positionEvidenceNodeList.length - 1; j >= 0; j--) {
				byte state = sampleMatrix[i][positionEvidenceNodeList[j]];
				row += state * evidenceStatesProduct;
				evidenceStatesProduct *= net.getNodeAt(positionEvidenceNodeList[j]).getStatesSize();
			}
			
			for (int j = 0; j < positionTargetNodeList.length; j++) {
				// Add to total frequency for target node independent of state
				frequencyTargetList[j][row]++;
				
				byte state = sampleMatrix[i][positionTargetNodeList[j]];
				row += state * evidenceStatesProduct;
				// Add to total frequency for specific target state
				frequencyTargetGivenEvidenceList[row]++;
			}
		}
		
		// 3. Compute probabilities for target node given evidence nodes
		float[] postProbTargetGivenEvidence = new float[statesProduct];
		for (int i = 0; i < postProbTargetGivenEvidence.length; i++) {
			float n = (float)frequencyTargetList[0][i % (statesProduct / targetNode.getStatesSize())];
			if (n != 0) {
				postProbTargetGivenEvidence[i] = (float)frequencyTargetGivenEvidenceList[i] / n;
			}
			System.out.println(postProbTargetGivenEvidence[i]);
		}
		
		getExatProbTargetGivenEvidence();
		
		//getRowByState(new byte[net.getNodesCount()]);
		
		try {
        	net.updateEvidences();
        } catch (Exception exc) {
        	System.out.println(exc.getMessage());               	
        }
		
	}

	private void getExatProbTargetGivenEvidence() throws Exception {
		// TODO for now I am just considering there is one target node!
		TreeVariable targetNode = targetNodeList[0];
		
		net.compile();
		
		float[] postProbList = new float[statesProduct];
		
		int sProd = targetNode.getStatesSize();
		
		byte[][] stateCombinationMatrix = new byte[statesProduct][net.getNodes().size()];
		int state = 0;
		for (int row = 0; row < statesProduct; row++) {
			stateCombinationMatrix[row][0] = (byte) (row / (statesProduct / sProd)); 
			for (int j = 0; j < evidenceNodeList.length; j++) {
				sProd *=  evidenceNodeList[j].getStatesSize();
				state = (row / (statesProduct / sProd)) % evidenceNodeList[j].getStatesSize();
				evidenceNodeList[j].addFinding(state);
				stateCombinationMatrix[row][j+1] = (byte)state;
			}
			sProd = targetNode.getStatesSize();
			try {
				net.updateEvidences();
				postProbList[row] = targetNode.getMarginalAt(stateCombinationMatrix[row][0]);
			} catch (Exception e) {
				postProbList[row] = 0;
			}
			net.compile();
		}
		
		printProbMatrix(stateCombinationMatrix, postProbList);
		
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

	private void init(List<String> targetNodeNameList, List<String> evidenceNodeNameList) {
		List<Node> nodeList = (List<Node>)net.getNodes().clone();
		targetNodeList = new TreeVariable[targetNodeNameList.size()];
		evidenceNodeList = new TreeVariable[evidenceNodeNameList.size()];
		statesProduct = 1;
		
		// Create list of target TreeVariable
		int count = 0;
		for (String targetNodeName : targetNodeNameList) {
			Node targetNode = net.getNode(targetNodeName);

			targetNodeList[count] = (TreeVariable)targetNode;
			
			statesProduct *= targetNode.getStatesSize();
			count++;
		}
		
		// Create list of evidence TreeVariable
		count = 0;
		for (String evidenceNodeName : evidenceNodeNameList) {
			Node evidenceNode = net.getNode(evidenceNodeName);

			evidenceNodeList[count] = (TreeVariable)evidenceNode;
			
			statesProduct *= evidenceNode.getStatesSize();
			count++;
		}
	}

	private void laodNetwork(String netFileName) {
		File netFile = new File(netFileName);
		String fileExt = netFileName.substring(netFileName.length() - 3);
		
		try {
			BaseIO io = null;
			if (fileExt.equalsIgnoreCase("xml")) {
				io = new XMLIO();
			} else if (fileExt.equalsIgnoreCase("net")) {
				io = new NetIO();
			} else {
				throw new Exception("The network must be in XMLBIF 0.4 or NET format!");
			}
			net = io.load(netFile);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}		
	}

	public static void main(String[] args) throws Exception {

		List<String> targetNodeNameList = new ArrayList<String>();
		targetNodeNameList.add("Cloudy");
		
		List<String> evidenceNodeNameList = new ArrayList<String>();
		evidenceNodeNameList.add("Springler");
		evidenceNodeNameList.add("Rain");
		evidenceNodeNameList.add("Wet");
		
		String netFileName = "../UnBBayes/examples/xml-bif/WetGrass.xml";
		
		new Evaluate(netFileName, targetNodeNameList, evidenceNodeNameList);
		

	}

	private static int getRowByState(byte[] states) {
		return 0;
	}
}
