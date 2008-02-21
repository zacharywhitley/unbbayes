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

import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.evaluation.batchEvaluation.PreprocessorParameters;

/**
 * This class implements random undersampling and oversampling algorithms.
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 26/08/2006
 */
public class Simplesampling extends Batch {

	private boolean remove;
	private boolean[] deleteIndex;
	
	private static boolean useRatio = true;
	private static boolean useK = false;
	private static boolean useOverThresh = false;
	private static boolean usePosThresh = false;
	private static boolean useNegThresh = false;
	private static boolean useCleaning = false;
	
	public Simplesampling(InstanceSet instanceSet) {
		this(instanceSet, null);
	}

	public Simplesampling(InstanceSet instanceSet, PreprocessorParameters parameters) {
		super(useRatio, useK, useOverThresh, usePosThresh, useNegThresh,
				useCleaning, instanceSet, parameters);
		preprocessorName = "Simplesampling";
	}

	/**
	 * InitializePreprocessors an instanceSet. The amount of increase or decrease is controlled
	 * by the <code>proportion</code> parameter, which will be multiplied by
	 * the counter attribute.	 * 
	 * @param instanceSet
	 * @param proportion
	 * @return
	 */
	public void runAll() {
		int numInstances = instanceSet.numInstances;

		/* Choose all instances for the sampling process */
		int[] instancesIDs = new int[numInstances];
		for (int i = 0; i < numInstances; i++) {
			instancesIDs[i] = i;
		}
		
		run(instancesIDs);
	}
	
	protected @Override void run() {
		Instance[] instances = instanceSet.instances;
		int numInstances = instanceSet.numInstances;

		/* Choose the instancesIDs for the sampling process */
		int counter = 0;
		int instancesIDsTmp[] = new int[numInstances];
		int classIndex = instanceSet.classIndex;
		for (int inst = 0; inst < numInstances; inst++) {
			if (instances[inst].data[classIndex] == interestingClass) {
				instancesIDsTmp[counter] = inst;
				++counter;
			}
		}
		int[] instancesIDs = new int[counter];
		for (int i = 0; i < counter; i++) {
			instancesIDs[i] = instancesIDsTmp[i];
		}
		
		run(instancesIDs);
	}
	
	/**
	 * InitializePreprocessors an instanceSet. The amount of increase or decrease is controlled
	 * by the <code>proportion</code> parameter, which will be multiplied by
	 * the counter attribute. Only those instances indicated by the array
	 * <code>instancesIDs</code> will be sampled.
	 * 
	 * @param instanceSet
	 * @param proportion
	 * @param instancesIDs Must be sorted ascending!!!
	 * @return
	 */
	public void run(int[] instancesIDs) {
		int counterIndex = instanceSet.counterIndex;
		int numInstancesIDs = instancesIDs.length;
		int inst = 0;

		boolean delete = false;
		int weight;
		int newWeight;
		int difference;
		for (int i = 0; i < numInstancesIDs; i++) {
			inst = instancesIDs[i];
			
			weight = (int) instanceSet.instances[inst].data[counterIndex];
			newWeight = Math.round((float) proportion * weight);
			difference = newWeight - weight;
			
			/* Increase/decrease from the total weight of the instanceSet */
			instanceSet.numWeightedInstances += difference;
			
			/* Increase/decrease the weight of the chosen instance */
			instanceSet.instances[inst].data[counterIndex] = newWeight;
			
			/* Check if removal of the chosen instance is needed */
			if (newWeight <= 0) {
				deleteIndex[inst] = true;
				delete = true;
			}
		}

		/* If there are instances to be removed */
		if (delete && remove) {
			/* Remove instances marked to be removed */
			instanceSet.removeInstances(deleteIndex);
		}
	}
	public void setInstanceSet(InstanceSet instanceSet) {
		this.instanceSet = instanceSet;
	}

	public void setRemove(boolean remove) {
		this.remove = remove;
	}

	public void setDeleteIndex(boolean[] deleteIndex) {
		this.deleteIndex = deleteIndex;
	}

	@Override
	protected void initializeBatch(InstanceSet instanceSet) {
		setInstanceSet(instanceSet);
		setInterestingClass(positiveClass);
	}

}