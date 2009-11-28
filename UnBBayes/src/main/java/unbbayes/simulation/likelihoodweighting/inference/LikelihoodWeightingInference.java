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
package unbbayes.simulation.likelihoodweighting.inference;

import java.io.File;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import unbbayes.io.BaseIO;
import unbbayes.io.NetIO;
import unbbayes.io.XMLBIFIO;
import unbbayes.prs.Graph;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.simulation.likelihoodweighting.sampling.LikelihoodWeightingSampling;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;

public class LikelihoodWeightingInference implements IInferenceAlgorithm {
	
	protected LikelihoodWeightingSampling lwSampling;
	protected ProbabilisticNetwork pn;
	protected int nTrials;
	

	/** Load resource file from util */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.util.resources.UtilResources");
	
	/**
	 * Default constructor created for plugin support
	 */
	public LikelihoodWeightingInference(){
		super();
		this.lwSampling= new LikelihoodWeightingSampling();
		this.setNTrials(100000);
	}
	
	public LikelihoodWeightingInference(ProbabilisticNetwork pn , int nTrials){		
		this();
		this.setNetwork(pn);
		this.nTrials = nTrials;	
	}
	
	public LikelihoodWeightingSampling getLikelihoodWeightingSampling() {
		return lwSampling;
	}
	
	public void run() {
		lwSampling.start(pn, nTrials);
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
		float[] probEvdGivenPar = lwSampling.getFullStatesSetWeight();
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
				io = new XMLBIFIO();
			} else if (fileExt.equalsIgnoreCase("net")) {
				io = new NetIO();
			} else {
				throw new Exception(
						"The network must be in XMLBIF 0.4 or NET format!");
			}
			pn = (ProbabilisticNetwork)io.load(netFile);
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
		return this.resource.getString("likelihoodWeightingAlgorithmDescription");
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#getName()
	 */
	public String getName() {
		return this.resource.getString("likelihoodWeightingAlgorithmName");
	}

	/**
	 * @return the nTrials
	 */
	public int getNTrials() {
		return nTrials;
	}

	/**
	 * @param trials the nTrials to set
	 */
	public void setNTrials(int trials) {
		nTrials = trials;
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
