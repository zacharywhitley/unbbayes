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
public class Cclear extends Batch {

	/** The current set of input instances */
	protected Instance[] instances;

	/** Number of instances in the instance set */
	protected int numInstances;

	/** Matrix of all clusters. Each row stores all instancesIDs of a cluster */
	protected int[][] clusters;
	protected int[][] clustersOriginal;
	
	/** The class given to each cluster according to its distribution */
	protected int[] clustersClass;
	
	/** Number of clusters created */
	private int numClusters;
	
	private float[] clustersPositiveFrequency;
	
	/** Array of booleans that tells which instances should be removed */
	protected boolean[] deleteIndex;

	protected boolean[] classClusterized;
	private int[][][] smoteNN;

	private ClusterBasedUtils clusterBasedUtils;

	private ClusterBasedUtils clusterBasedUtilsBackup;

	private Smote smote;

	private static boolean useRatio = true;
	private static boolean useK = true;
	private static boolean useOverThresh = true;
	private static boolean usePosThresh = true;
	private static boolean useNegThresh = true;
	private static boolean useCleaning = true;

	public Cclear(InstanceSet instanceSet, PreprocessorParameters parameters) {
		super(useRatio, useK, useOverThresh, usePosThresh, useNegThresh,
				useCleaning, instanceSet, parameters);
		preprocessorName = "Cclear";

		classClusterized = new boolean[2];
		Arrays.fill(classClusterized, false);
	}
	
	public Cclear(ClusterBasedUtils clusterBasedUtils, InstanceSet instanceSet)
	throws Exception {
		this(instanceSet, null);
		setClusterInfo(clusterBasedUtils);
	}
	
	public Cclear(ClusterBasedUtils clusterBasedUtils, InstanceSet instanceSet,
			PreprocessorParameters parameters) throws Exception {
		this(instanceSet, parameters);
		setClusterInfo(clusterBasedUtils);
	}
	
	public Cclear(InstanceSet instanceSet) throws Exception {
		this(instanceSet, null);
		setClusterInfo(null);
	}
	
	public void setClusterInfo(ClusterBasedUtils clusterBasedUtils)
	throws Exception {
		this.clusterBasedUtils = clusterBasedUtils;
	}

	private void initializeClusters() throws Exception {
		clusters = clusterBasedUtils.getClusters(k);
		numClusters = clusterBasedUtils.getNumClusters(k);
		smoteNN = clusterBasedUtils.getSmoteNN(k);
		
		/* Compute cluster distribution (if neeeded) */
		updateClustersPositiveFrequency();
	}
	
	protected @Override void run() throws Exception {
		if (instanceSet.isNominal()) {
			throw new Exception("Pau!");
		}
		
		initializeClusters();
		
		if (autoOversamplingThreshold) {
			oversamplingThreshold *= instanceSet.getClassFrequency(positiveClass);
		} else {
			oversamplingThreshold = instanceSet.getClassFrequency(positiveClass);
		}

		oversampling(instanceSet, oversamplingThreshold);

		if (clean) {
			initialize();
			rebuildClusters();
			
			clean(positiveThreshold, positiveClass);
			clean(1 - negativeThreshold + 0.000000004, negativeClass);
			
			/* Remove instances marked for deletion by the cleaning process */
			removeMarkedInstances();
			
			rollBackClusters();
		}
		
		if (instanceSet.getClassDistribution(true)[0] == 0 &&
				instanceSet.getClassDistribution(true)[1] == 0) {
			@SuppressWarnings("unused") boolean pau = true;
		}
	}
	
	private void oversampling(InstanceSet instanceSet, float positiveThreshold)
	throws Exception {
		/* Assign each cluster's class according to the positiveThreshold */
		assignClusterClass(positiveThreshold);
		
		int[] cluster = null;
		
		/*
		 * Compute proportion. First, count the number of positive cases. Then
		 * compute how many cases for each positive case must be created so we
		 * get a balance between positive and negative class.
		 */
		int counter = 0;
		int inst;
		for (int clusterID = 0; clusterID < numClusters; clusterID++) {
			if (clustersClass[clusterID] == positiveClass) {
				cluster = clusters[clusterID];
				for (int i = 0; i < cluster.length; i++) {
					inst = cluster[i];
					if (instances[inst].getClassValue() == positiveClass) {
						counter += instanceSet.instances[inst].getWeight();
					}
				}
			}
		}
		float[] dist = instanceSet.getClassDistribution(false);
		float proportion = dist[negativeClass] - dist[positiveClass] + counter;
		proportion *= (float) ratio;
		proportion /= counter * (float) (100 - ratio);

		for (int clusterID = 0; clusterID < numClusters; clusterID++) {
			/* Smote only the positive class */
			if (clustersClass[clusterID] == positiveClass) {
				cluster = smoteCluster(clusterID, proportion, positiveClass);
				if (cluster != null) {
					clusters[clusterID] = cluster;
//					clusterQtd[clusterID] = cluster.length;
				}
			}
		}
	}
	
	public int[] smoteCluster(int clusterID, double proportion, int classValue)
	throws Exception {
		/* Build the instancesIDs for the sampling process */
		int counter = 0;
		int[] cluster = clusters[clusterID];
		int clusterLength = cluster.length;
		int[] instancesIDsTmp = new int[clusterLength];
		int[][] smoteNNTmp = new int[clusterLength][];
		int inst;
		for (int i = 0; i < clusterLength; i++) {
			inst = cluster[i];
			if (instanceSet.getInstance(inst).getClassValue() == classValue) {
				instancesIDsTmp[counter] = inst;
				smoteNNTmp[counter] = smoteNN[clusterID][i];
				++counter;
			}
		}
		
		if (counter < 1) {
			/* The input cluster contains no positive cases */
			return null;
		}
		
		int[] instancesIDs = new int[counter];
		int[][] smoteNN = new int[counter][];
		for (int i = 0; i < counter; i++) {
			instancesIDs[i] = instancesIDsTmp[i];
			smoteNN[i] = smoteNNTmp[i];
		}
		int[] newInstances = null;
		smote.setNearestNeighborsIDs(smoteNN);
		smote.setProportion(proportion);
		newInstances = smote.run(instancesIDs);

		/* Create a new cluster adding the new smoted instances */
		int numNewInstances = newInstances.length;
		int newClusterSize = numNewInstances + clusterLength;
		int[] newCluster = new int[newClusterSize];
		for (int i = 0; i < clusterLength; i++) {
			newCluster[i] = cluster[i];
		}
		for (int i = clusterLength; i < newClusterSize; i++) {
			newCluster[i] = newInstances[i - clusterLength];
		}
		
		return newCluster;
	}

	private void assignClusterClass(double positiveThreshold) throws Exception {
		/* Assign a class to each cluster according to its distribution */
		clustersClass = new int[numClusters];
		for (int clusterID = 0; clusterID < numClusters; clusterID++) {
			if (clustersPositiveFrequency[clusterID] >= positiveThreshold) {
				clustersClass[clusterID] = positiveClass;
			} else {
				clustersClass[clusterID] = negativeClass;
			}
		}
		
		@SuppressWarnings("unused")
		boolean pause = true;
	}


	

	/*************************************************************************/
	/**************************** Clean method *******************************/
	
	private void clean(double cleanFactor, int classValue) throws Exception {
		/* Clean the clusters */
		assignClusterClass(cleanFactor);
		
		clean(classValue);
	}
	
	private void clean(int clusterClass) throws Exception {
		int clusterClassDesired;
		int instanceClassDesired;
		if (clusterClass == positiveClass) {
			/* Clean positive clusters (remove negative instances) */
			clusterClassDesired = positiveClass;
			instanceClassDesired = negativeClass;
		} else {
			/* Clean negative clusters (remove positive instances) */
			clusterClassDesired = negativeClass;
			instanceClassDesired = positiveClass;
		}
		int clusterLength;
		int inst;
		Instance instance;
		int[] cluster;
		for (int clusterID = 0; clusterID < numClusters; clusterID++) {
			if (clustersClass[clusterID] == clusterClassDesired) {
				cluster = clusters[clusterID];
				clusterLength = cluster.length;
				for (int i = 0; i < clusterLength; i++) {
					inst = cluster[i];
					instance = instanceSet.getInstance(inst);
					if (instance.getClassValue() == instanceClassDesired) {
						deleteIndex[inst] = true;
					}
				}
			}
		}
	}

	private void removeMarkedInstances() {
		Utils.removeMarkedInstances(instanceSet, deleteIndex);
		
		/* Initialize deleteIndex array (set no instance to be removed) */
		deleteIndex = new boolean[instanceSet.numInstances];
		Arrays.fill(deleteIndex, false);
	}

	
	

	/*************************************************************************/
	/************************** Auxiliary methods ****************************/
	
	private void rebuildClusters() throws Exception {
		initialize();
		clusterBasedUtilsBackup = clusterBasedUtils;

		/* Rebuild clusters */
		clusterBasedUtils = new ClusterBasedUtils(instanceSet);
		setClusterInfo(clusterBasedUtils);
		initializeClusters();
	}
	
	private void rollBackClusters() throws Exception {
		if (clusterBasedUtilsBackup != null) {
			clusterBasedUtils = clusterBasedUtilsBackup;
			setClusterInfo(clusterBasedUtils);
		}
	}

	private void initialize() {
		smote = new Smote(instanceSet);
		smote.setInstanceSet(instanceSet);
		numInstances = instanceSet.numInstances;
		instances = instanceSet.instances;
		
		/* Initialize deleteIndex array (set no instance to be removed) */
		deleteIndex = new boolean[numInstances];
		Arrays.fill(deleteIndex, false);
	}

	protected void updateClustersPositiveFrequency() {
		int numClusters = clusters.length;
		clustersPositiveFrequency = new float[numClusters];
		float dist;
		
		for (int clusterID = 0; clusterID < numClusters; clusterID++) {
			dist = computeClustersPositiveFrequency(clusterID);
			clustersPositiveFrequency[clusterID] = dist;
		}
		@SuppressWarnings("unused")
		boolean pause = true;
	}
	
	private float computeClustersPositiveFrequency(int clusterID) {
		float numPositives = 0;
		float clusterSize = 0;
		Instance instance;
		int inst;
		int[] cluster = clusters[clusterID];
		int clusterLength = cluster.length;
		float weight;
		for (int i = 0; i < clusterLength; i++) {
			inst = cluster[i];
			if (!deleteIndex[inst]) {
				instance = instanceSet.getInstance(inst);
				weight = instance.getWeight();
				if (instance.getClassValue() == positiveClass) {
					numPositives += weight;
				}
				clusterSize += weight;
			}
		}
//		clusterQtd[clusterID] = clusterSize;
		
		return numPositives / clusterSize;
	}

	@Override
	public void setInstanceSet(InstanceSet instanceSet) {
		this.instanceSet = instanceSet;
		initialize();
	}

	@Override
	protected void initializeBatch(InstanceSet instanceSet) {
		setInstanceSet(instanceSet);
	}
	
}