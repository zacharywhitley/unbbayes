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
package unbbayes.simulation.montecarlo.sampling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.util.Debug;
/*
 * TODO : since unbbayes.evaluation package is using 
 * unbbayes.simulation.montecarlo.sampling package,
 * we could not refactor these classes as plugins yet (this
 * is because evaluation has no plugin support yet).
 */
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
public class MatrixMonteCarloSampling extends AMonteCarloSampling {
	
	private List<SampleGenerationListener> sampleGenerationListener = null;
	
	public byte[][] getSampledStatesCompactMatrix() {
		return null;
	}
	
	/**
	 * Not implemented. 
	 * @return null.
	 */
	public int[] getStatesSetTimesSampled() {
		return null;
	}
	
	/**
	 * Not implemented. 
	 * @return null.
	 */
	public Map<Integer,Integer> getSampledStatesMap() {
		return null;
	}
	
	
	/**
	 * Returns the generated sample matrix.
	 * @return The generated sample matrix.
	 */
	public byte[][] getSampledStatesMatrix() {
		return sampledStatesMatrix;
	}
	
	/**
	 * Just delegates to {@link #start(ProbabilisticNetwork, int, long)} by setting {@link Long#MAX_VALUE} as the time to wait.
	 * @deprecated use {@link #start(ProbabilisticNetwork, int, long)} instead.
	 */
	public void start(ProbabilisticNetwork pn , int nTrials){
		this.start(pn, nTrials, Long.MAX_VALUE);
	}
	
	/**
	 * Generates the MC sample with the given size for the given probabilistic network.
	 * @param pn Probabilistic network that will be used for sampling.
	 * @param nTrials Number of trials to generate.
	 * @param elapsedTimeMillis : the sampling process will stop after executing this amount of time
	 * in milliseconds
	 */
	public void start(ProbabilisticNetwork pn , int nTrials, long elapsedTimeMillis){
		this.pn = pn;
		this.nTrials = nTrials;
		// set max value allowed for the progress
		this.maxProgress = nTrials; 
		samplingNodeOrderQueue = new ArrayList<Node>();		
		createSamplingOrderQueue();
		sampledStatesMatrix = new byte[nTrials][pn.getNodeCount()];		
		long startingTimeMillis = System.currentTimeMillis();	// the time when the sampling has started
		for(int i = 0; i < nTrials; i++){	
			// update the current value of the progress
			updateProgress(i);
			simulate(i);
			// check if we shall stop execution due to elapsed time
			if ((System.currentTimeMillis() - startingTimeMillis) > elapsedTimeMillis) {
				Debug.println(getClass(), "Stopping simulation because it exeeded time limit of " + elapsedTimeMillis + " ms.");
				break;
			}
			if (sampleGenerationListener != null) {
				for (SampleGenerationListener listener : sampleGenerationListener) {
					if (!listener.onSampleGenerated(this, i)) {
						// force loop to break on next iteration
						i = nTrials;
					}
				}
			}
		}
	}
	
	/**
	 * Responsible for simulating MC for sampling.
	 * @param nTrial The trial number to simulate.
	 */
	protected void simulate(int nTrial){
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
	 * Returns a copy of the list of {@link SampleGenerationListener},
	 * which is a listener called in {@link #start(ProbabilisticNetwork, int, long)}
	 * when a sample is generated.
	 * @return the sampleGenerationListener
	 */
	public List<SampleGenerationListener> getSampleGenerationListener() {
		if (sampleGenerationListener == null) {
			return Collections.emptyList();
		}
		return new ArrayList<SampleGenerationListener>(sampleGenerationListener);
	}
	
	/**
	 * Includes a new {@link SampleGenerationListener} to {@link #getSampleGenerationListener()}
	 * @param listener
	 */
	public void addSampleGenerationListener(SampleGenerationListener listener) {
		if (sampleGenerationListener == null) {
			sampleGenerationListener = new ArrayList<SampleGenerationListener>();
		}
		sampleGenerationListener.add(listener);
	}
	
	/**
	 * Removes a listener from {@link #getSampleGenerationListener()}
	 * @param listener : listener to remove
	 * @return : true if the content of the list was changed.
	 * @see #getSampleGenerationListener()
	 */
	public boolean removeSampleGenerationListener(SampleGenerationListener listener) {
		if (sampleGenerationListener != null) {
			return sampleGenerationListener.remove(listener);
		}
		return false;
	}

	/**
	 * @param sampleGenerationListener the sampleGenerationListener to set
	 */
	protected void setSampleGenerationListener(List<SampleGenerationListener> sampleGenerationListener) {
		this.sampleGenerationListener = sampleGenerationListener;
	}
	
}
