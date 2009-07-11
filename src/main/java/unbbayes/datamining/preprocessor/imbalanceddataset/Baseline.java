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
public class Baseline extends Batch {

	private boolean remove;
	private boolean[] deleteIndex;
	private int limit;
	
	private static boolean useRatio = true;
	private static boolean useK = false;
	private static boolean useOverThresh = false;
	private static boolean usePosThresh = false;
	private static boolean useNegThresh = false;
	private static boolean useCleaning = false;
	
	public Baseline(InstanceSet instanceSet, PreprocessorParameters parameters) {
		super(useRatio, useK, useOverThresh, usePosThresh, useNegThresh,
				useCleaning, instanceSet, parameters);
		preprocessorName = "Baseline";
	}

	public Baseline(InstanceSet instanceSet) {
		this(instanceSet, null);
	}

	/**
	 * InitializePreprocessors an instanceSet. The amount of increase or decrease is controlled
	 * by the <code>proportion</code> parameter, which will be multiplied by
	 * the counter attribute.
	 * @return
	 */
	public void runAll() {
		interestingClass = -1;
		run();
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
	 * @param instancesIDs Must be sorted ascending!!!
	 */
	public void run(int[] instancesIDs) {
		int classIndex = instanceSet.classIndex;
		int counterIndex = instanceSet.counterIndex;
		double difference;
		int numInstancesIDs = instancesIDs.length;
		int inst;
		Instance instance;
		boolean delete = false;

		for (int i = 0; i < numInstancesIDs; i++) {
			inst = instancesIDs[i];
			instance = instanceSet.instances[inst];
			if (instance.data[classIndex] == interestingClass
					|| interestingClass == -1) {
				difference = instance.data[counterIndex] - limit;
				if (difference > 0) {
					instanceSet.numWeightedInstances -= difference;
					instance.data[counterIndex] = limit;
				} else {
					deleteIndex[inst] = true;
					delete = true;
				}
			}
		}

		/* If there are instances to be removed */
		if (delete && remove) {
			/* Remove instances marked to be removed */
			instanceSet.removeInstances(deleteIndex);
		}
	}

	/*--------------------- simpleSampling - end ----------------------*/

	
	
	public void run(InstanceSet instanceSet, int limit,
			int interestingClass) {
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

	public void setLimit(int limit) {
		this.limit = limit;
	}

	@Override
	protected void initializeBatch(InstanceSet instanceSet) throws Exception {
		setInstanceSet(instanceSet);
	}

}