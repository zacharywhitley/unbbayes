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
package unbbayes.datamining.preprocessor.imbalanceddataset;

import java.util.ArrayList;

import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.evaluation.batchEvaluation.PreprocessorParameters;



/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 24/08/2007
 */
public abstract class Batch {
	
	/**
	 * 0: ratio
	 * 1: k
	 * 2: oversamplingThreshold (t1)
	 * 3: positiveThreshold (t2)
	 * 4: negativeThreshold (t3)
	 * 5: clean
	 */
	private ArrayList<Object>[] parametersList;
	
	protected int numBatchIterations;
	private int currentBatchIteration;
	
	/** The instanceSet that will be preprocessed */
	protected InstanceSet instanceSet;

	private boolean useRatio;
	private boolean useK;
	private boolean useOverThresh;
	private boolean usePosThresh;
	private boolean useNegThresh;
	private boolean useCleaning;

	protected String preprocessorName;
	protected int ratio;
	protected int k;
	protected float oversamplingThreshold;
	protected float positiveThreshold;
	protected float negativeThreshold;
	protected boolean clean;
	protected boolean autoOversamplingThreshold;
	protected int positiveClass;
	protected int negativeClass;
	protected double proportion;
	protected int interestingClass;
	protected boolean useSimplesampling;
	
	public Batch(boolean useRatio, boolean useK, boolean useOverThresh,
			boolean usePosThresh, boolean useNegThresh, boolean useCleaning,
			InstanceSet instanceSet, PreprocessorParameters parameters)
	throws IllegalArgumentException {
		this.useRatio = useRatio;
		this.useK = useK;
		this.useOverThresh = useOverThresh;
		this.usePosThresh = usePosThresh;
		this.useNegThresh = useNegThresh;
		this.useCleaning = useCleaning;
		this.instanceSet = instanceSet;
		
		positiveClass = instanceSet.getPositiveClass();
		negativeClass = instanceSet.getNegativeClass();
		
		if(parameters != null) {
			buildBatchParameters(parameters);
		}
	}
	
	protected abstract void run() throws Exception;

	public final void getNextBatchIteration(InstanceSet instanceSet)
	throws Exception {
		initializeBatch(instanceSet);
		setBatchParameters(currentBatchIteration);
		run();
		++currentBatchIteration;
	}

	protected abstract void initializeBatch(InstanceSet instanceSet)
	throws Exception;

	public final void getBatchIteration(int parameterID) throws Exception {
		setBatchParameters(parameterID);
		run();
	}

	public final int getNumBatchIterations() {
		return numBatchIterations;
	}

	public abstract void setInstanceSet(InstanceSet instanceSet);

	public void setProportion(double proportion) {
		this.proportion = proportion;
	}

	public void setInterestingClass(int interestingClass) {
		this.interestingClass = interestingClass;
	}

	public void setUseSimplesampling(boolean useSimplesampling) {
		this.useSimplesampling = useSimplesampling;
	}

	@SuppressWarnings("unchecked")
	public final void buildBatchParameters(PreprocessorParameters parameters)
	throws IllegalArgumentException {
		/* Get parameters */
		int ratioStart = parameters.getRatioStart();
		int ratioEnd = parameters.getRatioEnd();
		int ratioStep = parameters.getRatioStep();
		
		int t1Start = parameters.getOverThresholdStart();
		int t1End = parameters.getOverThresholdEnd();
		int t1Step = parameters.getOverThresholdStep();
		
		int t2Start = parameters.getPosThresholdStart();
		int t2End = parameters.getPosThresholdEnd();
		int t2Step = parameters.getPosThresholdStep();
		
		int t3Start = parameters.getNegThresholdStart();
		int t3End = parameters.getNegThresholdEnd();
		int t3Step = parameters.getNegThresholdStep();
		
		int clusterStart = parameters.getClusterStart();
		int clusterEnd = parameters.getClusterEnd();
		int clusterStep = parameters.getClusterStep();
		
		int oversamplingThresholdStart = parameters.getOverThresholdStart();
		int oversamplingThresholdEnd = parameters.getOverThresholdEnd();
		int oversamplingThresholdStep = parameters.getOverThresholdStep();
		
		int positiveThresholdStart = parameters.getPosThresholdStart();
		int positiveThresholdEnd = parameters.getPosThresholdEnd();
		int positiveThresholdStep = parameters.getPosThresholdStep();
		
		int negativeThresholdStart = parameters.getNegThresholdStart();
		int negativeThresholdEnd = parameters.getNegThresholdEnd();
		int negativeThresholdStep = parameters.getNegThresholdStep();
		
		/* Check steps */
		if (ratioStep < 1
				|| ratioStep < 1
				|| t1Step < 1
				|| t2Step < 1
				|| t3Step < 1
				|| clusterStep < 1
				|| oversamplingThresholdStep < 1
				|| positiveThresholdStep < 1
				|| negativeThresholdStep < 1) {
			throw new IllegalArgumentException("Illegal arguments!");
		}
		
		/* Check if ratio is greather than the positive class frequency */
		while (ratioStart /100f <= instanceSet.getClassFrequency(positiveClass)) {
			ratioStart += ratioStep;
		}
		
		/* Compute the number of batch iterations */
		if (useCleaning) {
			/* Cleaning On|Off */
			numBatchIterations = 2;
		} else {
			numBatchIterations = 1;
		}
		
		if (useRatio) {
			numBatchIterations *= (ratioEnd - ratioStart) / ratioStep + 1;
		} else {
			ratioStep = 100000;
		}
		
		if (useK) {
			numBatchIterations *= (clusterEnd - clusterStart) / clusterStep + 1;
		} else {
			clusterStep = 100000;
		}
		
		if (useOverThresh) {
			numBatchIterations *= (oversamplingThresholdEnd -
					oversamplingThresholdStart) /
					oversamplingThresholdStep +
					1;
		} else {
			t1Step = 100000;
		}
		
		if (usePosThresh) {
			numBatchIterations *= (positiveThresholdEnd -
					positiveThresholdStart) /
					positiveThresholdStep +
					1;
		} else {
			t2Step = 100000;
		}
		
		if (useNegThresh) {
			numBatchIterations *= (negativeThresholdEnd -
					negativeThresholdStart) /
					negativeThresholdStep +
					1;
		} else {
			t3Step = 100000;
		}
		
		parametersList = new ArrayList[numBatchIterations];
		
		boolean clean;
		int count = 0;
		ArrayList<Object> parametersListAux;
		
		for (int ratio = ratioStart; ratio <= ratioEnd; ratio += ratioStep) {
			for (int k = clusterStart; k <= clusterEnd; k += clusterStep) {
				for (int t1 = t1Start; t1 <= t1End; t1 += t1Step) {
					for (int t2 = t2Start; t2 <= t2End; t2 += t2Step) {
						for (int t3 = t3Start; t3 <= t3End; t3 += t3Step) {
							/* Without cleaning */
							clean = false;
							parametersListAux = new ArrayList<Object>(6);
							parametersListAux.add(ratio);
							parametersListAux.add(k);
							parametersListAux.add(t1);
							parametersListAux.add(t2);
							parametersListAux.add(t3);
							parametersListAux.add(clean);
							parametersListAux.trimToSize();
							parametersList[count] = parametersListAux;
							++count;
							
							/* With cleaning */
							if (useCleaning) {
								clean = true;
								parametersListAux = new ArrayList<Object>(6);
								parametersListAux.add(ratio);
								parametersListAux.add(k);
								parametersListAux.add(t1);
								parametersListAux.add(t2);
								parametersListAux.add(t3);
								parametersListAux.add(clean);
								parametersListAux.trimToSize();
								parametersList[count] = parametersListAux;
								++count;
							}
						}
					}
				}
			}
		}
	}

	protected final void setBatchParameters(int parameterID) {
		ArrayList<Object> parametersListAux = parametersList[parameterID];
		ratio = (Integer) parametersListAux.get(0);
		k = (Integer) parametersListAux.get(1);
		oversamplingThreshold = (float) (Integer) parametersListAux.get(2) / 100;
		positiveThreshold = (float) (Integer) parametersListAux.get(3) / 100;
		negativeThreshold = (float) (Integer) parametersListAux.get(4) / 100;
		clean = (Boolean) parametersListAux.get(5);
		
		computeProportion(instanceSet);
	}

	/* Two class problem */
	protected void computeProportion(InstanceSet instanceSet) {
		float[] originalDist = instanceSet.getClassDistribution(true);
		proportion = originalDist[negativeClass] * (float) ratio;
		proportion /= originalDist[positiveClass] * (float) (100 - ratio);
	}

	public final String parametersToString(int parameterID) {
		String result = "";
		if (useRatio) {
			result += "PosFreq: " + ratio;
		}
		result += "\t";
		if (useK) {
			result += "k: " + k;
		}
		result += "\t";
		if (useOverThresh) {
			result += "Over ";
			if (autoOversamplingThreshold) {
				result += oversamplingThreshold *
					instanceSet.getClassFrequency(positiveClass);
			} else {
				result += oversamplingThreshold;
			}
		}
		result += "\t";
		if (usePosThresh) {
			result += "Pos " + positiveThreshold;
		}
		result += "\t";
		if (useNegThresh) {
			result += "Neg " + negativeThreshold;
		}
		result += "\t";
		if (useCleaning) {
			result += "Clean: " + clean;
		}
		
		return result;
	}

	public final String getPreprocessorName() {
		return preprocessorName;
	}
	
}

