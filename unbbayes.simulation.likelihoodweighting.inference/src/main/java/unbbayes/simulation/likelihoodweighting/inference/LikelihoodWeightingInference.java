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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

import unbbayes.io.BaseIO;
import unbbayes.io.NetIO;
import unbbayes.io.XMLBIFIO;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.simulation.likelihoodweighting.sampling.LikelihoodWeightingSampling;
import unbbayes.util.Debug;
import unbbayes.util.extension.bn.inference.AbstractInferenceAlgorithm;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithmListener;

public class LikelihoodWeightingInference extends AbstractInferenceAlgorithm {
	
	protected LikelihoodWeightingSampling lwSampling;
	protected int nTrials;
	private String dataSplitter = "\t";
	

	/** Load resource file from util */
  	private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
  			unbbayes.util.resources.UtilResources.class.getName());
  	
  	/**
  	 * This is the default instance that will be added to {@link #addInferencceAlgorithmListener(IInferenceAlgorithmListener)}
  	 * on {@link #LikelihoodWeightingInference()}.
  	 * It will perform backup of samples in a file.
  	 * Remove it in order to disable file backup.
  	 */
  	public static final IInferenceAlgorithmListener DEFAULT_LIKELIHOOD_WEIGHTING_LISTENER = new IInferenceAlgorithmListener() {
		public void onBeforeRun(IInferenceAlgorithm algorithm) { Debug.println(getClass(), "Started run"); }
		public void onBeforeReset(IInferenceAlgorithm algorithm) { Debug.println(getClass(), "Started reset"); }
		public void onBeforePropagate(IInferenceAlgorithm algorithm) { Debug.println(getClass(), "Started propagate"); }
		public void onAfterReset(IInferenceAlgorithm algorithm) { Debug.println(getClass(), "Finished reset"); }
		public void onAfterPropagate(IInferenceAlgorithm algorithm) { Debug.println(getClass(), "Finished propagate"); }
		public void onAfterRun(IInferenceAlgorithm algorithm) { 
			try {
				((LikelihoodWeightingInference)algorithm).backupSamplesToFile(new File("samples.txt"));
			} catch (Throwable t) {
				Debug.println(getClass(), "Failed to backup samples in a file", t);
			}
			Debug.println(getClass(), "Finished run"); 
		}
	};
	
	/**
	 * Default constructor created for plugin support
	 */
	public LikelihoodWeightingInference(){
		super();
		this.lwSampling= new LikelihoodWeightingSampling();
		this.setNTrials(100000);
		this.addInferencceAlgorithmListener(DEFAULT_LIKELIHOOD_WEIGHTING_LISTENER);
	}

	public LikelihoodWeightingInference(ProbabilisticNetwork pn , int nTrials){		
		this();
		this.setNetwork(pn);
		this.nTrials = nTrials;	
	}
	
	/**
	 * @return
	 * Sampler used in this algorithm to simulate the distribution
	 */
	public LikelihoodWeightingSampling getLikelihoodWeightingSampling() {
		return lwSampling;
	}
	
	/**
	 * @return
	 * {@link #getNetwork()} casted to {@link ProbabilisticNetwork}
	 * @deprecated use {@link #getNetwork()} instead
	 */
	protected ProbabilisticNetwork getProbabilisticNetwork() {
		return (ProbabilisticNetwork) getNetwork();
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#run()
	 */
	public void run() {
		// TODO use interceptors or proxies in order to automatically invoke listeners.
		for (IInferenceAlgorithmListener listener : getInferenceAlgorithmListeners()) {
			listener.onBeforeRun(this);
		}
		lwSampling.start(getProbabilisticNetwork(), nTrials);
		for (int i = 0; i < getProbabilisticNetwork().getNodeCount(); i++) {
			Node node = getProbabilisticNetwork().getNodeAt(i);
			if (!((TreeVariable)node).hasEvidence()) {
				updateMarginal(node);
			}
		}
		// TODO use interceptors or proxies in order to automatically invoke listeners.
		for (IInferenceAlgorithmListener listener : getInferenceAlgorithmListeners()) {
			listener.onAfterRun(this);
		}
	}
	
	/**
	 * Updates marginal of referenced node
	 * @param node
	 */
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
		
		// TODO check if inclusion of likelihood evidence is really necessary...
//		((TreeVariable)node).addLikeliHood(marginal);
		
		((TreeVariable)node).setMarginalProbabilities(marginal);
	}

	/**
	 * Normalizes an array so that sum will be 1.
	 * @param floatList
	 * array to be normalized
	 */
	protected void normalize(float[] floatList) {
		
		double total = 0;
		for (int i = 0; i < floatList.length; i++) {
			total += floatList[i];
		}
		
		for (int i = 0; i < floatList.length; i++) {
			floatList[i] /= total;
		}
		
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
	 * @return 
	 * number of simulations to perform
	 */
	public int getNTrials() {
		return nTrials;
	}

	/**
	 * @param trials 
	 * number of simulations to perform
	 */
	public void setNTrials(int trials) {
		nTrials = trials;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#reset()
	 */
	public void reset() {
		// TODO use interceptors or proxies in order to automatically invoke listeners.
		for (IInferenceAlgorithmListener listener : getInferenceAlgorithmListeners()) {
			listener.onBeforeReset(this);
		}
		getProbabilisticNetwork().resetEvidences();
		this.run();
		// TODO use interceptors or proxies in order to automatically invoke listeners.
		for (IInferenceAlgorithmListener listener : getInferenceAlgorithmListeners()) {
			listener.onAfterReset(this);
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#propagate()
	 */
	public void propagate() {
		// TODO use interceptors or proxies in order to automatically invoke listeners.
		for (IInferenceAlgorithmListener listener : getInferenceAlgorithmListeners()) {
			listener.onBeforePropagate(this);
		}
		this.run();
		// TODO use interceptors or proxies in order to automatically invoke listeners.
		for (IInferenceAlgorithmListener listener : getInferenceAlgorithmListeners()) {
			listener.onAfterPropagate(this);
		}
	}
	
	/**
	 * Creates a file containing samples generated by this class.
	 * @param file
	 * @throws FileNotFoundException 
	 */
	public void backupSamplesToFile(File file) throws IOException {
		
		Debug.println(getClass(), "Saving backup of samples to " + file.getName());
		
		LikelihoodWeightingSampling sampler = this.getLikelihoodWeightingSampling();
		if (sampler == null) {
			throw new IllegalStateException("Likelihood weighting sampler not initialized yet.");
		}
		
		// extract samples
		byte[][] sampledMatrix = sampler.getSampledStatesMatrix();
		if (sampledMatrix == null || sampledMatrix.length <= 0) {
			throw new IllegalStateException("Samples were not generated yet.");
		}
		if (sampledMatrix[0].length != getNetwork().getNodeCount()) {
			throw new IllegalStateException("Number of nodes in network is " + getNetwork().getNodeCount()
					 + ", but number of columns in samples is " + sampledMatrix[0].length);
		}
		
		float[] normalizedWeights = null;
		{
			// extract weights
			float[] originalWeights = sampler.getFullStatesSetWeight();
			if (originalWeights == null || originalWeights.length <= 0) {
				throw new IllegalStateException("Weights were not generated yet.");
			}
			normalizedWeights = Arrays.copyOf(originalWeights, originalWeights.length);
		}
		
		// normalize weights, so that we can multiply with number of samples in order to get proper number of samples
		normalize(normalizedWeights);
		

		// prepare to generate file backup
		PrintStream out = new PrintStream(new FileOutputStream(file));
		
		
		// write header;
		for (int i = 0; i < getNetwork().getNodeCount(); i++) {
			out.print(getNetwork().getNodes().get(i).getName());
			if ( (i + 1) < getNetwork().getNodeCount() ) {
				out.print(getDataSplitter());
			}
		}
		out.println();
		
		// write rows;
		for (int row = 0; row < sampledMatrix.length; row++) {
			
			// calculate how many times current sample should repeat, 
			// based on the likelihood weight
			int numRepetition = Math.round(normalizedWeights[row] * getNTrials());
			
			// current sample should repeat this number of times
			for (int i = 0; i < numRepetition; i++) {
				
				// each column represent a node in network
				for (int column = 0; column < getNetwork().getNodeCount(); column++) {
					
					Node node = getNetwork().getNodes().get(column);
					
					// extract index of current node in sample matrix
					int columnInSampleMatrix = sampler.getSamplingNodeOrderQueue().indexOf(node);
					
					// get the value of the sample
					byte stateIndex = sampledMatrix[row][columnInSampleMatrix];
					
					// print the sample (use name of the state instead of index)
					out.print(node.getStateAt(stateIndex));
					
					if ( (column + 1) < getNetwork().getNodeCount() ) {
						out.print(getDataSplitter());
					}
					
				}
				
				// will print next sample/repetition
				out.println();
			
			}	// end loop on repetition
			
		}	// end loop on samples
		
		out.close();
	}

	
	
	/**
	 * @return 
	 * string to be used to split data entry in backup data
	 * generated by {@link #backupSamplesToFile(File)}
	 */
	public String getDataSplitter() {
		return dataSplitter;
	}

	/**
	 * @param dataSplitter 
	 * string to be used to split data entry in backup data
	 * generated by {@link #backupSamplesToFile(File)}
	 */
	public void setDataSplitter(String dataSplitter) {
		this.dataSplitter = dataSplitter;
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

	
	

}
