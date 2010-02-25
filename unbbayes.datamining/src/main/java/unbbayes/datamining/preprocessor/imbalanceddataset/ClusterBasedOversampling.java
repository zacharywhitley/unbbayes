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

import java.util.Arrays;

import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.evaluation.batchEvaluation.PreprocessorParameters;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 09/11/2006
 */
public class ClusterBasedOversampling extends Batch {
	
	/** The current set of input instances */
	protected Instance[] instances;

	/** Number of instances in the instance set */
	protected int numInstances;

	/** Number of classes in the instance set */
	protected int numClasses;

	/** 
	 * The index of the dataset's column that represents the counter attribute.
	 */
	protected int counterIndex;

	/** 
	 * The index of the dataset's column that represents the class attribute.
	 * Assume the value -1 in case there is no class attribute.
	 */
	protected int classIndex;

	/** Matrix of all clusters. Each row stores all instancesIDs of a cluster */
	protected int[][][] clusters;
	
	protected int[] numClusters;
	
	protected double[][] clustersSize;
	
	/** The class given to each cluster according to its distribution */
	protected int[] clustersClass;
	
	/**
	 * Array of booleans that tells which instances should be removed.
	 */
	protected boolean[] deleteIndex;

	protected boolean[] classClusterized;

	private ClusterBasedUtils clusterBasedUtils;

	private boolean overMajority;

	private RandomOversampling oversampling;
	private RandomUndersampling undersampling;
	private Simplesampling simplesampling;

	private Smote smote;

	private static boolean useRatio = true;
	private static boolean useK = true;
	private static boolean useOverThresh = false;
	private static boolean usePosThresh = false;
	private static boolean useNegThresh = false;
	private static boolean useCleaning = false;
	
	public ClusterBasedOversampling(InstanceSet instanceSet,
			PreprocessorParameters parameters) {
		super(useRatio, useK, useOverThresh, usePosThresh, useNegThresh,
				useCleaning, instanceSet, parameters);
		preprocessorName = "Cluster Based Oversampling";
	}

	public ClusterBasedOversampling(ClusterBasedUtils clusterBasedUtils,
			InstanceSet instanceSet, PreprocessorParameters parameters)
	throws Exception {
		this(instanceSet, parameters);
		setClusterInfo(clusterBasedUtils);
	}

	public ClusterBasedOversampling(ClusterBasedUtils clusterBasedUtils,
			InstanceSet instanceSet)
	throws Exception {
		this(instanceSet, null);
		setClusterInfo(clusterBasedUtils);
	}

	public ClusterBasedOversampling(InstanceSet instanceSet) throws Exception {
		this(instanceSet, null);
		setClusterInfo(null);
	}

	public void setClusterInfo(ClusterBasedUtils clusterBasedUtils)
	throws Exception {
		this.clusterBasedUtils = clusterBasedUtils;
	}

	private void initializeClusters() throws Exception {
		clusters = clusterBasedUtils.getClustersByClass(k);
		numClusters = clusterBasedUtils.getNumClustersByClass(k);
		clustersSize = clusterBasedUtils.getClustersSizeByClass(k);
	}
	
	@Override
	public void setInstanceSet(InstanceSet instanceSet) {
		this.instanceSet = instanceSet;
		initialize();
	}
	
	private void initialize() {
		smote = new Smote(instanceSet);
//		initializeSmote(smote);
		smote.setInstanceSet(instanceSet);
		numInstances = instanceSet.numInstances;
		instances = instanceSet.instances;

		/* Initialize deleteIndex array (set no instance to be removed) */
		deleteIndex = new boolean[instanceSet.numInstances];
		Arrays.fill(deleteIndex, false);
		
		if (useSimplesampling) {
			simplesampling = new Simplesampling(instanceSet);
			simplesampling.setInstanceSet(instanceSet);
		} else {
			oversampling = new RandomOversampling(instanceSet);
			oversampling.setInstanceSet(instanceSet);
			undersampling = new RandomUndersampling(instanceSet);
			undersampling.setInstanceSet(instanceSet);
		}
	}
	
	protected void run() throws Exception {
		/* Initialize */
		initialize();
		initializeClusters();
		
		int numClustersAux = numClusters[negativeClass];
		double finalSize;

		if (overMajority) {
			/* Pick the biggest cluster of the majority class */
			int biggestClusterIndex = 0;
			double biggestClusterSize = 0;
			for (int clusterID = 0; clusterID < numClustersAux; clusterID++) {
				if (clustersSize[negativeClass][clusterID] > biggestClusterSize) {
					biggestClusterSize = clustersSize[negativeClass][clusterID];
					biggestClusterIndex = clusterID;
				}
			}
			
			/* 
			 * Oversample the clusters of the majority class to the same size of
			 * its biggest cluster.
			 */
			double[] count = clustersSize[negativeClass];
			numClustersAux = numClusters[negativeClass];
			for (int clusterID = 0; clusterID < numClustersAux; clusterID++) {
				if (clusterID != biggestClusterIndex) {
					finalSize = biggestClusterSize / count[clusterID];
					oversampleCluster(clusterID, finalSize, negativeClass);
				}
			}
		}

		/* Reset the variables */
		initialize();
		
		/* Compute the new size of the majority class */
		double majorityClassSize = 0;
		for (int inst = 0; inst < instanceSet.numInstances; inst++) {
			if (instances[inst].data[classIndex] == negativeClass) {
				majorityClassSize += instances[inst].data[counterIndex];
			}
		}
		
		/* Now we oversample the clusters of the other classes one by one */
		double newSizePerCluster;
		for (int classValue = 0; classValue < numClasses; classValue++) {
			if (classValue != negativeClass) {
				numClustersAux = numClusters[classValue];
				newSizePerCluster = (majorityClassSize * ratio) / (10 - ratio);
				newSizePerCluster /= numClustersAux;
				for (int clusterID = 0; clusterID < numClustersAux; clusterID++) {
					finalSize = newSizePerCluster;
					finalSize /= clustersSize[classValue][clusterID];
					if (finalSize > 1) {
						oversampleCluster(clusterID, finalSize, classValue);
					}
				}
			}
		}
	}
	
	public void runUndersampling(double proportion) throws Exception {
		/* Initialize */
		initialize();
		initializeClusters();
		
		/* Undersample the clusters of the majority class */
		int numClustersAux = numClusters[negativeClass];
		for (int clusterID = 0; clusterID < numClustersAux; clusterID++) {
			undersampleCluster(clusterID, proportion);
		}
		
		/* Mark instances with weight < 1 for deletion */
		float weight;
		for (int inst = 0; inst < numInstances; inst++) {
			weight = instanceSet.instances[inst].data[counterIndex];
			if (weight < 1) {
				deleteIndex[inst] = true;
			}
		}
		
		/* Remove from instanceSet those instances marked for deletion */
		Utils.removeMarkedInstances(instanceSet, deleteIndex);
		instanceSet.removeInstances(deleteIndex);
		
		/* Update references to instanceSet */
		instances = instanceSet.instances;
		numInstances = instanceSet.numInstances;
	}
	
	public void runOversampling(double proportion) throws Exception {
		initialize();
		initializeClusters();
		
		int numClustersAux = numClusters[positiveClass];

		for (int clusterID = 0; clusterID < numClustersAux; clusterID++) {
			/* Oversample the minority class */
			oversampleCluster(clusterID, proportion, positiveClass);
		}
	}

	private void oversampleCluster(int clusterIndex, double proportion,
			int classValue) {
		int[] cluster = clusters[classValue][clusterIndex];
		if (useSimplesampling) {
			simplesampling.setProportion(proportion);
			simplesampling.run(cluster);
		} else {
			oversampling.setProportion(proportion);
			oversampling.run(cluster);
		}
	}

	private int[] chooseInstancesIDs(int clusterIndex, int classValue) {
		/* Choose the instancesIDs for the sampling process */
		int counter = 0;
		int[] instancesIDsTmp = new int[numInstances];
		int[] cluster = clusters[classValue][clusterIndex];
		int clusterSize = cluster.length;
		Instance instance;
		int instanceClass;
		int inst;
		for (int i = 0; i < clusterSize; i++) {
			inst = cluster[i];
			if (deleteIndex[inst]) {
				continue;
			}
			instance = instanceSet.instances[inst];
			instanceClass = (int) instance.data[classIndex];
			if (instanceClass == classValue || classValue == -1) {
				instancesIDsTmp[counter] = inst;
				++counter;
			}
		}
		int[] instancesIDs = new int[counter];
		for (int i = 0; i < counter; i++) {
			instancesIDs[i] = instancesIDsTmp[i];
		}
		
		return instancesIDs;
	}
	
	private void undersampleCluster(int clusterIndex, double proportion) {
		/* Choose the instancesIDs for the sampling process */
		int[] instancesIDs = chooseInstancesIDs(clusterIndex, negativeClass);

		if (useSimplesampling) {
			simplesampling.setProportion(proportion);
			simplesampling.setRemove(false);
			simplesampling.run(instancesIDs);
		} else {
			undersampling.setProportion(proportion);
			undersampling.setRemove(false);
			undersampling.run(instancesIDs);
		}
	}

	public void setOverMajority(boolean overMajority) {
		this.overMajority = overMajority;
	}

	@Override
	protected void initializeBatch(InstanceSet instanceSet) throws Exception {
		boolean overMajority = true;
		setOverMajority(overMajority);
		setInstanceSet(instanceSet);
	}

}