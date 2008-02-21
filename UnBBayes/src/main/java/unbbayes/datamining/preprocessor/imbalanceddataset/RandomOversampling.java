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

import java.util.Random;

import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.evaluation.batchEvaluation.PreprocessorParameters;

/**
 * This class implements random undersampling and oversampling algorithms.
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 26/08/2006
 */
public class RandomOversampling extends Batch {

	private static boolean useRatio = true;
	private static boolean useK = false;
	private static boolean useOverThresh = false;
	private static boolean usePosThresh = false;
	private static boolean useNegThresh = false;
	private static boolean useCleaning = false;
	
	public RandomOversampling(InstanceSet instanceSet) {
		this(instanceSet, null);
	}

	public RandomOversampling(InstanceSet instanceSet,
			PreprocessorParameters parameters) {
		super(useRatio, useK, useOverThresh, usePosThresh, useNegThresh,
				useCleaning, instanceSet, parameters);
		preprocessorName = "Oversampling";
	}

	/**
	 * InitializePreprocessors an instanceSet. The amount of increase or decrease is controlled
	 * by the <code>proportion</code> parameter, which will be multiplied by
	 * the counter attribute.
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

	protected @Override void run() throws Exception {
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
	 * @param instancesIDs Must be sorted ascending!!!
	 */
	public void run(int[] instancesIDs) {
		if (instanceSet.numInstances < 1 || instancesIDs.length < 1) {
			return;
		}
		
		if (instanceSet.isCompacted()) {
			instancesIDs = Utils.uncompactInstancesIDs(instancesIDs, instanceSet);
		}
		
		int counterIndex = instanceSet.counterIndex;
		int numInstances = instancesIDs.length;
		int inst = 0;

		/* Get current size */
		int currentSize = 0;
		for (int i = 0; i < numInstances; i++) {
			inst = instancesIDs[i];
			currentSize += instanceSet.instances[inst].data[counterIndex];
		}
		
		/* Get increase size */
		int increaseSize;
		increaseSize = (int) ((proportion - 1) * currentSize) + 1;
		
		/* Randomly oversample */
		Random randomizer = new Random();
		for (int i = 0; i < increaseSize; i++) {
			inst = instancesIDs[randomizer.nextInt(numInstances)];
			++instanceSet.instances[inst].data[counterIndex];
			++instanceSet.numWeightedInstances;
		}
		instanceSet.getClassDistribution(true);
	}

	public void setInstanceSet(InstanceSet instanceSet) {
		this.instanceSet = instanceSet;
	}

	@Override
	protected void initializeBatch(InstanceSet instanceSet) {
		setInstanceSet(instanceSet);
		setInterestingClass(positiveClass);
	}

}