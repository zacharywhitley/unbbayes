package unbbayes.simulation.likelihoodweighting.inference;

import java.io.File;
import java.text.NumberFormat;
import java.util.Locale;

import unbbayes.io.BaseIO;
import unbbayes.io.NetIO;
import unbbayes.io.XMLIO;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.simulation.likelihoodweighting.sampling.LikelihoodWeightingSampling;

public class LikelihoodWeightingInference {
	
	protected LikelihoodWeightingSampling lwSampling;
	protected ProbabilisticNetwork pn;
	protected int nTrials;
	
	public LikelihoodWeightingInference(ProbabilisticNetwork pn , int nTrials){		
		this.pn = pn;
		this.nTrials = nTrials;	
		this.lwSampling = new LikelihoodWeightingSampling(pn, nTrials);
	}
	
	public void run() {
		lwSampling.start();
		for (int i = 0; i < pn.getNodeCount(); i++) {
			Node node = pn.getNodeAt(i);
			if (!((TreeVariable)node).hasEvidence()) {
				updateMarginal(node);
			}
		}
	}
	
	protected void updateMarginal(Node node) {
		float[] marginal = new float[node.getStatesSize()];
		
		byte[][] sampledMatrix = lwSampling.getSampledStatesMatrix();
		float[] probEvdGivenPar = lwSampling.getProbabilityEvidenceGivenParentList();
		int nodeIndex = lwSampling.getSamplingNodeOrderQueue().indexOf(node);
		int state;
		for (int i = 0; i < sampledMatrix.length; i++) {
			// Get the corresponding state for this trial.
			state = sampledMatrix[i][nodeIndex];
			// Add this trial associated probability for its corresponding state. 
			marginal[state] += probEvdGivenPar[i];
		}
		
		normalize(marginal);
		
		((TreeVariable)node).initMarginalList();
		
		((TreeVariable)node).addLikeliHood(marginal);
	}

	protected void normalize(float[] floatList) {
		
		double total = 0;
		for (int i = 0; i < floatList.length; i++) {
			total += floatList[i];
		}
		
		for (int i = 0; i < floatList.length; i++) {
			floatList[i] /= total;
		}
		
	}
	
	private static ProbabilisticNetwork loadNetwork(String netFileName) {
		File netFile = new File(netFileName);
		String fileExt = netFileName.substring(netFileName.length() - 3);
		ProbabilisticNetwork pn = null;

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
			pn = io.load(netFile);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return pn;
	}
	
	public static void main(String[] args) throws Exception {

		NumberFormat nf = NumberFormat.getInstance(Locale.US);
		nf.setMaximumFractionDigits(2);
		
		String netFileName = "../UnBBayes/examples/asia.net";
		
		int sampleSize = 100000;
		
		ProbabilisticNetwork pn = loadNetwork(netFileName);
		
		LikelihoodWeightingInference lw = new LikelihoodWeightingInference(pn, sampleSize);
		
		lw.run();
		
		for (Node node : pn.getNodes()) {
			System.out.println(node.getDescription());
			for (int i = 0; i < node.getStatesSize(); i++) {
				System.out.println("	" + node.getStateAt(i) + ": " + nf.format(((TreeVariable)node).getMarginalAt(i) * 100) );
			}
			System.out.println();
		}
		
		((TreeVariable)pn.getNodeAt(0)).addFinding(0);
		
		lw.run();
		
		for (Node node : pn.getNodes()) {
			System.out.println(node.getDescription());
			for (int i = 0; i < node.getStatesSize(); i++) {
				System.out.println("	" + node.getStateAt(i) + ": " + nf.format(((TreeVariable)node).getMarginalAt(i) * 100) );
			}
			System.out.println();
		}
	}
}
